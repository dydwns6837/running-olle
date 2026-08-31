package com.runningolle.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        UUID senderId,
        String senderName,
        String senderProfileImageUrl,
        String content,
        LocalDateTime createdAt,
        boolean mine,
        boolean system
) {
}
