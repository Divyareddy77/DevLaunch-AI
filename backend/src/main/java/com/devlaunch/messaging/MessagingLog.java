package com.devlaunch.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Structured logging for the messaging backbone.
 * <p>
 * Single home for the observability log lines required by the event-driven
 * architecture — event published, message received, processing started and
 * completed, retry attempts, and dead-letter routing — so every publisher
 * and consumer logs the same format without duplicating log statements.
 * </p>
 *
 * @author DevLaunch
 */
public final class MessagingLog {

    private static final Logger log = LoggerFactory.getLogger(MessagingLog.class);

    /**
     * Logs that an event was published to the exchange.
     *
     * @param routingKey the routing key used
     * @param payloadType the event payload type
     */
    public static void published(final String routingKey, final String payloadType) {
        log.info("Event published: routingKey={}, payloadType={}", routingKey, payloadType);
    }

    /**
     * Logs that a consumer received a message.
     *
     * @param queue       the queue the message arrived on
     * @param payloadType the event payload type
     */
    public static void received(final String queue, final String payloadType) {
        log.info("Message received: queue={}, payloadType={}", queue, payloadType);
    }

    /**
     * Logs that a consumer started processing a message.
     *
     * @param queue       the queue the message arrived on
     * @param payloadType the event payload type
     */
    public static void processingStarted(final String queue, final String payloadType) {
        log.info("Processing started: queue={}, payloadType={}", queue, payloadType);
    }

    /**
     * Logs that a consumer finished processing a message.
     *
     * @param queue       the queue the message arrived on
     * @param payloadType the event payload type
     * @param outcome     a short description of what was completed
     */
    public static void processingCompleted(final String queue, final String payloadType,
                                           final String outcome) {
        log.info("Processing completed: queue={}, payloadType={}, outcome={}",
                queue, payloadType, outcome);
    }

    /**
     * Logs a transient failure that will be retried with backoff.
     *
     * @param attempt the attempt number that just failed
     * @param error   the failure message
     */
    public static void retryAttempt(final int attempt, final String error) {
        log.warn("Retry scheduled after failed attempt {}: {}", attempt, error);
    }

    /**
     * Logs that a message exhausted its retries and is being routed to the
     * dead-letter queue.
     *
     * @param routingKey the routing key of the failed message
     * @param error      the failure message
     */
    public static void routingToDlq(final String routingKey, final String error) {
        log.error("Retries exhausted, routing to DLQ: routingKey={}, error={}", routingKey, error);
    }

    private MessagingLog() {
        // Static utility — never instantiated.
    }

}
