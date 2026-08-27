package com.runningolle.domain.meetup.dto;

import com.runningolle.domain.meetup.enums.JoinMethod;
import com.runningolle.domain.meetup.enums.MeetupStatus;
import com.runningolle.domain.meetup.enums.ParticipantStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MeetupResponse(
        UUID id,
        String title,
        String description,
        LocalDateTime createdAt,
        UUID organizerId,
        String organizerName,
        String organizerProfileImageUrl,
        String themeCode,
        String themeLabel,
        LocalDateTime meetupDate,
        String meetingPlace,
        BigDecimal meetingLatitude,
        BigDecimal meetingLongitude,
        Integer maxParticipants,
        BigDecimal targetPace,
        JoinMethod joinMethod,
        MeetupStatus status,
        MeetupCourseSummaryResponse course,
        List<UUID> participantIds,
        List<MeetupParticipantResponse> participants,
        ParticipantStatus myParticipation,
        boolean organizer
) {
}
