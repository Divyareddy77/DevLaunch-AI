package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateJobApplicationRequest;
import com.devlaunch.dto.request.ScheduleInterviewRequest;
import com.devlaunch.dto.response.ApplicationAnalyticsResponse;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.entity.ApplicationTimelineEvent;
import com.devlaunch.entity.InterviewSchedule;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.TimelineEventType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.JobApplicationMapper;
import com.devlaunch.messaging.EventPublisher;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.ApplicationAttachmentRepository;
import com.devlaunch.repository.ApplicationTimelineEventRepository;
import com.devlaunch.repository.InterviewNoteRepository;
import com.devlaunch.repository.InterviewScheduleRepository;
import com.devlaunch.repository.JobApplicationRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.AttachmentStorageService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the placement management features in
 * {@link JobApplicationServiceImpl}.
 * <p>
 * Verifies automatic timeline recording, reminder-event publishing for
 * status changes (consumed by the messaging backbone), interview scheduling
 * and cancelling, and the analytics overview. All repository access is
 * mocked and every test authenticates as a fixed user so ownership scoping
 * can be asserted.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class JobApplicationServiceImplTest {

    private static final String USER_EMAIL = "dev@example.com";

    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JobApplicationMapper jobApplicationMapper;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private ApplicationTimelineEventRepository timelineEventRepository;
    @Mock
    private InterviewScheduleRepository interviewScheduleRepository;
    @Mock
    private InterviewNoteRepository interviewNoteRepository;
    @Mock
    private ApplicationAttachmentRepository attachmentRepository;
    @Mock
    private AttachmentStorageService attachmentStorageService;

    private JobApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JobApplicationServiceImpl(
                jobApplicationRepository, resumeRepository, userRepository,
                jobApplicationMapper, eventPublisher, timelineEventRepository,
                interviewScheduleRepository, interviewNoteRepository,
                attachmentRepository, attachmentStorageService);
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

    private JobApplication application(final Long id, final ApplicationStatus status,
                                       final User owner) {
        final JobApplication app = JobApplication.builder()
                .companyName("Acme").jobRole("Software Engineer")
                .status(status).applicationDate(LocalDate.of(2026, 8, 1))
                .build();
        app.setId(id);
        app.setUser(owner);
        return app;
    }

    // ─── Creation and timeline ──────────────────────────────────────────────

    @Test
    @DisplayName("creating an application records ADDED and status timeline events")
    void createRecordsInitialTimelineEvents() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        final JobApplication app = application(1L, ApplicationStatus.APPLIED, user);
        when(jobApplicationMapper.toJobApplication(any(CreateJobApplicationRequest.class)))
                .thenReturn(app);
        when(jobApplicationRepository.save(app)).thenReturn(app);
        when(jobApplicationMapper.toJobApplicationResponse(app))
                .thenReturn(new JobApplicationResponse());

        final JobApplicationResponse response = service.createJobApplication(
                CreateJobApplicationRequest.builder()
                        .companyName("Acme").jobRole("Software Engineer")
                        .status(ApplicationStatus.APPLIED).build());

        assertNotNull(response);
        verify(timelineEventRepository, times(2)).save(any(ApplicationTimelineEvent.class));

        final ArgumentCaptor<ApplicationTimelineEvent> captor =
                ArgumentCaptor.forClass(ApplicationTimelineEvent.class);
        verify(timelineEventRepository, times(2)).save(captor.capture());
        assertEquals(TimelineEventType.ADDED, captor.getAllValues().get(0).getEventType());
        assertEquals(TimelineEventType.APPLIED, captor.getAllValues().get(1).getEventType());

        // The reminder event is published for the messaging consumer
        verify(eventPublisher).publish(eq(EventTopics.JOB_APPLICATION_REMINDER_KEY),
                eq(new NotificationEvent(user.getId(), NotificationType.JOB,
                        "Job application added",
                        "Your application for Software Engineer at Acme was added to your tracker.")));
    }

    // ─── Status moves (Kanban) ──────────────────────────────────────────────

    @Test
    @DisplayName("moving an application to the interview stage records a timeline event and notifies")
    void updateStatusRecordsMilestoneAndNotifies() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        final JobApplication app = application(1L, ApplicationStatus.APPLIED, user);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobApplicationRepository.save(app)).thenReturn(app);
        when(jobApplicationMapper.toJobApplicationResponse(app))
                .thenReturn(new JobApplicationResponse());

        final JobApplicationResponse response = service.updateApplicationStatus(
                1L, ApplicationStatus.INTERVIEW);

        assertNotNull(response);
        assertEquals(ApplicationStatus.INTERVIEW, app.getStatus());

        final ArgumentCaptor<ApplicationTimelineEvent> captor =
                ArgumentCaptor.forClass(ApplicationTimelineEvent.class);
        verify(timelineEventRepository).save(captor.capture());
        assertEquals(TimelineEventType.INTERVIEW, captor.getValue().getEventType());
        assertEquals("Interview", captor.getValue().getTitle());

        verify(eventPublisher).publish(eq(EventTopics.JOB_APPLICATION_REMINDER_KEY),
                eq(new NotificationEvent(user.getId(), NotificationType.JOB,
                        "Interview scheduled",
                        "Great news! Your application at Acme has moved to the interview stage. Time to prepare!")));
    }

    @Test
    @DisplayName("moving to the offer stage sends an offer-received notification")
    void updateStatusToOfferNotifiesOfferReceived() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        final JobApplication app = application(1L, ApplicationStatus.INTERVIEW, user);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobApplicationRepository.save(app)).thenReturn(app);
        when(jobApplicationMapper.toJobApplicationResponse(app))
                .thenReturn(new JobApplicationResponse());

        service.updateApplicationStatus(1L, ApplicationStatus.OFFER);

        verify(eventPublisher).publish(eq(EventTopics.JOB_APPLICATION_REMINDER_KEY),
                eq(new NotificationEvent(user.getId(), NotificationType.JOB,
                        "Offer received",
                        "Congratulations! You received an offer from Acme.")));
    }

    @Test
    @DisplayName("moving to the assessment stage sends an assessment-required notification")
    void updateStatusToAssessmentNotifiesAssessmentRequired() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        final JobApplication app = application(1L, ApplicationStatus.APPLIED, user);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobApplicationRepository.save(app)).thenReturn(app);
        when(jobApplicationMapper.toJobApplicationResponse(app))
                .thenReturn(new JobApplicationResponse());

        service.updateApplicationStatus(1L, ApplicationStatus.ASSESSMENT);

        verify(eventPublisher).publish(eq(EventTopics.JOB_APPLICATION_REMINDER_KEY),
                eq(new NotificationEvent(user.getId(), NotificationType.JOB,
                        "Assessment required",
                        "Your application at Acme has moved to the assessment stage. "
                                + "Complete the assessment to keep moving forward!")));
    }

    @Test
    @DisplayName("updating to the same status is a no-op without events or notifications")
    void updateStatusToSameStatusIsNoOp() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        final JobApplication app = application(1L, ApplicationStatus.APPLIED, user);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobApplicationMapper.toJobApplicationResponse(app))
                .thenReturn(new JobApplicationResponse());

        service.updateApplicationStatus(1L, ApplicationStatus.APPLIED);

        verify(jobApplicationRepository, never()).save(any(JobApplication.class));
        verify(timelineEventRepository, never()).save(any(ApplicationTimelineEvent.class));
        verify(eventPublisher, never()).publish(any(), any());
    }

    @Test
    @DisplayName("updating the status of another user's application throws")
    void updateStatusForAnotherUsersApplicationThrows() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        final User foreignOwner = User.builder().email("other@example.com").build();
        foreignOwner.setId(99L);
        final JobApplication foreign = application(99L, ApplicationStatus.APPLIED, foreignOwner);
        when(jobApplicationRepository.findById(99L)).thenReturn(Optional.of(foreign));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateApplicationStatus(99L, ApplicationStatus.INTERVIEW));
    }

    // ─── Interview scheduling ───────────────────────────────────────────────

    @Test
    @DisplayName("scheduling an interview records a timeline event and notifies the user")
    void scheduleInterviewRecordsMilestoneAndNotifies() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        final JobApplication app = application(1L, ApplicationStatus.INTERVIEW, user);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        final InterviewSchedule saved = InterviewSchedule.builder()
                .application(app).title("Technical Interview").round("Round 1")
                .scheduledDate(LocalDate.of(2026, 8, 10))
                .scheduledTime(LocalTime.of(14, 0))
                .build();
        saved.setId(10L);
        when(interviewScheduleRepository.save(any(InterviewSchedule.class))).thenReturn(saved);

        final var response = service.scheduleInterview(1L, ScheduleInterviewRequest.builder()
                .title("Technical Interview").round("Round 1")
                .scheduledDate(LocalDate.of(2026, 8, 10))
                .scheduledTime(LocalTime.of(14, 0))
                .build());

        assertEquals("Technical Interview", response.getTitle());
        assertEquals(Boolean.FALSE, response.getCancelled());

        final ArgumentCaptor<ApplicationTimelineEvent> captor =
                ArgumentCaptor.forClass(ApplicationTimelineEvent.class);
        verify(timelineEventRepository).save(captor.capture());
        assertEquals(TimelineEventType.INTERVIEW_SCHEDULED, captor.getValue().getEventType());

        verify(eventPublisher).publish(eq(EventTopics.JOB_APPLICATION_REMINDER_KEY),
                eq(new NotificationEvent(user.getId(), NotificationType.JOB,
                        "Interview scheduled",
                        "Your interview for Software Engineer at Acme is scheduled for 2026-08-10 at 14:00.")));
    }

    @Test
    @DisplayName("cancelling an interview records a timeline event and notifies the user")
    void cancelInterviewRecordsMilestoneAndNotifies() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        final JobApplication app = application(1L, ApplicationStatus.INTERVIEW, user);

        final InterviewSchedule interview = InterviewSchedule.builder()
                .application(app).title("Technical Interview")
                .scheduledDate(LocalDate.of(2026, 8, 10)).cancelled(Boolean.FALSE)
                .build();
        interview.setId(10L);
        when(interviewScheduleRepository.findById(10L)).thenReturn(Optional.of(interview));
        when(interviewScheduleRepository.save(interview)).thenReturn(interview);

        final var response = service.cancelInterview(10L);

        assertTrue(Boolean.TRUE.equals(response.getCancelled()));

        final ArgumentCaptor<ApplicationTimelineEvent> captor =
                ArgumentCaptor.forClass(ApplicationTimelineEvent.class);
        verify(timelineEventRepository).save(captor.capture());
        assertEquals(TimelineEventType.INTERVIEW_CANCELLED, captor.getValue().getEventType());

        verify(eventPublisher).publish(eq(EventTopics.JOB_APPLICATION_REMINDER_KEY),
                eq(new NotificationEvent(user.getId(), NotificationType.JOB,
                        "Interview cancelled",
                        "Your interview for Software Engineer at Acme scheduled for 2026-08-10 was cancelled.")));
    }

    // ─── Analytics ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("analytics derive rates, status counts, and month counts from real data")
    void analyticsDerivesRatesFromRealData() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        // applicationDate is cleared so monthly bucketing falls back to createdAt
        final JobApplication applied = application(1L, ApplicationStatus.APPLIED, user);
        applied.setApplicationDate(null);
        applied.setCreatedAt(LocalDateTime.of(2026, 6, 15, 10, 0));
        final JobApplication interview = application(2L, ApplicationStatus.INTERVIEW, user);
        interview.setApplicationDate(null);
        interview.setCreatedAt(LocalDateTime.of(2026, 7, 2, 10, 0));
        final JobApplication offer = application(3L, ApplicationStatus.OFFER, user);
        offer.setApplicationDate(null);
        offer.setCreatedAt(LocalDateTime.of(2026, 7, 20, 10, 0));
        final JobApplication rejected = application(4L, ApplicationStatus.REJECTED, user);
        rejected.setApplicationDate(null);
        rejected.setCreatedAt(LocalDateTime.of(2026, 8, 1, 10, 0));

        when(jobApplicationRepository.findByUser(user))
                .thenReturn(List.of(applied, interview, offer, rejected));

        final ApplicationAnalyticsResponse analytics = service.getAnalytics();

        assertEquals(4L, analytics.getTotalApplications());
        assertEquals(1L, analytics.getStatusCounts().get(ApplicationStatus.INTERVIEW));
        assertEquals(1L, analytics.getStatusCounts().get(ApplicationStatus.OFFER));
        assertEquals(25.0, analytics.getInterviewRate());
        assertEquals(25.0, analytics.getOfferRate());
        assertEquals(25.0, analytics.getRejectionRate());
        assertEquals(50.0, analytics.getSuccessRate());
        assertEquals(1L, analytics.getActiveInterviews());

        // Applications are bucketed by yyyy-MM across the data set
        assertEquals(3, analytics.getMonthlyApplications().size());
        assertEquals("2026-06", analytics.getMonthlyApplications().get(0).getYearMonth());
        assertEquals("2026-08", analytics.getMonthlyApplications().get(2).getYearMonth());

        // No interview timeline events → average response time is unknown
        assertNull(analytics.getAverageResponseTimeDays());
    }

    @Test
    @DisplayName("analytics returns zeroed defaults for an empty pipeline")
    void analyticsReturnsDefaultsForEmptyPipeline() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(jobApplicationRepository.findByUser(user)).thenReturn(List.of());

        final ApplicationAnalyticsResponse analytics = service.getAnalytics();

        assertEquals(0L, analytics.getTotalApplications());
        assertNull(analytics.getInterviewRate());
        assertNull(analytics.getSuccessRate());
        assertTrue(analytics.getMonthlyApplications().isEmpty());
        assertEquals(0L, analytics.getActiveInterviews());
        assertTrue(analytics.getUpcomingInterviews().isEmpty());
    }

}
