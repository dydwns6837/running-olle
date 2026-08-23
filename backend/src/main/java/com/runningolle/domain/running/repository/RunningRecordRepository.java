package com.runningolle.domain.running.repository;

import com.runningolle.domain.running.entity.RunningRecord;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunningRecordRepository extends JpaRepository<RunningRecord, UUID> {
    @EntityGraph(attributePaths = "course")
    List<RunningRecord> findAllByUserIdOrderByStartedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "course")
    List<RunningRecord> findAllByTripIdOrderByStartedAtDesc(UUID tripId);

    long countByTripId(UUID tripId);

    List<RunningRecord> findAllByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
            UUID userId, LocalDateTime start, LocalDateTime endExclusive
    );
}
