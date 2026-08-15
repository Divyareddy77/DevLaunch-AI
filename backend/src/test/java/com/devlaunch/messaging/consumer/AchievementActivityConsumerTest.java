package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.messaging.event.ActivityEvent;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.GamificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AchievementActivityConsumer}.
 * <p>
 * Verifies that an activity event is delegated to the gamification service
 * with the resolved user and that events for unknown users are dropped
 * without crashing the consumer.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class AchievementActivityConsumerTest {

    @Mock
    private GamificationService gamificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AchievementActivityConsumer consumer;

    @Test
    @DisplayName("delegates the activity event to the gamification service")
    void delegatesActivityToGamificationService() {
        final User user = User.builder().email("tester@example.com").build();
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        final ActivityEvent event = new ActivityEvent(7L, ActivityType.INTERVIEW_COMPLETED,
                82, LocalDateTime.now());

        consumer.onActivity(event);

        verify(gamificationService).recordActivity(user, ActivityType.INTERVIEW_COMPLETED, 82);
    }

    @Test
    @DisplayName("an event for an unknown user is dropped without processing")
    void eventForUnknownUserIsDropped() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        consumer.onActivity(new ActivityEvent(999L, ActivityType.GITHUB_CONNECTED,
                null, LocalDateTime.now()));

        verify(gamificationService, never()).recordActivity(any(User.class), any(), any());
    }

    @Test
    @DisplayName("an event without a user id is dropped without processing")
    void eventWithoutUserIdIsDropped() {
        consumer.onActivity(new ActivityEvent(null, ActivityType.STUDY_TASK_COMPLETED,
                3, LocalDateTime.now()));

        verify(gamificationService, never()).recordActivity(any(User.class), any(), any());
        verify(userRepository, never()).findById(eq(null));
    }

}
