package com.runningolle.domain.chat.realtime;

import java.util.List;
import java.util.UUID;

public record ChatRoomUpdatedEvent(
        UUID roomId,
        List<UUID> affectedUserIds,
        boolean broadcastRoom,
        UUID actorUserId
) {
}
