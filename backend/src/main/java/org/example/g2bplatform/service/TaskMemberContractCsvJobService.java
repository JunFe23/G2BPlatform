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
public class TaskMemberContractCsvJobService {

    private static final int BATCH_SIZE = 2_000;

    private static final String INSERT_SQL =
            "INSERT IGNORE INTO task_member_contract_raw (" +
                    "  contract_type," +
                    "  procurement_method, contract_no, change_seq, is_final_contract," +
                    "  contract_date, first_contract_date, contract_name," +
                    "  demand_agency_code, demand_agency_name," +
                    "  contract_method, bid_notice_no, bid_notice_seq, bid_method, applicable_law, clause_content," +
                    "  new_long_term_type, is_long_term_first_year, first_year_contract_no, long_term_sequence," +
                    "  public_procurement_type, public_procurement_major, public_procurement_minor," +
                    "  work_location, start_date, end_date, total_end_date," +
                    "  vendor_enterprise_type, vendor_region," +
                    "  is_female_enterprise, is_disabled_enterprise, is_social_enterprise," +
                    "  jurisdiction_type, demand_agency_region, joint_contract_method, work_type," +
                    "  is_representative, vendor_name, vendor_biz_reg_no, representative_name," +
                    "  contract_share_rate_raw, contract_share_change_raw, contract_share_amount_raw," +
                    "  current_contract_amount_raw, first_contract_amount_raw, total_contract_amount_raw," +
                    "  source_file" +
                    ") VALUES (" +
                    "  ?," +
                    "  ?,?,?,?," +
                    "  ?,?,?," +
                    "  ?,?," +
                    "  ?,?,?,?,?,?," +
                    "  ?,?,?,?," +
                    "  ?,?,?," +
                    "  ?,?,?,?," +
                    "  ?,?," +
                    "  ?,?,?," +
                    "  ?,?,?,?," +
                    "  ?,?,?,?," +
                    "  ?,?,?," +
                    "  ?,?,?," +
                    "  ?" +
                    ")";

    private final JdbcTemplate jdbc;
    private final TaskExecutor csvJobExecutor;

