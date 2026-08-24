package com.runningolle.domain.chat.repository;

import com.runningolle.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    java.util.Optional<ChatMessage> findByIdAndChatRoomIdAndIsDeletedFalse(UUID id, UUID chatRoomId);
    List<ChatMessage> findByChatRoomIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID chatRoomId);
    long countByChatRoomIdAndIsDeletedFalseAndCreatedAtAfter(UUID chatRoomId, LocalDateTime createdAt);

    @Query("""
            select m
            from ChatMessage m
            where m.isDeleted = false
              and m.chatRoom.id in :roomIds
              and m.createdAt = (
                  select max(m2.createdAt)
                  from ChatMessage m2
                  where m2.chatRoom.id = m.chatRoom.id
                    and m2.isDeleted = false
              )
            """)
    List<ChatMessage> findLatestMessagesByRoomIds(List<UUID> roomIds);

    @Query("""
            select m.chatRoom.id as roomId, count(m) as unreadCount
            from ChatMessage m
            join ChatRoomParticipant p on p.chatRoom.id = m.chatRoom.id
            where p.user.id = :userId
              and m.chatRoom.id in :roomIds
              and m.isDeleted = false
              and m.sender.id <> :userId
              and (p.lastReadAt is null or m.createdAt > p.lastReadAt)
            group by m.chatRoom.id
            """)
    List<RoomUnreadCountProjection> countUnreadByUserAndRoomIds(UUID userId, List<UUID> roomIds);

    interface RoomUnreadCountProjection {
        UUID getRoomId();
        long getUnreadCount();
    }
}
