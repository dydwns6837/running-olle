package com.runningolle.domain.running.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateRunningRecordRequest(
        @NotNull @Size(min = 1, max = 100_000) List<@Valid RoutePoint> route,
        @PositiveOrZero double totalDistanceMeters,
        @PositiveOrZero int totalDurationSeconds,
        @DecimalMin("0.0") Double averagePace,
        @PositiveOrZero double calories,
        @NotNull OffsetDateTime startedAt,
        @NotNull OffsetDateTime endedAt
) {
    public record RoutePoint(
            @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") double longitude
    ) {
    }
}
