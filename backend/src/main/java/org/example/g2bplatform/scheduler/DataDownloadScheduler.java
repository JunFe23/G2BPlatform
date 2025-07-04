package org.example.g2bplatform.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.g2bplatform.service.ScheduledDownloadService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataDownloadScheduler {

    private final ScheduledDownloadService scheduledDownloadService;
    private final JdbcTemplate jdbcTemplate;

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
    @Scheduled(cron = "0 0 22 * * *")
    public void runUpdateThingsProcedure() {
        System.out.println("✅ [Scheduler] 물품 데이터 통합 처리 시작");
        try {
            jdbcTemplate.execute("CALL g2b.update_daily_contracts_things()");
            System.out.println("✅ [Scheduler] 물품 데이터 통합 처리 완료");
        } catch (Exception e) {
            System.err.println("❌ [Scheduler] 물품 데이터 통합 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 매일 새벽 7시 20분 (용역 데이터 통합 처리)
    @Scheduled(cron = "0 20 22 * * *")
    public void runUpdateServicesProcedure() {
        System.out.println("✅ [Scheduler] 용역 데이터 통합 처리 시작");
        try {
            jdbcTemplate.execute("CALL g2b.update_daily_contracts_services()");
            System.out.println("✅ [Scheduler] 용역 데이터 통합 처리 완료");
        } catch (Exception e) {
            System.err.println("❌ [Scheduler] 용역 데이터 통합 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 매일 새벽 7시 40분 (공사 데이터 통합 처리)
    @Scheduled(cron = "0 40 22 * * *")
    public void runUpdateConstructionsProcedure() {
        System.out.println("✅ [Scheduler] 공사 데이터 통합 처리 시작");
        try {
            jdbcTemplate.execute("CALL g2b.update_daily_contracts_constructions()");
            System.out.println("✅ [Scheduler] 공사 데이터 통합 처리 완료");
        } catch (Exception e) {
            System.err.println("❌ [Scheduler] 공사 데이터 통합 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 매일 새벽 8시 (탑 데이터 통합 처리)
    @Scheduled(cron = "0 0 23 * * *")
    public void runUpdateTopDatasProcedure() {
        System.out.println("✅ [Scheduler] 탑 데이터 통합 처리 시작");
        try {
            jdbcTemplate.execute("CALL g2b.update_daily_contracts_topDatas()");
            System.out.println("✅ [Scheduler] 탑 데이터 통합 처리 완료");
        } catch (Exception e) {
            System.err.println("❌ [Scheduler] 탑 데이터 통합 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}