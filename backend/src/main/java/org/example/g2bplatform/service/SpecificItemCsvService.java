package org.example.g2bplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.g2bplatform.DTO.CsvUploadJobDto;
import org.example.g2bplatform.DTO.CsvUploadResultDto;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 특정품목 조달 내역 CSV 파싱 및 procurement_specific_item_raw 적재.
 *
 * <p>CSV 양식 (조달데이터허브):
 * <ul>
 *   <li>인코딩: UTF-16 (BOM 포함 LE/BE 자동 감지)</li>
 *   <li>구분자: 탭(\t), 각 값은 쌍따옴표로 감싸져 있음</li>
 *   <li>헤더 행: "조달방식" 과 "업무구분" 을 모두 포함하는 행</li>
 *   <li>컬럼 수: 49개</li>
 * </ul>
 *
 * <p>중복 처리: INSERT IGNORE — UNIQUE KEY(contract_no, change_seq, item_seq) 충돌 시 조용히 건너뜀.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificItemCsvService {

    private static final int BATCH_SIZE = 2_000;

    private static final String INSERT_SQL =
            "INSERT IGNORE INTO procurement_specific_item_raw (" +
            "  procurement_method, business_type, contract_type, delivery_contract_type," +
            "  contract_date, contract_no, change_seq, item_seq, is_final_contract," +
            "  demand_agency_code, demand_agency_name, jurisdiction_type, demand_agency_region," +
            "  item_category_no, item_category_name, detail_item_no, detail_item_name," +
            "  item_identifier_no, item_name," +
            "  vendor_name, vendor_biz_reg_no, vendor_enterprise_type, vendor_region," +
            "  contract_name," +
            "  is_excellent_product, is_direct_purchase, is_mas," +
            "  is_two_stage_competition, is_sme_competitive," +
            "  first_contract_date," +
            "  base_contract_no, base_contract_change_seq," +
            "  contract_method, bid_method, applicable_law, clause_content," +
            "  new_long_term_type, is_long_term_first_year, first_year_contract_no, long_term_sequence," +
            "  delivery_location, delivery_deadline, delivery_condition, unit," +
            "  unit_price_raw, quantity_raw, quantity_change_raw, supply_amount_raw, supply_amount_change_raw," +
            "  source_file" +
            ") VALUES (" +
            "  ?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,  ?,?,?,?,?,?," +
            "  ?,?,?,?,  ?," +
            "  ?,?,?,?,?," +
            "  ?," +
            "  ?,?," +
            "  ?,?,?,?," +
            "  ?,?,?,?," +
            "  ?,?,?,?," +
            "  ?,?,?,?,?," +
            "  ?" +
            ")";

    private final JdbcTemplate jdbc;
    private final TaskExecutor csvJobExecutor;

    /**
     * 업로드 파일을 서버 임시 경로에 저장하고 비동기 Job을 시작합니다.
     * 진행률은 csv_upload_job 테이블에 갱신됩니다.
     */
    public CsvUploadJobDto startSpecificItemJob(MultipartFile file, String uploader) {
        String jobId = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        long fileSizeBytes = file.getSize();

        try {
            File dir = new File("/tmp/g2b-upload");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            String tempPath = new File(dir, jobId + ".csv").getAbsolutePath();

            // MultipartFile 내용을 컨테이너 파일시스템에 저장 (비동기 처리를 위해 필요)
            try (BufferedReader reader = openReader(file);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempPath), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            jdbc.update(
                    "INSERT INTO csv_upload_job (" +
                            "id, upload_type, file_name, file_size_bytes, temp_path, status, message, uploader, created_at, updated_at" +
                            ") VALUES (?,?,?,?,?,?,?,?,NOW(),NOW())",
                    jobId, "specific_item", fileName, fileSizeBytes, tempPath, "QUEUED", "대기 중", uploader
            );

            csvJobExecutor.execute(() -> runSpecificItemJob(jobId, tempPath, fileName, fileSizeBytes, uploader));

            return CsvUploadJobDto.builder()
                    .jobId(jobId)
                    .uploadType("specific_item")
                    .fileName(fileName)
                    .fileSizeBytes(fileSizeBytes)
                    .status("QUEUED")
                    .message("대기 중")
                    .processedRows(0)
                    .insertedCount(0)
                    .skippedCount(0)
                    .build();

        } catch (Exception e) {
            log.error("Job 시작 실패: {}", fileName, e);
            return CsvUploadJobDto.builder()
                    .jobId(jobId)
                    .uploadType("specific_item")
                    .fileName(fileName)
                    .fileSizeBytes(fileSizeBytes)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
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

    // ── Job 실행 ────────────────────────────────────────────────────────────

    private void runSpecificItemJob(String jobId, String tempPath, String fileName, long fileSizeBytes, String uploader) {
        try {
            jdbc.update("UPDATE csv_upload_job SET status='RUNNING', message='총 행 수 계산 중', started_at=NOW() WHERE id=?", jobId);

            int totalRows = countTotalRows(tempPath);
            jdbc.update("UPDATE csv_upload_job SET total_rows=?, message='적재 중' WHERE id=?", totalRows, jobId);

            CsvUploadResultDto result = uploadFromTempFile(jobId, tempPath, fileName, fileSizeBytes, uploader, totalRows);

            String finalStatus = (result.getErrorMessage() == null) ? "SUCCESS" : "FAILED";
            jdbc.update(
                    "UPDATE csv_upload_job SET status=?, message=?, finished_at=NOW(), error_message=? WHERE id=?",
                    finalStatus,
                    (result.getErrorMessage() == null) ? "완료" : "실패",
                    result.getErrorMessage(),
                    jobId
            );

        } catch (Exception e) {
            log.error("Job 실패: {}", jobId, e);
            jdbc.update(
                    "UPDATE csv_upload_job SET status='FAILED', message='실패', finished_at=NOW(), error_message=? WHERE id=?",
                    e.getMessage(),
                    jobId
            );
        } finally {
            // 임시 파일 정리
            try {
                //noinspection ResultOfMethodCallIgnored
                new File(tempPath).delete();
            } catch (Exception ignore) {}
        }
    }

    private int countTotalRows(String tempPath) throws Exception {
        int total = 0;
        boolean headerFound = false;
        try (BufferedReader reader = openTempReader(tempPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!headerFound) {
                    if (line.contains("조달방식") && line.contains("업무구분")) headerFound = true;
                    continue;
                }
                String[] cols = splitLine(line);
                if (cols.length < 49) continue;
                total++;
            }
        }
        return total;
    }

    private CsvUploadResultDto uploadFromTempFile(String jobId, String tempPath, String fileName, long fileSizeBytes, String uploader, int totalRows) {
        long start = System.currentTimeMillis();

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int processed = 0;
        int insertedRows = 0;
        boolean headerFound = false;

        try (BufferedReader reader = openTempReader(tempPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!headerFound) {
                    if (line.contains("조달방식") && line.contains("업무구분")) headerFound = true;
                    continue;
                }
                String[] cols = splitLine(line);
                if (cols.length < 49) continue;

                processed++;
                batch.add(toParams(cols, fileName));

                if (batch.size() >= BATCH_SIZE) {
                    insertedRows += executeBatch(batch);
                    batch.clear();
                    // 진행률 업데이트 (배치 단위)
                    int skipped = processed - insertedRows;
                    jdbc.update(
                            "UPDATE csv_upload_job SET processed_rows=?, inserted_count=?, skipped_count=?, message='적재 중' WHERE id=?",
                            processed, insertedRows, skipped, jobId
                    );
                }
            }

            if (!batch.isEmpty()) {
                insertedRows += executeBatch(batch);
            }

            int skipped = processed - insertedRows;
            jdbc.update(
                    "UPDATE csv_upload_job SET processed_rows=?, inserted_count=?, skipped_count=?, message='마무리 중' WHERE id=?",
                    processed, insertedRows, skipped, jobId
            );

            if (!headerFound) {
                CsvUploadResultDto err = CsvUploadResultDto.error(fileName, "헤더 행을 찾을 수 없습니다. 올바른 특정품목 조달 내역 CSV 파일인지 확인하세요.");
                err.setFileSizeBytes(fileSizeBytes);
                err.setTotalRows(totalRows);
                err.setInsertedCount(insertedRows);
                err.setSkippedCount(skipped);
                err.setElapsedMs(System.currentTimeMillis() - start);
                writeUploadHistory("specific_item", fileName, fileSizeBytes, uploader, err);
                return err;
            }

            long elapsed = System.currentTimeMillis() - start;
            CsvUploadResultDto ok = CsvUploadResultDto.builder()
                    .fileName(fileName)
                    .fileSizeBytes(fileSizeBytes)
                    .totalRows(totalRows)
                    .insertedCount(insertedRows)
                    .skippedCount(skipped)
                    .elapsedMs(elapsed)
                    .build();
            writeUploadHistory("specific_item", fileName, fileSizeBytes, uploader, ok);
            return ok;

        } catch (Exception e) {
            log.error("CSV 적재 실패(job={}): {}", jobId, fileName, e);
            CsvUploadResultDto err = CsvUploadResultDto.error(fileName, e.getMessage());
            err.setFileSizeBytes(fileSizeBytes);
            err.setTotalRows(totalRows);
            err.setInsertedCount(insertedRows);
            err.setSkippedCount(processed - insertedRows);
            err.setElapsedMs(System.currentTimeMillis() - start);
            writeUploadHistory("specific_item", fileName, fileSizeBytes, uploader, err);
            return err;
        }
    }

    private BufferedReader openTempReader(String tempPath) throws Exception {
        return new BufferedReader(new InputStreamReader(new java.io.FileInputStream(tempPath), StandardCharsets.UTF_8));
    }

    // ── 기존 동기 upload는 더 이상 사용하지 않음 (호환용 유지 가능) ─────────────
    public CsvUploadResultDto upload(MultipartFile file, String uploader) {
        return CsvUploadResultDto.error(file.getOriginalFilename(), "동기 업로드는 비활성화되었습니다. Job 방식 API를 사용하세요.");
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    private BufferedReader openReader(MultipartFile file) throws Exception {
        // UTF-16 BOM을 자동으로 인식하는 Charset 사용
        Charset cs;
        try {
            cs = Charset.forName("UTF-16");
        } catch (Exception e) {
            cs = StandardCharsets.UTF_16;
        }
        return new BufferedReader(new InputStreamReader(file.getInputStream(), cs));
    }

    /** 탭 구분 + 쌍따옴표 제거 */
    private String[] splitLine(String line) {
        String[] parts = line.split("\t", -1);
        for (int i = 0; i < parts.length; i++) {
            String v = parts[i].trim();
            if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                v = v.substring(1, v.length() - 1);
            }
            parts[i] = v;
        }
        return parts;
    }

    private Object[] toParams(String[] c, String fileName) {
        return new Object[]{
            // 조달/계약 분류 (1~4)
            n(c[0]),  n(c[1]),  n(c[2]),  n(c[3]),
            // 계약 식별 (5~9)
            n(c[4]),  n(c[5]),  n(c[6]),  seqInt(c[7]),  flag(c[8]),
            // 수요기관 (10~13)
            n(c[9]),  n(c[10]), n(c[11]), n(c[12]),
            // 물품 (14~19)
            n(c[13]), n(c[14]), n(c[15]), n(c[16]), n(c[17]), n(c[18]),
            // 업체 (20~23)
            n(c[19]), n(c[20]), n(c[21]), n(c[22]),
            // 계약명 (24)
            n(c[23]),
            // 플래그 (25~29)
            flag(c[24]), flag(c[25]), flag(c[26]), flag(c[27]), flag(c[28]),
            // 최초계약일 (30)
            n(c[29]),
            // 원계약 (31~32)
            n(c[30]), n(c[31]),
            // 계약방법 (33~36)
            n(c[32]), n(c[33]), n(c[34]), n(c[35]),
            // 장기계약 (37~40)
            n(c[36]), n(c[37]), n(c[38]), n(c[39]),
            // 납품 (41~44)
            n(c[40]), n(c[41]), n(c[42]), n(c[43]),
            // 금액/수량 원본 (45~49)
            n(c[44]), n(c[45]), n(c[46]), n(c[47]), n(c[48]),
            // 메타
            fileName
        };
    }

    /** 빈 문자열은 null로 변환 */
    private String n(String v) {
        return (v == null || v.isEmpty()) ? null : v;
    }

    /** Y/N 플래그: 빈값은 null */
    private String flag(String v) {
        if (v == null || v.isEmpty()) return null;
        return v.toUpperCase();
    }

    /** 물품순번을 int로 변환, 실패 시 1 */
    private int seqInt(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int executeBatch(List<Object[]> batch) {
        int[] results = jdbc.batchUpdate(INSERT_SQL, batch);
        int count = 0;
        for (int r : results) {
            if (r > 0) count += r;
        }
        return count;
    }

    private void writeUploadHistory(String uploadType, String fileName, long fileSizeBytes, String uploader, CsvUploadResultDto result) {
        String status = (result.getErrorMessage() == null) ? "SUCCESS" : "FAILED";
        jdbc.update(
                "INSERT INTO csv_upload_history (" +
                        "upload_type, file_name, file_size_bytes, total_rows, inserted_count, skipped_count, elapsed_ms, uploader, status, error_message" +
                        ") VALUES (?,?,?,?,?,?,?,?,?,?)",
                uploadType,
                fileName == null ? "" : fileName,
                fileSizeBytes,
                result.getTotalRows(),
                result.getInsertedCount(),
                result.getSkippedCount(),
                result.getElapsedMs(),
                uploader,
                status,
                result.getErrorMessage()
        );
    }
}
