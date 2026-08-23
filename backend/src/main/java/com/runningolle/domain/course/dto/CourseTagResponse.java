package com.runningolle.domain.course.dto;

import com.runningolle.domain.course.entity.CourseTag;
import java.util.UUID;

public record CourseTagResponse(
        UUID id,
        String name
) {

    public static CourseTagResponse from(CourseTag courseTag) {
        return new CourseTagResponse(
                courseTag.getId(),
                courseTag.getName()
        );
    }
}
