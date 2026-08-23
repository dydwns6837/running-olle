package com.runningolle.domain.course.entity;

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
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "course_tag_map",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_tag_map_course_id_course_tag_id",
                columnNames = {"course_id", "course_tag_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Access(AccessType.FIELD)
public class CourseTagMap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_tag_id", nullable = false)
    private CourseTag courseTag;

    public static CourseTagMap of(Course course, CourseTag courseTag) {
        CourseTagMap courseTagMap = new CourseTagMap();
        courseTagMap.course = course;
        courseTagMap.courseTag = courseTag;
        return courseTagMap;
    }
}
