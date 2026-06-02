package org.example.g2bplatform.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvUploadResultDto {

    private String  fileName;
    private long    fileSizeBytes;
    private int     totalRows;
    private int     insertedCount;
    private int     skippedCount;
    private long    elapsedMs;
    private String  errorMessage;

    public static CsvUploadResultDto error(String fileName, String message) {
        return CsvUploadResultDto.builder()
                .fileName(fileName)
                .errorMessage(message)
                .build();
    }
}
