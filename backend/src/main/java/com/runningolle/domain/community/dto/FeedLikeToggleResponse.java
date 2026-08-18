package com.runningolle.domain.community.dto;

public record FeedLikeToggleResponse(
        boolean liked,
        long likeCount
) {
}
