package com.runningolle.domain.chat.repository;

import com.runningolle.domain.chat.entity.ChatRoomParticipant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, UUID> {
    Optional<ChatRoomParticipant> findByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);
    List<ChatRoomParticipant> findByUserId(UUID userId);
    List<ChatRoomParticipant> findByChatRoomId(UUID chatRoomId);

    @Query("""
            select p.chatRoom.id as roomId, count(p) as participantCount
            from ChatRoomParticipant p
            where p.chatRoom.id in :roomIds
            group by p.chatRoom.id
            """)
    List<RoomParticipantCountProjection> countParticipantsByRoomIds(List<UUID> roomIds);

    interface RoomParticipantCountProjection {
        UUID getRoomId();
        long getParticipantCount();
    }
}
