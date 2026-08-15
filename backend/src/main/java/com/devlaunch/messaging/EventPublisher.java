package com.devlaunch.messaging;

/**
 * Abstraction over the messaging backbone used by business services.
 * <p>
 * Business services publish lightweight events through this interface
 * instead of sending emails or persisting notifications directly; the
 * matching consumers perform those side effects. The interface keeps
 * services decoupled from RabbitMQ and easy to unit-test with a mock.
 * </p>
 *
 * @author DevLaunch
 */
public interface EventPublisher {

    /**
     * Publishes an event to the configured exchange with the given routing
     * key.
     * <p>
     * Implementations must never propagate broker failures to the caller:
     * if the broker is unreachable the event is logged and dropped so the
     * business flow continues uninterrupted.
     * </p>
     *
     * @param routingKey the routing key that selects the target queue
     * @param payload    the event payload to publish
     */
    void publish(String routingKey, Object payload);

}
