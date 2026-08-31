package com.runningolle.domain.tourism.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TourismSyncResponse(
        String areaCode,
        List<String> contentTypeIds,
        int fetchedCount,
        int createdCount,
        int updatedCount,
        int skippedCount,
        int failedCount,
        LocalDateTime syncedAt
) {
}
