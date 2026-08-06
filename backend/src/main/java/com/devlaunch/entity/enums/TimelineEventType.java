package com.devlaunch.entity.enums;

/**
 * Enumeration of the event categories recorded on a job application timeline.
 * <p>
 * The timeline is populated automatically by the job tracker whenever a
 * meaningful milestone happens — the application is added, its status
 * changes, an interview is scheduled or cancelled, or an attachment is
 * uploaded. Each event carries a timestamp and optional notes.
 * </p>
 */
public enum TimelineEventType {

    /**
     * The application was added to the tracker.
     */
    ADDED,

    /**
     * The application moved to the applied stage.
     */
    APPLIED,

    /**
     * The application moved to the assessment stage.
     */
    ASSESSMENT,

    /**
     * The application moved to the interview stage.
     */
    INTERVIEW,

    /**
     * The application moved to the offer stage.
     */
    OFFER,

    /**
     * The application was rejected.
     */
    REJECTED,

    /**
     * A status change that does not map to a dedicated stage label.
     */
    STATUS_UPDATED,

    /**
     * An interview was scheduled for this application.
     */
    INTERVIEW_SCHEDULED,

    /**
     * A scheduled interview was cancelled.
     */
    INTERVIEW_CANCELLED,

    /**
     * An attachment document was uploaded.
     */
    ATTACHMENT_ADDED

}