    public CsvUploadJobDto startJob(MultipartFile file, String uploader) {
        String jobId = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        long fileSizeBytes = file.getSize();
        String contractType = detectContractType(fileName);

        try {
            File dir = new File("/tmp/g2b-upload");
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
                    jobId, "task_member_contract", fileName, fileSizeBytes, tempPath, "QUEUED", "대기 중", uploader
            );

            csvJobExecutor.execute(() -> runJob(jobId, tempPath, fileName, fileSizeBytes, contractType, uploader));

            return CsvUploadJobDto.builder()
                    .jobId(jobId)
                    .uploadType("task_member_contract")
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
                    .uploadType("task_member_contract")
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

    // 파일명으로 업종 자동 판별
    private String detectContractType(String fileName) {
        if (fileName == null) return "service";
        if (fileName.contains("공사")) return "construction";
        return "service";
    }

    private void runJob(String jobId, String tempPath, String fileName, long fileSizeBytes, String contractType, String uploader) {
        try {
            jdbc.update("UPDATE csv_upload_job SET status='RUNNING', message='총 행 수 계산 중', started_at=NOW() WHERE id=?", jobId);
            int totalRows = countTotalRows(tempPath);
            jdbc.update("UPDATE csv_upload_job SET total_rows=?, message='적재 중' WHERE id=?", totalRows, jobId);

            CsvUploadResultDto result = loadAndInsert(jobId, tempPath, fileName, fileSizeBytes, contractType, uploader, totalRows);
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
                    e.getMessage(), jobId
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
                    if (line.contains("조달방식") && line.contains("계약번호")) { headerFound = true; }
                    continue;
                }
                if (splitLine(line).length < 45) continue;
                total++;
            }
        }
        return total;
    }

    private CsvUploadResultDto loadAndInsert(String jobId, String tempPath, String fileName, long fileSizeBytes, String contractType, String uploader, int totalRows) {
        long start = System.currentTimeMillis();
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int processed = 0, inserted = 0;
        boolean headerFound = false;

        try (BufferedReader reader = openTempReader(tempPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!headerFound) {
                    if (line.contains("조달방식") && line.contains("계약번호")) { headerFound = true; }
                    continue;
                }
                String[] cols = splitLine(line);
                if (cols.length < 45) continue;

                processed++;
                batch.add(toParams(cols, contractType, fileName));

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
                CsvUploadResultDto err = CsvUploadResultDto.error(fileName, "헤더 행을 찾을 수 없습니다. 올바른 업무별 구성원별 계약내역 CSV 파일인지 확인하세요.");
                err.setFileSizeBytes(fileSizeBytes);
                err.setTotalRows(totalRows);
                err.setInsertedCount(inserted);
                err.setSkippedCount(processed - inserted);
                err.setElapsedMs(System.currentTimeMillis() - start);
                writeUploadHistory(fileName, fileSizeBytes, uploader, err);
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
            writeUploadHistory(fileName, fileSizeBytes, uploader, ok);
            return ok;

        } catch (Exception e) {
            CsvUploadResultDto err = CsvUploadResultDto.error(fileName, e.getMessage());
            err.setFileSizeBytes(fileSizeBytes);
            err.setTotalRows(totalRows);
            err.setInsertedCount(inserted);
            err.setSkippedCount(processed - inserted);
            err.setElapsedMs(System.currentTimeMillis() - start);
            writeUploadHistory(fileName, fileSizeBytes, uploader, err);
            return err;
        }
    }

    private Object[] toParams(String[] c, String contractType, String fileName) {
        return new Object[]{
                contractType,
                n(c[0]),  n(c[1]),  n(c[2]),  flag(c[3]),
                n(c[4]),  n(c[5]),  n(c[6]),
                n(c[7]),  n(c[8]),
                n(c[9]),  n(c[10]), n(c[11]), n(c[12]), n(c[13]), n(c[14]),
                n(c[15]), flag(c[16]), n(c[17]), n(c[18]),
                n(c[19]), n(c[20]), n(c[21]),
                n(c[22]), n(c[23]), n(c[24]), n(c[25]),
                n(c[26]), n(c[27]),
                flag(c[28]), flag(c[29]), flag(c[30]),
                n(c[31]), n(c[32]), n(c[33]), n(c[34]),
                flag(c[35]), n(c[36]), n(c[37]), n(c[38]),
                n(c[39]), n(c[40]), n(c[41]),
                n(c[42]), n(c[43]), n(c[44]),
                fileName
        };
    }

    private String[] splitLine(String line) {
        // RFC 4180 CSV 파서: 쌍따옴표로 감싼 필드 내 쉼표를 구분자로 오인하지 않도록 처리
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuote) {
                if (c == '"') {
                    // escaped quote ""
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuote = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuote = true;
                } else if (c == ',') {
                    fields.add(cur.toString().trim());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        fields.add(cur.toString().trim());
        return fields.toArray(new String[0]);
    }

    private String n(String v) { return (v == null || v.isEmpty()) ? null : v; }
    private String flag(String v) { return (v == null || v.isEmpty()) ? null : v.toUpperCase(); }

    private int executeBatch(List<Object[]> batch) {
        int[] results = jdbc.batchUpdate(INSERT_SQL, batch);
        int count = 0;
        for (int r : results) if (r > 0) count += r;
        return count;
    }

    private BufferedReader openTempReader(String tempPath) throws Exception {
        return new BufferedReader(new InputStreamReader(new FileInputStream(tempPath), StandardCharsets.UTF_8));
    }

    private BufferedReader openReaderUtf16(MultipartFile file) throws Exception {
        return new BufferedReader(new InputStreamReader(file.getInputStream(), Charset.forName("UTF-16")));
    }

    private void writeUploadHistory(String fileName, long fileSizeBytes, String uploader, CsvUploadResultDto result) {
        String status = (result.getErrorMessage() == null) ? "SUCCESS" : "FAILED";
        jdbc.update(
                "INSERT INTO csv_upload_history (upload_type, file_name, file_size_bytes, total_rows, inserted_count, skipped_count, elapsed_ms, uploader, status, error_message) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                "task_member_contract", fileName == null ? "" : fileName,
                fileSizeBytes, result.getTotalRows(), result.getInsertedCount(),
                result.getSkippedCount(), result.getElapsedMs(), uploader, status, result.getErrorMessage()
        );
    }
}
