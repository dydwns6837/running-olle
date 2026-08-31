package com.runningolle.domain.community.dto;

import com.runningolle.domain.course.enums.CourseType;
import java.util.UUID;

public record FeedSelectionOptionResponse(
        UUID id,
        String label,
        CourseType courseType,
        Double distanceKm,
        Integer durationSeconds
) {
}
