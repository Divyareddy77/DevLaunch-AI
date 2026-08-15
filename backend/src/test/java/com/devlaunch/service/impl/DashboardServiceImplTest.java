package com.devlaunch.service.impl;

import com.devlaunch.dto.response.DashboardResponse;
import com.devlaunch.dto.response.GitHubProfileResponse;
import com.devlaunch.dto.response.LanguageStatisticsResponse;
import com.devlaunch.dto.response.LeetCodeProfileResponse;
import com.devlaunch.dto.response.ReadinessModuleResponse;
import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.ReadinessSnapshot;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.messaging.EventPublisher;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.ActivityEvent;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.InterviewScheduleRepository;
import com.devlaunch.repository.InterviewSessionRepository;
import com.devlaunch.repository.JobApplicationRepository;
import com.devlaunch.repository.ReadinessSnapshotRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.StudyPlannerRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.GitHubService;
import com.devlaunch.service.interfaces.LeetCodeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the enhanced placement readiness summary in
 * {@link DashboardServiceImpl}.
 * <p>
 * Verifies that the dashboard derives the readiness status, module
 * breakdown, strengths, improvement areas, personalized recommendations,
 * and progress tracking from real module data, and that readiness
 * snapshots plus milestone notifications are only produced when the
 * score actually changes.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    private static final String USER_EMAIL = "dev@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private StudyPlannerRepository studyPlannerRepository;

    @Mock
    private ResumeReviewRepository resumeReviewRepository;

    @Mock
    private InterviewSessionRepository interviewSessionRepository;

    @Mock
    private InterviewScheduleRepository interviewScheduleRepository;

    @Mock
    private ReadinessSnapshotRepository readinessSnapshotRepository;

    @Mock
    private GitHubService gitHubService;

    @Mock
    private LeetCodeService leetCodeService;

    @Mock
    private EventPublisher eventPublisher;

    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(
                userRepository, resumeRepository, jobApplicationRepository,
                studyPlannerRepository, resumeReviewRepository,
                interviewSessionRepository, interviewScheduleRepository,
                readinessSnapshotRepository, gitHubService, leetCodeService,
                eventPublisher);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USER_EMAIL, null, List.of()));
    }

    private User user() {
        final User user = User.builder().email(USER_EMAIL).build();
        user.setId(1L);
        return user;
    }

    private Resume completedResume(final User user) {
        return Resume.builder()
                .headline("Senior Developer")
                .summary("Experienced full-stack developer.")
                .linkedinUrl("https://linkedin.com/in/dev")
                .githubUrl("https://github.com/octocat")
                .portfolioUrl("https://dev.example.com")
                .user(user)
                .build();
    }

    private ResumeReview review(final int atsScore) {
        final ResumeReview review = ResumeReview.builder().atsScore(atsScore).build();
        review.setCreatedAt(LocalDateTime.now().minusDays(2));
        return review;
    }

    /**
     * Stubs a well-rounded profile: completed resume, active GitHub account
     * with a top language, two interviews (scores 80 and 70), two
     * applications (one interview), and three study tasks (two completed).
     * Computes to a readiness score of 81 → "Placement Ready".
     */
    private void stubWellRoundedProfile(final User user) {
        user.setGithubUsername("octocat");
        when(resumeRepository.findByUser(user)).thenReturn(List.of(completedResume(user)));
        when(jobApplicationRepository.findByUser(user)).thenReturn(List.of(
                JobApplication.builder().companyName("Acme").status(ApplicationStatus.APPLIED).build(),
                JobApplication.builder().companyName("Globex").status(ApplicationStatus.INTERVIEW).build()));
        when(studyPlannerRepository.findByUser(user)).thenReturn(List.of(
                StudyPlanner.builder().status(StudyStatus.COMPLETED).build(),
                StudyPlanner.builder().status(StudyStatus.COMPLETED).build(),
                StudyPlanner.builder().status(StudyStatus.PENDING).build()));
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user))
                .thenReturn(List.of(session(80), session(70)));
        when(gitHubService.getGitHubProfile("octocat"))
                .thenReturn(GitHubProfileResponse.builder().publicRepositories(28).build());
        when(gitHubService.getLanguageStatistics("octocat"))
                .thenReturn(List.of(LanguageStatisticsResponse.builder().language("Java").build()));
        when(resumeReviewRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(review(85)));
    }

    /**
     * Builds a completed interview session with the given overall score.
     */
    private InterviewSession session(final int overallScore) {
        return InterviewSession.builder()
                .overallScore(overallScore)
                .questionCount(5)
                .build();
    }

    @Test
    @DisplayName("dashboard returns the full readiness summary from real module data")
    void dashboardReturnsReadinessSummaryFromModuleData() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        stubWellRoundedProfile(user);
        when(readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());

        final DashboardResponse response = service.getDashboard();

        // Overall score and status (81 → Placement Ready)
        assertEquals(81, response.getPlacementReadiness());
        assertEquals("Placement Ready", response.getReadinessStatus());
        assertEquals(2, response.getTotalJobApplications());
        assertEquals(1, response.getInterviewApplications());

        // Mock interview widget reflects the interview history
        assertEquals(2, response.getMockInterviewCount());
        assertEquals(80, response.getMockInterviewLatestScore());
        assertEquals(75.0, response.getMockInterviewAverageScore());
        assertEquals(80, response.getMockInterviewBestScore());
        assertEquals(10, response.getMockInterviewTrend());

        // Module breakdown reflects real data
        assertEquals(7, response.getReadinessModules().size());
        assertEquals("85 / 100", moduleValue(response, "RESUME_ATS"));
        assertEquals("100%", moduleValue(response, "RESUME_COMPLETION"));
        assertEquals("2 Interviews", moduleValue(response, "MOCK_INTERVIEW"));
        assertEquals("28 Repositories", moduleValue(response, "GITHUB"));
        assertEquals("Not Connected", moduleValue(response, "LEETCODE"));
        assertEquals("67%", moduleValue(response, "STUDY_PLANNER"));
        assertEquals("2", moduleValue(response, "JOB_APPLICATIONS"));

        // Strengths / improvements / recommendations are data driven
        assertTrue(response.getReadinessStrengths().contains("Resume completed"));
        assertTrue(response.getReadinessStrengths().contains("Strong ATS score"));
        assertTrue(response.getReadinessStrengths().contains("Active GitHub profile"));
        assertTrue(response.getReadinessImprovements().contains("Connect your LeetCode account"));
        assertTrue(response.getReadinessImprovements().contains("Finish study planner tasks"));
        assertTrue(response.getReadinessRecommendations().contains("Solve five LeetCode problems."));
        assertTrue(response.getReadinessRecommendations().contains("Apply to three companies."));

        // Strongest / weakest / next goal
        assertEquals("Resume Completion", response.getReadinessStrongestArea());
        assertEquals("LeetCode", response.getReadinessWeakestArea());
        assertEquals("Connect your LeetCode account", response.getReadinessNextGoal());

        // First measurement: no previous data, but a snapshot is recorded and
        // the gamification activity feeds the achievement engine.
        assertNull(response.getReadinessPrevious());
        assertNull(response.getReadinessChange());
        assertNull(response.getReadinessUpdatedAt());
        verify(readinessSnapshotRepository).save(any(ReadinessSnapshot.class));
        verify(eventPublisher).publish(eq(EventTopics.ACHIEVEMENT_ACTIVITY_KEY),
                any(ActivityEvent.class));
    }

    @Test
    @DisplayName("empty profile shows needs-improvement status, defaults, and actionable insights")
    void emptyProfileProducesDefaultsAndInsights() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findByUser(user)).thenReturn(List.of());
        when(jobApplicationRepository.findByUser(user)).thenReturn(List.of());
        when(studyPlannerRepository.findByUser(user)).thenReturn(List.of());
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user)).thenReturn(List.of());
        when(resumeReviewRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());

        final DashboardResponse response = service.getDashboard();

        assertEquals(0, response.getPlacementReadiness());
        assertEquals("Needs Improvement", response.getReadinessStatus());
        assertEquals(0, response.getMockInterviewCount());
        assertEquals(null, response.getMockInterviewLatestScore());
        assertNull(response.getReadinessStrongestArea());
        assertFalse(response.getReadinessStrengths().contains("Resume completed"));
        assertTrue(response.getReadinessImprovements().contains("Run your first ATS resume review"));
        assertTrue(response.getReadinessImprovements().contains("Complete your first mock interview"));
        assertEquals(5, response.getReadinessRecommendations().size());
        assertEquals("Run your first ATS resume review", response.getReadinessNextGoal());
        assertEquals("Not Reviewed", moduleValue(response, "RESUME_ATS"));
        assertEquals("0 Interviews", moduleValue(response, "MOCK_INTERVIEW"));
    }

    @Test
    @DisplayName("a linked LeetCode account feeds live statistics into the dashboard")
    void linkedLeetCodeAccountFeedsLiveDashboardData() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findByUser(user)).thenReturn(List.of());
        when(jobApplicationRepository.findByUser(user)).thenReturn(List.of());
        when(studyPlannerRepository.findByUser(user)).thenReturn(List.of());
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user)).thenReturn(List.of());
        when(resumeReviewRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());

        user.setLeetcodeUsername("octocat");
        when(leetCodeService.getLeetCodeProfile("octocat"))
                .thenReturn(LeetCodeProfileResponse.builder()
                        .username("octocat")
                        .totalSolved(120)
                        .easySolved(70)
                        .mediumSolved(40)
                        .hardSolved(10)
                        .ranking(5000)
                        .acceptanceRate(67.5)
                        .build());

        final DashboardResponse response = service.getDashboard();

        assertEquals("octocat", response.getLeetcodeUsername());
        assertEquals(120, response.getLeetcodeSolved());
        assertEquals(70, response.getLeetcodeEasySolved());
        assertEquals(40, response.getLeetcodeMediumSolved());
        assertEquals(10, response.getLeetcodeHardSolved());
        assertEquals(5000, response.getLeetcodeRanking());
        assertEquals(67.5, response.getLeetcodeAcceptanceRate());
        assertEquals("120 Problems", moduleValue(response, "LEETCODE"));
        assertFalse(response.getReadinessImprovements().contains("Connect your LeetCode account"));
    }

    @Test
    @DisplayName("a linked GitHub account feeds follower and following counts into the dashboard")
    void linkedGitHubAccountFeedsFollowerData() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findByUser(user)).thenReturn(List.of());
        when(jobApplicationRepository.findByUser(user)).thenReturn(List.of());
        when(studyPlannerRepository.findByUser(user)).thenReturn(List.of());
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user)).thenReturn(List.of());
        when(resumeReviewRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());

        user.setGithubUsername("octocat");
        when(gitHubService.getGitHubProfile("octocat"))
                .thenReturn(GitHubProfileResponse.builder()
                        .publicRepositories(28)
                        .followers(500)
                        .following(120)
                        .build());
        when(gitHubService.getLanguageStatistics("octocat"))
                .thenReturn(List.of(LanguageStatisticsResponse.builder().language("Java").build()));

        final DashboardResponse response = service.getDashboard();

        assertEquals("octocat", response.getGithubUsername());
        assertEquals(28, response.getGithubRepositories());
        assertEquals(500, response.getGithubFollowers());
        assertEquals(120, response.getGithubFollowing());
        assertEquals("Java", response.getGithubTopLanguage());
    }

    @Test
    @DisplayName("reaching a higher readiness level creates a level-up notification")
    void levelUpCreatesNotification() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        stubWellRoundedProfile(user);

        final ReadinessSnapshot previous = ReadinessSnapshot.builder()
                .user(user).score(50).build();
        previous.setCreatedAt(LocalDateTime.now().minusDays(7));
        when(readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(previous));

        final ReadinessSnapshot saved = ReadinessSnapshot.builder().user(user).score(78).build();
        saved.setCreatedAt(LocalDateTime.now());
        when(readinessSnapshotRepository.saveAndFlush(any(ReadinessSnapshot.class)))
                .thenReturn(saved);

        final DashboardResponse response = service.getDashboard();

        assertEquals(50, response.getReadinessPrevious());
        assertEquals(31, response.getReadinessChange());
        assertEquals(81, response.getPlacementReadiness());
        assertEquals(saved.getCreatedAt(), response.getReadinessUpdatedAt());

        verify(eventPublisher).publish(eq(EventTopics.READINESS_MILESTONE_KEY),
                eq(new NotificationEvent(user.getId(), NotificationType.READINESS,
                        "Placement readiness level up",
                        "Your placement readiness has reached \"Placement Ready\" (81/100).")));
        verify(readinessSnapshotRepository).saveAndFlush(any(ReadinessSnapshot.class));
    }

    @Test
    @DisplayName("a significant improvement within the same level creates an improvement notification")
    void significantImprovementCreatesNotification() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        // Profile computing to 47: resume 100% (25) + one application (10) + 4/5 study tasks (12)
        user.setGithubUsername("octocat");
        when(resumeRepository.findByUser(user)).thenReturn(List.of(completedResume(user)));
        when(jobApplicationRepository.findByUser(user)).thenReturn(List.of(
                JobApplication.builder().companyName("Acme").status(ApplicationStatus.APPLIED).build()));
        when(studyPlannerRepository.findByUser(user)).thenReturn(List.of(
                StudyPlanner.builder().status(StudyStatus.COMPLETED).build(),
                StudyPlanner.builder().status(StudyStatus.COMPLETED).build(),
                StudyPlanner.builder().status(StudyStatus.COMPLETED).build(),
                StudyPlanner.builder().status(StudyStatus.COMPLETED).build(),
                StudyPlanner.builder().status(StudyStatus.PENDING).build()));
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user)).thenReturn(List.of());
        when(resumeReviewRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(gitHubService.getGitHubProfile("octocat"))
                .thenReturn(GitHubProfileResponse.builder().publicRepositories(0).build());
        when(gitHubService.getLanguageStatistics("octocat")).thenReturn(List.of());

        final ReadinessSnapshot previous = ReadinessSnapshot.builder()
                .user(user).score(37).build();
        previous.setCreatedAt(LocalDateTime.now().minusDays(3));
        when(readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(previous));

        final ReadinessSnapshot saved = ReadinessSnapshot.builder().user(user).score(47).build();
        saved.setCreatedAt(LocalDateTime.now());
        when(readinessSnapshotRepository.saveAndFlush(any(ReadinessSnapshot.class)))
                .thenReturn(saved);

        final DashboardResponse response = service.getDashboard();

        assertEquals(47, response.getPlacementReadiness());
        assertEquals("Needs Improvement", response.getReadinessStatus());
        assertEquals(37, response.getReadinessPrevious());
        assertEquals(10, response.getReadinessChange());

        verify(eventPublisher).publish(eq(EventTopics.READINESS_MILESTONE_KEY),
                eq(new NotificationEvent(user.getId(), NotificationType.READINESS,
                        "Placement readiness improved",
                        "Your placement readiness improved by 10 points to "
                                + "47/100. Keep it up!")));
        verify(readinessSnapshotRepository).saveAndFlush(any(ReadinessSnapshot.class));
    }

    @Test
    @DisplayName("an unchanged score keeps the existing snapshot and sends no notification")
    void unchangedScoreSkipsNotificationAndSnapshot() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        // Profile computing to 50: resume 100% (25) + one application (10) + 2/2 study tasks (15)
        user.setGithubUsername("octocat");
        when(resumeRepository.findByUser(user)).thenReturn(List.of(completedResume(user)));
        when(jobApplicationRepository.findByUser(user)).thenReturn(List.of(
                JobApplication.builder().companyName("Acme").status(ApplicationStatus.APPLIED).build()));
        when(studyPlannerRepository.findByUser(user)).thenReturn(List.of(
                StudyPlanner.builder().status(StudyStatus.COMPLETED).build(),
                StudyPlanner.builder().status(StudyStatus.COMPLETED).build()));
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user)).thenReturn(List.of());
        when(resumeReviewRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(gitHubService.getGitHubProfile("octocat"))
                .thenReturn(GitHubProfileResponse.builder().publicRepositories(0).build());
        when(gitHubService.getLanguageStatistics("octocat")).thenReturn(List.of());

        final ReadinessSnapshot previous = ReadinessSnapshot.builder()
                .user(user).score(50).build();
        previous.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(previous));

        final DashboardResponse response = service.getDashboard();

        assertEquals(50, response.getPlacementReadiness());
        assertEquals(50, response.getReadinessPrevious());
        assertEquals(0, response.getReadinessChange());
        assertEquals(previous.getCreatedAt(), response.getReadinessUpdatedAt());

        verify(eventPublisher, never()).publish(any(), any());
        verify(readinessSnapshotRepository, never()).save(any(ReadinessSnapshot.class));
        verify(readinessSnapshotRepository, never()).saveAndFlush(any(ReadinessSnapshot.class));
    }

    /**
     * Looks up the display value of a module by key.
     */
    private String moduleValue(final DashboardResponse response, final String key) {
        return response.getReadinessModules().stream()
                .filter(module -> key.equals(module.getKey()))
                .findFirst()
                .map(ReadinessModuleResponse::getValue)
                .orElseThrow();
    }

}
