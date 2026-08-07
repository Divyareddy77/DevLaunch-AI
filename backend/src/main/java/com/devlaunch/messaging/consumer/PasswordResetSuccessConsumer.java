package com.devlaunch.messaging.consumer;

import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the password-reset-success notification flow.
 * <p>
 * Persists the confirmation notification for the user whose password was
 * reset, reusing the shared {@link NotificationEventProcessor}.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class PasswordResetSuccessConsumer {

    private final NotificationEventProcessor processor;

    /**
     * Constructs the consumer with the shared notification processor.
     *
     * @param processor the shared notification persistence logic
     */
    public PasswordResetSuccessConsumer(final NotificationEventProcessor processor) {
        this.processor = processor;
    }

    /**
     * Persists the password-reset-success notification.
     *
     * @param event the notification event to process
     */
    @RabbitListener(queues = EventTopics.PASSWORD_RESET_SUCCESS_QUEUE)
    public void onPasswordResetSuccess(final NotificationEvent event) {
        processor.process(event);
    }

}
