package com.devlaunch.service.impl;

import com.devlaunch.cache.CacheNames;
import com.devlaunch.dto.request.CreateInterviewNoteRequest;
import com.devlaunch.dto.request.CreateJobApplicationRequest;
import com.devlaunch.dto.request.ScheduleInterviewRequest;
import com.devlaunch.dto.request.UpdateInterviewScheduleRequest;
import com.devlaunch.dto.request.UpdateJobApplicationRequest;
import com.devlaunch.dto.response.ApplicationAnalyticsResponse;
import com.devlaunch.dto.response.ApplicationAttachmentResponse;
import com.devlaunch.dto.response.InterviewNoteResponse;
import com.devlaunch.dto.response.InterviewScheduleResponse;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.dto.response.MonthlyApplicationResponse;
import com.devlaunch.dto.response.TimelineEventResponse;
import com.devlaunch.entity.ApplicationAttachment;
import com.devlaunch.entity.ApplicationTimelineEvent;
import com.devlaunch.entity.InterviewNote;
import com.devlaunch.entity.InterviewSchedule;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.entity.enums.ApplicationPriority;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.AttachmentCategory;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.TimelineEventType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.JobApplicationMapper;
import com.devlaunch.messaging.EventPublisher;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.ActivityEvent;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.ApplicationAttachmentRepository;
import com.devlaunch.repository.ApplicationTimelineEventRepository;
import com.devlaunch.repository.InterviewNoteRepository;
import com.devlaunch.repository.InterviewScheduleRepository;
import com.devlaunch.repository.JobApplicationRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.AttachmentStorageService;
import com.devlaunch.service.interfaces.JobApplicationService;
import com.devlaunch.util.EnumLabels;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link JobApplicationService} providing job application
 * creation, retrieval, update, and deletion operations for the currently
 * authenticated user, plus the placement management features layered on top:
 * quick status moves, an automatic application timeline, interview
 * scheduling, private interview notes, document attachments, and an
 * analytics overview.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain the
 * authenticated user's email, then delegates persistence and mapping to
 * {@link JobApplicationRepository} and {@link JobApplicationMapper}
 * respectively. Every operation verifies that the affected record belongs
 * to the authenticated user. Milestones (status changes, interview
 * scheduling/cancelling, attachment uploads) are recorded on the
 * application timeline and published as lightweight reminder events on the
 * messaging backbone — the consumers persist the notifications through the
 * existing notification module, so no notification logic is duplicated here.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class JobApplicationServiceImpl implements JobApplicationService {

    /**
     * The maximum attachment file size in bytes (10 MB).
     */
    private static final long MAX_ATTACHMENT_BYTES = 10L * 1024 * 1024;

    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final EventPublisher eventPublisher;
    private final ApplicationTimelineEventRepository timelineEventRepository;
    private final InterviewScheduleRepository interviewScheduleRepository;
    private final InterviewNoteRepository interviewNoteRepository;
    private final ApplicationAttachmentRepository attachmentRepository;
    private final AttachmentStorageService attachmentStorageService;

    /**
     * Constructs the job application service with the required dependencies.
     *
     * @param jobApplicationRepository   repository for job application data access
     * @param resumeRepository           repository for resume data access
     * @param userRepository             repository for user data access
     * @param jobApplicationMapper       mapper for DTO-entity conversions
     * @param eventPublisher             publisher for the messaging backbone
     * @param timelineEventRepository    repository for application timeline events
     * @param interviewScheduleRepository repository for interview schedules
     * @param interviewNoteRepository    repository for private interview notes
     * @param attachmentRepository       repository for application attachments
     * @param attachmentStorageService   service for persisting attachment files
     */
    public JobApplicationServiceImpl(final JobApplicationRepository jobApplicationRepository,
                                     final ResumeRepository resumeRepository,
                                     final UserRepository userRepository,
                                     final JobApplicationMapper jobApplicationMapper,
                                     final EventPublisher eventPublisher,
                                     final ApplicationTimelineEventRepository timelineEventRepository,
                                     final InterviewScheduleRepository interviewScheduleRepository,
                                     final InterviewNoteRepository interviewNoteRepository,
                                     final ApplicationAttachmentRepository attachmentRepository,
                                     final AttachmentStorageService attachmentStorageService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.jobApplicationMapper = jobApplicationMapper;
        this.eventPublisher = eventPublisher;
        this.timelineEventRepository = timelineEventRepository;
        this.interviewScheduleRepository = interviewScheduleRepository;
        this.interviewNoteRepository = interviewNoteRepository;
        this.attachmentRepository = attachmentRepository;
        this.attachmentStorageService = attachmentStorageService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            @CacheEvict(cacheNames = {CacheNames.JOB, CacheNames.JOB_ANALYTICS})
    })
    public JobApplicationResponse createJobApplication(final CreateJobApplicationRequest request) {
        final User user = getAuthenticatedUser();

        // Map request DTO to a new JobApplication entity
        final JobApplication jobApplication = jobApplicationMapper.toJobApplication(request);

        // Normalize the optional priority before persisting
        if (jobApplication.getPriority() == null) {
            jobApplication.setPriority(ApplicationPriority.MEDIUM);
        }

        // Associate the job application with the authenticated user
        jobApplication.setUser(user);

        // If a resume ID is provided, fetch and verify ownership before associating
        if (request.getResumeId() != null) {
            final Resume resume = getResumeOwnedByAuthenticatedUser(request.getResumeId());
            jobApplication.setResume(resume);
        }

        // Persist the new job application
        final JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        // Record the initial timeline entries automatically
        addTimelineEvent(savedJobApplication, TimelineEventType.ADDED, "Added",
                "Application added to your tracker.");
        if (ApplicationStatus.WISHLIST != savedJobApplication.getStatus()) {
            addTimelineEvent(savedJobApplication,
                    eventTypeForStatus(savedJobApplication.getStatus()),
                    EnumLabels.toLabel(savedJobApplication.getStatus()), null);
        }

        // Publish the reminder event; the consumer persists the notification
        eventPublisher.publish(EventTopics.JOB_APPLICATION_REMINDER_KEY,
                new NotificationEvent(user.getId(), NotificationType.JOB,
                        "Job application added",
                        "Your application for " + jobApplication.getJobRole()
                                + " at " + jobApplication.getCompanyName() + " was added to your tracker."));

        // Publish the gamification activity; the consumer awards XP and
        // evaluates the job tracker achievements asynchronously.
        eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                new ActivityEvent(user.getId(), ActivityType.JOB_APPLICATION_CREATED,
                        null, LocalDateTime.now()));

        // Return the job application data
        return enrichResponse(jobApplicationMapper.toJobApplicationResponse(savedJobApplication),
                savedJobApplication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = CacheNames.JOB)
    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getAllJobApplications() {
        final User user = getAuthenticatedUser();
        final List<JobApplication> jobApplications = jobApplicationRepository.findByUser(user);
        return enrichAll(jobApplications);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponse getJobApplicationById(final Long id) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);
        return enrichResponse(jobApplicationMapper.toJobApplicationResponse(jobApplication),
                jobApplication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            @CacheEvict(cacheNames = {CacheNames.JOB, CacheNames.JOB_ANALYTICS})
    })
    public JobApplicationResponse updateJobApplication(final Long id,
                                                       final UpdateJobApplicationRequest request) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);

        final ApplicationStatus previousStatus = jobApplication.getStatus();

        // Update the editable fields
        jobApplication.setCompanyName(request.getCompanyName());
        jobApplication.setJobRole(request.getJobRole());
        jobApplication.setCompanyLocation(request.getCompanyLocation());
        jobApplication.setJobType(request.getJobType());
        jobApplication.setSalary(request.getSalary());
        jobApplication.setApplicationDate(request.getApplicationDate());
        jobApplication.setStatus(request.getStatus());
        jobApplication.setJobUrl(request.getJobUrl());
        jobApplication.setCompanyWebsite(request.getCompanyWebsite());
        jobApplication.setRecruiterName(request.getRecruiterName());
        jobApplication.setRecruiterEmail(request.getRecruiterEmail());
        jobApplication.setReferral(request.getReferral());
        jobApplication.setWorkMode(request.getWorkMode());
        jobApplication.setPriority(request.getPriority() == null
                ? ApplicationPriority.MEDIUM : request.getPriority());
        jobApplication.setTechnology(request.getTechnology());
        jobApplication.setNotes(request.getNotes());

        // Update the resume association if it has changed
        final Long currentResumeId = jobApplication.getResume() != null
                ? jobApplication.getResume().getId() : null;
        final Long newResumeId = request.getResumeId();

        if (currentResumeId == null && newResumeId == null) {
            // No change — both are null
        } else if (currentResumeId != null && currentResumeId.equals(newResumeId)) {
            // No change — same resume ID
        } else if (newResumeId == null) {
            // Resume association removed
            jobApplication.setResume(null);
        } else {
            // Resume association changed to a different resume
            final Resume resume = getResumeOwnedByAuthenticatedUser(newResumeId);
            jobApplication.setResume(resume);
        }

        // Persist the updated job application
        final JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        // Record a timeline entry and notify the user when the status changes
        // so important milestones (interview, offer, rejection) never go unnoticed
        final ApplicationStatus newStatus = jobApplication.getStatus();
        if (newStatus != previousStatus) {
            addTimelineEvent(savedJobApplication, eventTypeForStatus(newStatus),
                    EnumLabels.toLabel(newStatus), null);
            notifyStatusChange(jobApplication.getUser(),
                    jobApplication.getCompanyName(), jobApplication.getJobRole(), newStatus);
        }

        // Return the updated job application data
        return enrichResponse(jobApplicationMapper.toJobApplicationResponse(savedJobApplication),
                savedJobApplication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            @CacheEvict(cacheNames = {CacheNames.JOB, CacheNames.JOB_ANALYTICS})
    })
    public JobApplicationResponse updateApplicationStatus(final Long id,
                                                          final ApplicationStatus status) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);

        if (jobApplication.getStatus() == status) {
            // Nothing to change — return the current state
            return enrichResponse(jobApplicationMapper.toJobApplicationResponse(jobApplication),
                    jobApplication);
        }

        jobApplication.setStatus(status);
        final JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        // Record the milestone and notify the user (reusing the existing logic)
        addTimelineEvent(savedJobApplication, eventTypeForStatus(status),
                EnumLabels.toLabel(status), null);
        notifyStatusChange(savedJobApplication.getUser(),
                savedJobApplication.getCompanyName(), savedJobApplication.getJobRole(), status);

        return enrichResponse(jobApplicationMapper.toJobApplicationResponse(savedJobApplication),
                savedJobApplication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            @CacheEvict(cacheNames = {CacheNames.JOB, CacheNames.JOB_ANALYTICS})
    })
    public void deleteJobApplication(final Long id) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);

        // Remove child records first so the foreign key constraints stay valid
        attachmentRepository.findByApplicationOrderByCreatedAtDesc(jobApplication)
                .forEach(attachment -> {
                    attachmentStorageService.delete(attachment.getStoredFileName());
                    attachmentRepository.delete(attachment);
                });
        interviewNoteRepository.findByApplicationOrderByCreatedAtDesc(jobApplication)
                .forEach(interviewNoteRepository::delete);
        interviewScheduleRepository.findByApplicationOrderByScheduledDateAscScheduledTimeAsc(jobApplication)
                .forEach(interviewScheduleRepository::delete);
        timelineEventRepository.findByApplicationOrderByOccurredAtAsc(jobApplication)
                .forEach(timelineEventRepository::delete);

        jobApplicationRepository.delete(jobApplication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TimelineEventResponse> getTimeline(final Long id) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);
        return timelineEventRepository
                .findByApplicationOrderByOccurredAtAsc(jobApplication).stream()
                .map(this::toTimelineResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<InterviewScheduleResponse> getInterviews(final Long id) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);
        return interviewScheduleRepository
                .findByApplicationOrderByScheduledDateAscScheduledTimeAsc(jobApplication).stream()
                .map(this::toInterviewResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            @CacheEvict(cacheNames = {CacheNames.JOB, CacheNames.JOB_ANALYTICS})
    })
    public InterviewScheduleResponse scheduleInterview(final Long id,
                                                       final ScheduleInterviewRequest request) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);

        final InterviewSchedule interview = InterviewSchedule.builder()
                .application(jobApplication)
                .title(request.getTitle())
                .round(request.getRound())
                .scheduledDate(request.getScheduledDate())
                .scheduledTime(request.getScheduledTime())
                .meetingLink(request.getMeetingLink())
                .interviewer(request.getInterviewer())
                .notes(request.getNotes())
                .cancelled(Boolean.FALSE)
                .build();

        final InterviewSchedule saved = interviewScheduleRepository.save(interview);

        // Record the milestone on the application timeline
        final String scheduleNote = describeInterview(saved);
        addTimelineEvent(jobApplication, TimelineEventType.INTERVIEW_SCHEDULED,
                "Interview scheduled", scheduleNote);

        // Publish the reminder event; the consumer persists the notification
        eventPublisher.publish(EventTopics.JOB_APPLICATION_REMINDER_KEY,
                new NotificationEvent(jobApplication.getUser().getId(), NotificationType.JOB,
                        "Interview scheduled",
                        "Your interview for " + jobApplication.getJobRole() + " at "
                                + jobApplication.getCompanyName() + " is scheduled for "
                                + saved.getScheduledDate()
                                + (saved.getScheduledTime() == null ? "" : " at " + saved.getScheduledTime())
                                + "."));

        return toInterviewResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            @CacheEvict(cacheNames = {CacheNames.JOB, CacheNames.JOB_ANALYTICS})
    })
    public InterviewScheduleResponse updateInterview(final Long interviewId,
                                                     final UpdateInterviewScheduleRequest request) {
        final InterviewSchedule interview = getInterviewOwnedByAuthenticatedUser(interviewId);
        final boolean wasCancelled = Boolean.TRUE.equals(interview.getCancelled());

        interview.setTitle(request.getTitle());
        interview.setRound(request.getRound());
        interview.setScheduledDate(request.getScheduledDate());
        interview.setScheduledTime(request.getScheduledTime());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setInterviewer(request.getInterviewer());
        interview.setNotes(request.getNotes());
        if (request.getCancelled() != null) {
            interview.setCancelled(request.getCancelled());
        }

        final InterviewSchedule saved = interviewScheduleRepository.save(interview);

        // Record the cancellation milestone and notify the user
        if (!wasCancelled && Boolean.TRUE.equals(saved.getCancelled())) {
            addTimelineEvent(saved.getApplication(), TimelineEventType.INTERVIEW_CANCELLED,
                    "Interview cancelled", describeInterview(saved));
            eventPublisher.publish(EventTopics.JOB_APPLICATION_REMINDER_KEY,
                    new NotificationEvent(saved.getApplication().getUser().getId(),
                            NotificationType.JOB, "Interview cancelled",
                            "Your interview for " + saved.getApplication().getJobRole() + " at "
                                    + saved.getApplication().getCompanyName()
                                    + " scheduled for " + saved.getScheduledDate() + " was cancelled."));
        }

        return toInterviewResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            @CacheEvict(cacheNames = {CacheNames.JOB, CacheNames.JOB_ANALYTICS})
    })
    public InterviewScheduleResponse cancelInterview(final Long interviewId) {
        final InterviewSchedule interview = getInterviewOwnedByAuthenticatedUser(interviewId);
        interview.setCancelled(Boolean.TRUE);
        final InterviewSchedule saved = interviewScheduleRepository.save(interview);

        addTimelineEvent(saved.getApplication(), TimelineEventType.INTERVIEW_CANCELLED,
                "Interview cancelled", describeInterview(saved));
        eventPublisher.publish(EventTopics.JOB_APPLICATION_REMINDER_KEY,
                new NotificationEvent(saved.getApplication().getUser().getId(),
                        NotificationType.JOB, "Interview cancelled",
                        "Your interview for " + saved.getApplication().getJobRole() + " at "
                                + saved.getApplication().getCompanyName() + " scheduled for "
                                + saved.getScheduledDate() + " was cancelled."));

        return toInterviewResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<InterviewNoteResponse> getNotes(final Long id) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);
        return interviewNoteRepository.findByApplicationOrderByCreatedAtDesc(jobApplication).stream()
                .map(this::toNoteResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public InterviewNoteResponse addNote(final Long id, final CreateInterviewNoteRequest request) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);
        final InterviewNote note = interviewNoteRepository.save(InterviewNote.builder()
                .application(jobApplication)
                .content(request.getContent())
                .build());
        return toNoteResponse(note);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteNote(final Long applicationId, final Long noteId) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(applicationId);
        final InterviewNote note = interviewNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview note with id " + noteId + " not found"));
        if (!note.getApplication().getId().equals(jobApplication.getId())) {
            throw new ResourceNotFoundException(
                    "Interview note with id " + noteId + " not found for the authenticated user");
        }
        interviewNoteRepository.delete(note);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ApplicationAttachmentResponse> getAttachments(final Long id) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);
        return attachmentRepository.findByApplicationOrderByCreatedAtDesc(jobApplication).stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ApplicationAttachmentResponse uploadAttachment(final Long id, final MultipartFile file,
                                                          final AttachmentCategory category) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file must not be empty");
        }
        if (file.getSize() > MAX_ATTACHMENT_BYTES) {
            throw new IllegalArgumentException(
                    "Attachment file must not exceed 10 MB");
        }

        final String storedFileName = attachmentStorageService.store(file);

        final ApplicationAttachment attachment = attachmentRepository.save(
                ApplicationAttachment.builder()
                        .application(jobApplication)
                        .fileName(file.getOriginalFilename())
                        .storedFileName(storedFileName)
                        .contentType(file.getContentType())
                        .fileSize(file.getSize())
                        .category(category == null ? AttachmentCategory.OTHER : category)
                        .build());

        addTimelineEvent(jobApplication, TimelineEventType.ATTACHMENT_ADDED,
                "Attachment added", file.getOriginalFilename());

        return toAttachmentResponse(attachment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAttachment(final Long applicationId, final Long attachmentId) {
        final ApplicationAttachment attachment = getAttachmentOwnedByAuthenticatedUser(applicationId,
                attachmentId);
        attachmentStorageService.delete(attachment.getStoredFileName());
        attachmentRepository.delete(attachment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AttachmentDownload downloadAttachment(final Long applicationId, final Long attachmentId) {
        final ApplicationAttachment attachment = getAttachmentOwnedByAuthenticatedUser(applicationId,
                attachmentId);
        return new AttachmentDownload(
                attachmentStorageService.loadAsResource(attachment.getStoredFileName()),
                attachment.getFileName(),
                attachment.getContentType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = CacheNames.JOB_ANALYTICS)
    @Transactional(readOnly = true)
    public ApplicationAnalyticsResponse getAnalytics() {
        final User user = getAuthenticatedUser();
        final List<JobApplication> applications = jobApplicationRepository.findByUser(user);

        // Applications by status (enum order for a stable chart)
        final Map<ApplicationStatus, Long> statusCounts = new EnumMap<>(ApplicationStatus.class);
        for (final ApplicationStatus status : ApplicationStatus.values()) {
            statusCounts.put(status, 0L);
        }
        applications.forEach(app -> statusCounts.computeIfPresent(app.getStatus(),
                (key, count) -> count + 1));

        // Applications per month (yyyy-MM), oldest to newest
        final Map<String, Long> monthly = new TreeMap<>();
        applications.forEach(app -> {
            final LocalDate date = app.getApplicationDate() != null
                    ? app.getApplicationDate() : app.getCreatedAt().toLocalDate();
            monthly.merge(date.toString().substring(0, 7), 1L, Long::sum);
        });
        final List<MonthlyApplicationResponse> monthlyApplications = monthly.entrySet().stream()
                .map(entry -> MonthlyApplicationResponse.builder()
                        .yearMonth(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();

        // Rates derived from status counts
        final long total = applications.size();
        final long interviews = statusCounts.getOrDefault(ApplicationStatus.INTERVIEW, 0L);
        final long offers = statusCounts.getOrDefault(ApplicationStatus.OFFER, 0L);
        final long rejected = statusCounts.getOrDefault(ApplicationStatus.REJECTED, 0L);

        final Double interviewRate = total == 0 ? null : roundRate(interviews, total);
        final Double offerRate = total == 0 ? null : roundRate(offers, total);
        final Double rejectionRate = total == 0 ? null : roundRate(rejected, total);
        final Double successRate = (offers + rejected) == 0
                ? null : roundRate(offers, offers + rejected);

        // Average response time: days between applying and the first interview event
        final Double averageResponseTimeDays = averageResponseTime(applications);

        // Upcoming interviews across all applications
        final List<InterviewScheduleResponse> upcomingInterviews =
                interviewScheduleRepository
                        .findByApplication_UserAndCancelledFalseAndScheduledDateGreaterThanEqualOrderByScheduledDateAscScheduledTimeAsc(
                                user, LocalDate.now()).stream()
                        .map(this::toInterviewResponse)
                        .toList();

        return ApplicationAnalyticsResponse.builder()
                .totalApplications((long) total)
                .statusCounts(statusCounts)
                .monthlyApplications(monthlyApplications)
                .interviewRate(interviewRate)
                .offerRate(offerRate)
                .rejectionRate(rejectionRate)
                .successRate(successRate)
                .averageResponseTimeDays(averageResponseTimeDays)
                .activeInterviews(interviews)
                .upcomingInterviews(upcomingInterviews)
                .build();
    }

    /**
     * Computes the average number of days between applying and reaching the
     * first interview milestone across the given applications.
     *
     * @param applications the applications to analyse
     * @return the average response time in days, or {@code null} if no
     *         application has both an application date and an interview event
     */
    private Double averageResponseTime(final List<JobApplication> applications) {
        final Map<Long, List<ApplicationTimelineEvent>> eventsByApp =
                timelineEventRepository.findByApplicationIn(applications).stream()
                        .collect(Collectors.groupingBy(event -> event.getApplication().getId()));

        final List<Long> responseDays = new ArrayList<>();

        for (final JobApplication app : applications) {
            final List<ApplicationTimelineEvent> events =
                    eventsByApp.getOrDefault(app.getId(), List.of());

            final LocalDateTime appliedAt = events.stream()
                    .filter(event -> TimelineEventType.APPLIED == event.getEventType())
                    .map(ApplicationTimelineEvent::getOccurredAt)
                    .min(Comparator.naturalOrder())
                    .orElse(app.getApplicationDate() == null
                            ? null : app.getApplicationDate().atStartOfDay());

            final LocalDateTime firstInterviewAt = events.stream()
                    .filter(event -> TimelineEventType.INTERVIEW == event.getEventType())
                    .map(ApplicationTimelineEvent::getOccurredAt)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

            if (appliedAt != null && firstInterviewAt != null
                    && firstInterviewAt.isAfter(appliedAt)) {
                responseDays.add(ChronoUnit.DAYS.between(appliedAt, firstInterviewAt));
            }
        }

        if (responseDays.isEmpty()) {
            return null;
        }
        final double sum = responseDays.stream().mapToLong(Long::longValue).sum();
        return Math.round(sum / responseDays.size() * 10.0) / 10.0;
    }

    /**
     * Converts a part-to-total ratio into a percentage rounded to one
     * decimal place.
     *
     * @param part  the numerator
     * @param total the denominator
     * @return the percentage
     */
    private double roundRate(final long part, final long total) {
        return Math.round(part * 1000.0 / total) / 10.0;
    }

    /**
     * Enriches a batch of job application entities into responses using
     * bulk queries to avoid per-application database round trips.
     *
     * @param applications the applications to convert
     * @return the enriched response list
     */
    private List<JobApplicationResponse> enrichAll(final List<JobApplication> applications) {
        if (applications.isEmpty()) {
            return List.of();
        }

        final Map<Long, List<ApplicationTimelineEvent>> timelinesByApp =
                timelineEventRepository.findByApplicationIn(applications).stream()
                        .collect(Collectors.groupingBy(event -> event.getApplication().getId()));
        final Map<Long, List<InterviewSchedule>> interviewsByApp =
                interviewScheduleRepository.findByApplicationIn(applications).stream()
                        .collect(Collectors.groupingBy(interview -> interview.getApplication().getId()));
        final Map<Long, Long> notesByApp = interviewNoteRepository.findByApplicationIn(applications)
                .stream().collect(Collectors.groupingBy(
                        note -> note.getApplication().getId(), Collectors.counting()));
        final Map<Long, Long> attachmentsByApp =
                attachmentRepository.findByApplicationIn(applications).stream()
                        .collect(Collectors.groupingBy(
                                attachment -> attachment.getApplication().getId(),
                                Collectors.counting()));

        // Mutable ArrayList so the cached list root carries JSON type metadata
        // (immutable List.of()/toList() results are final and deserialize as
        // plain arrays, which the Redis cache cannot round-trip).
        return applications.stream()
                .map(app -> {
                    final List<InterviewSchedule> interviews =
                            interviewsByApp.getOrDefault(app.getId(), List.of());
                    final JobApplicationResponse response =
                            jobApplicationMapper.toJobApplicationResponse(app);
                    response.setTimeline(timelinesByApp.getOrDefault(app.getId(), List.of())
                            .stream().map(this::toTimelineResponse).toList());
                    response.setInterviews(interviews.stream()
                            .map(this::toInterviewResponse).toList());
                    response.setUpcomingInterview(nextUpcoming(interviews));
                    response.setNotesCount(Math.toIntExact(notesByApp.getOrDefault(app.getId(), 0L)));
                    response.setAttachmentCount(
                            Math.toIntExact(attachmentsByApp.getOrDefault(app.getId(), 0L)));
                    // Normalize legacy rows created before priority existed
                    if (response.getPriority() == null) {
                        response.setPriority(ApplicationPriority.MEDIUM);
                    }
                    return response;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Enriches a single job application entity into a response with its
     * timeline, interviews, upcoming interview, and summary counts.
     *
     * @param response     the mapper-produced response
     * @param application  the application entity
     * @return the enriched response
     */
    private JobApplicationResponse enrichResponse(final JobApplicationResponse response,
                                                  final JobApplication application) {
        final List<InterviewSchedule> interviews = interviewScheduleRepository
                .findByApplicationOrderByScheduledDateAscScheduledTimeAsc(application);
        response.setTimeline(timelineEventRepository
                .findByApplicationOrderByOccurredAtAsc(application).stream()
                .map(this::toTimelineResponse).toList());
        response.setInterviews(interviews.stream().map(this::toInterviewResponse).toList());
        response.setUpcomingInterview(nextUpcoming(interviews));
        response.setNotesCount((int) interviewNoteRepository
                .findByApplicationOrderByCreatedAtDesc(application).size());
        response.setAttachmentCount((int) attachmentRepository
                .findByApplicationOrderByCreatedAtDesc(application).size());
        // Normalize legacy rows created before priority existed
        if (response.getPriority() == null) {
            response.setPriority(ApplicationPriority.MEDIUM);
        }
        return response;
    }

    /**
     * Returns the next upcoming (future, non-cancelled) interview from a
     * date-ordered list, or {@code null}.
     *
     * @param interviews the date-ordered interviews
     * @return the next upcoming interview, or {@code null}
     */
    private InterviewScheduleResponse nextUpcoming(final List<InterviewSchedule> interviews) {
        final LocalDate today = LocalDate.now();
        return interviews.stream()
                .filter(interview -> !Boolean.TRUE.equals(interview.getCancelled()))
                .filter(interview -> !interview.getScheduledDate().isBefore(today))
                .findFirst()
                .map(this::toInterviewResponse)
                .orElse(null);
    }

    /**
     * Records a milestone on the application timeline.
     *
     * @param application the application the event belongs to
     * @param eventType   the event category
     * @param title       the human-readable headline
     * @param notes       optional notes, may be {@code null}
     */
    private void addTimelineEvent(final JobApplication application,
                                  final TimelineEventType eventType,
                                  final String title, final String notes) {
        timelineEventRepository.save(ApplicationTimelineEvent.builder()
                .application(application)
                .eventType(eventType)
                .title(title)
                .notes(notes)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Creates a status-change notification for the given application.
     * <p>
     * Interview, offer, rejection, and assessment milestones get dedicated
     * messages so the user sees the headline at a glance; every other
     * status change falls back to a generic update message.
     * </p>
     *
     * @param user    the user to notify
     * @param company the company the user applied to
     * @param role    the job role applied for
     * @param status  the new application status
     */
    private void notifyStatusChange(final User user, final String company, final String role,
                                    final ApplicationStatus status) {
        final String title;
        final String message;
        switch (status) {
            case INTERVIEW -> {
                title = "Interview scheduled";
                message = "Great news! Your application at " + company
                        + " has moved to the interview stage. Time to prepare!";
            }
            case OFFER -> {
                title = "Offer received";
                message = "Congratulations! You received an offer from " + company + ".";
            }
            case REJECTED -> {
                title = "Application rejected";
                message = "Your application at " + company
                        + " was rejected. Don't give up — keep applying!";
            }
            case ASSESSMENT -> {
                title = "Assessment required";
                message = "Your application at " + company
                        + " has moved to the assessment stage. Complete the assessment to keep moving forward!";
            }
            default -> {
                title = "Status updated";
                message = "Your application for " + role + " at " + company
                        + " is now " + EnumLabels.toLabel(status) + ".";
            }
        }
        eventPublisher.publish(EventTopics.JOB_APPLICATION_REMINDER_KEY,
                new NotificationEvent(user.getId(), NotificationType.JOB, title, message));
    }

    /**
     * Maps a status to the timeline event type that represents reaching it.
     *
     * @param status the application status
     * @return the matching timeline event type
     */
    private TimelineEventType eventTypeForStatus(final ApplicationStatus status) {
        return switch (status) {
            case WISHLIST -> TimelineEventType.STATUS_UPDATED;
            case APPLIED -> TimelineEventType.APPLIED;
            case ASSESSMENT -> TimelineEventType.ASSESSMENT;
            case INTERVIEW -> TimelineEventType.INTERVIEW;
            case OFFER -> TimelineEventType.OFFER;
            case REJECTED -> TimelineEventType.REJECTED;
        };
    }

    /**
     * Builds a short human-readable summary of an interview schedule.
     *
     * @param interview the interview
     * @return a description like "Technical Interview — Round 1 · 2024-03-15 14:00"
     */
    private String describeInterview(final InterviewSchedule interview) {
        final StringBuilder description = new StringBuilder(interview.getTitle());
        if (interview.getRound() != null && !interview.getRound().isBlank()) {
            description.append(" — ").append(interview.getRound());
        }
        description.append(" · ").append(interview.getScheduledDate());
        if (interview.getScheduledTime() != null) {
            description.append(' ').append(interview.getScheduledTime());
        }
        return description.toString();
    }

    /**
     * Maps a timeline event entity to its response DTO.
     */
    private TimelineEventResponse toTimelineResponse(final ApplicationTimelineEvent event) {
        return TimelineEventResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .title(event.getTitle())
                .notes(event.getNotes())
                .occurredAt(event.getOccurredAt())
                .build();
    }

    /**
     * Maps an interview schedule entity to its response DTO.
     */
    private InterviewScheduleResponse toInterviewResponse(final InterviewSchedule interview) {
        return InterviewScheduleResponse.builder()
                .id(interview.getId())
                .title(interview.getTitle())
                .round(interview.getRound())
                .scheduledDate(interview.getScheduledDate())
                .scheduledTime(interview.getScheduledTime())
                .meetingLink(interview.getMeetingLink())
                .interviewer(interview.getInterviewer())
                .notes(interview.getNotes())
                .cancelled(interview.getCancelled())
                .build();
    }

    /**
     * Maps an interview note entity to its response DTO.
     */
    private InterviewNoteResponse toNoteResponse(final InterviewNote note) {
        return InterviewNoteResponse.builder()
                .id(note.getId())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .build();
    }

    /**
     * Maps an attachment entity to its response DTO.
     */
    private ApplicationAttachmentResponse toAttachmentResponse(
            final ApplicationAttachment attachment) {
        return ApplicationAttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .category(attachment.getCategory())
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    /**
     * Retrieves the currently authenticated user from the database.
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

    /**
     * Retrieves a resume by ID and verifies it belongs to the currently
     * authenticated user.
     *
     * @param resumeId the resume ID to retrieve
     * @return the {@link Resume} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the resume is not found or does not
     *                                   belong to the authenticated user
     */
    private Resume getResumeOwnedByAuthenticatedUser(final Long resumeId) {
        final User user = getAuthenticatedUser();
        final Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with id " + resumeId + " not found"));

        // Verify ownership: the resume must belong to the authenticated user
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Resume with id " + resumeId + " not found for the authenticated user");
        }

        return resume;
    }

    /**
     * Retrieves a job application by ID and verifies it belongs to the
     * currently authenticated user.
     *
     * @param id the job application ID to retrieve
     * @return the {@link JobApplication} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the job application is not found or
     *                                   does not belong to the authenticated user
     */
    private JobApplication getJobApplicationOwnedByAuthenticatedUser(final Long id) {
        final User user = getAuthenticatedUser();
        final JobApplication jobApplication = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job application with id " + id + " not found"));

        // Verify ownership: the job application must belong to the authenticated user
        if (!jobApplication.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Job application with id " + id + " not found for the authenticated user");
        }

        return jobApplication;
    }

    /**
     * Retrieves an interview schedule by ID and verifies it belongs to the
     * currently authenticated user's application.
     *
     * @param interviewId the interview ID to retrieve
     * @return the {@link InterviewSchedule} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the interview is not found or does
     *                                   not belong to the user's application
     */
    private InterviewSchedule getInterviewOwnedByAuthenticatedUser(final Long interviewId) {
        final User user = getAuthenticatedUser();
        final InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview with id " + interviewId + " not found"));

        // Verify ownership: the interview must belong to the user's application
        if (!interview.getApplication().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Interview with id " + interviewId + " not found for the authenticated user");
        }

        return interview;
    }

    /**
     * Retrieves an attachment by ID and verifies it belongs to the currently
     * authenticated user's application.
     *
     * @param applicationId the application ID
     * @param attachmentId  the attachment ID
     * @return the {@link ApplicationAttachment} entity owned by the user
     * @throws ResourceNotFoundException if the attachment is not found or does
     *                                   not belong to the user's application
     */
    private ApplicationAttachment getAttachmentOwnedByAuthenticatedUser(
            final Long applicationId, final Long attachmentId) {
        getJobApplicationOwnedByAuthenticatedUser(applicationId);
        final ApplicationAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attachment with id " + attachmentId + " not found"));
        if (!attachment.getApplication().getId().equals(applicationId)) {
            throw new ResourceNotFoundException(
                    "Attachment with id " + attachmentId + " not found for the authenticated user");
        }
        return attachment;
    }

}
