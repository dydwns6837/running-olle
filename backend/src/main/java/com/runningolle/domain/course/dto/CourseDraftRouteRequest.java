package com.runningolle.domain.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CourseDraftRouteRequest(
        @Valid
        @Size(min = 2, max = 20, message = "경로 계산에는 2개 이상 20개 이하의 경유지가 필요합니다.")
        List<WaypointRequest> waypoints
) {

    public record WaypointRequest(
            String kakaoPlaceId,

            @NotBlank(message = "경유지 이름은 필수입니다.")
            String name,

            @NotNull(message = "위도는 필수입니다.")
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
            Double lat,

            @NotNull(message = "경도는 필수입니다.")
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
            Double lng,

            @NotNull(message = "경유지 순서는 필수입니다.")
            @Min(value = 0, message = "경유지 순서는 0 이상이어야 합니다.")
            Integer orderIndex
    ) {
    }
}
