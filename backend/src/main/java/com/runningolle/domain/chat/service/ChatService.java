package com.runningolle.domain.chat.service;

import com.runningolle.domain.chat.dto.ChatMessageCreateRequest;
import com.runningolle.domain.chat.dto.ChatMessageResponse;
import com.runningolle.domain.chat.dto.ChatRoomResponse;
import com.runningolle.domain.chat.dto.InquiryRoomCreateRequest;
import com.runningolle.domain.chat.entity.ChatMessage;
import com.runningolle.domain.chat.entity.ChatRoom;
import com.runningolle.domain.chat.entity.ChatRoomParticipant;
import com.runningolle.domain.chat.enums.ChatRoomType;
import com.runningolle.domain.chat.realtime.ChatRoomUpdatedEvent;
import com.runningolle.domain.chat.repository.ChatMessageRepository;
import com.runningolle.domain.chat.repository.ChatRoomParticipantRepository;
import com.runningolle.domain.chat.repository.ChatRoomRepository;
import com.runningolle.domain.meetup.entity.Meetup;
import com.runningolle.domain.meetup.repository.MeetupRepository;
import com.runningolle.domain.user.entity.User;
import com.runningolle.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MeetupRepository meetupRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(UUID userId) {
        return buildRoomSummaries(userId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRoomsForRealtime(UUID userId) {
        return buildRoomSummaries(userId);
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoomSummaryForRealtime(UUID userId, UUID roomId) {
        ChatRoomParticipant membership = getMembership(roomId, userId);
        ChatMessage latestMessage = chatMessageRepository.findLatestMessagesByRoomIds(List.of(roomId)).stream()
                .findFirst()
                .orElse(null);
        int participantCount = chatRoomParticipantRepository.countParticipantsByRoomIds(List.of(roomId)).stream()
                .findFirst()
                .map(projection -> Math.toIntExact(projection.getParticipantCount()))
                .orElse(0);
        int unreadCount = chatMessageRepository.countUnreadByUserAndRoomIds(userId, List.of(roomId)).stream()
                .findFirst()
                .map(projection -> Math.toIntExact(projection.getUnreadCount()))
                .orElse(0);
        return toRoomSummary(membership.getChatRoom(), latestMessage, participantCount, unreadCount);
    }

    @Transactional
    public ChatRoomResponse getRoom(UUID userId, UUID roomId) {
        ChatRoomParticipant membership = getMembership(roomId, userId);
        membership.markReadNow();
        return toRoomDetail(membership.getChatRoom(), userId, membership);
    }

    @Transactional
    public ChatRoomResponse sendMessage(UUID userId, UUID roomId, ChatMessageCreateRequest request) {
        ChatRoomParticipant membership = getMembership(roomId, userId);
        User sender = getUser(userId);
        chatMessageRepository.save(ChatMessage.create(membership.getChatRoom(), sender, request.content().trim()));
        membership.markReadNow();
        ChatRoomResponse response = toRoomDetail(membership.getChatRoom(), userId, membership);
        applicationEventPublisher.publishEvent(new ChatRoomUpdatedEvent(roomId, getParticipantUserIds(roomId), true, userId));
        return response;
    }

    @Transactional
    public ChatRoomResponse deleteMessage(UUID userId, UUID roomId, UUID messageId) {
        ChatRoomParticipant membership = getMembership(roomId, userId);
        ChatMessage message = chatMessageRepository.findByIdAndChatRoomIdAndIsDeletedFalse(messageId, roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat message not found."));

        if (!message.getSender().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the sender can delete this message.");
        }

        message.delete();
        membership.markReadNow();

        ChatRoomResponse response = toRoomDetail(membership.getChatRoom(), userId, membership);
        applicationEventPublisher.publishEvent(new ChatRoomUpdatedEvent(roomId, getParticipantUserIds(roomId), true, userId));
        return response;
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoomForRealtime(UUID userId, UUID roomId) {
        ChatRoomParticipant membership = getMembership(roomId, userId);
        return toRoomDetail(membership.getChatRoom(), userId, membership);
    }

    @Transactional(readOnly = true)
    public boolean canAccessRoom(UUID roomId, UUID userId) {
        return chatRoomParticipantRepository.findByChatRoomIdAndUserId(roomId, userId).isPresent();
    }

    @Transactional
    public ChatRoomResponse createOrGetInquiryRoom(UUID userId, InquiryRoomCreateRequest request) {
        User inquirer = getUser(userId);
        Meetup meetup = meetupRepository.findByIdAndIsDeletedFalse(request.meetupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meetup not found."));
        User organizer = meetup.getOrganizer();

        ChatRoom room = chatRoomRepository
                .findByMeetupIdAndOrganizerIdAndInquirerIdAndRoomType(
                        meetup.getId(),
                        organizer.getId(),
                        inquirer.getId(),
                        ChatRoomType.DIRECT_INQUIRY
                )
                .orElseGet(() -> {
                    ChatRoom created = chatRoomRepository.save(ChatRoom.createDirectInquiry(meetup, organizer, inquirer));
                    chatRoomParticipantRepository.save(ChatRoomParticipant.create(created, organizer));
                    chatRoomParticipantRepository.save(ChatRoomParticipant.create(created, inquirer));
                    return created;
        });

        ChatRoomParticipant membership = getMembership(room.getId(), userId);
        applicationEventPublisher.publishEvent(
                new ChatRoomUpdatedEvent(room.getId(), getParticipantUserIds(room.getId()), true, userId)
        );
        return toRoomDetail(room, userId, membership);
    }

    private List<ChatRoomResponse> buildRoomSummaries(UUID userId) {
        List<ChatRoomParticipant> memberships = chatRoomParticipantRepository.findByUserId(userId);
        if (memberships.isEmpty()) {
            return List.of();
        }

        List<UUID> roomIds = memberships.stream()
                .map(participant -> participant.getChatRoom().getId())
                .toList();
        List<ChatRoom> rooms = chatRoomRepository.findByIdIn(
                roomIds
        );
        Map<UUID, ChatRoomParticipant> membershipByRoomId = memberships.stream()
                .collect(Collectors.toMap(participant -> participant.getChatRoom().getId(), Function.identity()));
        Map<UUID, Integer> participantCountByRoomId = chatRoomParticipantRepository.countParticipantsByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(
                        ChatRoomParticipantRepository.RoomParticipantCountProjection::getRoomId,
                        projection -> Math.toIntExact(projection.getParticipantCount())
                ));
        Map<UUID, ChatMessage> latestMessageByRoomId = chatMessageRepository.findLatestMessagesByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(message -> message.getChatRoom().getId(), Function.identity()));
        Map<UUID, Integer> unreadCountByRoomId = chatMessageRepository.countUnreadByUserAndRoomIds(userId, roomIds).stream()
                .collect(Collectors.toMap(
                        ChatMessageRepository.RoomUnreadCountProjection::getRoomId,
                        projection -> Math.toIntExact(projection.getUnreadCount())
                ));

        return rooms.stream()
                .sorted(Comparator.comparing(
                        room -> {
                            ChatMessage latestMessage = latestMessageByRoomId.get(room.getId());
                            return latestMessage == null ? room.getCreatedAt() : latestMessage.getCreatedAt();
                        },
                        Comparator.reverseOrder()
                ))
                .map(room -> toRoomSummary(
                        room,
                        latestMessageByRoomId.get(room.getId()),
                        participantCountByRoomId.getOrDefault(room.getId(), 0),
                        unreadCountByRoomId.getOrDefault(room.getId(), 0)
                ))
                .toList();
    }

    private ChatRoomResponse toRoomSummary(ChatRoom room, ChatMessage lastMessage, int participantCount, int unreadCount) {
        return new ChatRoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getMeetup() == null ? null : room.getMeetup().getId(),
                buildTitle(room),
                buildSubtitle(room, participantCount),
                buildIconLabel(room),
                unreadCount,
                lastMessage == null ? "" : lastMessage.getContent(),
                lastMessage == null ? room.getCreatedAt() : lastMessage.getCreatedAt(),
                participantCount,
                List.of()
        );
    }

    private ChatRoomResponse toRoomDetail(ChatRoom room, UUID userId, ChatRoomParticipant membership) {
        List<ChatRoomParticipant> participants = chatRoomParticipantRepository.findByChatRoomId(room.getId());
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedFalseOrderByCreatedAtAsc(room.getId());
        ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

        return new ChatRoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getMeetup() == null ? null : room.getMeetup().getId(),
                buildTitle(room),
                buildSubtitle(room, participants.size()),
                buildIconLabel(room),
                calculateUnreadCount(room.getId(), membership, userId, messages),
                lastMessage == null ? "" : lastMessage.getContent(),
                lastMessage == null ? room.getCreatedAt() : lastMessage.getCreatedAt(),
                participants.size(),
                messages.stream().map(message -> new ChatMessageResponse(
                        message.getId(),
                        message.getSender().getId(),
                        message.getSender().getNickname(),
                        message.getSender().getProfileImageUrl(),
                        message.getContent(),
                        message.getCreatedAt(),
                        message.getSender().getId().equals(userId),
                        Boolean.TRUE.equals(message.getIsSystem())
                )).toList()
        );
    }

    private int calculateUnreadCount(UUID roomId, ChatRoomParticipant membership, UUID userId, List<ChatMessage> messages) {
        LocalDateTime lastReadAt = membership.getLastReadAt();
        return (int) messages.stream()
                .filter(message -> !message.getSender().getId().equals(userId))
                .filter(message -> lastReadAt == null || message.getCreatedAt().isAfter(lastReadAt))
                .count();
    }

    private String buildTitle(ChatRoom room) {
        if (room.getRoomType() == ChatRoomType.MEETUP_GROUP) {
            return room.getMeetup().getTitle();
        }
        return room.getOrganizer().getNickname() + " (Host)";
    }

    private String buildSubtitle(ChatRoom room, int participantCount) {
        if (room.getRoomType() == ChatRoomType.MEETUP_GROUP) {
            return "Meetup chat · " + participantCount + " participants";
        }
        return room.getMeetup().getTitle() + " inquiry";
    }

    private String buildIconLabel(ChatRoom room) {
        return room.getOrganizer().getNickname().substring(0, 1).toUpperCase();
    }

    private List<UUID> getParticipantUserIds(UUID roomId) {
        return chatRoomParticipantRepository.findByChatRoomId(roomId).stream()
                .map(participant -> participant.getUser().getId())
                .toList();
    }

    private ChatRoomParticipant getMembership(UUID roomId, UUID userId) {
        return chatRoomParticipantRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Chat room access denied."));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
