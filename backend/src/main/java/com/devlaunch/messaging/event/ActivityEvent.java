package com.devlaunch.messaging.event;

import com.devlaunch.entity.enums.ActivityType;

import java.time.LocalDateTime;

/**
 * Lightweight event published whenever a platform module changes in a way
 * that matters for gamification.
 * <p>
 * Business services publish this event (alongside their own flow events)
 * when a resume is created, an ATS review completes, a job application is
 * added, a mock interview finishes, a study task is completed, a
 * GitHub/LeetCode account is connected, or the placement readiness score
 * is updated. The achievement consumer evaluates achievements and awards
 * XP from it asynchronously — the publishing service never touches the
 * gamification tables.
 * </p>
 *
 * @param userId     the ID of the user who performed the activity
 * @param type       the activity type (which badges and XP it feeds)
 * @param value      an optional activity value: the ATS score for resume
 *                   reviews, the overall score for interviews, the current
 *                   study streak for completed study tasks, or the placement
 *                   readiness score — {@code null} when not applicable
 * @param occurredAt when the activity happened
 * @author DevLaunch
 */
public record ActivityEvent(Long userId,
                            ActivityType type,
                            Integer value,
                            LocalDateTime occurredAt) {
}
