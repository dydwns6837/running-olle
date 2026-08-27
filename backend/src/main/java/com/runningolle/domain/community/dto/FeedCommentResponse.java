package com.runningolle.domain.community.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedCommentResponse(
        UUID id,
        UUID userId,
        String nickname,
        String content,
        LocalDateTime createdAt,
        boolean mine
) {
}
