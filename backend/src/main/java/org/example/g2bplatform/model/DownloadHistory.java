package org.example.g2bplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "download_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dataType;  // 예: 물품, 공사, 용역 등

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;    // 성공, 실패

    @Column(columnDefinition = "TEXT")
    private String message;   // 응답 내용이나 에러 메시지

    private LocalDateTime createdAt;
}
