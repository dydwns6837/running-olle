package com.runningolle.domain.course.repository;

import com.runningolle.domain.course.entity.CourseTag;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseTagRepository extends JpaRepository<CourseTag, UUID> {
}
