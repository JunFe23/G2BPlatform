package org.example.g2bplatform.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.g2bplatform.service.ScheduledDownloadService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataDownloadScheduler {

    private final ScheduledDownloadService scheduledDownloadService;

    // 매일 새벽 3시 (물품)
    @Scheduled(cron = "0 0 3 * * *")
    public void downloadThng() {
        scheduledDownloadService.download("getCntrctInfoListThng", "/1230000/ao/CntrctInfoService/getCntrctInfoListThng");
    }

    // 매일 새벽 4시 (물품세부)
    @Scheduled(cron = "0 0 4 * * *")
    public void downloadThngDetail() {
        scheduledDownloadService.download("getCntrctInfoListThngDetail", "/1230000/ao/CntrctInfoService/getCntrctInfoListThngDetail");
    }

    // 매일 새벽 5시 (공사)
    @Scheduled(cron = "0 0 5 * * *")
    public void downloadCnstwk() {
        scheduledDownloadService.download("getCntrctInfoListCnstwk", "/1230000/ao/CntrctInfoService/getCntrctInfoListCnstwk");
    }

    // 매일 새벽 6시 (용역)
    @Scheduled(cron = "0 0 6 * * *")
    public void downloadServc() {
        scheduledDownloadService.download("getCntrctInfoListServc", "/1230000/ao/CntrctInfoService/getCntrctInfoListServc");
    }
}