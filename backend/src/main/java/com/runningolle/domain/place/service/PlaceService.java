package com.runningolle.domain.place.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.runningolle.domain.place.client.KakaoPlaceClient;
import com.runningolle.domain.place.client.KakaoPlaceClient.KakaoPlace;
import com.runningolle.domain.place.dto.PlaceDetailResponse;
import com.runningolle.domain.place.dto.PlaceSearchResultResponse;
import com.runningolle.domain.tourism.entity.TourismPlace;
import com.runningolle.domain.tourism.repository.TourismPlaceRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final String TOURISM_CATEGORY_GROUP_CODE = "AT4";
    private static final int DEFAULT_SEARCH_RADIUS_METERS = 5_000;
    private static final int MAX_SEARCH_RADIUS_METERS = 20_000;
    private static final int DETAIL_SEARCH_RADIUS_METERS = 1_000;
    private static final double TOURISM_MATCH_RADIUS_METERS = 500.0;
    private static final int TOURISM_MATCH_LIMIT = 20;
    private static final double MIN_TOURISM_NAME_SCORE = 0.55;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final KakaoPlaceClient kakaoPlaceClient;
    private final TourismPlaceRepository tourismPlaceRepository;

    public List<PlaceSearchResultResponse> searchPlaces(String keyword, double lat, double lng, Integer radiusMeters) {
        validateKeyword(keyword);
        validateCoordinate(lat, lng);
        int radius = normalizeRadius(radiusMeters);

        return kakaoPlaceClient.searchKeyword(keyword, lat, lng, radius).stream()
                .map(place -> PlaceSearchResultResponse.from(place, isTourismCandidate(place.categoryGroupCode())))
                .toList();
    }

    public PlaceDetailResponse getPlaceDetail(
            String kakaoPlaceId,
            String name,
            double lat,
            double lng,
            String categoryGroupCode
    ) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카카오 장소 ID가 필요합니다.");
        }
        validateKeyword(name);
        validateCoordinate(lat, lng);

        KakaoPlace kakaoPlace = resolveKakaoPlace(kakaoPlaceId, name, lat, lng, categoryGroupCode)
                .map(place -> keepRequestedKakaoPlaceId(place, kakaoPlaceId))
                .orElseGet(() -> fallbackKakaoPlace(kakaoPlaceId, name, lat, lng, categoryGroupCode));
        String resolvedCategoryGroupCode = firstNonBlank(kakaoPlace.categoryGroupCode(), categoryGroupCode);

        if (isTourismCandidate(resolvedCategoryGroupCode)) {
            Optional<TourismPlace> tourismPlace = findBestTourismMatch(kakaoPlace.name(), kakaoPlace.lat(), kakaoPlace.lng());
            if (tourismPlace.isPresent()) {
                return matchedDetail(kakaoPlace, tourismPlace.get());
            }
        }

        return kakaoOnlyDetail(kakaoPlace);
    }

    private Optional<KakaoPlace> resolveKakaoPlace(
            String kakaoPlaceId,
            String name,
            double lat,
            double lng,
            String categoryGroupCode
    ) {
        List<KakaoPlace> candidates = kakaoPlaceClient.searchKeyword(
                name,
                lat,
                lng,
                DETAIL_SEARCH_RADIUS_METERS,
                categoryGroupCode
        );
        Optional<KakaoPlace> exactPlace = findByKakaoPlaceId(candidates, kakaoPlaceId);
        if (exactPlace.isPresent()) {
            return exactPlace;
        }

        if (StringUtils.hasText(categoryGroupCode)) {
            candidates = kakaoPlaceClient.searchKeyword(name, lat, lng, DETAIL_SEARCH_RADIUS_METERS);
            exactPlace = findByKakaoPlaceId(candidates, kakaoPlaceId);
            if (exactPlace.isPresent()) {
                return exactPlace;
            }
        }

        return candidates.stream()
                .filter(place -> normalizedNameScore(place.name(), name) >= MIN_TOURISM_NAME_SCORE)
                .min(Comparator.comparingInt(place -> place.distanceMeters() == null
                        ? Integer.MAX_VALUE
                        : place.distanceMeters()));
    }

    private Optional<TourismPlace> findBestTourismMatch(String kakaoPlaceName, double lat, double lng) {
        // TODO: Improve TourAPI-Kakao name matching with aliases/tokenization if false positives appear.
        return tourismPlaceRepository.findNearbyOfficialTourismPlaces(
                        lat,
                        lng,
                        TOURISM_MATCH_RADIUS_METERS,
                        TOURISM_MATCH_LIMIT
                ).stream()
                .map(place -> new TourismMatch(
                        place,
                        normalizedNameScore(place.getTitle(), kakaoPlaceName),
                        distanceMeters(lat, lng, place.getLocation())
                ))
                .filter(match -> match.nameScore() >= MIN_TOURISM_NAME_SCORE)
                .sorted(Comparator.comparingDouble(TourismMatch::nameScore)
                        .reversed()
                        .thenComparingDouble(TourismMatch::distanceMeters))
                .map(TourismMatch::tourismPlace)
                .findFirst();
    }

    private static Optional<KakaoPlace> findByKakaoPlaceId(List<KakaoPlace> places, String kakaoPlaceId) {
        return places.stream()
                .filter(place -> kakaoPlaceId.equals(place.kakaoPlaceId()))
                .findFirst();
    }

    private static PlaceDetailResponse matchedDetail(KakaoPlace kakaoPlace, TourismPlace tourismPlace) {
        return new PlaceDetailResponse(
                kakaoPlace.kakaoPlaceId(),
                kakaoPlace.name(),
                kakaoPlace.categoryName(),
                address(kakaoPlace),
                kakaoPlace.lat(),
                kakaoPlace.lng(),
                kakaoPlace.phone(),
                true,
                tourismPlace.getContentId(),
                tourismPlace.getContentTypeId(),
                tourismPlace.getOverview(),
                tourismPlace.getFirstImageUrl(),
                tourismPlace.getUseTime(),
                nullIfMissing(tourismPlace.getRawData())
        );
    }

    private static PlaceDetailResponse kakaoOnlyDetail(KakaoPlace kakaoPlace) {
        return new PlaceDetailResponse(
                kakaoPlace.kakaoPlaceId(),
                kakaoPlace.name(),
                kakaoPlace.categoryName(),
                address(kakaoPlace),
                kakaoPlace.lat(),
                kakaoPlace.lng(),
                kakaoPlace.phone(),
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static KakaoPlace fallbackKakaoPlace(
            String kakaoPlaceId,
            String name,
            double lat,
            double lng,
            String categoryGroupCode
    ) {
        return new KakaoPlace(
                kakaoPlaceId,
                name,
                categoryGroupCode,
                null,
                null,
                null,
                null,
                null,
                lat,
                lng,
                null,
                null
        );
    }

    private static KakaoPlace keepRequestedKakaoPlaceId(KakaoPlace place, String kakaoPlaceId) {
        if (kakaoPlaceId.equals(place.kakaoPlaceId())) {
            return place;
        }
        return new KakaoPlace(
                kakaoPlaceId,
                place.name(),
                place.categoryGroupCode(),
                place.categoryGroupName(),
                place.categoryName(),
                place.address(),
                place.roadAddress(),
                place.phone(),
                place.lat(),
                place.lng(),
                place.placeUrl(),
                place.distanceMeters()
        );
    }

    private static boolean isTourismCandidate(String categoryGroupCode) {
        return TOURISM_CATEGORY_GROUP_CODE.equals(categoryGroupCode);
    }

    private static String address(KakaoPlace kakaoPlace) {
        return firstNonBlank(kakaoPlace.roadAddress(), kakaoPlace.address());
    }

    private static String firstNonBlank(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : fallback;
    }

    private static JsonNode nullIfMissing(JsonNode jsonNode) {
        return jsonNode == null || jsonNode.isMissingNode() || jsonNode.isNull() ? null : jsonNode;
    }

    private static void validateKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요.");
        }
    }

    private static void validateCoordinate(double lat, double lng) {
        if (Double.isNaN(lat) || Double.isNaN(lng) || lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바른 좌표가 필요합니다.");
        }
    }

    private static int normalizeRadius(Integer radiusMeters) {
        if (radiusMeters == null) {
            return DEFAULT_SEARCH_RADIUS_METERS;
        }
        if (radiusMeters <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색 반경은 1m 이상이어야 합니다.");
        }
        return Math.min(radiusMeters, MAX_SEARCH_RADIUS_METERS);
    }

    private static double normalizedNameScore(String left, String right) {
        String normalizedLeft = normalizeName(left);
        String normalizedRight = normalizeName(right);
        if (!StringUtils.hasText(normalizedLeft) || !StringUtils.hasText(normalizedRight)) {
            return 0.0;
        }
        if (normalizedLeft.equals(normalizedRight)) {
            return 1.0;
        }
        if (normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) {
            return 0.85;
        }

        long commonCharacterCount = normalizedLeft.chars()
                .filter(character -> normalizedRight.indexOf(character) >= 0)
                .count();
        return commonCharacterCount / (double) Math.max(normalizedLeft.length(), normalizedRight.length());
    }

    private static String normalizeName(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("[^0-9A-Za-z가-힣]", "").toLowerCase();
    }

    private static double distanceMeters(double lat, double lng, Point point) {
        double targetLat = point.getY();
        double targetLng = point.getX();
        double latRadians = Math.toRadians(targetLat - lat);
        double lngRadians = Math.toRadians(targetLng - lng);
        double haversine = Math.sin(latRadians / 2) * Math.sin(latRadians / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(targetLat))
                * Math.sin(lngRadians / 2) * Math.sin(lngRadians / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private record TourismMatch(
            TourismPlace tourismPlace,
            double nameScore,
            double distanceMeters
    ) {
    }
}
