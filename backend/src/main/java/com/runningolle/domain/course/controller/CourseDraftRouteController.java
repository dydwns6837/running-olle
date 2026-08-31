package com.runningolle.domain.course.controller;

import com.runningolle.domain.course.dto.CourseDraftRouteRequest;
import com.runningolle.domain.course.dto.CourseDraftRouteResponse;
import com.runningolle.domain.course.service.CourseDraftRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseDraftRouteController {

    private final CourseDraftRouteService courseDraftRouteService;

    @PostMapping("/draft/route")
    public ResponseEntity<CourseDraftRouteResponse> calculateDraftRoute(
            @Valid @RequestBody CourseDraftRouteRequest request
    ) {
        return ResponseEntity.ok(courseDraftRouteService.calculateDraftRoute(request));
    }
}
