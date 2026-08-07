package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.messaging.event.MockInterviewCompletedEvent;
import com.devlaunch.messaging.event.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link MockInterviewCompletedConsumer}.
 * <p>
 * Verifies that the consumer reproduces the existing milestone notifications
 * (completion, outstanding score, new personal best, improved average,
 * growing streak) from the metrics carried by the event.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class MockInterviewCompletedConsumerTest {

    @Mock
    private NotificationEventProcessor processor;

    private MockInterviewCompletedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new MockInterviewCompletedConsumer(processor);
    }

    private MockInterviewCompletedEvent event(final int overallScore,
                                              final int previousMaxScore,
                                              final int previousStreak,
                                              final int newStreak,
                                              final double previousAverageScore,
                                              final long previousCount,
                                              final long previousSum) {
        return new MockInterviewCompletedEvent(1L, "session-1", InterviewType.JAVA,
                overallScore, 80, 85, 3, 120.5, 600,
                previousMaxScore, previousAverageScore, previousStreak,
                previousCount, previousSum, newStreak);
    }

    @Test
    @DisplayName("persists completion, outstanding, new-best, and streak notifications")
    void persistsAllMilestones() {
        consumer.onMockInterviewCompleted(event(92, 80, 0, 3, 70.0, 2, 140));

        final ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor, times(4)).process(captor.capture());

        final List<NotificationEvent> events = captor.getAllValues();
        final List<String> titles = events.stream().map(NotificationEvent::title).toList();
        assertTrue(titles.contains("Interview completed"));
        assertTrue(titles.contains("Outstanding interview score"));
        assertTrue(titles.contains("New highest score"));
        assertTrue(titles.contains("Interview streak"));
        assertTrue(events.stream().allMatch(e -> NotificationType.MOCK_INTERVIEW.equals(e.type())));
        assertTrue(events.stream().allMatch(e -> e.userId() == 1L));
    }

    @Test
    @DisplayName("persists only the completion notification when no milestone applies")
    void persistsOnlyCompletionWhenNoMilestoneApplies() {
        // 75 is not outstanding, below the previous best (90), the average
        // drops ((160 + 75) / 3 = 78.3 < 80), and the streak does not grow.
        consumer.onMockInterviewCompleted(event(75, 90, 1, 1, 80.0, 2, 160));

        final ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor).process(captor.capture());
        assertEquals("Interview completed", captor.getValue().title());
        assertEquals(1, captor.getAllValues().size());
    }

    @Test
    @DisplayName("persists the improved-average notification instead of a new best")
    void persistsImprovedAverage() {
        // Below the previous best (90), but the average rises to 65 > 60.
        consumer.onMockInterviewCompleted(event(75, 90, 1, 1, 60.0, 2, 120));

        final ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor, times(2)).process(captor.capture());

        final List<String> titles = captor.getAllValues().stream()
                .map(NotificationEvent::title).toList();
        assertTrue(titles.contains("Interview completed"));
        assertTrue(titles.contains("Average score improved"));
    }

}
