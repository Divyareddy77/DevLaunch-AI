package com.devlaunch.messaging;

import com.devlaunch.entity.AchievementDefinition;
import com.devlaunch.entity.Notification;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.AchievementCategory;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.messaging.event.ActivityEvent;
import com.devlaunch.messaging.event.ForgotPasswordEmailEvent;
import com.devlaunch.messaging.event.MockInterviewCompletedEvent;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.messaging.event.ResumeReviewedEvent;
import com.devlaunch.messaging.event.StudyMilestone;
import com.devlaunch.messaging.event.StudyTaskEvent;
import com.devlaunch.messaging.event.StudyTaskType;
import com.devlaunch.repository.AchievementDefinitionRepository;
import com.devlaunch.repository.NotificationRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.RoleRepository;
import com.devlaunch.repository.UserAchievementRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.repository.XpHistoryRepository;
import com.devlaunch.service.interfaces.EmailService;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * End-to-end integration tests for the RabbitMQ messaging backbone.
 * <p>
 * Boots the full application against an in-process RabbitMQ broker mock
 * (no Docker required), publishes lightweight events through the real
 * {@link EventPublisher}, and verifies that the consumers perform their
 * side effects: sending emails through the existing {@link EmailService}
 * and persisting notifications through the existing notification module.
 * Also verifies the graceful handling of events for users that no longer
 * exist (a permanent failure that must never crash the consumer).
 * </p>
 *
 * @author DevLaunch
 */
@SpringBootTest
@ActiveProfiles("test")
class RabbitMqMessagingIntegrationTest {

    /**
     * Swaps the real broker connection for the in-process AMQP mock so the
     * whole publish → route → consume pipeline runs without Docker.
     */
    @TestConfiguration
    static class InProcessRabbitMqConfiguration {

        @Bean
        ConnectionFactory connectionFactory() {
            return new CachingConnectionFactory(new MockConnectionFactory());
        }
    }

    private static final long TIMEOUT_MILLIS = 10_000L;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeReviewRepository resumeReviewRepository;

    @Autowired
    private AchievementDefinitionRepository achievementDefinitionRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private XpHistoryRepository xpHistoryRepository;

    /**
     * Replaces the real email service so delivery can be asserted; the
     * real {@code EmailServiceImpl} is covered by its own unit tests.
     */
    @MockitoBean
    private EmailService emailService;

    private User testUser;

