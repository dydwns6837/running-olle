package com.runningolle.domain.chat.entity;

import com.runningolle.domain.chat.enums.ChatRoomType;
import com.runningolle.global.entity.BaseCreatedAtEntity;
import com.runningolle.domain.meetup.entity.Meetup;
import com.runningolle.domain.user.entity.User;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_rooms_inquiry_room_scope",
                columnNames = {"meetup_id", "organizer_id", "inquirer_id", "room_type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class ChatRoom extends BaseCreatedAtEntity {

    // Partial unique index:
    // 1) room_type = MEETUP_GROUP -> meetup_id unique
    // 2) room_type = DIRECT_INQUIRY -> (meetup_id, organizer_id, inquirer_id) unique
    // This is handled in migration SQL, not @Table(uniqueConstraints).

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_id")
    private Meetup meetup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquirer_id")
    private User inquirer;

    public static ChatRoom createMeetupGroup(Meetup meetup) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomType = ChatRoomType.MEETUP_GROUP;
        chatRoom.meetup = meetup;
        chatRoom.organizer = meetup.getOrganizer();
        return chatRoom;
    }

    public static ChatRoom createDirectInquiry(Meetup meetup, User organizer, User inquirer) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomType = ChatRoomType.DIRECT_INQUIRY;
        chatRoom.meetup = meetup;
        chatRoom.organizer = organizer;
        chatRoom.inquirer = inquirer;
        return chatRoom;
    }
}
