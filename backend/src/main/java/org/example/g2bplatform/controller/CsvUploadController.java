package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.g2bplatform.DTO.CsvUploadJobDto;
import org.example.g2bplatform.service.SpecificItemCsvJobService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "CSV 업로드", description = "Raw 데이터 CSV 업로드 API")
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class CsvUploadController {

    private final SpecificItemCsvJobService specificItemCsvJobService;

    /**
     * 특정품목 조달 내역 CSV 업로드 → 비동기 적재 Job 생성.
     * 접근 권한: SUPER_ADMIN
     */
    @Operation(summary = "특정품목 조달 내역 CSV 적재(Job 시작)",
               description = "CSV를 업로드하면 서버에서 비동기 적재 Job을 생성하고 jobId를 반환합니다. " +
                             "진행률은 GET /api/admin/upload/jobs/{jobId} 로 조회합니다.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(value = "/specific-item", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvUploadJobDto> uploadSpecificItem(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(CsvUploadJobDto.builder()
                    .status("FAILED")
                    .errorMessage("파일이 첨부되지 않았습니다.")
                    .build());
        }

        String uploader = (auth == null) ? null : auth.getName();
        CsvUploadJobDto job = specificItemCsvJobService.startSpecificItemJob(file, uploader);

        if ("FAILED".equals(job.getStatus())) return ResponseEntity.unprocessableEntity().body(job);

        return ResponseEntity.ok(job);
    }

    @Operation(summary = "CSV 적재 Job 상태 조회",
            description = "CSV 적재 Job 진행률/상태를 조회합니다.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<CsvUploadJobDto> getJob(@PathVariable String jobId) {
        CsvUploadJobDto job = specificItemCsvJobService.getJob(jobId);
        if (job == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(job);
    }
}