    @BeforeEach
    void createTestUser() {
        // The users.role_id column is NOT NULL; reuse the STUDENT role
        // seeded by the DataInitializer on context startup.
        final Role studentRole = roleRepository.findByRoleName(RoleType.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT role not seeded"));
        testUser = userRepository.save(User.builder()
                .firstName("Messaging")
                .lastName("Tester")
                .email("messaging-" + System.nanoTime() + "@example.com")
                .password("hashed-password")
                .phone("+1 555 000 0000")
                .isActive(true)
                .role(studentRole)
                .build());
    }

    @AfterEach
    void cleanUpNotifications() {
        notificationRepository.deleteAll();
        // Children first: user_achievements and xp_history reference the
        // definitions table, so they must be removed before the catalog.
        userAchievementRepository.deleteAll();
        xpHistoryRepository.deleteAll();
        achievementDefinitionRepository.deleteAll();
    }

    // ─── Forgot Password Email ──────────────────────────────────────────────

    @Test
    @DisplayName("forgot-password email event is consumed and the email service delivers it")
    void forgotPasswordEmailEventDeliversEmail() {
        eventPublisher.publish(EventTopics.FORGOT_PASSWORD_EMAIL_KEY,
                new ForgotPasswordEmailEvent(testUser.getId(), "reset-token-123"));

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(emailService, timeout(TIMEOUT_MILLIS))
                .sendPasswordResetEmail(userCaptor.capture(), eq("reset-token-123"));
        assertEquals(testUser.getId(), userCaptor.getValue().getId());
    }

    // ─── Password Reset Success ─────────────────────────────────────────────

    @Test
    @DisplayName("password-reset-success event persists the confirmation notification")
    void passwordResetSuccessEventPersistsNotification() {
        eventPublisher.publish(EventTopics.PASSWORD_RESET_SUCCESS_KEY,
                new NotificationEvent(testUser.getId(), NotificationType.SYSTEM,
                        "Password Reset Successful",
                        "Your password has been changed successfully."));

        final Notification notification = awaitNotification("Password Reset Successful");

        assertEquals(NotificationType.SYSTEM, notification.getType());
        assertEquals("Your password has been changed successfully.", notification.getMessage());
        assertEquals(Boolean.FALSE, notification.getIsRead());
    }

    // ─── Resume Review Completed ────────────────────────────────────────────

    @Test
    @DisplayName("resume-review-completed events store review history and persist notifications")
    void resumeReviewCompletedEventStoresHistoryAndNotifications() {
        final Resume resume = resumeRepository.save(Resume.builder()
                .headline("Senior Developer")
                .summary("Experienced full-stack developer.")
                .user(testUser)
                .build());

        eventPublisher.publish(EventTopics.RESUME_REVIEW_COMPLETED_KEY,
                new ResumeReviewedEvent(testUser.getId(), resume.getId(),
                        "Java Developer", 78, 65, 70));

        awaitUntil(() -> notificationsForTestUser().size() == 2, TIMEOUT_MILLIS);

        final List<Notification> notifications = notificationsForTestUser();
        assertEquals(NotificationType.RESUME_REVIEW, notifications.get(0).getType());
        assertTrue(notifications.stream().anyMatch(n -> "ATS Resume Review completed".equals(n.getTitle())));
        assertTrue(notifications.stream().anyMatch(n -> "Resume score improved".equals(n.getTitle())));

        // The consumer stores the review history row used by the admin module.
        final List<ResumeReview> history = resumeReviewRepository.findByResume(resume);
        assertEquals(1, history.size());
        assertEquals(65, history.get(0).getAtsScore());
        assertEquals("Java Developer", history.get(0).getTargetRole());
    }

    // ─── Interview Reminder ─────────────────────────────────────────────────

    @Test
    @DisplayName("interview-reminder event persists the interview-tomorrow notification")
    void interviewReminderEventPersistsNotification() {
        eventPublisher.publish(EventTopics.INTERVIEW_REMINDER_KEY,
                new NotificationEvent(testUser.getId(), NotificationType.JOB,
                        "Interview tomorrow",
                        "Your interview for Software Engineer at Acme is scheduled for tomorrow at 9:00 AM."));

        final Notification notification = awaitNotification("Interview tomorrow");

        assertEquals(NotificationType.JOB, notification.getType());
        assertTrue(notification.getMessage().contains("Software Engineer"));
    }

    // ─── Study Reminder ─────────────────────────────────────────────────────

    @Test
    @DisplayName("study task events persist the study milestone notification")
    void studyTaskEventPersistsNotification() {
        eventPublisher.publish(EventTopics.STUDY_REMINDER_KEY,
                new StudyTaskEvent(testUser.getId(), 42L, "DSA practice",
                        LocalDate.now(), StudyTaskType.COMPLETED, StudyMilestone.DAILY,
                        null, null));

        final Notification notification = awaitNotification("Daily goal completed");

        assertEquals(NotificationType.STUDY, notification.getType());
        assertTrue(notification.getMessage().contains("DSA practice"));
    }

    // ─── Mock Interview Completed ───────────────────────────────────────────

    @Test
    @DisplayName("mock-interview-completed event persists the milestone notifications")
    void mockInterviewCompletedEventPersistsNotifications() {
        eventPublisher.publish(EventTopics.MOCK_INTERVIEW_COMPLETED_KEY,
                new MockInterviewCompletedEvent(testUser.getId(), "session-1",
                        InterviewType.JAVA, 92, 80, 85, 3, 120.5, 600,
                        80, 70.0, 0, 2, 140, 1));

        final Notification completion = awaitNotification("Interview completed");
        assertEquals(NotificationType.MOCK_INTERVIEW, completion.getType());
        assertTrue(completion.getMessage().contains("Java"));

        awaitUntil(() -> notificationsForTestUser().stream()
                .anyMatch(n -> "Outstanding interview score".equals(n.getTitle())), TIMEOUT_MILLIS);
        awaitUntil(() -> notificationsForTestUser().stream()
                .anyMatch(n -> "New highest score".equals(n.getTitle())), TIMEOUT_MILLIS);
    }

    // ─── GitHub Connected ───────────────────────────────────────────────────

    @Test
    @DisplayName("github-connected event persists the account notification")
    void githubConnectedEventPersistsNotification() {
        eventPublisher.publish(EventTopics.GITHUB_CONNECTED_KEY,
                new NotificationEvent(testUser.getId(), NotificationType.SYSTEM,
                        "GitHub Account Connected",
                        "GitHub account connected successfully."));

        final Notification notification = awaitNotification("GitHub Account Connected");

        assertEquals(NotificationType.SYSTEM, notification.getType());
        assertEquals("GitHub account connected successfully.", notification.getMessage());
    }

    // ─── Job Application Reminder ───────────────────────────────────────────

    @Test
    @DisplayName("job-application-reminder event persists the application notification")
    void jobApplicationReminderEventPersistsNotification() {
        eventPublisher.publish(EventTopics.JOB_APPLICATION_REMINDER_KEY,
                new NotificationEvent(testUser.getId(), NotificationType.JOB,
                        "Offer received",
                        "Congratulations! You received an offer from Acme."));

        final Notification notification = awaitNotification("Offer received");

        assertEquals(NotificationType.JOB, notification.getType());
        assertTrue(notification.getMessage().contains("Acme"));
    }

    // ─── Gamification Activity ──────────────────────────────────────────────

    @Test
    @DisplayName("activity events award XP, unlock badges, and raise the achievement notification")
    void activityEventAwardsXpUnlocksBadgeAndNotifies() {
        // Seed a badge definition (the test profile skips data.sql). The ATS
        // badges evaluate the event value directly, so no session row is needed.
        achievementDefinitionRepository.save(AchievementDefinition.builder()
                .code("ATS_EXPERT")
                .category(AchievementCategory.RESUME)
                .title("ATS Expert")
                .description("Achieve an ATS score of 80 or higher")
                .icon("🎯")
                .color("#f59e0b")
                .xpReward(150)
                .activityType(ActivityType.RESUME_REVIEWED)
                .targetValue(80)
                .sortOrder(2)
                .build());

        eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                new ActivityEvent(testUser.getId(), ActivityType.RESUME_REVIEWED,
                        85, java.time.LocalDateTime.now()));

        // The unlock notification is persisted by the achievement consumer.
        final Notification notification = awaitNotification("Achievement Unlocked");
        assertEquals(NotificationType.ACHIEVEMENT, notification.getType());
        assertTrue(notification.getMessage().contains("ATS Expert"));

        // The badge is unlocked and the XP ledger holds activity + badge XP.
        awaitUntil(() -> userAchievementRepository.countByUser(testUser) == 1, TIMEOUT_MILLIS);
        assertEquals(1L, userAchievementRepository.countByUser(testUser));
        assertEquals(200L, xpHistoryRepository.sumByUser(testUser)); // +50 review, +150 badge
    }

