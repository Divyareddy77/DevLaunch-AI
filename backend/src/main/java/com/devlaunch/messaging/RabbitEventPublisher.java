package com.devlaunch.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ-backed implementation of {@link EventPublisher}.
 * <p>
 * Publishes JSON-serialized events to the DevLaunch topic exchange with the
 * given routing key. When the broker is unreachable the {@link AmqpException}
 * is logged and swallowed, so a RabbitMQ outage never breaks the business
 * flow — the event is simply skipped and the application keeps serving
 * requests. Payload contents (such as reset tokens) are never logged.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class RabbitEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * Constructs the publisher with the shared RabbitMQ template.
     *
     * @param rabbitTemplate the auto-configured messaging template
     */
    public RabbitEventPublisher(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(final String routingKey, final Object payload) {
        try {
            rabbitTemplate.convertAndSend(EventTopics.EXCHANGE, routingKey, payload);
            MessagingLog.published(routingKey, payload.getClass().getSimpleName());
        } catch (final AmqpException ex) {
            // The broker may be down or still starting. Never propagate:
            // the caller (a business service) must keep working regardless.
            log.error("Could not publish event to routingKey={}: {}. Event skipped, "
                    + "business flow continues.", routingKey, ex.getMessage());
        }
    }

}
