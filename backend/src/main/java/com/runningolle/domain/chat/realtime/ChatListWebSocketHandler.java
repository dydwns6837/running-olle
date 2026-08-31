package com.runningolle.domain.chat.realtime;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatListWebSocketHandler extends TextWebSocketHandler {

    private final ChatListRealtimeSessionRegistry sessionRegistry;
    private final ChatRealtimeBroadcaster chatRealtimeBroadcaster;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = extractUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User authentication required."));
            return;
        }

        sessionRegistry.register(userId, session);
        chatRealtimeBroadcaster.sendListSnapshot(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client does not send websocket messages for list updates.
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessionRegistry.unregister(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.unregister(session);
    }

    private UUID extractUserId(WebSocketSession session) {
        Object rawUserId = session.getAttributes().get("userId");
        if (!(rawUserId instanceof String userId)) {
            return null;
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
