package com.devlaunch.service.impl;

import com.devlaunch.dto.response.AchievementSummaryResponse;
import com.devlaunch.entity.AchievementDefinition;
import com.devlaunch.entity.User;
import com.devlaunch.entity.UserAchievement;
import com.devlaunch.entity.XpHistory;
import com.devlaunch.entity.enums.AchievementCategory;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.mapper.GamificationMapper;
import com.devlaunch.messaging.consumer.NotificationEventProcessor;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.AchievementDefinitionRepository;
import com.devlaunch.repository.InterviewSessionRepository;
import com.devlaunch.repository.JobApplicationRepository;
import com.devlaunch.repository.ReadinessSnapshotRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.StudyPlannerRepository;
import com.devlaunch.repository.UserAchievementRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.repository.XpHistoryRepository;
import com.devlaunch.service.interfaces.GitHubService;
import com.devlaunch.service.interfaces.LeetCodeService;
import com.devlaunch.service.interfaces.StudyPlannerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GamificationServiceImpl}.
 * <p>
 * Verifies the XP calculation per activity, the automatic achievement
 * evaluation and unlock (with the badge XP reward), the duplicate
 * prevention on re-delivered events, the level-up notification, and the
 * cached summary read.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class GamificationServiceImplTest {

    private static final String USER_EMAIL = "gamification-tester@example.com";

    @Mock
    private AchievementDefinitionRepository definitionRepository;
    @Mock
    private UserAchievementRepository userAchievementRepository;
    @Mock
    private XpHistoryRepository xpHistoryRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private InterviewSessionRepository interviewSessionRepository;
    @Mock
    private StudyPlannerRepository studyPlannerRepository;
    @Mock
    private ResumeReviewRepository resumeReviewRepository;
    @Mock
    private ReadinessSnapshotRepository readinessSnapshotRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GitHubService gitHubService;
    @Mock
    private LeetCodeService leetCodeService;
    @Mock
    private StudyPlannerService studyPlannerService;
    @Mock
    private GamificationMapper achievementMapper;
    @Mock
    private NotificationEventProcessor notificationProcessor;

    private GamificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GamificationServiceImpl(
                definitionRepository, userAchievementRepository, xpHistoryRepository,
                resumeRepository, jobApplicationRepository, interviewSessionRepository,
                studyPlannerRepository, resumeReviewRepository, readinessSnapshotRepository,
                userRepository, gitHubService, leetCodeService, studyPlannerService,
                new LevelService(), achievementMapper, notificationProcessor);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User user() {
        final User user = User.builder().email(USER_EMAIL).build();
        user.setId(1L);
        return user;
    }

    private AchievementDefinition definition(final String code, final String title,
                                             final int xpReward, final ActivityType type,
                                             final int target) {
        return AchievementDefinition.builder()
                .code(code)
                .category(AchievementCategory.INTERVIEW)
                .title(title)
                .description("Test badge")
                .icon("🎖️")
                .color("#8b5cf6")
                .xpReward(xpReward)
                .activityType(type)
                .targetValue(target)
                .sortOrder(1)
                .build();
    }

    @Test
    @DisplayName("a completed interview awards activity XP and unlocks Interview Beginner with its reward")
    void interviewActivityAwardsXpAndUnlocksBeginner() {
        final User user = user();
        final AchievementDefinition beginner = definition("INTERVIEW_BEGINNER",
                "Interview Beginner", 75, ActivityType.INTERVIEW_COMPLETED, 1);
        when(definitionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(beginner));
        when(userAchievementRepository.findByUserOrderByUnlockedAtDesc(user)).thenReturn(List.of());
        when(xpHistoryRepository.sumByUser(user)).thenReturn(0L, 115L);
        when(interviewSessionRepository.countByUser(user)).thenReturn(1L);
        when(resumeRepository.countByUser(user)).thenReturn(0L);
        when(jobApplicationRepository.countByUser(user)).thenReturn(0L);
        when(studyPlannerRepository.countByUserAndStatus(user, StudyStatus.COMPLETED))
                .thenReturn(0L);
        when(interviewSessionRepository.averageScoreByUser(user)).thenReturn(0.0);

        service.recordActivity(user, ActivityType.INTERVIEW_COMPLETED, 78);

        // Activity XP (+40) and the badge reward (+75) are both recorded.
        final ArgumentCaptor<XpHistory> xpCaptor = ArgumentCaptor.forClass(XpHistory.class);
        verify(xpHistoryRepository, times(2)).save(xpCaptor.capture());
        assertEquals(40, xpCaptor.getAllValues().get(0).getAmount());
        assertEquals(75, xpCaptor.getAllValues().get(1).getAmount());

        verify(userAchievementRepository).saveAndFlush(any(UserAchievement.class));

        // Two notifications: the achievement unlock and the level-up (115 XP
        // crosses the Level 2 boundary at 100 XP).
        final ArgumentCaptor<NotificationEvent> notifCaptor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationProcessor, times(2)).process(notifCaptor.capture());
        final NotificationEvent unlock = notifCaptor.getAllValues().stream()
                .filter(event -> "Achievement Unlocked".equals(event.title()))
                .findFirst()
                .orElseThrow();
        assertTrue(unlock.message().contains("Interview Beginner"));
        assertTrue(unlock.message().contains("+75 XP"));
        assertTrue(notifCaptor.getAllValues().stream()
                .anyMatch(event -> "Level Up!".equals(event.title())));
    }

    @Test
    @DisplayName("an ATS review unlocks the ATS Expert badge but not the higher ATS Master")
    void atsReviewUnlocksOnlyMatchingThresholds() {
        final User user = user();
        final AchievementDefinition expert = definition("ATS_EXPERT", "ATS Expert", 150,
                ActivityType.RESUME_REVIEWED, 80);
        final AchievementDefinition master = definition("ATS_MASTER", "ATS Master", 250,
                ActivityType.RESUME_REVIEWED, 90);
        when(definitionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(expert, master));
        when(userAchievementRepository.findByUserOrderByUnlockedAtDesc(user)).thenReturn(List.of());
        when(xpHistoryRepository.sumByUser(user)).thenReturn(0L, 200L);
        when(resumeRepository.countByUser(user)).thenReturn(1L);
        when(jobApplicationRepository.countByUser(user)).thenReturn(0L);
        when(interviewSessionRepository.countByUser(user)).thenReturn(0L);
        when(studyPlannerRepository.countByUserAndStatus(user, StudyStatus.COMPLETED))
                .thenReturn(0L);
        when(interviewSessionRepository.averageScoreByUser(user)).thenReturn(0.0);

        service.recordActivity(user, ActivityType.RESUME_REVIEWED, 85);

        // Only the expert badge (target 80) unlocks for a score of 85.
        verify(userAchievementRepository, times(1)).saveAndFlush(any(UserAchievement.class));
        verify(xpHistoryRepository, times(2)).save(any(XpHistory.class)); // +50 review XP, +150 badge
    }

    @Test
    @DisplayName("a re-delivered activity never unlocks a badge twice")
    void duplicateActivityDoesNotUnlockTwice() {
        final User user = user();
        final AchievementDefinition beginner = definition("INTERVIEW_BEGINNER",
                "Interview Beginner", 75, ActivityType.INTERVIEW_COMPLETED, 1);
        when(definitionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(beginner));
        when(xpHistoryRepository.sumByUser(user)).thenReturn(0L, 115L);
        when(interviewSessionRepository.countByUser(user)).thenReturn(1L);
        when(resumeRepository.countByUser(user)).thenReturn(0L);
        when(jobApplicationRepository.countByUser(user)).thenReturn(0L);
        when(studyPlannerRepository.countByUserAndStatus(user, StudyStatus.COMPLETED))
                .thenReturn(0L);
        when(interviewSessionRepository.averageScoreByUser(user)).thenReturn(0.0);

        // First delivery: nothing unlocked yet.
        when(userAchievementRepository.findByUserOrderByUnlockedAtDesc(user))
                .thenReturn(List.of());
        service.recordActivity(user, ActivityType.INTERVIEW_COMPLETED, 78);

        // Second delivery (e.g. broker redelivery): the badge is already unlocked.
        final UserAchievement unlocked = UserAchievement.builder()
                .user(user).achievementDefinition(beginner)
                .unlockedAt(LocalDateTime.now()).build();
        when(userAchievementRepository.findByUserOrderByUnlockedAtDesc(user))
                .thenReturn(List.of(unlocked));
        service.recordActivity(user, ActivityType.INTERVIEW_COMPLETED, 78);

        verify(userAchievementRepository, times(1)).saveAndFlush(any(UserAchievement.class));
    }

    @Test
    @DisplayName("crossing a level boundary raises a level-up notification")
    void levelUpRaisesNotification() {
        final User user = user();
        when(definitionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        when(userAchievementRepository.findByUserOrderByUnlockedAtDesc(user)).thenReturn(List.of());
        // 90 XP before the interview; the +40 activity XP crosses level 2 at 100.
        when(xpHistoryRepository.sumByUser(user)).thenReturn(90L, 130L);
        when(interviewSessionRepository.countByUser(user)).thenReturn(1L);
        when(resumeRepository.countByUser(user)).thenReturn(0L);
        when(jobApplicationRepository.countByUser(user)).thenReturn(0L);
        when(studyPlannerRepository.countByUserAndStatus(user, StudyStatus.COMPLETED))
                .thenReturn(0L);
        when(interviewSessionRepository.averageScoreByUser(user)).thenReturn(0.0);

        service.recordActivity(user, ActivityType.INTERVIEW_COMPLETED, 70);

        final ArgumentCaptor<NotificationEvent> notifCaptor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationProcessor).process(notifCaptor.capture());
        assertEquals("Level Up!", notifCaptor.getValue().title());
        assertTrue(notifCaptor.getValue().message().contains("Level 2"));
    }

    @Test
    @DisplayName("no notification is raised when the level does not change")
    void noLevelUpNotificationWithoutLevelChange() {
        final User user = user();
        when(definitionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        when(userAchievementRepository.findByUserOrderByUnlockedAtDesc(user)).thenReturn(List.of());
        when(xpHistoryRepository.sumByUser(user)).thenReturn(0L, 40L);
        when(interviewSessionRepository.countByUser(user)).thenReturn(1L);
        when(resumeRepository.countByUser(user)).thenReturn(0L);
        when(jobApplicationRepository.countByUser(user)).thenReturn(0L);
        when(studyPlannerRepository.countByUserAndStatus(user, StudyStatus.COMPLETED))
                .thenReturn(0L);
        when(interviewSessionRepository.averageScoreByUser(user)).thenReturn(0.0);

        service.recordActivity(user, ActivityType.INTERVIEW_COMPLETED, 70);

        verify(notificationProcessor, never()).process(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("the summary aggregates level, XP, and badge completion")
    void summaryAggregatesLevelXpAndCompletion() {
        final User user = user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USER_EMAIL, null, List.of()));
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        when(xpHistoryRepository.sumByUser(user)).thenReturn(260L);

        final AchievementDefinition resumeExplorer = AchievementDefinition.builder()
                .code("RESUME_EXPLORER").category(AchievementCategory.RESUME)
                .title("Resume Explorer").xpReward(100).targetValue(1).build();
        final AchievementDefinition atsExpert = AchievementDefinition.builder()
                .code("ATS_EXPERT").category(AchievementCategory.RESUME)
                .title("ATS Expert").xpReward(150).targetValue(80).build();
        when(definitionRepository.findAllByOrderBySortOrderAsc())
                .thenReturn(List.of(resumeExplorer, atsExpert));

        final UserAchievement unlocked = UserAchievement.builder()
                .user(user).achievementDefinition(resumeExplorer)
                .unlockedAt(LocalDateTime.of(2026, 8, 7, 10, 0)).build();
        when(userAchievementRepository.findByUserOrderByUnlockedAtDesc(user))
                .thenReturn(List.of(unlocked));

        final AchievementSummaryResponse summary = service.getSummary();

        assertEquals(3, summary.getLevel());
        assertEquals("Achiever", summary.getLevelTitle());
        assertEquals(260L, summary.getTotalXp());
        assertEquals(2, summary.getTotalAchievements());
        assertEquals(1, summary.getUnlockedCount());
        assertEquals(1, summary.getLockedCount());
        assertEquals(50, summary.getCompletionPercent());
        assertEquals(1, summary.getRecentUnlocks().size());
    }

}
