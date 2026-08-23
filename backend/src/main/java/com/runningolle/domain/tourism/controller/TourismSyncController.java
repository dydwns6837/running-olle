package com.runningolle.domain.tourism.controller;

import com.runningolle.domain.tourism.dto.TourismSyncResponse;
import com.runningolle.domain.tourism.service.TourismPlaceSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tourism")
public class TourismSyncController {

    private final TourismPlaceSyncService tourismPlaceSyncService;

    @PostMapping("/sync/jeju")
    public TourismSyncResponse syncJejuTourismPlaces() {
        return tourismPlaceSyncService.syncJejuTourismPlaces();
    }
}
