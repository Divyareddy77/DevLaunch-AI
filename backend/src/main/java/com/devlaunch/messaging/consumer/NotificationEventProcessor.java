package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.User;
import com.devlaunch.messaging.MessagingLog;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Shared notification-persistence logic for every notification consumer.
 * <p>
 * All five notification flows resolve the recipient user from the event and
 * delegate to the existing {@link NotificationService} — the single owner of
 * notification persistence. A message for a user that no longer exists is a
 * permanent failure: it is logged and acknowledged (returned normally) so it
 * is never retried or dead-lettered.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class NotificationEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventProcessor.class);

    /**
     * Queue label used in the structured logs; the processor is shared by
     * every notification consumer so the flow identity comes from the
     * caller's log lines.
     */
    private static final String PROCESSING_QUEUE = "notification";

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Constructs the processor with the reusable notification service.
     *
     * @param notificationService the existing notification module
     * @param userRepository      repository for user lookups
     */
    public NotificationEventProcessor(final NotificationService notificationService,
                                      final UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    /**
     * Persists the notification carried by the event for the recipient user.
     *
     * @param event the notification event to process
     */
    public void process(final NotificationEvent event) {
        MessagingLog.processingStarted(PROCESSING_QUEUE, event.getClass().getSimpleName());
        if (event.userId() == null) {
            log.warn("Skipping notification '{}' without a recipient user id", event.title());
            return;
        }
        final User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) {
            log.warn("Skipping notification '{}' for unknown user id={}", event.title(), event.userId());
            return;
        }
        notificationService.createNotification(user, event.type(), event.title(), event.message());
        MessagingLog.processingCompleted(PROCESSING_QUEUE, event.getClass().getSimpleName(),
                "notification '" + event.title() + "' persisted for user id=" + event.userId());
    }

}
