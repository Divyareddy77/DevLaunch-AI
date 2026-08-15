package com.devlaunch.entity;

import com.devlaunch.entity.enums.TimelineEventType;
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

import java.time.LocalDateTime;

/**
 * Represents a single milestone on a job application's timeline.
 * <p>
 * The timeline is populated automatically by the job tracker whenever a
 * meaningful milestone happens — the application is added, its status
 * changes, an interview is scheduled or cancelled, or an attachment is
 * uploaded. Each event records the type, a human-readable title, optional
 * notes, and the moment it occurred.
 * </p>
 */
@Entity
@Table(name = "application_timeline_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "application")
@EqualsAndHashCode(callSuper = true)
public class ApplicationTimelineEvent extends BaseEntity {

    /**
     * The job application this timeline event belongs to.
     * <p>
     * An application may have many timeline events, and every event is
     * owned by exactly one application.
     * </p>
     */
    @NotNull(message = "Application is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    /**
     * The category of this timeline event.
     */
    @NotNull(message = "Event type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private TimelineEventType eventType;

    /**
     * The human-readable headline of the event (e.g. "Applied").
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Event title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Optional notes attached to the event (e.g. a response time note).
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * The moment the event occurred.
     */
    @NotNull(message = "Occurred at is required")
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

}
