package com.runningolle.domain.place.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.runningolle.global.client.ExternalApiRestClientSupport;
import com.runningolle.global.config.properties.ExternalApiProperties;
import com.runningolle.global.exception.ExternalApiException;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoPlaceClient {

    private static final String PROVIDER = "Kakao Local";
    private static final String BASE_URL = "https://dapi.kakao.com";
    private static final int KEYWORD_SEARCH_SIZE = 15;
    private static final double JEJU_MIN_LAT = 33.0;
    private static final double JEJU_MAX_LAT = 34.0;
    private static final double JEJU_MIN_LNG = 126.0;
    private static final double JEJU_MAX_LNG = 127.1;

    private final ExternalApiProperties properties;
    private final RestClient restClient;

    public KakaoPlaceClient(ExternalApiProperties properties) {
        this.properties = properties;
        this.restClient = ExternalApiRestClientSupport.restClient(BASE_URL);
    }

    public List<KakaoPlace> searchKeyword(String keyword, double lat, double lng, int radiusMeters) {
        return searchKeyword(keyword, lat, lng, radiusMeters, null);
    }

    public List<KakaoPlace> searchKeyword(
            String keyword,
            double lat,
            double lng,
            int radiusMeters,
            String categoryGroupCode
    ) {
        return requestKeywordSearch(keyword, lat, lng, radiusMeters, categoryGroupCode);
    }

    public List<KakaoPlace> searchKeywordInJeju(String keyword) {
        List<KakaoPlace> directResults = jejuPlaces(requestKeywordSearch(keyword, null, null, null, null));
        if (!directResults.isEmpty()) {
            return directResults;
        }

        return jejuPlaces(requestKeywordSearch(jejuScopedKeyword(keyword), null, null, null, null));
    }

    private static List<KakaoPlace> jejuPlaces(List<KakaoPlace> places) {
        return places.stream()
                .filter(KakaoPlaceClient::isJejuPlace)
                .toList();
    }

    private List<KakaoPlace> requestKeywordSearch(
            String keyword,
            Double lat,
            Double lng,
            Integer radiusMeters,
            String categoryGroupCode
    ) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        validateApiKey();

        try {
            KakaoKeywordSearchResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/v2/local/search/keyword.json")
                                .queryParam("query", keyword.trim())
                                .queryParam("size", KEYWORD_SEARCH_SIZE);

                        if (lat != null && lng != null && radiusMeters != null) {
                            builder.queryParam("x", lng)
                                    .queryParam("y", lat)
                                    .queryParam("radius", radiusMeters)
                                    .queryParam("sort", "distance");
                        } else {
                            builder.queryParam("sort", "accuracy");
                        }

                        if (StringUtils.hasText(categoryGroupCode)) {
                            builder.queryParam("category_group_code", categoryGroupCode);
                        }

                        return builder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.getKakaoMapApiKey())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ExternalApiRestClientSupport.errorHandler(PROVIDER))
                    .body(KakaoKeywordSearchResponse.class);

            if (response == null || response.documents() == null) {
                return List.of();
            }

            return response.documents().stream()
                    .map(KakaoPlaceDocument::toKakaoPlace)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (ExternalApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ExternalApiException(PROVIDER, "카카오 장소 검색 호출에 실패했습니다.", exception);
        }
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(properties.getKakaoMapApiKey())) {
            throw new ExternalApiException(PROVIDER, "카카오 REST API 키가 설정되지 않았습니다.");
        }
    }

    private static String jejuScopedKeyword(String keyword) {
        String trimmedKeyword = keyword == null ? "" : keyword.trim();
        return trimmedKeyword.contains("제주") ? trimmedKeyword : "제주 " + trimmedKeyword;
    }

    private static boolean isJejuPlace(KakaoPlace place) {
        if (place.lat() != null
                && place.lng() != null
                && place.lat() >= JEJU_MIN_LAT
                && place.lat() <= JEJU_MAX_LAT
                && place.lng() >= JEJU_MIN_LNG
                && place.lng() <= JEJU_MAX_LNG) {
            return true;
        }
        return containsJeju(place.address()) || containsJeju(place.roadAddress());
    }

    private static boolean containsJeju(String value) {
        return StringUtils.hasText(value) && value.contains("제주");
    }

    public record KakaoPlace(
            String kakaoPlaceId,
            String name,
            String categoryGroupCode,
            String categoryGroupName,
            String categoryName,
            String address,
            String roadAddress,
            String phone,
            Double lat,
            Double lng,
            String placeUrl,
            Integer distanceMeters
    ) {
    }

    private record KakaoKeywordSearchResponse(
            List<KakaoPlaceDocument> documents
    ) {
    }

    private record KakaoPlaceDocument(
            String id,
            @JsonProperty("place_name")
            String placeName,
            @JsonProperty("category_group_code")
            String categoryGroupCode,
            @JsonProperty("category_group_name")
            String categoryGroupName,
            @JsonProperty("category_name")
            String categoryName,
            @JsonProperty("address_name")
            String addressName,
            @JsonProperty("road_address_name")
            String roadAddressName,
            String phone,
            String x,
            String y,
            @JsonProperty("place_url")
            String placeUrl,
            String distance
    ) {

        private KakaoPlace toKakaoPlace() {
            Double lat = parseDouble(y);
            Double lng = parseDouble(x);
            if (lat == null || lng == null) {
                return null;
            }

            return new KakaoPlace(
                    id,
                    placeName,
                    categoryGroupCode,
                    categoryGroupName,
                    categoryName,
                    addressName,
                    roadAddressName,
                    phone,
                    lat,
                    lng,
                    placeUrl,
                    parseInteger(distance)
            );
        }
    }

    private static Double parseDouble(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
