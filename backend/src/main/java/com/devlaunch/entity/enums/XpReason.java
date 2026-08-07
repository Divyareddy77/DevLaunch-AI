package com.devlaunch.entity.enums;

/**
 * The reason an XP entry was awarded, stored on every {@code XpHistory}
 * row so the XP ledger stays auditable.
 * <p>
 * Activity reasons map 1:1 to {@link ActivityType}; achievements award a
 * dedicated reason so the history page can distinguish "completed an
 * interview" (+40 XP) from "unlocked an achievement" (+300 XP).
 * </p>
 *
 * @author DevLaunch
 */
public enum XpReason {

    /** XP for creating a resume. */
    RESUME_CREATED,

    /** XP for running an AI ATS resume review. */
    RESUME_REVIEWED,

    /** XP for adding a job application. */
    JOB_APPLICATION_CREATED,

    /** XP for completing a mock interview. */
    INTERVIEW_COMPLETED,

    /** XP for completing a study task. */
    STUDY_TASK_COMPLETED,

    /** XP for connecting a GitHub account. */
    GITHUB_CONNECTED,

    /** XP for connecting / syncing a LeetCode account. */
    LEETCODE_SYNCED,

    /** XP for updating the placement readiness score. */
    PLACEMENT_UPDATED,

    /** XP awarded for unlocking an achievement badge. */
    ACHIEVEMENT_UNLOCKED;

    /**
     * Maps an activity type to its XP ledger reason.
     *
     * @param activityType the activity that generated XP
     * @return the matching ledger reason
     */
    public static XpReason fromActivity(final ActivityType activityType) {
        return XpReason.valueOf(activityType.name());
    }

}
