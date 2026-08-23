package com.runningolle.domain.course.service;

import com.runningolle.domain.course.dto.CourseDraftRouteRequest;
import com.runningolle.domain.course.dto.CourseDraftRouteResponse;
import com.runningolle.domain.course.enums.Difficulty;
import com.runningolle.domain.routing.client.OpenRouteServiceClient;
import com.runningolle.domain.routing.client.OpenRouteServiceClient.OrsRouteResult;
import com.runningolle.domain.routing.client.OpenRouteServiceClient.Waypoint;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseDraftRouteService {

    private static final double LOW_DIFFICULTY_ELEVATION_GAIN_PER_KM = 10.0;
    private static final double MID_DIFFICULTY_ELEVATION_GAIN_PER_KM = 30.0;

    private final OpenRouteServiceClient openRouteServiceClient;

    public CourseDraftRouteResponse calculateDraftRoute(CourseDraftRouteRequest request) {
        List<Waypoint> waypoints = toRoutingWaypoints(request);
        OrsRouteResult routeResult = openRouteServiceClient.calculateFootWalkingRoute(waypoints);

        return CourseDraftRouteResponse.from(routeResult, suggestDifficulty(routeResult));
    }

    private List<Waypoint> toRoutingWaypoints(CourseDraftRouteRequest request) {
        if (request == null || request.waypoints() == null || request.waypoints().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경로 계산에는 최소 2개 이상의 경유지가 필요합니다.");
        }

        return request.waypoints().stream()
                .sorted(Comparator.comparing(CourseDraftRouteRequest.WaypointRequest::orderIndex))
                .map(waypoint -> new Waypoint(
                        waypoint.name().trim(),
                        waypoint.lat(),
                        waypoint.lng()
                ))
                .toList();
    }

    private Difficulty suggestDifficulty(OrsRouteResult routeResult) {
        if (routeResult.distanceKm() <= 0) {
            return Difficulty.LOW;
        }

        double elevationGainPerKm = routeResult.elevationGainM() / routeResult.distanceKm();
        if (elevationGainPerKm < LOW_DIFFICULTY_ELEVATION_GAIN_PER_KM) {
            return Difficulty.LOW;
        }
        if (elevationGainPerKm < MID_DIFFICULTY_ELEVATION_GAIN_PER_KM) {
            return Difficulty.MID;
        }
        return Difficulty.HIGH;
    }
}
