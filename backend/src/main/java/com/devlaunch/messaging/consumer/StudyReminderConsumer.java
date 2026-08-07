package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.MessagingLog;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.messaging.event.StudyMilestone;
import com.devlaunch.messaging.event.StudyTaskEvent;
import com.devlaunch.messaging.event.StudyTaskType;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the study planner flow.
 * <p>
 * Persists notifications for created, completed (with the daily goal,
 * weekly target, and streak milestones decided by the publisher), and
 * overdue study tasks through the shared notification processor, and logs
 * the progress and streak metrics for the dashboard aggregation pipeline.
 * </p>
 * <p>
 * Boundary note: the dashboard derives its study-progress and streak
 * aggregates on read from the task source data — this consumer only logs
 * the metrics and generates the notifications; no aggregate table is
 * written.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class StudyReminderConsumer {

    private static final String OVERDUE_TITLE = "Study task overdue";

    private final NotificationEventProcessor processor;

    /**
     * Constructs the consumer with the shared notification processor.
     *
     * @param processor the shared notification persistence logic
     */
    public StudyReminderConsumer(final NotificationEventProcessor processor) {
        this.processor = processor;
    }

    /**
     * Persists the notification(s) for the study task event.
     *
     * @param event the study task event
     */
    @RabbitListener(queues = EventTopics.STUDY_REMINDER_QUEUE)
    public void onStudyTask(final StudyTaskEvent event) {
        MessagingLog.received(EventTopics.STUDY_REMINDER_QUEUE, event.getClass().getSimpleName());
        MessagingLog.processingStarted(EventTopics.STUDY_REMINDER_QUEUE,
                event.getClass().getSimpleName());

        switch (event.type()) {
            case OVERDUE -> processor.process(new NotificationEvent(event.userId(),
                    NotificationType.STUDY, OVERDUE_TITLE, event.message()));
            case CREATED -> processor.process(new NotificationEvent(event.userId(),
                    NotificationType.STUDY, "Study task scheduled",
                    "Task \"" + event.title() + "\" is scheduled for " + event.studyDate()
                            + ". Stay on track!"));
            case COMPLETED -> processCompletion(event);
        }

        MessagingLog.processingCompleted(EventTopics.STUDY_REMINDER_QUEUE,
                event.getClass().getSimpleName(),
                "taskId=" + event.taskId() + ", type=" + event.type()
                        + ", milestone=" + event.milestone()
                        + ", dayCount=" + event.dayCount());
    }

    /**
     * Persists the milestone notifications for a completed task. The
     * publisher emits one event per applicable milestone; each carries the
     * data needed to compose its message.
     *
     * @param event the completed-task event
     */
    private void processCompletion(final StudyTaskEvent event) {
        final StudyMilestone milestone = event.milestone() == null
                ? StudyMilestone.DAILY : event.milestone();
        switch (milestone) {
            case WEEKLY -> processor.process(new NotificationEvent(event.userId(),
                    NotificationType.STUDY, "Weekly target achieved",
                    "You completed tasks on " + event.dayCount()
                            + " days this week — weekly target reached!"));
            case STREAK -> processor.process(new NotificationEvent(event.userId(),
                    NotificationType.STUDY, "Study streak milestone",
                    event.dayCount() + "-day study streak! You're on fire — keep it going."));
            default -> processor.process(new NotificationEvent(event.userId(),
                    NotificationType.STUDY, "Daily goal completed",
                    "Task \"" + event.title() + "\" was marked as completed. Keep up the momentum!"));
        }
    }

}
