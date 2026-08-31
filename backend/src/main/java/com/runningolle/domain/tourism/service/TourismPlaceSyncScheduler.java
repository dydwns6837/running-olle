package com.runningolle.domain.tourism.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tourism.sync", name = "scheduler-enabled", havingValue = "true")
public class TourismPlaceSyncScheduler {

    private final TourismPlaceSyncService tourismPlaceSyncService;

    @Scheduled(cron = "${tourism.sync.cron}", zone = "${tourism.sync.zone}")
    public void syncJejuTourismPlaces() {
        try {
            log.info("Scheduled TourAPI Jeju tourism place sync started.");
            var response = tourismPlaceSyncService.syncJejuTourismPlaces();
            log.info("Scheduled TourAPI Jeju tourism place sync finished. response={}", response);
        } catch (RuntimeException exception) {
            log.error("Scheduled TourAPI Jeju tourism place sync failed.", exception);
        }
    }
}
