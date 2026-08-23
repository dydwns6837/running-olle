package com.runningolle.domain.place.dto;

import com.runningolle.domain.place.client.KakaoPlaceClient.KakaoPlace;
import com.runningolle.domain.tourism.entity.TourismPlace;

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

    public static PlaceSearchResultResponse fromOfficialTourism(TourismPlace place) {
        return new PlaceSearchResultResponse(
                "tourapi:" + place.getContentId(),
                place.getTitle(),
                "AT4",
                officialTourismCategoryName(place.getContentTypeId()),
                address(place),
                place.getLocation().getY(),
                place.getLocation().getX(),
                true
        );
    }

    private static String officialTourismCategoryName(String contentTypeId) {
        return switch (contentTypeId) {
            case "14" -> "문화시설";
            case "28" -> "레포츠";
            default -> "관광지";
        };
    }

    private static String address(TourismPlace place) {
        if (place.getAddress() == null || place.getAddress().isBlank()) {
            return place.getDetailAddress();
        }
        if (place.getDetailAddress() == null || place.getDetailAddress().isBlank()) {
            return place.getAddress();
        }
        return place.getAddress() + " " + place.getDetailAddress();
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}
