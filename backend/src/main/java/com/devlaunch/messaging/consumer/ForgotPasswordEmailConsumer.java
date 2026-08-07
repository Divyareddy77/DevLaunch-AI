package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.User;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.ForgotPasswordEmailEvent;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the forgot-password email flow.
 * <p>
 * Resolves the recipient user and delegates to the existing
 * {@link EmailService}, which owns the message template and delivery. A
 * message for a user that no longer exists is logged and acknowledged so it
 * is never retried. Delivery failures are handled inside the email service
 * and never break the forgot-password contract.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class ForgotPasswordEmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordEmailConsumer.class);

    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Constructs the consumer with the reusable email service.
     *
     * @param emailService   the existing email module
     * @param userRepository repository for user lookups
     */
    public ForgotPasswordEmailConsumer(final EmailService emailService,
                                       final UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /**
     * Sends the password reset email for the given event.
     *
     * @param event the forgot-password email event
     */
    @RabbitListener(queues = EventTopics.FORGOT_PASSWORD_EMAIL_QUEUE)
    public void onForgotPasswordEmail(final ForgotPasswordEmailEvent event) {
        if (event.userId() == null) {
            log.warn("Skipping password reset email without a recipient user id");
            return;
        }
        final User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) {
            log.warn("Skipping password reset email for unknown user id={}", event.userId());
            return;
        }
        emailService.sendPasswordResetEmail(user, event.resetToken());
    }

}
