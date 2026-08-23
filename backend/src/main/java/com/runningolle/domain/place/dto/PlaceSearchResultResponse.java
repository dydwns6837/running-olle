package com.runningolle.domain.place.dto;

import com.runningolle.domain.place.client.KakaoPlaceClient.KakaoPlace;

public record PlaceSearchResultResponse(
        String kakaoPlaceId,
        String name,
        String categoryGroupCode,
        String categoryName,
        String address,
        double lat,
        double lng,
        boolean isTourismCandidate
) {

    public static PlaceSearchResultResponse from(KakaoPlace place, boolean isTourismCandidate) {
        return new PlaceSearchResultResponse(
                place.kakaoPlaceId(),
                place.name(),
                place.categoryGroupCode(),
                place.categoryName(),
                firstNonBlank(place.roadAddress(), place.address()),
                place.lat(),
                place.lng(),
                isTourismCandidate
        );
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}
