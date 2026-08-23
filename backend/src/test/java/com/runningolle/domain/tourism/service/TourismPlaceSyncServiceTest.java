package com.runningolle.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningolle.domain.tourism.client.TourApiClient;
import com.runningolle.domain.tourism.client.TourApiClient.TourAreaItem;
import com.runningolle.domain.tourism.client.TourApiClient.TourAreaPage;
import com.runningolle.domain.tourism.client.TourApiClient.TourDetail;
import com.runningolle.domain.tourism.config.TourismSyncProperties;
import com.runningolle.domain.tourism.entity.TourismPlace;
import com.runningolle.domain.tourism.entity.TourismPlace.TourismPlaceSnapshot;
import com.runningolle.domain.tourism.repository.TourismPlaceRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TourismPlaceSyncServiceTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private TourismPlaceRepository tourismPlaceRepository;

    private TourismPlaceSyncService tourismPlaceSyncService;

    @BeforeEach
    void setUp() {
        TourismSyncProperties properties = new TourismSyncProperties();
        properties.setAreaCode("39");
        properties.setContentTypeIds(List.of("12"));
        properties.setPageSize(100);

        tourismPlaceSyncService = new TourismPlaceSyncService(
                tourApiClient,
                tourismPlaceRepository,
                properties,
                new ObjectMapper()
        );
    }

    @Test
    void createsTourismPlaceWithDetailData() {
        TourAreaItem item = tourAreaItem("1", "한라산", 33.361667, 126.529167);
        TourDetail detail = new TourDetail(
                "1",
                "12",
                "한라산 상세",
                "제주특별자치도 제주시",
                "1100로",
                "39",
                "4",
                "A01",
                "A0101",
                "A01010100",
                33.362,
                126.53,
                "제주의 대표 산",
                "https://example.com/halla.jpg",
                "09:00 - 18:00",
                Map.of("detailCommon2", Map.of("contentid", "1"))
        );
        given(tourApiClient.getAreaBasedList("39", "12", 1, 100))
                .willReturn(new TourAreaPage(List.of(item), 1, 100, 1));
        given(tourApiClient.getDetail("1", "12")).willReturn(Optional.of(detail));
        given(tourismPlaceRepository.findByContentId("1")).willReturn(Optional.empty());

        var response = tourismPlaceSyncService.syncJejuTourismPlaces();

        assertThat(response.fetchedCount()).isEqualTo(1);
        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.updatedCount()).isZero();
        assertThat(response.skippedCount()).isZero();
        assertThat(response.failedCount()).isZero();

        ArgumentCaptor<TourismPlace> placeCaptor = ArgumentCaptor.forClass(TourismPlace.class);
        verify(tourismPlaceRepository).save(placeCaptor.capture());
        TourismPlace savedPlace = placeCaptor.getValue();
        assertThat(savedPlace.getContentId()).isEqualTo("1");
        assertThat(savedPlace.getTitle()).isEqualTo("한라산 상세");
        assertThat(savedPlace.getOverview()).isEqualTo("제주의 대표 산");
        assertThat(savedPlace.getUseTime()).isEqualTo("09:00 - 18:00");
        assertThat(savedPlace.getFirstImageUrl()).isEqualTo("https://example.com/halla.jpg");
        assertThat(savedPlace.getLocation().getY()).isEqualTo(33.362);
        assertThat(savedPlace.getLocation().getX()).isEqualTo(126.53);
        assertThat(savedPlace.getRawData().has("areaBasedList2")).isTrue();
        assertThat(savedPlace.getRawData().has("detail")).isTrue();
    }

    @Test
    void skipsItemsWithoutCoordinates() {
        TourAreaItem item = tourAreaItem("2", "좌표 없는 장소", null, 126.529167);
        given(tourApiClient.getAreaBasedList("39", "12", 1, 100))
                .willReturn(new TourAreaPage(List.of(item), 1, 100, 1));

        var response = tourismPlaceSyncService.syncJejuTourismPlaces();

        assertThat(response.fetchedCount()).isEqualTo(1);
        assertThat(response.skippedCount()).isEqualTo(1);
        assertThat(response.createdCount()).isZero();
        verify(tourApiClient, never()).getDetail(any(), any());
        verify(tourismPlaceRepository, never()).save(any());
    }

    @Test
    void updatesExistingTourismPlace() {
        TourAreaItem item = tourAreaItem("3", "성산일출봉", 33.462147, 126.936424);
        TourismPlace existingPlace = TourismPlace.create(snapshot("3", "성산일출봉 옛 이름"));
        given(tourApiClient.getAreaBasedList("39", "12", 1, 100))
                .willReturn(new TourAreaPage(List.of(item), 1, 100, 1));
        given(tourApiClient.getDetail("3", "12")).willReturn(Optional.empty());
        given(tourismPlaceRepository.findByContentId("3")).willReturn(Optional.of(existingPlace));

        var response = tourismPlaceSyncService.syncJejuTourismPlaces();

        assertThat(response.createdCount()).isZero();
        assertThat(response.updatedCount()).isEqualTo(1);
        assertThat(existingPlace.getTitle()).isEqualTo("성산일출봉");
        verify(tourismPlaceRepository).save(existingPlace);
    }

    private static TourAreaItem tourAreaItem(String contentId, String title, Double lat, Double lng) {
        return new TourAreaItem(
                contentId,
                "12",
                title,
                "제주특별자치도 제주시",
                null,
                "39",
                "4",
                "A01",
                "A0101",
                "A01010100",
                "064-000-0000",
                lat,
                lng,
                "https://example.com/image.jpg",
                "https://example.com/thumb.jpg",
                "20240101000000",
                "20240102000000",
                Map.of("contentid", contentId, "title", title)
        );
    }

    private static TourismPlaceSnapshot snapshot(String contentId, String title) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(126.529167, 33.361667));
        point.setSRID(4326);
        return new TourismPlaceSnapshot(
                contentId,
                "12",
                title,
                "제주특별자치도 제주시",
                null,
                null,
                "A01",
                "A0101",
                "A01010100",
                "39",
                "4",
                point,
                null,
                null,
                null,
                null,
                "20240101000000",
                "20240102000000",
                new ObjectMapper().createObjectNode(),
                LocalDateTime.now()
        );
    }
}
