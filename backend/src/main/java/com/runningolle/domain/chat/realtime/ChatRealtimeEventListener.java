package com.runningolle.domain.chat.realtime;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatRealtimeEventListener {

    private final ChatRealtimeBroadcaster chatRealtimeBroadcaster;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatRoomUpdatedEvent event) {
        if (event.broadcastRoom()) {
            chatRealtimeBroadcaster.broadcastRoom(event.roomId(), event.actorUserId());
        }
        List<UUID> targetUserIds = event.affectedUserIds().stream()
                .filter(userId -> !userId.equals(event.actorUserId()))
                .toList();
        if (!targetUserIds.isEmpty()) {
            chatRealtimeBroadcaster.broadcastChatListRoomUpdate(event.roomId(), targetUserIds);
        }
    }
}
