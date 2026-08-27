package com.runningolle.domain.chat.controller;

import com.runningolle.domain.chat.dto.ChatMessageCreateRequest;
import com.runningolle.domain.chat.dto.ChatRoomResponse;
import com.runningolle.domain.chat.dto.InquiryRoomCreateRequest;
import com.runningolle.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getRooms(Authentication authentication) {
        return ResponseEntity.ok(chatService.getRooms(UUID.fromString(authentication.getName())));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoom(Authentication authentication, @PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.getRoom(UUID.fromString(authentication.getName()), roomId));
    }

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatRoomResponse> sendMessage(
            Authentication authentication,
            @PathVariable UUID roomId,
            @Valid @RequestBody ChatMessageCreateRequest request
    ) {
        return ResponseEntity.ok(chatService.sendMessage(UUID.fromString(authentication.getName()), roomId, request));
    }

    @DeleteMapping("/{roomId}/messages/{messageId}")
    public ResponseEntity<ChatRoomResponse> deleteMessage(
            Authentication authentication,
            @PathVariable UUID roomId,
            @PathVariable UUID messageId
    ) {
        return ResponseEntity.ok(chatService.deleteMessage(UUID.fromString(authentication.getName()), roomId, messageId));
    }

    @PostMapping("/inquiry")
    public ResponseEntity<ChatRoomResponse> createInquiryRoom(
            Authentication authentication,
            @Valid @RequestBody InquiryRoomCreateRequest request
    ) {
        ChatRoomResponse response = chatService.createOrGetInquiryRoom(UUID.fromString(authentication.getName()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
