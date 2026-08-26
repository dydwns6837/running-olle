package com.runningolle.domain.chat.realtime;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class ChatRealtimeSessionRegistry {

    private final Map<UUID, Map<String, SessionEntry>> sessionsByRoom = new ConcurrentHashMap<>();
    private final Map<String, UUID> roomIdBySessionId = new ConcurrentHashMap<>();

    public void register(UUID roomId, UUID userId, WebSocketSession session) {
        sessionsByRoom.computeIfAbsent(roomId, ignored -> new ConcurrentHashMap<>())
                .put(session.getId(), new SessionEntry(session, userId));
        roomIdBySessionId.put(session.getId(), roomId);
    }

    public void unregister(WebSocketSession session) {
        UUID roomId = roomIdBySessionId.remove(session.getId());
        if (roomId == null) {
            return;
        }

        Map<String, SessionEntry> roomSessions = sessionsByRoom.get(roomId);
        if (roomSessions == null) {
            return;
        }

        roomSessions.remove(session.getId());
        if (roomSessions.isEmpty()) {
            sessionsByRoom.remove(roomId);
        }
    }

    public Collection<SessionEntry> getRoomSessions(UUID roomId) {
        return sessionsByRoom.getOrDefault(roomId, Map.of()).values();
    }

    @Getter
    public static class SessionEntry {
        private final WebSocketSession session;
        private final UUID userId;

        public SessionEntry(WebSocketSession session, UUID userId) {
            this.session = session;
            this.userId = userId;
        }
    }
}
