package org.example.g2bplatform.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvUploadJobDto {

    private String jobId;
    private String uploadType;

    private String fileName;
    private Long fileSizeBytes;

    private String status;      // QUEUED/RUNNING/SUCCESS/FAILED/CANCELLED
    private String message;

    private Integer totalRows;
    private Integer processedRows;
    private Integer insertedCount;
    private Integer skippedCount;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String errorMessage;
}

