package com.runningolle.domain.meetup.repository;

import com.runningolle.domain.meetup.entity.Meetup;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetupRepository extends JpaRepository<Meetup, UUID> {
    List<Meetup> findByIsDeletedFalseOrderByMeetupDateAscCreatedAtDesc();
    Optional<Meetup> findByIdAndIsDeletedFalse(UUID id);
    List<Meetup> findByOrganizerIdAndIsDeletedFalse(UUID organizerId);
    List<Meetup> findByMeetupDateBetweenAndIsDeletedFalse(LocalDateTime start, LocalDateTime end);
}
