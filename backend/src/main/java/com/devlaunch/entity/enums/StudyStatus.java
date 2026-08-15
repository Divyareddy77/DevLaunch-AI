package com.devlaunch.entity.enums;

/**
 * Enumeration of the possible statuses for a study plan entry.
 * <p>
 * Tracks the current completion stage of a study session or task,
 * from initial scheduling through to completion.
 * </p>
 */
public enum StudyStatus {

    /**
     * The study session has been scheduled but not yet started.
     */
    PENDING,

    /**
     * The study session is currently in progress.
     */
    IN_PROGRESS,

    /**
     * The study session has been completed.
     */
    COMPLETED

}
