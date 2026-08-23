package com.runningolle.domain.course.repository;

import com.runningolle.domain.course.entity.CourseBookmark;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseBookmarkRepository extends JpaRepository<CourseBookmark, UUID> {
    @EntityGraph(attributePaths = {"course", "course.creator"})
    List<CourseBookmark> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<CourseBookmark> findByIdAndUserId(UUID id, UUID userId);
}
