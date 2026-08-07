package com.devlaunch.messaging.config;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RabbitMQConfig}.
 * <p>
 * Verifies that the shared listener container factory is wired with the
 * retry interceptor required for transient-failure support. (The actual
 * retry → dead-letter routing is a RabbitMQ server behavior that the
 * in-process broker mock does not emulate, so it is verified here at the
 * configuration level.)
 * </p>
 *
 * @author DevLaunch
 */
class RabbitMQConfigTest {

    @Test
    @DisplayName("the listener container factory carries the retry interceptor")
    void containerFactoryHasRetryAdvice() {
        final RabbitMQConfig config = new RabbitMQConfig();
        final ConnectionFactory connectionFactory =
                new CachingConnectionFactory(new MockConnectionFactory());

        final SimpleRabbitListenerContainerFactory factory =
                config.rabbitListenerContainerFactory(
                        connectionFactory, new Jackson2JsonMessageConverter());

        final Advice[] advice = factory.getAdviceChain();
        assertNotNull(advice, "container factory must declare an advice chain");
        assertTrue(advice.length == 1 && advice[0] instanceof RetryOperationsInterceptor,
                "expected exactly the retry interceptor on the container factory");
    }

}
