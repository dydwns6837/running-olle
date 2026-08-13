package com.runningolle.domain.course.entity;

import com.runningolle.domain.course.enums.NearbyPlaceCategory;
import com.runningolle.global.entity.BaseCreatedAtEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(
        name = "course_nearby_places",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_nearby_places_course_id_kakao_place_id",
                columnNames = {"course_id", "kakao_place_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class CourseNearbyPlace extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "kakao_place_id", nullable = false, length = 100)
    private String kakaoPlaceId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private NearbyPlaceCategory category;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(name = "distance_from_course_m", precision = 10, scale = 2)
    private BigDecimal distanceFromCourseM;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;
}
