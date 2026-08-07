package com.devlaunch.entity.enums;

/**
 * The platform activity types that feed the gamification engine.
 * <p>
 * Business services publish an {@code ActivityEvent} carrying one of these
 * types whenever a module-level action completes (resume created, resume
 * reviewed, application added, interview completed, study task completed,
 * GitHub/LeetCode connected, placement score updated). The achievement
 * consumer evaluates achievements and awards XP from these events.
 * </p>
 *
 * @author DevLaunch
 */
public enum ActivityType {

    /** A resume was created in the resume builder. */
    RESUME_CREATED,

    /** An AI ATS resume review completed. */
    RESUME_REVIEWED,

    /** A job application was added to the tracker. */
    JOB_APPLICATION_CREATED,

    /** A mock interview was completed. */
    INTERVIEW_COMPLETED,

    /** A study planner task was marked completed. */
    STUDY_TASK_COMPLETED,

    /** A GitHub account was connected. */
    GITHUB_CONNECTED,

    /** A LeetCode account was connected / synced. */
    LEETCODE_SYNCED,

    /** The placement readiness score was updated. */
    PLACEMENT_UPDATED

}
