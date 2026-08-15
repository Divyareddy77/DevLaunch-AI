package com.devlaunch.messaging.event;


import java.time.LocalDate;

/**
 * Lightweight event published for study planner lifecycle changes: task
 * created, task completed (with the applicable milestone), or task overdue.
 * <p>
 * For {@code CREATED} and {@code COMPLETED} events the consumer composes
 * the notification from the structured fields; for {@code OVERDUE} events
 * the publishing scheduler owns the message (it needs the exact text for
 * its daily deduplication check) and carries it in {@code message}.
 * </p>
 *
 * @param userId     the ID of the task owner
 * @param taskId     the ID of the study task
 * @param title      the task title
 * @param studyDate  the scheduled study date
 * @param type       the lifecycle event type
 * @param milestone  the completion milestone for {@code COMPLETED} events, or {@code null}
 * @param dayCount   the milestone day count (weekly days / streak length), or {@code null}
 * @param message    the pre-composed message for {@code OVERDUE} events, or {@code null}
 * @author DevLaunch
 */
public record StudyTaskEvent(Long userId,
                             Long taskId,
                             String title,
                             LocalDate studyDate,
                             StudyTaskType type,
                             StudyMilestone milestone,
                             Integer dayCount,
                             String message) {
}
