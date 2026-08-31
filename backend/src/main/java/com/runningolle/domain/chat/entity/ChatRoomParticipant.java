package com.runningolle.domain.chat.entity;

import com.runningolle.domain.user.entity.User;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "chat_room_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_room_participants_chat_room_id_user_id",
                columnNames = {"chat_room_id", "user_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Access(AccessType.FIELD)
public class ChatRoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public static ChatRoomParticipant create(ChatRoom chatRoom, User user) {
        ChatRoomParticipant participant = new ChatRoomParticipant();
        participant.chatRoom = chatRoom;
        participant.user = user;
        participant.joinedAt = LocalDateTime.now();
        participant.lastReadAt = null;
        return participant;
    }

    public void markReadNow() {
        this.lastReadAt = LocalDateTime.now();
    }
}
