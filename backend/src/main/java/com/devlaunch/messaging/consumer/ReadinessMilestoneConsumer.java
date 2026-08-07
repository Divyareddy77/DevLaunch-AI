package com.devlaunch.messaging.consumer;

import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the placement readiness milestone notification flow.
 * <p>
 * Persists readiness level-up / improvement notifications through the
 * shared {@link NotificationEventProcessor}. The readiness score itself is
 * aggregated by the dashboard module from source data; business services
 * never touch dashboard statistics directly.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class ReadinessMilestoneConsumer {

    private final NotificationEventProcessor processor;

    /**
     * Constructs the consumer with the shared notification processor.
     *
     * @param processor the shared notification persistence logic
     */
    public ReadinessMilestoneConsumer(final NotificationEventProcessor processor) {
        this.processor = processor;
    }

    /**
     * Persists the readiness milestone notification.
     *
     * @param event the notification event to process
     */
    @RabbitListener(queues = EventTopics.READINESS_MILESTONE_QUEUE)
    public void onReadinessMilestone(final NotificationEvent event) {
        processor.process(event);
    }

}
