package com.devlaunch.messaging;

import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.messaging.event.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RabbitEventPublisher}.
 * <p>
 * Verifies that events are published to the DevLaunch exchange with the
 * given routing key and that a broker outage is logged and swallowed so
 * the publishing business flow never crashes.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitEventPublisher(rabbitTemplate);
    }

    @Test
    @DisplayName("publishing sends the payload to the exchange with the routing key")
    void publishSendsPayloadToExchangeWithRoutingKey() {
        final NotificationEvent event =
                new NotificationEvent(1L, NotificationType.SYSTEM, "Title", "Message");

        publisher.publish(EventTopics.PASSWORD_RESET_SUCCESS_KEY, event);

        verify(rabbitTemplate).convertAndSend(
                EventTopics.EXCHANGE, EventTopics.PASSWORD_RESET_SUCCESS_KEY, event);
    }

    @Test
    @DisplayName("a broker outage is logged and never propagates to the caller")
    void publishSwallowsBrokerFailures() {
        doThrow(new AmqpException("connection refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));

        // The RabbitMQ outage must not break the business flow.
        assertDoesNotThrow(() -> publisher.publish(
                EventTopics.PASSWORD_RESET_SUCCESS_KEY,
                new NotificationEvent(1L, NotificationType.SYSTEM, "Title", "Message")));

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));
    }

}
