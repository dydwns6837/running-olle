package com.runningolle.domain.course.entity;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Getter
@Entity
@Table(name = "course_waypoints")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Access(AccessType.FIELD)
public class CourseWaypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "kakao_place_id", length = 100)
    private String kakaoPlaceId;

    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "distance_from_start_km", precision = 10, scale = 2)
    private BigDecimal distanceFromStartKm;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "tour_content_id", length = 100)
    private String tourContentId;

    @Column(name = "tour_content_type_id", length = 10)
    private String tourContentTypeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tour_data", columnDefinition = "jsonb")
    private JsonNode tourData;

    @Column(name = "tour_synced_at")
    private LocalDateTime tourSyncedAt;
}
