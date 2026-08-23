package com.runningolle.domain.course.dto;

import com.runningolle.domain.course.enums.Difficulty;
import com.runningolle.domain.routing.client.OpenRouteServiceClient.OrsRouteResult;
import com.runningolle.domain.routing.client.OpenRouteServiceClient.SurfaceBreakdown;
import java.util.Arrays;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;

public record CourseDraftRouteResponse(
        double distanceKm,
        int estimatedDurationMinutes,
        double elevationGainM,
        SurfaceResponse surface,
        List<RouteCoordinateResponse> routeCoordinates,
        String routeLineStringWkt,
        Difficulty suggestedDifficulty
) {

    public static CourseDraftRouteResponse from(OrsRouteResult routeResult, Difficulty suggestedDifficulty) {
        return new CourseDraftRouteResponse(
                routeResult.distanceKm(),
                routeResult.estimatedDurationMinutes(),
                routeResult.elevationGainM(),
                SurfaceResponse.from(routeResult.surface()),
                RouteCoordinateResponse.from(routeResult.routeLineString().getCoordinates()),
                routeResult.routeLineStringWkt(),
                suggestedDifficulty
        );
    }

    public record SurfaceResponse(
            double asphaltPct,
            double dirtPct,
            double stairsPct
    ) {

        private static SurfaceResponse from(SurfaceBreakdown surface) {
            if (surface == null) {
                return null;
            }
            return new SurfaceResponse(surface.asphaltPct(), surface.dirtPct(), surface.stairsPct());
        }
    }

    public record RouteCoordinateResponse(
            double lat,
            double lng
    ) {

        private static List<RouteCoordinateResponse> from(Coordinate[] coordinates) {
            return Arrays.stream(coordinates)
                    .map(coordinate -> new RouteCoordinateResponse(coordinate.y, coordinate.x))
                    .toList();
        }
    }
}
