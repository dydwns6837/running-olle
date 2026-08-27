package com.runningolle.domain.chat.realtime;

import com.runningolle.domain.chat.service.ChatService;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatRoomWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ChatRealtimeSessionRegistry sessionRegistry;
    private final ChatRealtimeBroadcaster chatRealtimeBroadcaster;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = extractUserId(session);
        UUID roomId = extractRoomId(session);

        if (userId == null || roomId == null || !chatService.canAccessRoom(roomId, userId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Chat room access denied."));
            return;
        }

        sessionRegistry.register(roomId, userId, session);
        chatRealtimeBroadcaster.sendSnapshot(userId, roomId, session);
        chatRealtimeBroadcaster.broadcastChatList(userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Incoming chat messages continue to use the existing REST API.
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

    private UUID extractRoomId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }

        String path = uri.getPath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == path.length() - 1) {
            return null;
        }

        try {
            return UUID.fromString(path.substring(lastSlash + 1));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
