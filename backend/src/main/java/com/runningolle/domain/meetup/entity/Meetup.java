package com.runningolle.domain.meetup.entity;

import com.runningolle.domain.course.entity.Course;
import com.runningolle.global.entity.BaseTimeEntity;
import com.runningolle.domain.meetup.enums.JoinMethod;
import com.runningolle.domain.meetup.enums.MeetupStatus;
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
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "meetups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class Meetup extends BaseTimeEntity {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "meetup_date", nullable = false)
    private LocalDateTime meetupDate;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "target_pace", precision = 6, scale = 2)
    private BigDecimal targetPace;

    @Column(name = "meeting_place", nullable = false, length = 200)
    private String meetingPlace;

    @Column(name = "meeting_point", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point meetingPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_method", nullable = false, length = 20)
    private JoinMethod joinMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @ColumnDefault("'RECRUITING'")
    private MeetupStatus status = MeetupStatus.RECRUITING;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Meetup create(
            User organizer,
            Course course,
            String title,
            String description,
            LocalDateTime meetupDate,
            Integer maxParticipants,
            BigDecimal targetPace,
            String meetingPlace,
            Point meetingPoint,
            JoinMethod joinMethod
    ) {
        Meetup meetup = new Meetup();
        meetup.organizer = organizer;
        meetup.course = course;
        meetup.title = title;
        meetup.description = description;
        meetup.meetupDate = meetupDate;
        meetup.maxParticipants = maxParticipants;
        meetup.targetPace = targetPace == null ? ZERO : targetPace.setScale(2, RoundingMode.HALF_UP);
        meetup.meetingPlace = meetingPlace;
        meetup.meetingPoint = meetingPoint;
        meetup.joinMethod = joinMethod;
        meetup.status = MeetupStatus.RECRUITING;
        return meetup;
    }

    public void closeWhenFull(int acceptedCount) {
        if (acceptedCount >= maxParticipants) {
            this.status = MeetupStatus.CLOSED;
        } else if (this.status == MeetupStatus.CLOSED) {
            this.status = MeetupStatus.RECRUITING;
        }
    }

    public void refreshStatus(LocalDateTime now, int acceptedCount) {
        if (this.status == MeetupStatus.CANCELLED) {
            return;
        }

        if (!meetupDate.isAfter(now)) {
            this.status = MeetupStatus.COMPLETED;
            return;
        }

        this.status = acceptedCount >= maxParticipants ? MeetupStatus.CLOSED : MeetupStatus.RECRUITING;
    }

    public void update(
            Course course,
            String title,
            String description,
            LocalDateTime meetupDate,
            Integer maxParticipants,
            BigDecimal targetPace,
            String meetingPlace,
            Point meetingPoint,
            JoinMethod joinMethod
    ) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.meetupDate = meetupDate;
        this.maxParticipants = maxParticipants;
        this.targetPace = targetPace == null ? ZERO : targetPace.setScale(2, RoundingMode.HALF_UP);
        this.meetingPlace = meetingPlace;
        this.meetingPoint = meetingPoint;
        this.joinMethod = joinMethod;
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.status = MeetupStatus.CANCELLED;
    }
}