    // ─── Resilience ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("an event for a deleted user is dropped without crashing the consumer")
    void eventForDeletedUserIsDroppedGracefully() {
        eventPublisher.publish(EventTopics.STUDY_REMINDER_KEY,
                new StudyTaskEvent(999_999L, 1L, "orphan task", LocalDate.now(),
                        StudyTaskType.COMPLETED, StudyMilestone.DAILY, null, null));

        // Give the consumer a moment to process (and skip) the orphan event.
        sleepQuietly(500);

        // The consumer must still be alive and processing new events.
        eventPublisher.publish(EventTopics.STUDY_REMINDER_KEY,
                new StudyTaskEvent(testUser.getId(), 2L, "Still alive", LocalDate.now(),
                        StudyTaskType.COMPLETED, StudyMilestone.DAILY, null, null));

        final Notification notification = awaitNotification("Daily goal completed");
        assertTrue(notification.getMessage().contains("Still alive"));
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    /**
     * Polls the notification repository until a notification with the given
     * title exists for the test user, or fails after the timeout.
     *
     * @param title the expected notification title
     * @return the matching notification
     */
    private Notification awaitNotification(final String title) {
        final long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            final Optional<Notification> found = notificationsForTestUser().stream()
                    .filter(notification -> title.equals(notification.getTitle()))
                    .findFirst();
            if (found.isPresent()) {
                return found.get();
            }
            sleepQuietly(50);
        }
        fail("Notification '" + title + "' was not persisted within " + TIMEOUT_MILLIS + "ms");
        return null;
    }

    /**
     * Polls until the condition holds or the timeout elapses, then fails.
     *
     * @param condition     the condition to wait for
     * @param timeoutMillis the maximum wait time
     */
    private void awaitUntil(final BooleanSupplier condition, final long timeoutMillis) {
        final long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            sleepQuietly(50);
        }
        fail("Condition not met within " + timeoutMillis + "ms");
    }

    private List<Notification> notificationsForTestUser() {
        return notificationRepository.findByUserOrderByCreatedAtDesc(testUser);
    }

    private void sleepQuietly(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for messaging", e);
        }
    }

}
