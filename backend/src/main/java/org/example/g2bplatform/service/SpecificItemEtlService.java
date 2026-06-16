package org.example.g2bplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.g2bplatform.DTO.CsvUploadJobDto;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificItemEtlService {

    private static final int BATCH_SIZE = 2_000;

    private static final String INSERT_SQL =
            "INSERT IGNORE INTO specific_item_flat (" +
            "  data_type, contract_no, change_seq, item_seq, is_final_contract, is_long_term, first_year_contract_no," +
            "  demand_agency_code, demand_agency_name, demand_agency_region," +
            "  vendor_name, vendor_biz_reg_no, vendor_enterprise_type, vendor_region," +
            "  contract_name, contract_method, bid_notice_no, contract_type, delivery_contract_type," +
            "  item_category_no, item_category_name, detail_item_no, detail_item_name," +
            "  item_identifier_no, item_identifier_name, unit," +
            "  unit_price, quantity, quantity_change, supply_amount, supply_amount_change," +
            "  is_mas, is_excellent_product, is_direct_purchase, is_sme_competitive," +
            "  contract_date, first_contract_date," +
            "  jurisdiction_type, delivery_location, source_file" +
            ") VALUES (" +
            "  ?,?,?,?,?,?,?," +
            "  ?,?,?," +
            "  ?,?,?,?," +
            "  ?,?,?,?,?," +
            "  ?,?,?,?," +
            "  ?,?,?," +
            "  ?,?,?,?,?," +
            "  ?,?,?,?," +
            "  ?,?," +
            "  ?,?,?" +
            ")";

    private final JdbcTemplate jdbc;
    private final TaskExecutor csvJobExecutor;

    /** CSV 업로드 완료 후 자동 트리거 — jobId 반환 */
    public CsvUploadJobDto startEtlJob(String triggeredBy) {
        String jobId = UUID.randomUUID().toString();

        long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM procurement_specific_item_raw WHERE business_type = '물품' AND is_final_contract = 'Y'",
                Long.class);

        jdbc.update(
                "INSERT INTO csv_upload_job (id, upload_type, file_name, file_size_bytes, temp_path, status, message, total_rows, uploader, created_at, updated_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,NOW(),NOW())",
                jobId, "specific_item_etl", "ETL", 0L, "", "QUEUED", "대기 중", (int) total, triggeredBy
        );

        csvJobExecutor.execute(() -> runEtl(jobId, total));

        return CsvUploadJobDto.builder()
                .jobId(jobId)
                .uploadType("specific_item_etl")
                .status("QUEUED")
                .message("대기 중")
                .totalRows((int) total)
                .processedRows(0)
                .insertedCount(0)
                .skippedCount(0)
                .build();
    }

    public CsvUploadJobDto getJob(String jobId) {
        List<CsvUploadJobDto> list = jdbc.query(
                "SELECT id, upload_type, file_name, file_size_bytes, status, message, total_rows, processed_rows, inserted_count, skipped_count, started_at, finished_at, error_message " +
                "FROM csv_upload_job WHERE id = ?",
                (rs, rowNum) -> CsvUploadJobDto.builder()
                        .jobId(rs.getString("id"))
                        .uploadType(rs.getString("upload_type"))
                        .fileName(rs.getString("file_name"))
                        .fileSizeBytes((Long) rs.getObject("file_size_bytes"))
                        .status(rs.getString("status"))
                        .message(rs.getString("message"))
                        .totalRows((Integer) rs.getObject("total_rows"))
                        .processedRows(rs.getInt("processed_rows"))
                        .insertedCount(rs.getInt("inserted_count"))
                        .skippedCount(rs.getInt("skipped_count"))
                        .startedAt(rs.getObject("started_at", LocalDateTime.class))
                        .finishedAt(rs.getObject("finished_at", LocalDateTime.class))
                        .errorMessage(rs.getString("error_message"))
                        .build(),
                jobId
        );
        return list.isEmpty() ? null : list.get(0);
    }

    private void runEtl(String jobId, long total) {
        jdbc.update("UPDATE csv_upload_job SET status='RUNNING', message='ETL 처리 중', started_at=NOW() WHERE id=?", jobId);
        log.info("[ETL] 시작 jobId={}, total={}", jobId, total);

        long processed = 0;
        long inserted = 0;
        long lastId = 0;

        try {
            while (true) {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT * FROM procurement_specific_item_raw WHERE business_type = '물품' AND is_final_contract = 'Y' AND id > ? ORDER BY id LIMIT ?",
                        lastId, BATCH_SIZE);

                if (rows.isEmpty()) break;

                List<Object[]> batch = new ArrayList<>(rows.size());
                for (Map<String, Object> r : rows) {
                    batch.add(toParams(r));
                    processed++;
                }

                int[] results = jdbc.batchUpdate(INSERT_SQL, batch);
                for (int res : results) if (res > 0) inserted++;

                lastId = toLong(rows.get(rows.size() - 1).get("id"));

                jdbc.update("UPDATE csv_upload_job SET processed_rows=?, inserted_count=?, skipped_count=?, message='ETL 처리 중' WHERE id=?",
                        (int) processed, (int) inserted, (int) (processed - inserted), jobId);
            }

            // flat 적재 완료 → grouped 재집계 (saved 보존)
            jdbc.update("UPDATE csv_upload_job SET message='grouped 집계 중' WHERE id=?", jobId);
            rebuildGrouped();

            jdbc.update("UPDATE csv_upload_job SET status='SUCCESS', message='완료', processed_rows=?, inserted_count=?, skipped_count=?, finished_at=NOW() WHERE id=?",
                    (int) processed, (int) inserted, (int) (processed - inserted), jobId);
            log.info("[ETL] 완료 jobId={}, inserted={}", jobId, inserted);

        } catch (Exception e) {
            log.error("[ETL] 실패 jobId={}", jobId, e);
            jdbc.update("UPDATE csv_upload_job SET status='FAILED', message='실패', error_message=?, finished_at=NOW() WHERE id=?",
                    e.getMessage(), jobId);
        }
    }

    /**
     * specific_item_flat → specific_item_grouped 재집계.
     * 단기/단일계약은 1행 그대로, 장기계약은 초년도계약번호로 연차를 1행으로 병합.
     * 기존 grouped의 saved 값은 묶음 키(data_type, group_key, vendor_biz_reg_no) 기준으로 보존.
     */
    private void rebuildGrouped() {
        log.info("[ETL] grouped 재집계 시작");

        // 1) 기존 saved 백업
        jdbc.execute("DROP TEMPORARY TABLE IF EXISTS tmp_grouped_saved");
        jdbc.execute(
                "CREATE TEMPORARY TABLE tmp_grouped_saved AS " +
                "SELECT data_type, group_key, vendor_biz_reg_no, saved " +
                "FROM specific_item_grouped WHERE saved = 'Y'");

        // 2) 재집계
        jdbc.update("TRUNCATE TABLE specific_item_grouped");
        int grouped = jdbc.update(
                "INSERT INTO specific_item_grouped (" +
                "  data_type, group_key, vendor_biz_reg_no, first_year_contract_no, contract_no," +
                "  vendor_name, demand_agency_name, demand_agency_region, contract_method, contract_type," +
                "  item_category_no, detail_item_name, is_long_term," +
                "  first_contract_date, last_contract_date, total_supply_amount, contract_count, saved" +
                ") SELECT" +
                "  data_type," +
                "  COALESCE(first_year_contract_no, contract_no) AS group_key," +
                "  vendor_biz_reg_no," +
                "  MAX(first_year_contract_no)," +
                "  MIN(contract_no)," +
                "  MAX(vendor_name)," +
                "  MAX(demand_agency_name)," +
                "  MIN(demand_agency_region)," +
                "  MAX(contract_method)," +
                "  MAX(contract_type)," +
                "  MAX(item_category_no)," +
                "  MAX(detail_item_name)," +
                "  MAX(is_long_term)," +
                "  MIN(first_contract_date)," +
                "  MAX(contract_date)," +
                "  SUM(COALESCE(supply_amount, 0))," +
                "  COUNT(DISTINCT contract_no)," +
                "  'N'" +
                " FROM specific_item_flat" +
                " WHERE is_active = 'Y'" +
                " GROUP BY data_type, COALESCE(first_year_contract_no, contract_no), vendor_biz_reg_no");

        // 3) saved 복원
        int restored = jdbc.update(
                "UPDATE specific_item_grouped g " +
                "JOIN tmp_grouped_saved t " +
                "  ON g.data_type = t.data_type" +
                "  AND g.group_key = t.group_key" +
                "  AND g.vendor_biz_reg_no <=> t.vendor_biz_reg_no " +
                "SET g.saved = 'Y'");

        jdbc.execute("DROP TEMPORARY TABLE IF EXISTS tmp_grouped_saved");
        log.info("[ETL] grouped 재집계 완료 grouped={}, saved복원={}", grouped, restored);
    }

    private Object[] toParams(Map<String, Object> r) {
        String contractType = str(r.get("contract_type"));
        String deliveryType = str(r.get("delivery_contract_type"));
        String dataType = ("제3자단가계약".equals(contractType) && "납품".equals(deliveryType))
                ? "shopping_mall" : "general";
        String longTermType = str(r.get("new_long_term_type"));
        boolean isLong = "신규(장기)".equals(longTermType)
                || "장기".equals(longTermType)
                || "계속비".equals(longTermType);
        String isLongTerm = isLong ? "Y" : "N";

        return new Object[]{
                dataType,
                str(r.get("contract_no")),
                str(r.get("change_seq")),
                toInt(r.get("item_seq"), 1),
                flag(str(r.get("is_final_contract"))),
                isLongTerm,
                str(r.get("first_year_contract_no")),
                str(r.get("demand_agency_code")),
                str(r.get("demand_agency_name")),
                str(r.get("demand_agency_region")),
                str(r.get("vendor_name")),
                str(r.get("vendor_biz_reg_no")),
                str(r.get("vendor_enterprise_type")),
                str(r.get("vendor_region")),
                str(r.get("contract_name")),
                str(r.get("contract_method")),
                str(r.get("base_contract_no")),
                contractType,
                deliveryType,
                str(r.get("item_category_no")),
                str(r.get("item_category_name")),
                str(r.get("detail_item_no")),
                str(r.get("detail_item_name")),
                str(r.get("item_identifier_no")),
                str(r.get("item_name")),
                str(r.get("unit")),
                parseLong(str(r.get("unit_price_raw"))),
                parseLong(str(r.get("quantity_raw"))),
                parseLong(str(r.get("quantity_change_raw"))),
                parseLong(str(r.get("supply_amount_raw"))),
                parseLong(str(r.get("supply_amount_change_raw"))),
                flag(str(r.get("is_mas"))),
                flag(str(r.get("is_excellent_product"))),
                flag(str(r.get("is_direct_purchase"))),
                flag(str(r.get("is_sme_competitive"))),
                toDate(str(r.get("contract_date"))),
                toDate(str(r.get("first_contract_date"))),
                str(r.get("jurisdiction_type")),
                str(r.get("delivery_location")),
                str(r.get("source_file"))
        };
    }

    private String str(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private String flag(String v) {
        if (v == null || v.isEmpty()) return null;
        String u = v.toUpperCase();
        return ("Y".equals(u) || "N".equals(u)) ? u : null;
    }

    private int toInt(Object v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.toString().trim()); } catch (Exception e) { return def; }
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        try { return Long.parseLong(v.toString().trim()); } catch (Exception e) { return 0L; }
    }

    private Long parseLong(String v) {
        if (v == null || v.isEmpty()) return null;
        try { return Long.parseLong(v.replace(",", "").trim()); } catch (Exception e) { return null; }
    }

    private String toDate(String v) {
        if (v == null || v.length() < 8) return null;
        if (v.length() == 8 && v.matches("\\d{8}")) {
            return v.substring(0, 4) + "-" + v.substring(4, 6) + "-" + v.substring(6, 8);
        }
        return v;
    }
}
