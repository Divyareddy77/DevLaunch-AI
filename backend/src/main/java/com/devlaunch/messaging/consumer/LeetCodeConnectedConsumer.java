package com.devlaunch.messaging.consumer;

import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the LeetCode account connection notification flow.
 * <p>
 * Persists the connect/disconnect confirmation notification through the
 * shared {@link NotificationEventProcessor}.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class LeetCodeConnectedConsumer {

    private final NotificationEventProcessor processor;

    /**
     * Constructs the consumer with the shared notification processor.
     *
     * @param processor the shared notification persistence logic
     */
    public LeetCodeConnectedConsumer(final NotificationEventProcessor processor) {
        this.processor = processor;
    }

    /**
     * Persists the LeetCode connection notification.
     *
     * @param event the notification event to process
     */
    @RabbitListener(queues = EventTopics.LEETCODE_CONNECTED_QUEUE)
    public void onLeetCodeConnected(final NotificationEvent event) {
        processor.process(event);
    }

}
