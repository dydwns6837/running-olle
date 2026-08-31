package com.runningolle.domain.course.dto;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

public record RouteCoordinateResponse(
        double lat,
        double lng
) {

    public static List<RouteCoordinateResponse> from(LineString lineString) {
        if (lineString == null) {
            return List.of();
        }
        return from(lineString.getCoordinates());
    }

    public static List<RouteCoordinateResponse> preview(LineString lineString, int maxPoints) {
        if (lineString == null) {
            return List.of();
        }
        Coordinate[] coordinates = lineString.getCoordinates();
        if (maxPoints <= 0 || coordinates.length <= maxPoints) {
            return from(coordinates);
        }

        List<RouteCoordinateResponse> sampled = new ArrayList<>();
        int lastIndex = coordinates.length - 1;
        for (int index = 0; index < maxPoints; index++) {
            int sourceIndex = (int) Math.round((double) index * lastIndex / (maxPoints - 1));
            Coordinate coordinate = coordinates[sourceIndex];
            sampled.add(new RouteCoordinateResponse(coordinate.y, coordinate.x));
        }
        return sampled;
    }

    private static List<RouteCoordinateResponse> from(Coordinate[] coordinates) {
        List<RouteCoordinateResponse> responses = new ArrayList<>();
        for (Coordinate coordinate : coordinates) {
            responses.add(new RouteCoordinateResponse(coordinate.y, coordinate.x));
        }
        return responses;
    }
}
