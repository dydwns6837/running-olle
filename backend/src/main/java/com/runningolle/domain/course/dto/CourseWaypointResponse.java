package com.runningolle.domain.course.dto;

import com.runningolle.domain.course.entity.CourseWaypoint;
import java.math.BigDecimal;
import java.util.UUID;

public record CourseWaypointResponse(
        UUID id,
        String name,
        String kakaoPlaceId,
        double lat,
        double lng,
        Integer orderIndex,
        BigDecimal distanceFromStartKm,
        String description,
        String tourContentId,
        String tourContentTypeId
) {

    public static CourseWaypointResponse from(CourseWaypoint waypoint) {
        return new CourseWaypointResponse(
                waypoint.getId(),
                waypoint.getName(),
                waypoint.getKakaoPlaceId(),
                waypoint.getLocation().getY(),
                waypoint.getLocation().getX(),
                waypoint.getOrderIndex(),
                waypoint.getDistanceFromStartKm(),
                waypoint.getDescription(),
                waypoint.getTourContentId(),
                waypoint.getTourContentTypeId()
        );
    }
}
