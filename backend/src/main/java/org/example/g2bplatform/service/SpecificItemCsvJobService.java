package org.example.g2bplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.g2bplatform.DTO.CsvUploadJobDto;
import org.example.g2bplatform.DTO.CsvUploadResultDto;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificItemCsvJobService {

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
    private final SpecificItemEtlService specificItemEtlService;

    public CsvUploadJobDto startSpecificItemJob(MultipartFile file, String uploader) {
        String jobId = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        long fileSizeBytes = file.getSize();

        try {
            File dir = new File("/tmp/g2b-upload");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            String tempPath = new File(dir, jobId + ".csv").getAbsolutePath();

            try (BufferedReader reader = openReaderUtf16(file);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempPath), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            jdbc.update(
                    "INSERT INTO csv_upload_job (id, upload_type, file_name, file_size_bytes, temp_path, status, message, uploader, created_at, updated_at) " +
                            "VALUES (?,?,?,?,?,?,?,?,NOW(),NOW())",
                    jobId, "specific_item", fileName, fileSizeBytes, tempPath, "QUEUED", "대기 중", uploader
            );

            csvJobExecutor.execute(() -> runJob(jobId, tempPath, fileName, fileSizeBytes, uploader));

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

    private void runJob(String jobId, String tempPath, String fileName, long fileSizeBytes, String uploader) {
        try {
            jdbc.update("UPDATE csv_upload_job SET status='RUNNING', message='총 행 수 계산 중', started_at=NOW() WHERE id=?", jobId);
            int totalRows = countTotalRows(tempPath);
            jdbc.update("UPDATE csv_upload_job SET total_rows=?, message='적재 중' WHERE id=?", totalRows, jobId);

            CsvUploadResultDto result = loadAndInsert(jobId, tempPath, fileName, fileSizeBytes, uploader, totalRows);
            String finalStatus = (result.getErrorMessage() == null) ? "SUCCESS" : "FAILED";

            jdbc.update(
                    "UPDATE csv_upload_job SET status=?, message=?, finished_at=NOW(), error_message=? WHERE id=?",
                    finalStatus,
                    (result.getErrorMessage() == null) ? "완료" : "실패",
                    result.getErrorMessage(),
                    jobId
            );

            // CSV 적재 성공 시 flat 테이블 ETL 자동 트리거
            if ("SUCCESS".equals(finalStatus)) {
                specificItemEtlService.startEtlJob(uploader);
            }
        } catch (Exception e) {
            log.error("Job 실패: {}", jobId, e);
            jdbc.update(
                    "UPDATE csv_upload_job SET status='FAILED', message='실패', finished_at=NOW(), error_message=? WHERE id=?",
                    e.getMessage(),
                    jobId
            );
        } finally {
            try { new File(tempPath).delete(); } catch (Exception ignore) {}
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

    private CsvUploadResultDto loadAndInsert(String jobId, String tempPath, String fileName, long fileSizeBytes, String uploader, int totalRows) {
        long start = System.currentTimeMillis();
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);

        int processed = 0;
        int inserted = 0;
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
                    inserted += executeBatch(batch);
                    batch.clear();
                    jdbc.update("UPDATE csv_upload_job SET processed_rows=?, inserted_count=?, skipped_count=?, message='적재 중' WHERE id=?",
                            processed, inserted, processed - inserted, jobId);
                }
            }
            if (!batch.isEmpty()) inserted += executeBatch(batch);

            jdbc.update("UPDATE csv_upload_job SET processed_rows=?, inserted_count=?, skipped_count=?, message='마무리 중' WHERE id=?",
                    processed, inserted, processed - inserted, jobId);

            if (!headerFound) {
                CsvUploadResultDto err = CsvUploadResultDto.error(fileName, "헤더 행을 찾을 수 없습니다. 올바른 특정품목 조달 내역 CSV 파일인지 확인하세요.");
                err.setFileSizeBytes(fileSizeBytes);
                err.setTotalRows(totalRows);
                err.setInsertedCount(inserted);
                err.setSkippedCount(processed - inserted);
                err.setElapsedMs(System.currentTimeMillis() - start);
                writeUploadHistory("specific_item", fileName, fileSizeBytes, uploader, err);
                return err;
            }

            long elapsed = System.currentTimeMillis() - start;
            CsvUploadResultDto ok = CsvUploadResultDto.builder()
                    .fileName(fileName)
                    .fileSizeBytes(fileSizeBytes)
                    .totalRows(totalRows)
                    .insertedCount(inserted)
                    .skippedCount(processed - inserted)
                    .elapsedMs(elapsed)
                    .build();
            writeUploadHistory("specific_item", fileName, fileSizeBytes, uploader, ok);
            return ok;

        } catch (Exception e) {
            CsvUploadResultDto err = CsvUploadResultDto.error(fileName, e.getMessage());
            err.setFileSizeBytes(fileSizeBytes);
            err.setTotalRows(totalRows);
            err.setInsertedCount(inserted);
            err.setSkippedCount(processed - inserted);
            err.setElapsedMs(System.currentTimeMillis() - start);
            writeUploadHistory("specific_item", fileName, fileSizeBytes, uploader, err);
            return err;
        }
    }

    private BufferedReader openTempReader(String tempPath) throws Exception {
        return new BufferedReader(new InputStreamReader(new FileInputStream(tempPath), StandardCharsets.UTF_8));
    }

    private BufferedReader openReaderUtf16(MultipartFile file) throws Exception {
        Charset cs = Charset.forName("UTF-16");
        return new BufferedReader(new InputStreamReader(file.getInputStream(), cs));
    }

    private String[] splitLine(String line) {
        String[] parts = line.split("\t", -1);
        for (int i = 0; i < parts.length; i++) {
            String v = parts[i].trim();
            if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) v = v.substring(1, v.length() - 1);
            parts[i] = v;
        }
        return parts;
    }

    private Object[] toParams(String[] c, String fileName) {
        return new Object[]{
                n(c[0]),  n(c[1]),  n(c[2]),  n(c[3]),
                n(c[4]),  n(c[5]),  n(c[6]),  seqInt(c[7]),  flag(c[8]),
                n(c[9]),  n(c[10]), n(c[11]), n(c[12]),
                n(c[13]), n(c[14]), n(c[15]), n(c[16]), n(c[17]), n(c[18]),
                n(c[19]), n(c[20]), n(c[21]), n(c[22]),
                n(c[23]),
                flag(c[24]), flag(c[25]), flag(c[26]), flag(c[27]), flag(c[28]),
                n(c[29]),
                n(c[30]), n(c[31]),
                n(c[32]), n(c[33]), n(c[34]), n(c[35]),
                n(c[36]), n(c[37]), n(c[38]), n(c[39]),
                n(c[40]), n(c[41]), n(c[42]), n(c[43]),
                n(c[44]), n(c[45]), n(c[46]), n(c[47]), n(c[48]),
                fileName
        };
    }

    private String n(String v) { return (v == null || v.isEmpty()) ? null : v; }
    private String flag(String v) { return (v == null || v.isEmpty()) ? null : v.toUpperCase(); }
    private int seqInt(String v) { try { return Integer.parseInt(v.trim()); } catch (Exception e) { return 1; } }

    private int executeBatch(List<Object[]> batch) {
        int[] results = jdbc.batchUpdate(INSERT_SQL, batch);
        int count = 0;
        for (int r : results) if (r > 0) count += r;
        return count;
    }

    private void writeUploadHistory(String uploadType, String fileName, long fileSizeBytes, String uploader, CsvUploadResultDto result) {
        String status = (result.getErrorMessage() == null) ? "SUCCESS" : "FAILED";
        jdbc.update(
                "INSERT INTO csv_upload_history (upload_type, file_name, file_size_bytes, total_rows, inserted_count, skipped_count, elapsed_ms, uploader, status, error_message) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
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

