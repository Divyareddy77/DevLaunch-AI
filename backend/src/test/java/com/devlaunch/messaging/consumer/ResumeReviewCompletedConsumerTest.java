package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.messaging.event.ResumeReviewedEvent;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ResumeReviewCompletedConsumer}.
 * <p>
 * Verifies that the consumer stores the review history row consumed by the
 * admin module and persists the completion notification — plus the
 * score-improvement notification when the review beats the previous best —
 * through the shared notification processor, and that events for unknown
 * users or resumes are dropped without side effects.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class ResumeReviewCompletedConsumerTest {

    private static final long USER_ID = 1L;
    private static final long RESUME_ID = 10L;

    @Mock
    private NotificationEventProcessor processor;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeReviewRepository resumeReviewRepository;

    private ResumeReviewCompletedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ResumeReviewCompletedConsumer(
                processor, userRepository, resumeRepository, resumeReviewRepository);
    }

    private User user() {
        final User user = User.builder().email("dev@example.com").build();
        user.setId(USER_ID);
        return user;
    }

    private Resume resume(final User user) {
        final Resume resume = Resume.builder()
                .headline("Senior Developer")
                .summary("Experienced full-stack developer.")
                .user(user)
                .build();
        resume.setId(RESUME_ID);
        return resume;
    }

    @Test
    @DisplayName("stores the review history and persists completion and improvement notifications")
    void storesHistoryAndPersistsNotifications() {
        final User user = user();
        final Resume resume = resume(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(RESUME_ID)).thenReturn(Optional.of(resume));

        consumer.onResumeReviewed(new ResumeReviewedEvent(
                USER_ID, RESUME_ID, "Java Developer", 78, 65, 70));

        final ArgumentCaptor<ResumeReview> reviewCaptor =
                ArgumentCaptor.forClass(ResumeReview.class);
        verify(resumeReviewRepository).save(reviewCaptor.capture());
        assertEquals(RESUME_ID, reviewCaptor.getValue().getResume().getId());
        assertEquals("Java Developer", reviewCaptor.getValue().getTargetRole());
        assertEquals(78, reviewCaptor.getValue().getResumeScore());
        assertEquals(65, reviewCaptor.getValue().getAtsScore());

        final ArgumentCaptor<NotificationEvent> eventCaptor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor, times(2)).process(eventCaptor.capture());
        final List<NotificationEvent> events = eventCaptor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> "ATS Resume Review completed".equals(e.title())));
        assertTrue(events.stream().anyMatch(e -> "Resume score improved".equals(e.title())));
        assertTrue(events.stream().allMatch(e -> NotificationType.RESUME_REVIEW.equals(e.type())));
    }

    @Test
    @DisplayName("persists only the completion notification when the score did not rise")
    void skipsImprovementWhenScoreDidNotRise() {
        final User user = user();
        final Resume resume = resume(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(RESUME_ID)).thenReturn(Optional.of(resume));

        consumer.onResumeReviewed(new ResumeReviewedEvent(
                USER_ID, RESUME_ID, "Java Developer", 70, 60, 80));

        final ArgumentCaptor<NotificationEvent> eventCaptor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(processor).process(eventCaptor.capture());
        assertEquals("ATS Resume Review completed", eventCaptor.getValue().title());
        verify(resumeReviewRepository).save(any(ResumeReview.class));
    }

    @Test
    @DisplayName("an event for an unknown user is dropped without side effects")
    void unknownUserIsDropped() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        consumer.onResumeReviewed(new ResumeReviewedEvent(
                999L, RESUME_ID, "Java Developer", 78, 65, 70));

        verifyNoInteractions(resumeReviewRepository);
        verifyNoInteractions(processor);
    }

    @Test
    @DisplayName("an event for an unknown resume is dropped without side effects")
    void unknownResumeIsDropped() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(resumeRepository.findById(RESUME_ID)).thenReturn(Optional.empty());

        consumer.onResumeReviewed(new ResumeReviewedEvent(
                USER_ID, RESUME_ID, "Java Developer", 78, 65, 70));

        verify(resumeReviewRepository, never()).save(any(ResumeReview.class));
        verifyNoInteractions(processor);
    }

}
