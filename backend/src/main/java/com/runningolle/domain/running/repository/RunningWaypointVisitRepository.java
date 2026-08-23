package com.runningolle.domain.running.repository;

import com.runningolle.domain.running.entity.RunningWaypointVisit;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunningWaypointVisitRepository extends JpaRepository<RunningWaypointVisit, UUID> {
    long countByRunningRecordTripId(UUID tripId);
}
