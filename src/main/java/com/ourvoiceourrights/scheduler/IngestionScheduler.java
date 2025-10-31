package com.ourvoiceourrights.scheduler;

import com.ourvoiceourrights.service.ingestion.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionScheduler {

    private final IngestionService ingestionService;

    @Scheduled(cron = "${INGEST_SCHEDULE_CRON:${app.ingestion-schedule-cron:0 0 */6 * * *}}", zone = "UTC")
    public void ingest() {
        log.info("Starting scheduled DataGov ingestion job");
        ingestionService.ingestAllConfiguredStates();
    }
}
