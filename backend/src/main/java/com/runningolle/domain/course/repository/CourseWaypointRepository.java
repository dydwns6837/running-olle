package com.runningolle.domain.course.repository;

import com.runningolle.domain.course.entity.CourseWaypoint;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseWaypointRepository extends JpaRepository<CourseWaypoint, UUID> {

    List<CourseWaypoint> findByCourse_IdInOrderByCourse_IdAscOrderIndexAsc(Collection<UUID> courseIds);
    List<CourseWaypoint> findByCourse_IdOrderByOrderIndexAsc(UUID courseId);
}
