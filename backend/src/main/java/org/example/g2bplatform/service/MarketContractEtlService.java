package org.example.g2bplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.g2bplatform.DTO.CsvUploadJobDto;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 시장데이터(공사/용역) ETL.
 * task_member_contract_raw → market_contract_flat → market_contract_grouped.
 * 대상 분류(market_target_category)만 적재. 계약별 최종(최신 차수) 1행 → 초년도계약번호 묶음.
 * 금액/날짜 기준은 first_contract_date(원계약일)로 통일(특정품목과 동일).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketContractEtlService {

    private final JdbcTemplate jdbc;
    private final TaskExecutor csvJobExecutor;

    public CsvUploadJobDto startEtlJob(String triggeredBy) {
        String jobId = UUID.randomUUID().toString();
        long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM task_member_contract_raw t WHERE EXISTS (" +
                "  SELECT 1 FROM market_target_category c WHERE c.is_active='Y'" +
                "   AND c.contract_type=t.contract_type AND c.category_name=t.public_procurement_type)",
                Long.class);
        jdbc.update(
                "INSERT INTO csv_upload_job (id, upload_type, file_name, file_size_bytes, temp_path, status, message, total_rows, uploader, created_at, updated_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,NOW(),NOW())",
                jobId, "market_contract_etl", "ETL", 0L, "", "QUEUED", "대기 중", (int) total, triggeredBy);
        csvJobExecutor.execute(() -> runEtl(jobId));
        return CsvUploadJobDto.builder().jobId(jobId).uploadType("market_contract_etl")
                .status("QUEUED").message("대기 중").totalRows((int) total).build();
    }

    public CsvUploadJobDto getJob(String jobId) {
        List<CsvUploadJobDto> list = jdbc.query(
                "SELECT id, upload_type, status, message, total_rows, processed_rows, inserted_count, skipped_count, error_message " +
                "FROM csv_upload_job WHERE id = ?",
                (rs, n) -> CsvUploadJobDto.builder()
                        .jobId(rs.getString("id")).uploadType(rs.getString("upload_type"))
                        .status(rs.getString("status")).message(rs.getString("message"))
                        .totalRows((Integer) rs.getObject("total_rows")).processedRows(rs.getInt("processed_rows"))
                        .insertedCount(rs.getInt("inserted_count")).skippedCount(rs.getInt("skipped_count"))
                        .errorMessage(rs.getString("error_message")).build(),
                jobId);
        return list.isEmpty() ? null : list.get(0);
    }

    private void runEtl(String jobId) {
        jdbc.update("UPDATE csv_upload_job SET status='RUNNING', message='flat 적재 중', started_at=NOW() WHERE id=?", jobId);
        log.info("[MARKET-ETL] 시작 jobId={}", jobId);
        try {
            // 1) flat saved 백업
            List<Map<String, Object>> flatSaved = jdbc.queryForList(
                    "SELECT contract_type, contract_no FROM market_contract_flat WHERE saved='Y'");
            List<Map<String, Object>> grpSaved = jdbc.queryForList(
                    "SELECT contract_type, group_key, vendor_biz_reg_no FROM market_contract_grouped WHERE saved='Y'");

            // 2) flat 재적재: 대상분류 필터 + 계약별 최종행(is_final='Y'). 윈도우 함수 대신 필터로 경량화.
            jdbc.update("TRUNCATE TABLE market_contract_flat");
            int flat = jdbc.update(
                "INSERT IGNORE INTO market_contract_flat (" +
                "  contract_type, contract_no, change_seq, is_final_contract, first_year_contract_no, is_long_term," +
                "  vendor_name, vendor_biz_reg_no, contract_name, demand_agency_name, demand_agency_region," +
                "  public_procurement_major, public_procurement_mid, public_procurement_name, contract_method," +
                "  first_contract_date, contract_date, first_contract_amount, total_contract_amount, current_contract_amount," +
                "  start_date, end_date, saved" +
                ") SELECT" +
                "  r.contract_type, r.contract_no, r.change_seq, r.is_final_contract, NULLIF(r.first_year_contract_no,'')," +
                "  CASE WHEN r.new_long_term_type IN ('신규(장기)','장기','계속비') THEN 'Y' ELSE 'N' END," +
                "  r.vendor_name, r.vendor_biz_reg_no, r.contract_name, r.demand_agency_name, r.demand_agency_region," +
                "  r.public_procurement_major, r.public_procurement_minor, r.public_procurement_type, r.contract_method," +
                "  STR_TO_DATE(r.first_contract_date,'%Y%m%d'), STR_TO_DATE(r.contract_date,'%Y%m%d')," +
                "  CAST(NULLIF(REPLACE(r.first_contract_amount_raw,',',''),'') AS UNSIGNED)," +
                "  CAST(NULLIF(REPLACE(r.total_contract_amount_raw,',',''),'') AS UNSIGNED)," +
                "  CAST(NULLIF(REPLACE(r.current_contract_amount_raw,',',''),'') AS UNSIGNED)," +
                "  CASE WHEN r.start_date REGEXP '^[0-9]{8}$' THEN STR_TO_DATE(r.start_date,'%Y%m%d') END," +
                "  CASE WHEN r.end_date REGEXP '^[0-9]{8}$' THEN STR_TO_DATE(r.end_date,'%Y%m%d') END," +
                "  'N'" +
                " FROM task_member_contract_raw r" +
                " JOIN market_target_category c ON c.is_active='Y'" +
                "   AND c.contract_type=r.contract_type AND c.category_name=r.public_procurement_type" +
                " WHERE r.is_final_contract='Y'");
            jdbc.update("UPDATE csv_upload_job SET processed_rows=?, inserted_count=?, message='grouped 집계 중' WHERE id=?", flat, flat, jobId);
            log.info("[MARKET-ETL] flat 적재 {}건", flat);

            // 3) flat saved 복원
            if (!flatSaved.isEmpty()) {
                jdbc.batchUpdate("UPDATE market_contract_flat SET saved='Y' WHERE contract_type=? AND contract_no=?",
                        flatSaved.stream().map(m -> new Object[]{m.get("contract_type"), m.get("contract_no")}).toList());
            }

            // 4) grouped 재집계: 초년도계약번호 묶음, first_contract_date 기준
            jdbc.update("TRUNCATE TABLE market_contract_grouped");
            int grouped = jdbc.update(
                "INSERT INTO market_contract_grouped (" +
                "  contract_type, group_key, contract_no, first_year_contract_no, vendor_name, vendor_biz_reg_no," +
                "  contract_name, demand_agency_name, demand_agency_region, public_procurement_major, public_procurement_mid," +
                "  public_procurement_name, contract_method, is_long_term, first_contract_date, last_contract_date," +
                "  initial_contract_amount, total_contract_amount_sum, contract_count, saved" +
                ") SELECT" +
                "  contract_type, COALESCE(first_year_contract_no, contract_no) AS group_key, MIN(contract_no)," +
                "  MAX(first_year_contract_no), MAX(vendor_name), vendor_biz_reg_no, MAX(contract_name)," +
                "  MAX(demand_agency_name), MIN(demand_agency_region), MAX(public_procurement_major), MAX(public_procurement_mid)," +
                "  MAX(public_procurement_name), MAX(contract_method), MAX(is_long_term)," +
                "  MIN(first_contract_date), MAX(first_contract_date)," +
                "  SUM(CASE WHEN first_contract_date = grp_min THEN COALESCE(first_contract_amount,0) ELSE 0 END)," +
                "  SUM(COALESCE(total_contract_amount,0)), COUNT(DISTINCT contract_no), 'N'" +
                " FROM (" +
                "   SELECT *, MIN(first_contract_date) OVER (PARTITION BY contract_type, COALESCE(first_year_contract_no, contract_no), vendor_biz_reg_no) AS grp_min" +
                "   FROM market_contract_flat WHERE is_active='Y'" +
                " ) t" +
                " GROUP BY contract_type, COALESCE(first_year_contract_no, contract_no), vendor_biz_reg_no");

            // 5) grouped saved 복원
            int restored = 0;
            if (!grpSaved.isEmpty()) {
                int[] res = jdbc.batchUpdate(
                        "UPDATE market_contract_grouped SET saved='Y' WHERE contract_type=? AND group_key=? AND vendor_biz_reg_no <=> ?",
                        grpSaved.stream().map(m -> new Object[]{m.get("contract_type"), m.get("group_key"), m.get("vendor_biz_reg_no")}).toList());
                for (int r : res) if (r > 0) restored++;
            }

            jdbc.update("UPDATE csv_upload_job SET status='SUCCESS', message='완료', inserted_count=?, finished_at=NOW() WHERE id=?", flat, jobId);
            log.info("[MARKET-ETL] 완료 flat={}, grouped={}, saved복원={}", flat, grouped, restored);
        } catch (Exception e) {
            log.error("[MARKET-ETL] 실패 jobId={}", jobId, e);
            jdbc.update("UPDATE csv_upload_job SET status='FAILED', message='실패', error_message=?, finished_at=NOW() WHERE id=?",
                    e.getMessage(), jobId);
        }
    }
}
