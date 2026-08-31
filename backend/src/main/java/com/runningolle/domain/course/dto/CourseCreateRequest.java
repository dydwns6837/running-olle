package com.runningolle.domain.course.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.runningolle.domain.course.enums.CourseType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CourseCreateRequest(
        @NotBlank
        @Size(max = 200)
        String name,

        String description,

        @NotNull
        CourseType courseType,

        @NotEmpty
        @Size(min = 2, max = 20)
        List<@Valid WaypointRequest> waypoints,

        List<UUID> themeIds,

        List<UUID> tagIds,

        Boolean isPublic
) {

    public record WaypointRequest(
            String kakaoPlaceId,

            @NotBlank
            @Size(max = 200)
            String name,

            @NotNull
            @DecimalMin("-90.0")
            @DecimalMax("90.0")
            Double lat,

            @NotNull
            @DecimalMin("-180.0")
            @DecimalMax("180.0")
            Double lng,

            @NotNull
            Integer orderIndex,

            String description,
            String tourContentId,
            String tourContentTypeId,
            String firstImageUrl,
            String thumbnailImageUrl,
            JsonNode tourDataRaw
    ) {
    }
}
