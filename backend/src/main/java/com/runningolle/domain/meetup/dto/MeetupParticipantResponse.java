package com.runningolle.domain.meetup.dto;

import com.runningolle.domain.meetup.enums.ParticipantStatus;
import java.util.UUID;

public record MeetupParticipantResponse(
        UUID id,
        String nickname,
        String profileImageUrl,
        ParticipantStatus status,
        MeetupParticipantStatsResponse stats
) {
}
