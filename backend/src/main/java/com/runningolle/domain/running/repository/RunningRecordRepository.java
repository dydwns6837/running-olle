package com.runningolle.domain.running.repository;

import com.runningolle.domain.running.entity.RunningRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunningRecordRepository extends JpaRepository<RunningRecord, UUID> {
    Optional<RunningRecord> findByIdAndUserId(UUID id, UUID userId);
    List<RunningRecord> findTop10ByUserIdOrderByStartedAtDesc(UUID userId);
}
