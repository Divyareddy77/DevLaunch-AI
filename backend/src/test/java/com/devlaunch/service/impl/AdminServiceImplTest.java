package com.devlaunch.service.impl;

import com.devlaunch.dto.request.AnnouncementRequest;
import com.devlaunch.dto.request.FeedbackSubmissionRequest;
import com.devlaunch.dto.response.AdminAnnouncementResponse;
import com.devlaunch.dto.response.AdminDashboardResponse;
import com.devlaunch.dto.response.AdminStudyPlannerResponse;
import com.devlaunch.dto.response.AdminUserResponse;
import com.devlaunch.dto.response.PagedResponse;
import com.devlaunch.entity.Announcement;
import com.devlaunch.entity.Feedback;
import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.Role;
import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.entity.enums.StudyPriority;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.AchievementRepository;
import com.devlaunch.repository.AnnouncementRepository;
import com.devlaunch.repository.CertificationRepository;
import com.devlaunch.repository.EducationRepository;
import com.devlaunch.repository.ExperienceRepository;
import com.devlaunch.repository.FeedbackRepository;
import com.devlaunch.repository.InterviewSessionRepository;
import com.devlaunch.repository.JobApplicationRepository;
import com.devlaunch.repository.ProjectRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.SkillRepository;
import com.devlaunch.repository.StudyPlannerRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdminServiceImpl}.
 * <p>
 * Verifies the admin dashboard aggregation, user management (search,
 * activation, deletion with data cleanup), content moderation, AI module
 * history, announcement CRUD, and feedback management.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    private static final String ADMIN_EMAIL = "admin@devlaunch.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private StudyPlannerRepository studyPlannerRepository;
    @Mock
    private InterviewSessionRepository interviewSessionRepository;
    @Mock
    private ResumeReviewRepository resumeReviewRepository;
    @Mock
    private AnnouncementRepository announcementRepository;
    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private AchievementRepository achievementRepository;
    @Mock
    private CertificationRepository certificationRepository;
    @Mock
    private EducationRepository educationRepository;
    @Mock
    private ExperienceRepository experienceRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private NotificationService notificationService;

    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminServiceImpl(
                userRepository, resumeRepository, jobApplicationRepository,
                studyPlannerRepository, interviewSessionRepository,
                resumeReviewRepository, announcementRepository, feedbackRepository,
                achievementRepository, certificationRepository, educationRepository,
                experienceRepository, projectRepository, skillRepository,
                notificationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, null, List.of()));
    }

    private User admin() {
        final Role role = Role.builder().roleName(RoleType.ADMIN).build();
        final User admin = User.builder()
                .firstName("DevLaunch").lastName("Admin")
                .email(ADMIN_EMAIL).isActive(true).role(role)
                .build();
        admin.setId(1L);
        return admin;
    }

    private User student(final long id) {
        final Role role = Role.builder().roleName(RoleType.STUDENT).build();
        final User user = User.builder()
                .firstName("Jane").lastName("Doe")
                .email("jane@example.com").isActive(true).role(role)
                .build();
        user.setId(id);
        return user;
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("dashboard aggregates platform statistics and recent activity")
    void dashboardAggregatesStatistics() {
        final User recent = student(2L);
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByIsActive(true)).thenReturn(4L);
        when(userRepository.countByIsActive(false)).thenReturn(1L);
        when(resumeRepository.count()).thenReturn(3L);
        when(jobApplicationRepository.count()).thenReturn(7L);
        when(studyPlannerRepository.count()).thenReturn(2L);
        when(interviewSessionRepository.count()).thenReturn(6L);
        when(resumeReviewRepository.count()).thenReturn(8L);
        when(userRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(recent));

        final InterviewSession session = InterviewSession.builder()
                .sessionId("s1").interviewType(InterviewType.REACT)
                .overallScore(72).questionCount(10)
                .completedAt(LocalDateTime.of(2026, 8, 5, 10, 0))
                .user(recent).build();
        when(interviewSessionRepository.findTop10ByOrderByCompletedAtDesc())
                .thenReturn(List.of(session));

        final AdminDashboardResponse response = service.getDashboard();

        assertEquals(5L, response.getTotalUsers());
        assertEquals(4L, response.getActiveUsers());
        assertEquals(1L, response.getInactiveUsers());
        assertEquals(3L, response.getTotalResumes());
        assertEquals(7L, response.getTotalJobApplications());
        assertEquals(2L, response.getTotalStudyPlans());
        assertEquals(6L, response.getTotalInterviewSessions());
        assertEquals(8L, response.getTotalResumeReviews());
        assertEquals(1, response.getRecentRegistrations().size());
        assertEquals("jane@example.com", response.getRecentRegistrations().get(0).getEmail());
        assertEquals(1, response.getRecentInterviews().size());
        assertEquals(Integer.valueOf(72), response.getRecentInterviews().get(0).getOverallScore());
    }

    // ─── User management ─────────────────────────────────────────────────────

    @Test
    @DisplayName("user list maps the search results page into the response")
    void getUsersMapsThePage() {
        final User jane = student(2L);
        when(userRepository.searchUsers(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(jane)));

        final PagedResponse<AdminUserResponse> response =
                service.getUsers("jane", RoleType.STUDENT, true, 0, 10);

        assertEquals(1, response.getContent().size());
        final AdminUserResponse mapped = response.getContent().get(0);
        assertEquals("Jane", mapped.getFirstName());
        assertEquals("STUDENT", mapped.getRole());
        assertEquals(0, response.getPage());
    }

    @Test
    @DisplayName("an admin cannot deactivate their own account")
    void adminCannotDeactivateSelf() {
        authenticateAsAdmin();
        final User admin = admin();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class,
                () -> service.setUserActive(1L, false));
    }

    @Test
    @DisplayName("an admin can deactivate another user")
    void adminCanDeactivateAnotherUser() {
        authenticateAsAdmin();
        final User admin = admin();
        final User jane = student(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(jane));
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(userRepository.save(jane)).thenReturn(jane);

        final AdminUserResponse response = service.setUserActive(2L, false);

        assertFalse(jane.getIsActive());
        assertFalse(response.getIsActive());
    }

    @Test
    @DisplayName("an admin cannot delete their own account")
    void adminCannotDeleteSelf() {
        authenticateAsAdmin();
        final User admin = admin();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> service.deleteUser(1L));
        verify(userRepository, never()).delete(admin);
    }

    @Test
    @DisplayName("deleting a user removes all of their platform data")
    void deleteUserCleansUpAllUserData() {
        authenticateAsAdmin();
        final User admin = admin();
        final User jane = student(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(jane));
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(resumeRepository.findByUser(jane)).thenReturn(List.of());
        when(jobApplicationRepository.findByUser(jane)).thenReturn(List.of());
        when(studyPlannerRepository.findByUser(jane)).thenReturn(List.of());
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(jane))
                .thenReturn(List.of());
        when(resumeReviewRepository.findByUserOrderByCreatedAtDesc(jane))
                .thenReturn(List.of());
        when(feedbackRepository.findByUser(jane)).thenReturn(List.of());
        when(announcementRepository.findByCreatedBy(jane)).thenReturn(List.of());

        service.deleteUser(2L);

        verify(userRepository).delete(jane);
        verify(jobApplicationRepository).deleteAll(List.of());
        verify(announcementRepository).deleteAll(List.of());
        // The user's notifications are removed before the user row so the
        // notifications.user_id foreign key stays valid.
        verify(notificationService).deleteAllForUser(jane);
    }

    @Test
    @DisplayName("deleting a user removes resume review history before their resumes")
    void deleteUserRemovesResumeReviewsBeforeResumes() {
        authenticateAsAdmin();
        final User admin = admin();
        final User jane = student(2L);
        final Resume resume = Resume.builder().headline("Senior Developer").user(jane).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(jane));
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(resumeRepository.findByUser(jane)).thenReturn(List.of(resume));
        when(jobApplicationRepository.findByResume(resume)).thenReturn(List.of());
        when(announcementRepository.findByCreatedBy(jane)).thenReturn(List.of());
        when(resumeReviewRepository.findByUserOrderByCreatedAtDesc(jane)).thenReturn(List.of());
        when(achievementRepository.findByResume(resume)).thenReturn(List.of());
        when(certificationRepository.findByResume(resume)).thenReturn(List.of());
        when(educationRepository.findByResume(resume)).thenReturn(List.of());
        when(experienceRepository.findByResume(resume)).thenReturn(List.of());
        when(projectRepository.findByResume(resume)).thenReturn(List.of());
        when(skillRepository.findByResume(resume)).thenReturn(List.of());
        when(jobApplicationRepository.findByUser(jane)).thenReturn(List.of());
        when(studyPlannerRepository.findByUser(jane)).thenReturn(List.of());
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(jane))
                .thenReturn(List.of());
        when(feedbackRepository.findByUser(jane)).thenReturn(List.of());

        service.deleteUser(2L);

        // Resume reviews reference both the user and their resumes, so they
        // must be deleted before either row — otherwise the FK constraint on
        // resume_reviews.resume_id would be violated.
        final InOrder inOrder = inOrder(resumeReviewRepository, resumeRepository);
        inOrder.verify(resumeReviewRepository).deleteAll(List.of());
        inOrder.verify(resumeRepository).deleteAll(List.of(resume));
    }

    @Test
    @DisplayName("deleting an unknown user throws a resource not found exception")
    void deleteUnknownUserThrows() {
        authenticateAsAdmin();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteUser(99L));
    }

    // ─── Content moderation ──────────────────────────────────────────────────

    @Test
    @DisplayName("deleting a resume detaches linked job applications and removes sections")
    void deleteResumeDetachesApplicationsAndSections() {
        authenticateAsAdmin();
        final User admin = admin();
        final User jane = student(2L);
        final Resume resume = Resume.builder().headline("Senior Developer").user(jane).build();
        resume.setId(5L);
        final JobApplication linked = JobApplication.builder()
                .companyName("Acme").jobRole("Engineer").user(jane).resume(resume).build();

        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(resumeRepository.findById(5L)).thenReturn(Optional.of(resume));
        when(jobApplicationRepository.findByResume(resume)).thenReturn(List.of(linked));
        when(achievementRepository.findByResume(resume)).thenReturn(List.of());
        when(certificationRepository.findByResume(resume)).thenReturn(List.of());
        when(educationRepository.findByResume(resume)).thenReturn(List.of());
        when(experienceRepository.findByResume(resume)).thenReturn(List.of());
        when(projectRepository.findByResume(resume)).thenReturn(List.of());
        when(skillRepository.findByResume(resume)).thenReturn(List.of());

        service.deleteResume(5L);

        assertNull(linked.getResume());
        verify(jobApplicationRepository).save(linked);
        verify(resumeRepository).delete(resume);
        verify(educationRepository).deleteAll(List.of());
    }

    @Test
    @DisplayName("job application statistics group counts by status")
    void jobApplicationStatsGroupByStatus() {
        final User jane = student(2L);
        when(jobApplicationRepository.findAll()).thenReturn(List.of(
                JobApplication.builder().status(ApplicationStatus.APPLIED).user(jane).build(),
                JobApplication.builder().status(ApplicationStatus.APPLIED).user(jane).build(),
                JobApplication.builder().status(ApplicationStatus.INTERVIEW).user(jane).build()));

        final Map<ApplicationStatus, Long> stats = service.getJobApplicationStats();

        assertEquals(2L, stats.get(ApplicationStatus.APPLIED));
        assertEquals(1L, stats.get(ApplicationStatus.INTERVIEW));
    }

    @Test
    @DisplayName("study plan listing maps priority and status")
    void studyPlanListingMapsFields() {
        final User jane = student(2L);
        final StudyPlanner plan = StudyPlanner.builder()
                .title("Learn React").priority(StudyPriority.HIGH)
                .status(StudyStatus.COMPLETED).user(jane).build();
        when(studyPlannerRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(plan)));

        final PagedResponse<AdminStudyPlannerResponse> response =
                service.getStudyPlans(0, 10);

        assertEquals(1, response.getContent().size());
        assertEquals("Learn React", response.getContent().get(0).getTitle());
        assertEquals("jane@example.com", response.getContent().get(0).getUserEmail());
    }

    // ─── Announcements ───────────────────────────────────────────────────────

    @Test
    @DisplayName("creating an announcement authorises it to the current admin")
    void createAnnouncementSetsAuthor() {
        authenticateAsAdmin();
        final User admin = admin();
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(announcementRepository.save(any(Announcement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final AdminAnnouncementResponse response =
                service.createAnnouncement(AnnouncementRequest.builder()
                        .title("  Welcome  ")
                        .content("New feature is live.")
                        .isActive(true)
                        .build());

        assertEquals("Welcome", response.getTitle());
        assertTrue(response.getIsActive());
        assertEquals(ADMIN_EMAIL, response.getCreatedByEmail());
        // Published announcements fan out to every user as a notification,
        // reusing the announcement's own title and content.
        verify(notificationService).notifyAllUsers(NotificationType.ANNOUNCEMENT,
                "Welcome", "New feature is live.");
    }

    @Test
    @DisplayName("updating an announcement applies the new values")
    void updateAnnouncementAppliesChanges() {
        authenticateAsAdmin();
        final User admin = admin();
        final Announcement announcement = Announcement.builder()
                .title("Old").content("Old body").isActive(true).createdBy(admin).build();
        when(announcementRepository.findById(7L)).thenReturn(Optional.of(announcement));
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(announcementRepository.save(announcement)).thenReturn(announcement);

        final AdminAnnouncementResponse response =
                service.updateAnnouncement(7L, AnnouncementRequest.builder()
                        .title("New").content("New body").isActive(false)
                        .build());

        assertEquals("New", response.getTitle());
        assertFalse(response.getIsActive());
        // Unpublishing never fans out notifications
        verify(notificationService, never()).notifyAllUsers(any(), any(), any());
    }

    @Test
    @DisplayName("publishing a drafted announcement notifies all users")
    void publishingDraftedAnnouncementNotifiesAllUsers() {
        authenticateAsAdmin();
        final User admin = admin();
        final Announcement draft = Announcement.builder()
                .title("New feature").content("We shipped new features.")
                .isActive(false).createdBy(admin).build();
        when(announcementRepository.findById(7L)).thenReturn(Optional.of(draft));
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(announcementRepository.save(draft)).thenReturn(draft);

        service.updateAnnouncement(7L, AnnouncementRequest.builder()
                .title("New feature").content("We shipped new features.")
                .isActive(true)
                .build());

        verify(notificationService).notifyAllUsers(NotificationType.ANNOUNCEMENT,
                "New feature", "We shipped new features.");
    }

    @Test
    @DisplayName("active announcements expose only published announcements")
    void getActiveAnnouncementsReturnsOnlyActive() {
        final User admin = admin();
        final Announcement live = Announcement.builder()
                .title("New features")
                .content("We shipped new features.")
                .isActive(true)
                .createdBy(admin)
                .build();
        when(announcementRepository.findAllByIsActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(live));

        final List<AdminAnnouncementResponse> result = service.getActiveAnnouncements();

        assertEquals(1, result.size());
        assertEquals("New features", result.get(0).getTitle());
        // Inactive announcements are filtered at the query level, so the
        // user-facing read never touches the admin list query.
        verify(announcementRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("deleting an unknown announcement throws a resource not found exception")
    void deleteUnknownAnnouncementThrows() {
        authenticateAsAdmin();
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteAnnouncement(99L));
    }

    // ─── Feedback ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("submitting feedback persists the message for the authenticated user")
    void submitFeedbackPersistsMessage() {
        authenticateAsAdmin();
        final User admin = admin();
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.submitFeedback(FeedbackSubmissionRequest.builder()
                .message("  Great app!  ")
                .build());

        final ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(captor.capture());
        assertEquals("Great app!", captor.getValue().getMessage());
        assertEquals(admin, captor.getValue().getUser());
    }

    @Test
    @DisplayName("deleting feedback removes the entry")
    void deleteFeedbackRemovesEntry() {
        authenticateAsAdmin();
        final User admin = admin();
        final User jane = student(2L);
        final Feedback feedback = Feedback.builder().message("Nice").user(jane).build();
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(feedbackRepository.findById(3L)).thenReturn(Optional.of(feedback));

        service.deleteFeedback(3L);

        verify(feedbackRepository).delete(feedback);
    }

}
