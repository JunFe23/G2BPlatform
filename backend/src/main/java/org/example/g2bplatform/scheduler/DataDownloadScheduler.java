package org.example.g2bplatform.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.g2bplatform.service.DataService;
import org.example.g2bplatform.service.ScheduledDownloadService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataDownloadScheduler {

    private final ScheduledDownloadService scheduledDownloadService;
    private final JdbcTemplate jdbcTemplate;
    private final DataService dataService;

    // 매일 새벽 3시 (물품)
    @Scheduled(cron = "0 0 18 * * *")
    public void downloadThng() {
        scheduledDownloadService.download("getCntrctInfoListThng", "/1230000/ao/CntrctInfoService/getCntrctInfoListThng");
    }

    // 매일 새벽 4시 (물품세부)
    @Scheduled(cron = "0 0 19 * * *")
    public void downloadThngDetail() {
        scheduledDownloadService.download("getCntrctInfoListThngDetail", "/1230000/ao/CntrctInfoService/getCntrctInfoListThngDetail");
    }

    // 매일 새벽 5시 (공사)
    @Scheduled(cron = "0 0 20 * * *")
    public void downloadCnstwk() {
        scheduledDownloadService.download("getCntrctInfoListCnstwk", "/1230000/ao/CntrctInfoService/getCntrctInfoListCnstwk");
    }

    // 매일 새벽 6시 (용역)
    @Scheduled(cron = "0 0 21 * * *")
    public void downloadServc() {
        scheduledDownloadService.download("getCntrctInfoListServc", "/1230000/ao/CntrctInfoService/getCntrctInfoListServc");
    }

    // 매일 새벽 7시 (물품 데이터 통합 처리)
    @Scheduled(cron = "0 13 14 * * *")
    public void runUpdateThingsProcedure() {
        try {
            System.out.println("✅ [Scheduler] 물품 데이터 통합 처리 시작");
            dataService.callProcedure("g2b.update_daily_contracts_things");
            System.out.println("✅ [Scheduler] 물품 데이터 통합 처리 완료");
        } catch (Exception e) {
            System.err.println("❌ [Scheduler] 물품 데이터 통합 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 매일 새벽 7시 20분 (용역 데이터 통합 처리)
    @Scheduled(cron = "0 16 14 * * *")
    public void runUpdateServicesProcedure() {
        jdbcTemplate.execute("CALL g2b.update_daily_contracts_services()");
    }

    // 매일 새벽 7시 40분 (공사 데이터 통합 처리)
    @Scheduled(cron = "0 40 22 * * *")
    public void runUpdateConstructionsProcedure() {
        jdbcTemplate.execute("CALL g2b.update_daily_contracts_constructions()");
    }

    // 매일 새벽 8시 (탑 데이터 통합 처리)
    @Scheduled(cron = "0 0 23 * * *")
    public void runUpdateTopDatasProcedure() {
        jdbcTemplate.execute("CALL g2b.update_daily_contracts_topDatas()");
    }
}