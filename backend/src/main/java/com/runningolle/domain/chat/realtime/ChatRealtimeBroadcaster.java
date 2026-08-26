package com.runningolle.domain.chat.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningolle.domain.chat.dto.ChatRoomResponse;
import com.runningolle.domain.chat.service.ChatService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRealtimeBroadcaster {

    private final ChatRealtimeSessionRegistry sessionRegistry;
    private final ChatListRealtimeSessionRegistry chatListSessionRegistry;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public void broadcastRoom(UUID roomId, UUID excludedUserId) {
        for (ChatRealtimeSessionRegistry.SessionEntry entry : sessionRegistry.getRoomSessions(roomId)) {
            if (entry.getUserId().equals(excludedUserId)) {
                continue;
            }
            sendSnapshot(entry.getUserId(), roomId, entry.getSession());
        }
    }

    public void broadcastChatLists(List<UUID> userIds) {
        throw new UnsupportedOperationException("Use room-scoped chat list updates instead.");
    }

    public void broadcastChatList(UUID userId) {
        for (WebSocketSession session : chatListSessionRegistry.getUserSessions(userId)) {
            sendListSnapshot(userId, session);
        }
    }

    public void broadcastChatListRoomUpdate(UUID roomId, List<UUID> userIds) {
        userIds.stream().distinct().forEach(userId -> broadcastChatListRoomUpdate(userId, roomId));
    }

    public void broadcastChatListRoomUpdate(UUID userId, UUID roomId) {
        for (WebSocketSession session : chatListSessionRegistry.getUserSessions(userId)) {
            sendListRoomUpdate(userId, roomId, session);
        }
    }

    public void sendSnapshot(UUID userId, UUID roomId, WebSocketSession session) {
        try {
            ChatRoomResponse response = chatService.getRoomForRealtime(userId, roomId);
            String payload = objectMapper.writeValueAsString(new ChatRealtimeEnvelope("room_snapshot", response));
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException exception) {
            log.warn("Failed to send realtime chat snapshot for room {}", roomId, exception);
        } catch (RuntimeException exception) {
            log.warn("Failed to build realtime chat snapshot for room {}", roomId, exception);
        }
    }

    public void sendListSnapshot(UUID userId, WebSocketSession session) {
        try {
            List<ChatRoomResponse> response = chatService.getRoomsForRealtime(userId);
            String payload = objectMapper.writeValueAsString(new ChatListRealtimeEnvelope("chat_list_snapshot", response));
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException exception) {
            log.warn("Failed to send realtime chat list snapshot for user {}", userId, exception);
        } catch (RuntimeException exception) {
            log.warn("Failed to build realtime chat list snapshot for user {}", userId, exception);
        }
    }

    public void sendListRoomUpdate(UUID userId, UUID roomId, WebSocketSession session) {
        try {
            ChatRoomResponse response = chatService.getRoomSummaryForRealtime(userId, roomId);
            String payload = objectMapper.writeValueAsString(new ChatListRoomUpdateEnvelope("chat_list_room_update", response));
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException exception) {
            log.warn("Failed to send realtime chat list room update for user {} room {}", userId, roomId, exception);
        } catch (RuntimeException exception) {
            log.warn("Failed to build realtime chat list room update for user {} room {}", userId, roomId, exception);
        }
    }

    private record ChatRealtimeEnvelope(String type, ChatRoomResponse room) {
    }

    private record ChatListRealtimeEnvelope(String type, List<ChatRoomResponse> rooms) {
    }

    private record ChatListRoomUpdateEnvelope(String type, ChatRoomResponse room) {
    }
}
