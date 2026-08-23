package com.runningolle.domain.course.controller;

import com.runningolle.domain.course.dto.CourseCreateRequest;
import com.runningolle.domain.course.dto.CourseCreateResponse;
import com.runningolle.domain.course.service.CourseCreateService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseCreateService courseCreateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseCreateResponse createCourse(
            Authentication authentication,
            @Valid @RequestBody CourseCreateRequest request
    ) {
        return courseCreateService.createCourse(UUID.fromString(authentication.getName()), request);
    }
}
