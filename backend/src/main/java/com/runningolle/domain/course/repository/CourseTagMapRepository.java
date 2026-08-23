package com.runningolle.domain.course.repository;

import com.runningolle.domain.course.entity.CourseTagMap;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseTagMapRepository extends JpaRepository<CourseTagMap, UUID> {
}
