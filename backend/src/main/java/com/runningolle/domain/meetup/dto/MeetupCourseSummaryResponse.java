package com.runningolle.domain.meetup.dto;

import com.runningolle.domain.course.enums.CourseType;
import com.runningolle.domain.course.enums.Difficulty;
import java.math.BigDecimal;
import java.util.UUID;

public record MeetupCourseSummaryResponse(
        UUID id,
        String name,
        BigDecimal distanceKm,
        Integer durationMinutes,
        Difficulty difficulty,
        CourseType courseType
) {
}
