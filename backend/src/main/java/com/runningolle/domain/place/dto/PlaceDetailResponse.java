package com.runningolle.domain.place.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record PlaceDetailResponse(
        String kakaoPlaceId,
        String name,
        String categoryName,
        String address,
        double lat,
        double lng,
        String phone,
        String kakaoPlaceUrl,
        boolean tourApiMatched,
        String tourContentId,
        String tourContentTypeId,
        String overview,
        String firstImageUrl,
        String useTime,
        JsonNode tourDataRaw
) {
}
