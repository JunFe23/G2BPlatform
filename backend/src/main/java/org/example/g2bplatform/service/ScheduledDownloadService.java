package org.example.g2bplatform.service;

import lombok.RequiredArgsConstructor;
import org.example.g2bplatform.controller.HomeController;
import org.example.g2bplatform.model.DownloadHistory;
import org.example.g2bplatform.repository.DownloadHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduledDownloadService {

    private final HomeController homeController;
    private final DataCleanupService dataCleanupService; // 날짜 기준 삭제 서비스 (아래 예시)

    @Value("${g2b.service-key}")
    private String serviceKey;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledDownloadService.class);

    // 필드 추가
    private final DownloadHistoryRepository downloadHistoryRepository;

    public void download(String apiName, String endpoint) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        LocalDateTime start = startDate.atStartOfDay(); // 00:00
        LocalDateTime end = today.atTime(23, 59);   // 23:59

        // 삭제
        dataCleanupService.deleteByRgstDtRange(endpoint, start, end);

        // 정확한 형식으로 포맷
        String inqryBgnDt = start.format(dateTimeFormatter); // 202504130000
        String inqryEndDt = end.format(dateTimeFormatter);   // 202505132359


        // fetch 호출
        Map<String, String> req = new HashMap<>();
        req.put("endpoint", endpoint);
        req.put("serviceKey", serviceKey);
        req.put("inqryBgnDt", inqryBgnDt);
        req.put("inqryEndDt", inqryEndDt);

        homeController.fetchDataFromApi(req).subscribe(resp -> {
            logger.info("[{}] 자동 다운로드 완료: {}", apiName, resp.getBody());

            downloadHistoryRepository.save(
                    DownloadHistory.builder()
                            .dataType(apiName)
                            .startDate(startDate)
                            .endDate(today)
                            .status("성공")
                            .message(resp.getBody())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }, err -> {
            logger.error("[{}] 자동 다운로드 실패", apiName, err);

            downloadHistoryRepository.save(
                    DownloadHistory.builder()
                            .dataType(apiName)
                            .startDate(startDate)
                            .endDate(today)
                            .status("실패")
                            .message(err.getMessage())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        });
    }
}
