package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.messaging.event.StudyMilestone;
import com.devlaunch.messaging.event.StudyTaskEvent;
import com.devlaunch.messaging.event.StudyTaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link StudyReminderConsumer}.
 * <p>
 * Verifies that the consumer composes and persists the notifications for
 * created, completed (per milestone), and overdue study tasks.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class StudyReminderConsumerTest {

    @Mock
    private NotificationEventProcessor processor;

    private StudyReminderConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new StudyReminderConsumer(processor);
    }

    private StudyTaskEvent event(final StudyTaskType type, final StudyMilestone milestone,
                                 final Integer dayCount, final String message) {
        return new StudyTaskEvent(1L, 7L, "DSA practice",
                LocalDate.of(2026, 8, 7), type, milestone, dayCount, message);
    }

    @Test
    @DisplayName("a created task persists the scheduling notification")
    void createdTaskPersistsSchedulingNotification() {
        consumer.onStudyTask(event(StudyTaskType.CREATED, null, null, null));

        final ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor).process(captor.capture());
        assertEquals("Study task scheduled", captor.getValue().title());
        assertEquals(NotificationType.STUDY, captor.getValue().type());
        assertTrue(captor.getValue().message().contains("DSA practice"));
        assertTrue(captor.getValue().message().contains("2026-08-07"));
    }

    @Test
    @DisplayName("a completed task with the daily milestone persists the daily goal notification")
    void completedDailyMilestonePersistsDailyGoal() {
        consumer.onStudyTask(event(StudyTaskType.COMPLETED, StudyMilestone.DAILY, null, null));

        final ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor).process(captor.capture());
        assertEquals("Daily goal completed", captor.getValue().title());
        assertTrue(captor.getValue().message().contains("DSA practice"));
    }

    @Test
    @DisplayName("a completed task with the weekly milestone persists the weekly target notification")
    void completedWeeklyMilestonePersistsWeeklyTarget() {
        consumer.onStudyTask(event(StudyTaskType.COMPLETED, StudyMilestone.WEEKLY, 5, null));

        final ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor).process(captor.capture());
        assertEquals("Weekly target achieved", captor.getValue().title());
        assertTrue(captor.getValue().message().contains("5 days"));
    }

    @Test
    @DisplayName("a completed task with the streak milestone persists the streak notification")
    void completedStreakMilestonePersistsStreak() {
        consumer.onStudyTask(event(StudyTaskType.COMPLETED, StudyMilestone.STREAK, 7, null));

        final ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor).process(captor.capture());
        assertEquals("Study streak milestone", captor.getValue().title());
        assertTrue(captor.getValue().message().contains("7-day"));
    }

    @Test
    @DisplayName("an overdue task persists the scheduler-owned message unchanged")
    void overdueTaskPersistsSchedulerMessage() {
        final String message = "Task \"DSA practice\" was due on 2026-08-05 "
                + "and is still pending. Try to complete it today!";
        consumer.onStudyTask(event(StudyTaskType.OVERDUE, null, null, message));

        final ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor).process(captor.capture());
        assertEquals("Study task overdue", captor.getValue().title());
        assertEquals(message, captor.getValue().message());
    }

}
