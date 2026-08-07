package com.devlaunch.messaging.consumer;

import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the interview-reminder notification flow.
 * <p>
 * Persists the daily "interview tomorrow" reminder through the shared
 * {@link NotificationEventProcessor}. Deduplication is handled by the
 * publishing scheduler, which never re-publishes a reminder that was
 * already sent for the same interview.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class InterviewReminderConsumer {

    private final NotificationEventProcessor processor;

    /**
     * Constructs the consumer with the shared notification processor.
     *
     * @param processor the shared notification persistence logic
     */
    public InterviewReminderConsumer(final NotificationEventProcessor processor) {
        this.processor = processor;
    }

    /**
     * Persists the interview-reminder notification.
     *
     * @param event the notification event to process
     */
    @RabbitListener(queues = EventTopics.INTERVIEW_REMINDER_QUEUE)
    public void onInterviewReminder(final NotificationEvent event) {
        processor.process(event);
    }

}
