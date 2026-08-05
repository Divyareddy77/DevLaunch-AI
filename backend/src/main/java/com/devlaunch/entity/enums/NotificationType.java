package com.devlaunch.entity.enums;

/**
 * Enumeration of the notification categories a user can receive.
 * <p>
 * Notifications are created automatically by the existing platform
 * modules whenever an important action completes. {@code ANNOUNCEMENT}
 * notifications are generated from the admin announcement module,
 * while the remaining categories map to the resume builder, job
 * tracker, study planner, and AI modules.
 * </p>
 */
public enum NotificationType {

    /**
     * A platform announcement published by an administrator.
     */
    ANNOUNCEMENT,

    /**
     * An action within the resume builder (created, updated, downloaded).
     */
    RESUME,

    /**
     * An action within the job application tracker (added, status change).
     */
    JOB,

    /**
     * A study planner milestone (task completed, weekly target, streak).
     */
    STUDY,

    /**
     * A completed mock interview or a new interview milestone.
     */
    MOCK_INTERVIEW,

    /**
     * A completed AI resume review or an improved resume score.
     */
    RESUME_REVIEW,

    /**
     * A general system notification.
     */
    SYSTEM

}
