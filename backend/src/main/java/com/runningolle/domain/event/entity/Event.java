package com.runningolle.domain.event.entity;

import com.runningolle.domain.course.entity.Course;
import com.runningolle.domain.event.enums.EventType;
import com.runningolle.global.entity.BaseTimeEntity;
import com.runningolle.domain.user.entity.User;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "venue_name", length = 200)
    private String venueName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "organizer", length = 200)
    private String organizer;

    @Column(name = "entry_fee", precision = 10, scale = 2)
    private BigDecimal entryFee;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(name = "external_apply_url", columnDefinition = "text")
    private String externalApplyUrl;

    @Column(name = "is_public", nullable = false)
    @ColumnDefault("true")
    private Boolean isPublic = true;

    @Column(name = "display_order")
    private Integer displayOrder;
}
