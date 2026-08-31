package com.runningolle.domain.course.controller;

import com.runningolle.domain.course.dto.CourseTagResponse;
import com.runningolle.domain.course.repository.CourseTagRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-tags")
public class CourseTagController {

    private final CourseTagRepository courseTagRepository;

    @GetMapping
    public List<CourseTagResponse> getCourseTags() {
        return courseTagRepository.findAll().stream()
                .map(CourseTagResponse::from)
                .sorted(Comparator.comparing(CourseTagResponse::name))
                .toList();
    }
}
