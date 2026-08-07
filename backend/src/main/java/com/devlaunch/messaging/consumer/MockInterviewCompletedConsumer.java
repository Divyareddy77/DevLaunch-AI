package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.MessagingLog;
import com.devlaunch.messaging.event.MockInterviewCompletedEvent;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.util.EnumLabels;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the mock-interview-completed flow.
 * <p>
 * Reproduces the existing milestone notifications (completion, outstanding
 * score, new personal best, improved average, growing streak) from the
 * metrics carried by the event, persisting them through the shared
 * notification processor, and logs the speaking metrics for the dashboard
 * aggregation pipeline.
 * </p>
 * <p>
 * Boundary note: the interview session itself is persisted synchronously by
 * the submitting service (the history endpoint returns it immediately), and
 * the dashboard derives its mock-interview aggregates on read from that
 * source data — this consumer only generates the notifications.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class MockInterviewCompletedConsumer {

    /** Scores at or above this are celebrated as outstanding. */
    private static final int OUTSTANDING_SCORE = 90;

    /** Streaks of at least this many days are worth celebrating. */
    private static final int STREAK_NOTIFICATION_MIN = 2;

    private final NotificationEventProcessor processor;

    /**
     * Constructs the consumer with the shared notification processor.
     *
     * @param processor the shared notification persistence logic
     */
    public MockInterviewCompletedConsumer(final NotificationEventProcessor processor) {
        this.processor = processor;
    }

    /**
     * Persists the milestone notifications for the completed interview.
     *
     * @param event the interview completion event
     */
    @RabbitListener(queues = EventTopics.MOCK_INTERVIEW_COMPLETED_QUEUE)
    public void onMockInterviewCompleted(final MockInterviewCompletedEvent event) {
        MessagingLog.received(EventTopics.MOCK_INTERVIEW_COMPLETED_QUEUE,
                event.getClass().getSimpleName());
        MessagingLog.processingStarted(EventTopics.MOCK_INTERVIEW_COMPLETED_QUEUE,
                event.getClass().getSimpleName());

        final String typeLabel = EnumLabels.toLabel(event.interviewType());

        // Always notify the user that the interview completed.
        processor.process(new NotificationEvent(event.userId(), NotificationType.MOCK_INTERVIEW,
                "Interview completed",
                "Your " + typeLabel + " mock interview scored " + event.overallScore()
                        + "/100. Review the feedback to level up."));

        // Celebrate an outstanding performance.
        if (event.overallScore() >= OUTSTANDING_SCORE) {
            processor.process(new NotificationEvent(event.userId(), NotificationType.MOCK_INTERVIEW,
                    "Outstanding interview score",
                    "You scored " + event.overallScore()
                            + "/100 — an outstanding performance. Keep it up!"));
        }

        // Celebrate a new personal best, otherwise an improved average.
        if (event.overallScore() > event.previousMaxScore()) {
            processor.process(new NotificationEvent(event.userId(), NotificationType.MOCK_INTERVIEW,
                    "New highest score",
                    "New personal best! You scored " + event.overallScore()
                            + "/100 in your " + typeLabel + " interview."));
        } else {
            final double newAverage =
                    (event.previousSum() + event.overallScore())
                            / (double) (event.previousCount() + 1);
            if (newAverage > event.previousAverageScore()) {
                final double roundedAverage = Math.round(newAverage * 10.0) / 10.0;
                processor.process(new NotificationEvent(event.userId(),
                        NotificationType.MOCK_INTERVIEW,
                        "Average score improved",
                        "Your average interview score improved to " + roundedAverage
                                + "/100. Consistency pays off!"));
            }
        }

        // Celebrate a growing practice streak.
        if (event.newStreak() >= STREAK_NOTIFICATION_MIN
                && event.newStreak() > event.previousStreak()) {
            processor.process(new NotificationEvent(event.userId(), NotificationType.MOCK_INTERVIEW,
                    "Interview streak",
                    "You've practised on " + event.newStreak() + " consecutive day"
                            + (event.newStreak() == 1 ? "" : "s")
                            + " — consistency builds confidence!"));
        }

        MessagingLog.processingCompleted(EventTopics.MOCK_INTERVIEW_COMPLETED_QUEUE,
                event.getClass().getSimpleName(),
                "notifications persisted; metrics: overallScore=" + event.overallScore()
                        + ", confidence=" + event.confidenceScore()
                        + ", communication=" + event.communicationScore()
                        + ", fillerCount=" + event.fillerCount()
                        + ", speakingPace=" + event.speakingPace()
                        + ", durationSeconds=" + event.durationSeconds());
    }

}
