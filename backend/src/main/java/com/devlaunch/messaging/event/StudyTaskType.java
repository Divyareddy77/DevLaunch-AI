package com.devlaunch.messaging.event;

/**
 * Enumeration of the study task lifecycle events published on the study
 * reminder queue.
 *
 * @author DevLaunch
 */
public enum StudyTaskType {

    /** A study task was created. */
    CREATED,

    /** A study task was marked as completed. */
    COMPLETED,

    /** A scheduled study task is overdue (past its date, not completed). */
    OVERDUE

}
