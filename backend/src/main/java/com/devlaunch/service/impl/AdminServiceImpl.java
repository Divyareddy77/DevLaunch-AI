package com.devlaunch.service.impl;

import com.devlaunch.dto.request.AnnouncementRequest;
import com.devlaunch.dto.request.FeedbackSubmissionRequest;
import com.devlaunch.dto.response.AdminAnnouncementResponse;
import com.devlaunch.dto.response.AdminDashboardResponse;
import com.devlaunch.dto.response.AdminFeedbackResponse;
import com.devlaunch.dto.response.AdminInterviewSessionResponse;
import com.devlaunch.dto.response.AdminJobApplicationResponse;
import com.devlaunch.dto.response.AdminResumeResponse;
import com.devlaunch.dto.response.AdminResumeReviewResponse;
import com.devlaunch.dto.response.AdminStudyPlannerResponse;
import com.devlaunch.dto.response.AdminUserResponse;
import com.devlaunch.dto.response.PagedResponse;
import com.devlaunch.entity.Announcement;
import com.devlaunch.entity.Feedback;
import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.RoleType;
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
import com.devlaunch.service.interfaces.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AdminService} providing platform-wide
 * administrative operations.
 * <p>
 * Aggregates statistics across the existing repositories, manages users
 * (search, filtering, activation, deletion with full data cleanup),
 * moderates resumes, job applications and study plans, exposes AI module
 * history, and provides announcement and feedback management. All write
 * operations run in transactions so multi-entity deletions stay atomic.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final StudyPlannerRepository studyPlannerRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final ResumeReviewRepository resumeReviewRepository;
    private final AnnouncementRepository announcementRepository;
    private final FeedbackRepository feedbackRepository;
    private final AchievementRepository achievementRepository;
    private final CertificationRepository certificationRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;

    /**
     * Constructs the admin service with the required repositories.
     *
     * @param userRepository           repository for user data access
     * @param resumeRepository         repository for resume data access
     * @param jobApplicationRepository repository for job application data access
     * @param studyPlannerRepository   repository for study planner data access
     * @param interviewSessionRepository repository for interview history data access
     * @param resumeReviewRepository   repository for resume review history data access
     * @param announcementRepository   repository for announcement data access
     * @param feedbackRepository       repository for feedback data access
     * @param achievementRepository    repository for achievement data access
     * @param certificationRepository  repository for certification data access
     * @param educationRepository      repository for education data access
     * @param experienceRepository     repository for experience data access
     * @param projectRepository        repository for project data access
     * @param skillRepository          repository for skill data access
     */
    public AdminServiceImpl(final UserRepository userRepository,
                            final ResumeRepository resumeRepository,
                            final JobApplicationRepository jobApplicationRepository,
                            final StudyPlannerRepository studyPlannerRepository,
                            final InterviewSessionRepository interviewSessionRepository,
                            final ResumeReviewRepository resumeReviewRepository,
                            final AnnouncementRepository announcementRepository,
                            final FeedbackRepository feedbackRepository,
                            final AchievementRepository achievementRepository,
                            final CertificationRepository certificationRepository,
                            final EducationRepository educationRepository,
                            final ExperienceRepository experienceRepository,
                            final ProjectRepository projectRepository,
                            final SkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.studyPlannerRepository = studyPlannerRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.resumeReviewRepository = resumeReviewRepository;
        this.announcementRepository = announcementRepository;
        this.feedbackRepository = feedbackRepository;
        this.achievementRepository = achievementRepository;
        this.certificationRepository = certificationRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        final List<User> recentRegistrations = userRepository.findTop10ByOrderByCreatedAtDesc();
        final List<InterviewSession> recentInterviews =
                interviewSessionRepository.findTop10ByOrderByCompletedAtDesc();

        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByIsActive(Boolean.TRUE))
                .inactiveUsers(userRepository.countByIsActive(Boolean.FALSE))
                .totalResumes(resumeRepository.count())
                .totalJobApplications(jobApplicationRepository.count())
                .totalStudyPlans(studyPlannerRepository.count())
                .totalInterviewSessions(interviewSessionRepository.count())
                .totalResumeReviews(resumeReviewRepository.count())
                .recentRegistrations(recentRegistrations.stream()
                        .map(this::toAdminUserResponse)
                        .toList())
                .recentInterviews(recentInterviews.stream()
                        .map(this::toAdminInterviewSessionResponse)
                        .toList())
                .build();
    }

    // ─── User management ─────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminUserResponse> getUsers(final String search, final RoleType role,
                                                     final Boolean active, final int page, final int size) {
        final PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        final String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        final Page<User> users = userRepository.searchUsers(normalizedSearch, role, active, pageable);
        return PagedResponse.of(users.map(this::toAdminUserResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserDetails(final Long id) {
        return toAdminUserResponse(getUser(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdminUserResponse setUserActive(final Long id, final boolean active) {
        final User user = getUser(id);
        final User admin = getAuthenticatedAdmin();

        if (user.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("You cannot change the status of your own account");
        }

        user.setIsActive(active);
        userRepository.save(user);

        log.info("Admin {} {} user id={}", admin.getEmail(),
                active ? "activated" : "deactivated", user.getId());

        return toAdminUserResponse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteUser(final Long id) {
        final User user = getUser(id);
        final User admin = getAuthenticatedAdmin();

        if (user.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }

        deleteUserData(user);
        userRepository.delete(user);

        log.info("Admin {} deleted user id={} ({})", admin.getEmail(), user.getId(), user.getEmail());
    }

    // ─── Resume management ───────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminResumeResponse> getResumes(final int page, final int size) {
        final Page<Resume> resumes = resumeRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PagedResponse.of(resumes.map(this::toAdminResumeResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AdminResumeResponse getResumeDetails(final Long id) {
        return toAdminResumeResponse(resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with id " + id + " not found")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteResume(final Long id) {
        final Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with id " + id + " not found"));

        detachResumeFromApplications(resume);

        // Remove the resume's AI review history before deleting the resume so
        // the resume_reviews.resume_id foreign key constraint stays valid.
        resumeReviewRepository.deleteAll(resumeReviewRepository.findByResume(resume));

        deleteResumeSections(resume);
        resumeRepository.delete(resume);

        log.info("Admin {} deleted resume id={}", getAuthenticatedAdmin().getEmail(), id);
    }

    // ─── Job application management ──────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminJobApplicationResponse> getJobApplications(final int page, final int size) {
        final Page<JobApplication> applications = jobApplicationRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PagedResponse.of(applications.map(this::toAdminJobApplicationResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Map<ApplicationStatus, Long> getJobApplicationStats() {
        return jobApplicationRepository.findAll().stream()
                .collect(Collectors.groupingBy(JobApplication::getStatus, Collectors.counting()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteJobApplication(final Long id) {
        final JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job application with id " + id + " not found"));
        jobApplicationRepository.delete(application);

        log.info("Admin {} deleted job application id={}",
                getAuthenticatedAdmin().getEmail(), id);
    }

    // ─── Study planner management ────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminStudyPlannerResponse> getStudyPlans(final int page, final int size) {
        final Page<StudyPlanner> plans = studyPlannerRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "studyDate")));
        return PagedResponse.of(plans.map(this::toAdminStudyPlannerResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteStudyPlan(final Long id) {
        final StudyPlanner plan = studyPlannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study plan with id " + id + " not found"));
        studyPlannerRepository.delete(plan);

        log.info("Admin {} deleted study plan id={}",
                getAuthenticatedAdmin().getEmail(), id);
    }

    // ─── AI module monitoring ────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminResumeReviewResponse> getResumeReviews(final int page, final int size) {
        final Page<ResumeReview> reviews = resumeReviewRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PagedResponse.of(reviews.map(this::toAdminResumeReviewResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminInterviewSessionResponse> getInterviewSessions(final int page, final int size) {
        final Page<InterviewSession> sessions = interviewSessionRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "completedAt")));
        return PagedResponse.of(sessions.map(this::toAdminInterviewSessionResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteInterviewSession(final Long id) {
        final InterviewSession session = interviewSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview session with id " + id + " not found"));
        interviewSessionRepository.delete(session);

        log.info("Admin {} deleted interview session id={}",
                getAuthenticatedAdmin().getEmail(), id);
    }

    // ─── Announcement management ─────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminAnnouncementResponse> getAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAdminAnnouncementResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminAnnouncementResponse> getActiveAnnouncements() {
        return announcementRepository.findAllByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(this::toAdminAnnouncementResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdminAnnouncementResponse createAnnouncement(final AnnouncementRequest request) {
        final Announcement announcement = Announcement.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .createdBy(getAuthenticatedAdmin())
                .build();

        final Announcement saved = announcementRepository.save(announcement);

        log.info("Admin {} created announcement id={}",
                saved.getCreatedBy().getEmail(), saved.getId());

        return toAdminAnnouncementResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdminAnnouncementResponse updateAnnouncement(final Long id, final AnnouncementRequest request) {
        final Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Announcement with id " + id + " not found"));

        announcement.setTitle(request.getTitle().trim());
        announcement.setContent(request.getContent().trim());
        if (request.getIsActive() != null) {
            announcement.setIsActive(request.getIsActive());
        }

        final Announcement saved = announcementRepository.save(announcement);

        log.info("Admin {} updated announcement id={}",
                getAuthenticatedAdmin().getEmail(), id);

        return toAdminAnnouncementResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAnnouncement(final Long id) {
        final Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Announcement with id " + id + " not found"));
        announcementRepository.delete(announcement);

        log.info("Admin {} deleted announcement id={}",
                getAuthenticatedAdmin().getEmail(), id);
    }

    // ─── Feedback management ─────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminFeedbackResponse> getFeedback(final int page, final int size) {
        final Page<Feedback> feedback = feedbackRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PagedResponse.of(feedback.map(this::toAdminFeedbackResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteFeedback(final Long id) {
        final Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feedback with id " + id + " not found"));
        feedbackRepository.delete(feedback);

        log.info("Admin {} deleted feedback id={}",
                getAuthenticatedAdmin().getEmail(), id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void submitFeedback(final FeedbackSubmissionRequest request) {
        final User user = getAuthenticatedAdmin();

        feedbackRepository.save(Feedback.builder()
                .message(request.getMessage().trim())
                .user(user)
                .build());

        log.info("User {} submitted feedback", user.getEmail());
    }

    // ─── Data cleanup helpers ────────────────────────────────────────────────

    /**
     * Deletes all platform data owned by the given user: resume sections,
     * resumes, job applications, study plans, interview sessions, resume
     * reviews, and feedback.
     *
     * @param user the user whose data to delete
     */
    private void deleteUserData(final User user) {
        final List<Resume> resumes = resumeRepository.findByUser(user);

        // Detach the user's job applications from their resumes before either
        // is deleted so the job_applications.resume_id foreign key stays valid.
        resumes.forEach(this::detachResumeFromApplications);

        // Delete authored announcements before the user row so the
        // announcements.created_by foreign key stays valid.
        announcementRepository.deleteAll(announcementRepository.findByCreatedBy(user));

        // Resume reviews reference both the user and their resumes, so they
        // must be removed before either row is deleted.
        resumeReviewRepository.deleteAll(
                resumeReviewRepository.findByUserOrderByCreatedAtDesc(user));

        resumes.forEach(this::deleteResumeSections);
        resumeRepository.deleteAll(resumes);
        jobApplicationRepository.deleteAll(jobApplicationRepository.findByUser(user));
        studyPlannerRepository.deleteAll(studyPlannerRepository.findByUser(user));
        interviewSessionRepository.deleteAll(
                interviewSessionRepository.findByUserOrderByCompletedAtDesc(user));
        feedbackRepository.deleteAll(feedbackRepository.findByUser(user));
    }

    /**
     * Detaches any job applications referencing the given resume by clearing
     * the resume reference, keeping the {@code job_applications.resume_id}
     * foreign key constraint satisfied before the resume is deleted.
     *
     * @param resume the resume to detach from its linked applications
     */
    private void detachResumeFromApplications(final Resume resume) {
        jobApplicationRepository.findByResume(resume).forEach(application -> {
            application.setResume(null);
            jobApplicationRepository.save(application);
        });
    }

    /**
     * Deletes all section entities belonging to the given resume.
     *
     * @param resume the resume whose sections to delete
     */
    private void deleteResumeSections(final Resume resume) {
        achievementRepository.deleteAll(achievementRepository.findByResume(resume));
        certificationRepository.deleteAll(certificationRepository.findByResume(resume));
        educationRepository.deleteAll(educationRepository.findByResume(resume));
        experienceRepository.deleteAll(experienceRepository.findByResume(resume));
        projectRepository.deleteAll(projectRepository.findByResume(resume));
        skillRepository.deleteAll(skillRepository.findByResume(resume));
    }

    // ─── Mapping helpers ─────────────────────────────────────────────────────

    /**
     * Maps a {@link User} entity to the admin response DTO.
     */
    private AdminUserResponse toAdminUserResponse(final User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getRoleName().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Maps a {@link Resume} entity to the admin response DTO.
     */
    private AdminResumeResponse toAdminResumeResponse(final Resume resume) {
        return AdminResumeResponse.builder()
                .id(resume.getId())
                .headline(resume.getHeadline())
                .summary(resume.getSummary())
                .linkedinUrl(resume.getLinkedinUrl())
                .githubUrl(resume.getGithubUrl())
                .portfolioUrl(resume.getPortfolioUrl())
                .userId(resume.getUser().getId())
                .userEmail(resume.getUser().getEmail())
                .userName(resume.getUser().getFirstName() + " " + resume.getUser().getLastName())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link JobApplication} entity to the admin response DTO.
     */
    private AdminJobApplicationResponse toAdminJobApplicationResponse(
            final JobApplication application) {
        return AdminJobApplicationResponse.builder()
                .id(application.getId())
                .companyName(application.getCompanyName())
                .jobRole(application.getJobRole())
                .companyLocation(application.getCompanyLocation())
                .jobType(application.getJobType())
                .salary(application.getSalary())
                .status(application.getStatus())
                .applicationDate(application.getApplicationDate())
                .userId(application.getUser().getId())
                .userEmail(application.getUser().getEmail())
                .userName(application.getUser().getFirstName()
                        + " " + application.getUser().getLastName())
                .createdAt(application.getCreatedAt())
                .build();
    }

    /**
     * Maps a {@link StudyPlanner} entity to the admin response DTO.
     */
    private AdminStudyPlannerResponse toAdminStudyPlannerResponse(final StudyPlanner plan) {
        return AdminStudyPlannerResponse.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .studyDate(plan.getStudyDate())
                .startTime(plan.getStartTime())
                .endTime(plan.getEndTime())
                .priority(plan.getPriority())
                .status(plan.getStatus())
                .userId(plan.getUser().getId())
                .userEmail(plan.getUser().getEmail())
                .userName(plan.getUser().getFirstName() + " " + plan.getUser().getLastName())
                .createdAt(plan.getCreatedAt())
                .build();
    }

    /**
     * Maps a {@link ResumeReview} entity to the admin response DTO.
     */
    private AdminResumeReviewResponse toAdminResumeReviewResponse(final ResumeReview review) {
        return AdminResumeReviewResponse.builder()
                .id(review.getId())
                .resumeId(review.getResume().getId())
                .resumeTitle(review.getResume().getHeadline())
                .targetRole(review.getTargetRole())
                .resumeScore(review.getResumeScore())
                .atsScore(review.getAtsScore())
                .userId(review.getUser().getId())
                .userEmail(review.getUser().getEmail())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .createdAt(review.getCreatedAt())
                .build();
    }

    /**
     * Maps an {@link InterviewSession} entity to the admin response DTO.
     */
    private AdminInterviewSessionResponse toAdminInterviewSessionResponse(
            final InterviewSession session) {
        return AdminInterviewSessionResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .interviewType(session.getInterviewType())
                .overallScore(session.getOverallScore())
                .questionCount(session.getQuestionCount())
                .completedAt(session.getCompletedAt())
                .userId(session.getUser().getId())
                .userEmail(session.getUser().getEmail())
                .userName(session.getUser().getFirstName() + " " + session.getUser().getLastName())
                .build();
    }

    /**
     * Maps an {@link Announcement} entity to the admin response DTO.
     */
    private AdminAnnouncementResponse toAdminAnnouncementResponse(
            final Announcement announcement) {
        return AdminAnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .isActive(announcement.getIsActive())
                .createdById(announcement.getCreatedBy().getId())
                .createdByEmail(announcement.getCreatedBy().getEmail())
                .createdByName(announcement.getCreatedBy().getFirstName()
                        + " " + announcement.getCreatedBy().getLastName())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link Feedback} entity to the admin response DTO.
     */
    private AdminFeedbackResponse toAdminFeedbackResponse(final Feedback feedback) {
        return AdminFeedbackResponse.builder()
                .id(feedback.getId())
                .message(feedback.getMessage())
                .userId(feedback.getUser().getId())
                .userEmail(feedback.getUser().getEmail())
                .userName(feedback.getUser().getFirstName() + " " + feedback.getUser().getLastName())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    // ─── Shared helpers ──────────────────────────────────────────────────────

    /**
     * Loads a user by id or throws a {@link ResourceNotFoundException}.
     */
    private User getUser(final Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + id + " not found"));
    }

    /**
     * Retrieves the currently authenticated user (always an administrator
     * thanks to the {@code ROLE_ADMIN} security rule on the admin API).
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedAdmin() {
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

}
