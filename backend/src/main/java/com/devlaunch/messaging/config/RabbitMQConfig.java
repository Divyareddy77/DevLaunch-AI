package com.devlaunch.messaging.config;

import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.MessagingLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.support.RetryTemplate;

/**
 * Declares the DevLaunch RabbitMQ topology and listener infrastructure.
 * <p>
 * All events flow through one durable topic exchange
 * ({@link EventTopics#EXCHANGE}). Each flow has its own durable queue bound
 * with its own routing key and exactly one consumer. Every queue points at
 * the dead-letter fan-out exchange so a message that exhausts its retry
 * budget is parked on the dead-letter queue instead of being redelivered
 * forever. Messages are serialized as JSON (via Spring's {@link ObjectMapper})
 * and listeners retry transient failures with exponential backoff before
 * rejecting to the dead-letter queue.
 * </p>
 *
 * @author DevLaunch
 */
@Configuration
public class RabbitMQConfig {

    /**
     * The durable topic exchange all DevLaunch events are published to.
     */
    @Bean
    public TopicExchange devlaunchExchange() {
        return new TopicExchange(EventTopics.EXCHANGE, true, false);
    }

    /**
     * Fan-out exchange collecting rejected messages from every work queue.
     */
    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(EventTopics.DLX, true, false);
    }

    /**
     * Durable queue where messages that exhausted their retries end up.
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(EventTopics.DLQ).build();
    }

    /**
     * Binds every rejected message to the dead-letter queue (fan-out
     * ignores routing keys, so this catches all flows).
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    @Bean
    public Queue forgotPasswordEmailQueue() {
        return durableWorkQueue(EventTopics.FORGOT_PASSWORD_EMAIL_QUEUE);
    }

    @Bean
    public Binding forgotPasswordEmailBinding() {
        return bindWorkQueue(forgotPasswordEmailQueue(), devlaunchExchange(),
                EventTopics.FORGOT_PASSWORD_EMAIL_KEY);
    }

    @Bean
    public Queue passwordResetSuccessQueue() {
        return durableWorkQueue(EventTopics.PASSWORD_RESET_SUCCESS_QUEUE);
    }

    @Bean
    public Binding passwordResetSuccessBinding() {
        return bindWorkQueue(passwordResetSuccessQueue(), devlaunchExchange(),
                EventTopics.PASSWORD_RESET_SUCCESS_KEY);
    }

    @Bean
    public Queue resumeReviewCompletedQueue() {
        return durableWorkQueue(EventTopics.RESUME_REVIEW_COMPLETED_QUEUE);
    }

    @Bean
    public Binding resumeReviewCompletedBinding() {
        return bindWorkQueue(resumeReviewCompletedQueue(), devlaunchExchange(),
                EventTopics.RESUME_REVIEW_COMPLETED_KEY);
    }

    @Bean
    public Queue interviewReminderQueue() {
        return durableWorkQueue(EventTopics.INTERVIEW_REMINDER_QUEUE);
    }

    @Bean
    public Binding interviewReminderBinding() {
        return bindWorkQueue(interviewReminderQueue(), devlaunchExchange(),
                EventTopics.INTERVIEW_REMINDER_KEY);
    }

    @Bean
    public Queue studyReminderQueue() {
        return durableWorkQueue(EventTopics.STUDY_REMINDER_QUEUE);
    }

    @Bean
    public Binding studyReminderBinding() {
        return bindWorkQueue(studyReminderQueue(), devlaunchExchange(),
                EventTopics.STUDY_REMINDER_KEY);
    }

    @Bean
    public Queue jobApplicationReminderQueue() {
        return durableWorkQueue(EventTopics.JOB_APPLICATION_REMINDER_QUEUE);
    }

    @Bean
    public Binding jobApplicationReminderBinding() {
        return bindWorkQueue(jobApplicationReminderQueue(), devlaunchExchange(),
                EventTopics.JOB_APPLICATION_REMINDER_KEY);
    }

    @Bean
    public Queue mockInterviewCompletedQueue() {
        return durableWorkQueue(EventTopics.MOCK_INTERVIEW_COMPLETED_QUEUE);
    }

    @Bean
    public Binding mockInterviewCompletedBinding() {
        return bindWorkQueue(mockInterviewCompletedQueue(), devlaunchExchange(),
                EventTopics.MOCK_INTERVIEW_COMPLETED_KEY);
    }

    @Bean
    public Queue githubConnectedQueue() {
        return durableWorkQueue(EventTopics.GITHUB_CONNECTED_QUEUE);
    }

    @Bean
    public Binding githubConnectedBinding() {
        return bindWorkQueue(githubConnectedQueue(), devlaunchExchange(),
                EventTopics.GITHUB_CONNECTED_KEY);
    }

    @Bean
    public Queue leetcodeConnectedQueue() {
        return durableWorkQueue(EventTopics.LEETCODE_CONNECTED_QUEUE);
    }

    @Bean
    public Binding leetcodeConnectedBinding() {
        return bindWorkQueue(leetcodeConnectedQueue(), devlaunchExchange(),
                EventTopics.LEETCODE_CONNECTED_KEY);
    }

    @Bean
    public Queue readinessMilestoneQueue() {
        return durableWorkQueue(EventTopics.READINESS_MILESTONE_QUEUE);
    }

    @Bean
    public Binding readinessMilestoneBinding() {
        return bindWorkQueue(readinessMilestoneQueue(), devlaunchExchange(),
                EventTopics.READINESS_MILESTONE_KEY);
    }

    @Bean
    public Queue achievementActivityQueue() {
        return durableWorkQueue(EventTopics.ACHIEVEMENT_ACTIVITY_QUEUE);
    }

    @Bean
    public Binding achievementActivityBinding() {
        return bindWorkQueue(achievementActivityQueue(), devlaunchExchange(),
                EventTopics.ACHIEVEMENT_ACTIVITY_KEY);
    }

    /**
     * JSON message converter used by both the publisher and the consumers.
     * <p>
     * Built on Spring's {@link ObjectMapper} so date/time types and the
     * project's Jackson configuration are honored end to end.
     * </p>
     */
    @Bean
    public MessageConverter jsonMessageConverter(final ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Listener container factory shared by all {@code @RabbitListener}
     * consumers: JSON conversion plus a stateless retry interceptor that
     * retries transient failures up to three times with exponential
     * backoff and rejects the message to the dead-letter queue afterwards.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            final ConnectionFactory connectionFactory,
            final MessageConverter jsonMessageConverter) {
        final SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);

        // Retry template: 3 attempts with exponential backoff (1s → 2s → 5s)
        // and a listener that logs every retry attempt.
        final RetryTemplate retryTemplate = RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(1_000L, 2.0, 5_000L)
                .withListener(new RetryListenerSupport() {
                    @Override
                    public <T, E extends Throwable> void onError(final RetryContext context,
                                                                 final RetryCallback<T, E> callback,
                                                                 final Throwable throwable) {
                        MessagingLog.retryAttempt(context.getRetryCount(), throwable.getMessage());
                    }
                })
                .build();

        final RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate)
                .recoverer(new DlqLoggingRecoverer())
                .build();
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    /**
     * Creates a durable work queue that dead-letters rejected messages.
     *
     * @param name the queue name
     * @return the durable queue with the dead-letter exchange configured
     */
    private Queue durableWorkQueue(final String name) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(EventTopics.DLX)
                .build();
    }

    /**
     * Binds a work queue to the topic exchange with its routing key.
     *
     * @param queue      the queue to bind
     * @param exchange   the exchange to bind to
     * @param routingKey the routing key selecting this queue
     * @return the binding
     */
    private Binding bindWorkQueue(final Queue queue, final TopicExchange exchange,
                                  final String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    /**
     * Message recoverer that logs the dead-letter routing before rejecting
     * the message so RabbitMQ moves it to the dead-letter queue.
     */
    private static final class DlqLoggingRecoverer implements MessageRecoverer {

        private final RejectAndDontRequeueRecoverer delegate = new RejectAndDontRequeueRecoverer();

        @Override
        public void recover(final Message message, final Throwable cause) {
            final String routingKey = message.getMessageProperties() == null
                    ? "unknown" : String.valueOf(message.getMessageProperties().getReceivedRoutingKey());
            MessagingLog.routingToDlq(routingKey, cause.getMessage());
            delegate.recover(message, cause);
        }

    }

}
