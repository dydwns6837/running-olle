package com.runningolle.domain.course.repository;

import com.runningolle.domain.course.entity.CourseWaypoint;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseWaypointRepository extends JpaRepository<CourseWaypoint, UUID> {
}
