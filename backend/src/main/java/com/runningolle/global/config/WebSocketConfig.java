package com.runningolle.global.config;

import com.runningolle.domain.chat.realtime.ChatRealtimeHandshakeInterceptor;
import com.runningolle.domain.chat.realtime.ChatListWebSocketHandler;
import com.runningolle.domain.chat.realtime.ChatRoomWebSocketHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatRoomWebSocketHandler chatRoomWebSocketHandler;
    private final ChatListWebSocketHandler chatListWebSocketHandler;
    private final ChatRealtimeHandshakeInterceptor chatRealtimeHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatRoomWebSocketHandler, "/ws/community/chats/{roomId}")
                .addInterceptors(chatRealtimeHandshakeInterceptor)
                .setAllowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:8080",
                        "http://127.0.0.1:8080"
                );
        registry.addHandler(chatListWebSocketHandler, "/ws/community/chat-list")
                .addInterceptors(chatRealtimeHandshakeInterceptor)
                .setAllowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:8080",
                        "http://127.0.0.1:8080"
                );
    }
}
