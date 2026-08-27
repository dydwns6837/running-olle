package com.runningolle.domain.chat.dto;

import com.runningolle.domain.chat.enums.ChatRoomType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatRoomResponse(
        UUID id,
        ChatRoomType roomType,
        UUID meetupId,
        String title,
        String subtitle,
        String iconLabel,
        int unreadCount,
        String lastMessage,
        LocalDateTime lastMessageAt,
        int participantCount,
        List<ChatMessageResponse> messages
) {
}
