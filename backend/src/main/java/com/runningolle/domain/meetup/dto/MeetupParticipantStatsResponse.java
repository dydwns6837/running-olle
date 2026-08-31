package com.runningolle.domain.meetup.dto;

import java.math.BigDecimal;

public record MeetupParticipantStatsResponse(
        BigDecimal totalDistanceKm,
        Integer averagePaceSeconds,
        long meetupCount
) {
}
