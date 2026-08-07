package com.devlaunch.entity.enums;

/**
 * The category of a gamification achievement.
 * <p>
 * Categories mirror the platform modules plus cross-cutting themes
 * (consistency, special). The frontend groups the achievement catalog
 * and filters badge cards by this value.
 * </p>
 *
 * @author DevLaunch
 */
public enum AchievementCategory {

    /** Resume builder module (creating a resume, ATS reviews). */
    RESUME,

    /** AI mock interview module. */
    INTERVIEW,

    /** Job application tracker module. */
    JOB_TRACKER,

    /** Study planner module. */
    STUDY_PLANNER,

    /** GitHub analytics module. */
    GITHUB,

    /** LeetCode tracker module. */
    LEETCODE,

    /** Placement readiness dashboard. */
    PLACEMENT,

    /** Cross-module consistency (streaks, repetition). */
    CONSISTENCY,

    /** Special, cross-cutting achievements. */
    SPECIAL

}
