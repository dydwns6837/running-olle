package com.runningolle.domain.chat.realtime;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class ChatListRealtimeSessionRegistry {

    private final Map<UUID, Map<String, WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();
    private final Map<String, UUID> userIdBySessionId = new ConcurrentHashMap<>();

    public void register(UUID userId, WebSocketSession session) {
        sessionsByUser.computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>()).put(session.getId(), session);
        userIdBySessionId.put(session.getId(), userId);
    }

    public void unregister(WebSocketSession session) {
        UUID userId = userIdBySessionId.remove(session.getId());
        if (userId == null) {
            return;
        }

        Map<String, WebSocketSession> userSessions = sessionsByUser.get(userId);
        if (userSessions == null) {
            return;
        }

        userSessions.remove(session.getId());
        if (userSessions.isEmpty()) {
            sessionsByUser.remove(userId);
        }
    }

    public Collection<WebSocketSession> getUserSessions(UUID userId) {
        return sessionsByUser.getOrDefault(userId, Map.of()).values();
    }
}
