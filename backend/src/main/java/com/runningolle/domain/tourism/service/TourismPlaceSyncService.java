package com.runningolle.domain.tourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningolle.domain.tourism.client.TourApiClient;
import com.runningolle.domain.tourism.client.TourApiClient.TourAreaItem;
import com.runningolle.domain.tourism.client.TourApiClient.TourAreaPage;
import com.runningolle.domain.tourism.client.TourApiClient.TourDetail;
import com.runningolle.domain.tourism.config.TourismSyncProperties;
import com.runningolle.domain.tourism.dto.TourismSyncResponse;
import com.runningolle.domain.tourism.entity.TourismPlace;
import com.runningolle.domain.tourism.entity.TourismPlace.TourismPlaceSnapshot;
import com.runningolle.domain.tourism.repository.TourismPlaceRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourismPlaceSyncService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final TourApiClient tourApiClient;
    private final TourismPlaceRepository tourismPlaceRepository;
    private final TourismSyncProperties tourismSyncProperties;
    private final ObjectMapper objectMapper;

    public TourismSyncResponse syncJejuTourismPlaces() {
        LocalDateTime syncedAt = LocalDateTime.now();
        SyncStats stats = new SyncStats();
        List<String> contentTypeIds = targetContentTypeIds();

        for (String contentTypeId : contentTypeIds) {
            syncContentType(contentTypeId, stats, syncedAt);
        }

        return new TourismSyncResponse(
                tourismSyncProperties.getAreaCode(),
                contentTypeIds,
                stats.fetchedCount,
                stats.createdCount,
                stats.updatedCount,
                stats.skippedCount,
                stats.failedCount,
                syncedAt
        );
    }

    private List<String> targetContentTypeIds() {
        return tourismSyncProperties.getContentTypeIds().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private void syncContentType(String contentTypeId, SyncStats stats, LocalDateTime syncedAt) {
        int pageNo = 1;
        int pageSize = Math.max(1, tourismSyncProperties.getPageSize());

        while (true) {
            TourAreaPage page = tourApiClient.getAreaBasedList(
                    tourismSyncProperties.getAreaCode(),
                    contentTypeId,
                    pageNo,
                    pageSize
            );

            if (page.items().isEmpty()) {
                return;
            }

            for (TourAreaItem item : page.items()) {
                syncItem(item, stats, syncedAt);
            }

            if (page.pageNo() * page.numOfRows() >= page.totalCount()) {
                return;
            }
            pageNo++;
        }
    }

    private void syncItem(TourAreaItem item, SyncStats stats, LocalDateTime syncedAt) {
        stats.fetchedCount++;

        if (!isSyncable(item)) {
            stats.skippedCount++;
            return;
        }

        Optional<TourDetail> detail = fetchDetail(item, stats);

        try {
            TourismPlaceSnapshot snapshot = toSnapshot(item, detail.orElse(null), syncedAt);
            Optional<TourismPlace> savedPlace = tourismPlaceRepository.findByContentId(item.contentId());

            if (savedPlace.isPresent()) {
                savedPlace.get().sync(snapshot);
                tourismPlaceRepository.save(savedPlace.get());
                stats.updatedCount++;
            } else {
                tourismPlaceRepository.save(TourismPlace.create(snapshot));
                stats.createdCount++;
            }
        } catch (RuntimeException exception) {
            stats.failedCount++;
            log.warn(
                    "Failed to sync TourAPI place. contentId={}, title={}",
                    item.contentId(),
                    item.title(),
                    exception
            );
        }
    }

    private Optional<TourDetail> fetchDetail(TourAreaItem item, SyncStats stats) {
        try {
            return tourApiClient.getDetail(item.contentId(), item.contentTypeId());
        } catch (RuntimeException exception) {
            stats.failedCount++;
            log.warn(
                    "Failed to enrich TourAPI place detail. contentId={}, title={}",
                    item.contentId(),
                    item.title(),
                    exception
            );
            return Optional.empty();
        }
    }

    private static boolean isSyncable(TourAreaItem item) {
        return StringUtils.hasText(item.contentId())
                && StringUtils.hasText(item.contentTypeId())
                && StringUtils.hasText(item.title())
                && item.lat() != null
                && item.lng() != null;
    }

    private TourismPlaceSnapshot toSnapshot(TourAreaItem item, TourDetail detail, LocalDateTime syncedAt) {
        double lat = detail == null || detail.lat() == null ? item.lat() : detail.lat();
        double lng = detail == null || detail.lng() == null ? item.lng() : detail.lng();

        return new TourismPlaceSnapshot(
                item.contentId(),
                firstNonBlank(detail == null ? null : detail.contentTypeId(), item.contentTypeId()),
                firstNonBlank(detail == null ? null : detail.title(), item.title()),
                firstNonBlank(detail == null ? null : detail.address(), item.address()),
                firstNonBlank(detail == null ? null : detail.detailAddress(), item.detailAddress()),
                item.tel(),
                firstNonBlank(detail == null ? null : detail.category1(), item.category1()),
                firstNonBlank(detail == null ? null : detail.category2(), item.category2()),
                firstNonBlank(detail == null ? null : detail.category3(), item.category3()),
                firstNonBlank(detail == null ? null : detail.areaCode(), item.areaCode()),
                firstNonBlank(detail == null ? null : detail.sigunguCode(), item.sigunguCode()),
                point(lng, lat),
                firstNonBlank(detail == null ? null : detail.firstImageUrl(), item.firstImageUrl()),
                item.thumbnailImageUrl(),
                detail == null ? null : detail.overview(),
                detail == null ? null : detail.useTime(),
                item.createdTime(),
                item.modifiedTime(),
                rawData(item, detail),
                syncedAt
        );
    }

    private JsonNode rawData(TourAreaItem item, TourDetail detail) {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("areaBasedList2", item.raw());
        if (detail != null) {
            raw.put("detail", detail.raw());
        }
        return objectMapper.valueToTree(raw);
    }

    private static Point point(double lng, double lat) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);
        return point;
    }

    private static String firstNonBlank(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : fallback;
    }

    private static class SyncStats {

        private int fetchedCount;
        private int createdCount;
        private int updatedCount;
        private int skippedCount;
        private int failedCount;
    }
}
