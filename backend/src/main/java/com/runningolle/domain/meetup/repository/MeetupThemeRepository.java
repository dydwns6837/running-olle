package com.runningolle.domain.meetup.repository;

import com.runningolle.domain.meetup.entity.MeetupTheme;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetupThemeRepository extends JpaRepository<MeetupTheme, UUID> {
    List<MeetupTheme> findByMeetupId(UUID meetupId);
    void deleteByMeetupId(UUID meetupId);
}
