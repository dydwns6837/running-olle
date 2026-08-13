package com.runningolle.domain.running.entity;

import com.runningolle.domain.course.entity.CourseWaypoint;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "running_waypoint_visits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Access(AccessType.FIELD)
public class RunningWaypointVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "running_record_id", nullable = false)
    private RunningRecord runningRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_waypoint_id", nullable = false)
    private CourseWaypoint courseWaypoint;

    @Column(name = "photo_url", columnDefinition = "text")
    private String photoUrl;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;
}
