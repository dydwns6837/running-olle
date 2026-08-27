package com.runningolle.domain.chat.repository;

import com.runningolle.domain.chat.entity.ChatRoom;
import com.runningolle.domain.chat.enums.ChatRoomType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findByMeetupIdAndRoomType(UUID meetupId, ChatRoomType roomType);
    Optional<ChatRoom> findByMeetupIdAndOrganizerIdAndInquirerIdAndRoomType(
            UUID meetupId,
            UUID organizerId,
            UUID inquirerId,
            ChatRoomType roomType
    );
    @EntityGraph(attributePaths = {"meetup", "organizer"})
    List<ChatRoom> findByIdIn(List<UUID> ids);
}
