package com.runningolle.domain.course.repository;

import com.runningolle.domain.course.entity.CourseTheme;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseThemeRepository extends JpaRepository<CourseTheme, UUID> {
}
