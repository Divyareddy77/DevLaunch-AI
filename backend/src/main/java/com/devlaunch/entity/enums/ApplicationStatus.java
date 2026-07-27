package com.devlaunch.entity.enums;

/**
 * Enumeration of the possible statuses for a job application.
 * <p>
 * Tracks the current stage of a job application within the hiring pipeline,
 * from initial interest through to offer or rejection.
 * </p>
 */
public enum ApplicationStatus {

    /**
     * The job has been saved for future reference but no application has been submitted yet.
     */
    WISHLIST,

    /**
     * The user has submitted an application for this position.
     */
    APPLIED,

    /**
     * The employer has requested the user to complete an assessment (e.g. coding test,
     * personality test, or skills evaluation).
     */
    ASSESSMENT,

    /**
     * The user has been invited to an interview (phone, video, or in-person).
     */
    INTERVIEW,

    /**
     * The employer has extended a job offer to the user.
     */
    OFFER,

    /**
     * The application has been rejected by the employer.
     */
    REJECTED

}
