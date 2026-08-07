package com.devlaunch.messaging.consumer;

import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the job-application-reminder notification flow.
 * <p>
 * Persists job tracker notifications (application added, status changes,
 * interview scheduled/cancelled) through the shared
 * {@link NotificationEventProcessor}. The message wording stays in the
 * publishing job application service.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class JobApplicationReminderConsumer {

    private final NotificationEventProcessor processor;

    /**
     * Constructs the consumer with the shared notification processor.
     *
     * @param processor the shared notification persistence logic
     */
    public JobApplicationReminderConsumer(final NotificationEventProcessor processor) {
        this.processor = processor;
    }

    /**
     * Persists the job-application-reminder notification.
     *
     * @param event the notification event to process
     */
    @RabbitListener(queues = EventTopics.JOB_APPLICATION_REMINDER_QUEUE)
    public void onJobApplicationReminder(final NotificationEvent event) {
        processor.process(event);
    }

}
