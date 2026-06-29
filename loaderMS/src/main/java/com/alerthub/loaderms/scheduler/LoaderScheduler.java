package com.alerthub.loaderms.scheduler;

import com.alerthub.loaderms.dto.ScanResultDto;
import com.alerthub.loaderms.service.LoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoaderScheduler {

    private final LoaderService loaderService;

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledScan() {
        log.info("Hourly scan triggered by scheduler");
        ScanResultDto result = loaderService.scanAllProviders();
        log.info("Scheduled scan complete: {} files processed, {} records loaded",
                result.getFilesScanned(), result.getTotalRecordsLoaded());
    }
}
