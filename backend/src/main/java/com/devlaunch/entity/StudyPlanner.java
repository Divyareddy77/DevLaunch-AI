package com.devlaunch.entity;

import com.devlaunch.entity.enums.StudyPriority;
import com.devlaunch.entity.enums.StudyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a scheduled study session or task tracked by a registered user.
 * <p>
 * Each record captures the title and description of a study activity, the
 * scheduled date and optional time window, its priority level, and its current
 * completion status. A user may have many study planner entries, allowing them
 * to organise and track their learning activities over time.
 * </p>
 */
@Entity
@Table(name = "study_planners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"user"})
@EqualsAndHashCode(callSuper = true)
public class StudyPlanner extends BaseEntity {

    /**
     * The title or name of the study session or task.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * A detailed description of the study session or task, including any
     * specific topics, resources, or objectives to cover.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * The scheduled date for this study session.
     * <p>
     * Specifies the calendar date on which the study activity is planned or
     * was completed. Must not be null.
     * </p>
     */
    @NotNull(message = "Study date is required")
    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    /**
     * The intended or actual start time of the study session.
     * <p>
     * May be {@code null} if only the date is specified without a specific
     * start time.
     * </p>
     */
    @Column(name = "start_time")
    private LocalTime startTime;

    /**
     * The intended or actual end time of the study session.
     * <p>
     * May be {@code null} if the session duration is not predefined.
     * </p>
     */
    @Column(name = "end_time")
    private LocalTime endTime;

    /**
     * The priority level of this study session.
     * <p>
     * Indicates the relative importance or urgency of the study activity.
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private StudyPriority priority;

    /**
     * The current completion status of this study session.
     * <p>
     * Tracks whether the session is pending, in progress, or completed.
     * Must not be null. Defaults to {@link StudyStatus#PENDING}.
     * </p>
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StudyStatus status = StudyStatus.PENDING;

    /**
     * The user who owns this study planner entry.
     * <p>
     * A user may have many study planner entries. Each entry must be
     * associated with exactly one user.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
