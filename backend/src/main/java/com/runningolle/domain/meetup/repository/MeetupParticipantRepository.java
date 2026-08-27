package com.runningolle.domain.meetup.repository;

import com.runningolle.domain.meetup.entity.MeetupParticipant;
import com.runningolle.domain.meetup.enums.ParticipantStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeetupParticipantRepository extends JpaRepository<MeetupParticipant, UUID> {
    List<MeetupParticipant> findByMeetupId(UUID meetupId);
    List<MeetupParticipant> findByMeetupIdAndStatus(UUID meetupId, ParticipantStatus status);
    Optional<MeetupParticipant> findByMeetupIdAndUserId(UUID meetupId, UUID userId);
    long countByMeetupIdAndStatus(UUID meetupId, ParticipantStatus status);

    @Query("""
            select p.user.id as userId, count(p) as meetupCount
            from MeetupParticipant p
            where p.user.id in :userIds
              and p.status = :status
            group by p.user.id
            """)
    List<UserMeetupCountProjection> countByUserIdsAndStatus(List<UUID> userIds, ParticipantStatus status);

    interface UserMeetupCountProjection {
        UUID getUserId();
        long getMeetupCount();
    }
}
