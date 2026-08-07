package com.devlaunch.messaging.event;

/**
 * Enumeration of the study completion milestones a completed task can
 * trigger. The publishing service decides which milestones apply (from the
 * user's completion history); the consumer composes the matching message.
 *
 * @author DevLaunch
 */
public enum StudyMilestone {

    /** The daily goal notification for a completed task. */
    DAILY,

    /** The weekly target notification (multiple of five completion days). */
    WEEKLY,

    /** The practice-streak milestone notification. */
    STREAK

}
