package com.devlaunch.service.impl;

import com.devlaunch.dto.response.DashboardResponse;
import com.devlaunch.dto.response.GitHubProfileResponse;
import com.devlaunch.dto.response.LanguageStatisticsResponse;
import com.devlaunch.dto.response.LeetCodeProfileResponse;
import com.devlaunch.dto.response.RecentApplicationResponse;
import com.devlaunch.dto.response.ReadinessModuleResponse;
import com.devlaunch.dto.response.UpcomingInterviewResponse;
import com.devlaunch.entity.InterviewSchedule;
import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.ReadinessSnapshot;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.cache.CacheNames;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.exception.ResourceNotFoundException;
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
import com.devlaunch.service.interfaces.DashboardService;
import com.devlaunch.service.interfaces.GitHubService;
import com.devlaunch.service.interfaces.LeetCodeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link DashboardService} providing a comprehensive
 * dashboard summary for the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain the
 * authenticated user's email, then queries existing repositories and
 * services to compile aggregate metrics across resumes, job applications,
 * study tasks, interview history, and GitHub/LeetCode activity. GitHub and
 * LeetCode statistics are fetched live from the existing GitHub and
 * LeetCode services using the usernames linked to the user's account; no
 * data is shown when an account is not connected. The placement readiness
 * score is calculated on the fly using a weighted formula. A lightweight {@link ReadinessSnapshot} is
 * recorded each time the score changes so the dashboard can show the
 * previous score, the score difference, and the last-updated date. Readiness
 * milestone notifications (level up, significant improvement) are published
 * as events on the messaging backbone and persisted by the consumer — the
 * dashboard never writes notifications directly, and business services
 * never touch dashboard statistics.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    /**
     * The minimum score for the "Excellent" readiness level.
     */
    private static final int EXCELLENT_MIN_SCORE = 90;

    /**
     * The minimum score for the "Placement Ready" readiness level.
     */
    private static final int PLACEMENT_READY_MIN_SCORE = 75;

    /**
     * The minimum score for the "Improving" readiness level.
     */
    private static final int IMPROVING_MIN_SCORE = 60;

    /**
     * A readiness score jump of at least this many points is considered
     * a significant improvement worth celebrating with a notification.
     */
    private static final int SIGNIFICANT_IMPROVEMENT_POINTS = 10;

    /**
     * The recommended ATS score threshold referenced by insights.
     */
    private static final int STRONG_ATS_SCORE = 80;

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final StudyPlannerRepository studyPlannerRepository;
    private final ResumeReviewRepository resumeReviewRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewScheduleRepository interviewScheduleRepository;
    private final ReadinessSnapshotRepository readinessSnapshotRepository;
    private final GitHubService gitHubService;
    private final LeetCodeService leetCodeService;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the dashboard service with the required repositories and
     * external service clients.
     *
     * @param userRepository            repository for user data access
     * @param resumeRepository          repository for resume data access
     * @param jobApplicationRepository  repository for job application data access
     * @param studyPlannerRepository    repository for study planner data access
     * @param resumeReviewRepository    repository for AI resume review history
     * @param interviewSessionRepository repository for mock interview history
     * @param interviewScheduleRepository repository for job interview schedules
     * @param readinessSnapshotRepository repository for readiness score history
     * @param gitHubService             service for fetching GitHub profile and
     *                                  repository data
     * @param leetCodeService           service for fetching LeetCode profile data
     * @param eventPublisher            publisher for the messaging backbone
     */
    public DashboardServiceImpl(final UserRepository userRepository,
                                final ResumeRepository resumeRepository,
                                final JobApplicationRepository jobApplicationRepository,
                                final StudyPlannerRepository studyPlannerRepository,
                                final ResumeReviewRepository resumeReviewRepository,
                                final InterviewSessionRepository interviewSessionRepository,
                                final InterviewScheduleRepository interviewScheduleRepository,
                                final ReadinessSnapshotRepository readinessSnapshotRepository,
                                final GitHubService gitHubService,
                                final LeetCodeService leetCodeService,
                                final EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.studyPlannerRepository = studyPlannerRepository;
        this.resumeReviewRepository = resumeReviewRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewScheduleRepository = interviewScheduleRepository;
        this.readinessSnapshotRepository = readinessSnapshotRepository;
        this.gitHubService = gitHubService;
        this.leetCodeService = leetCodeService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = CacheNames.DASHBOARD)
    @Transactional
    public DashboardResponse getDashboard() {
        final User user = getAuthenticatedUser();

        // Gather data from each module (reusing existing repositories)
        final Integer resumeCompletion = calculateResumeCompletion(user);
        final List<JobApplication> jobApplications =
                jobApplicationRepository.findByUser(user);
        final List<StudyPlanner> studyPlanners =
                studyPlannerRepository.findByUser(user);
        final List<InterviewSession> mockInterviews =
                interviewSessionRepository.findByUserOrderByCompletedAtDesc(user);
        final int mockInterviewsCompleted = mockInterviews.size();
        final Integer mockInterviewLatestScore = mockInterviews.isEmpty()
                ? null : mockInterviews.getFirst().getOverallScore();
        final Double mockInterviewAverageScore = mockInterviews.isEmpty()
                ? null
                : Math.round(mockInterviews.stream()
                        .mapToInt(InterviewSession::getOverallScore)
                        .average().orElse(0.0) * 10.0) / 10.0;
        final Integer mockInterviewBestScore = mockInterviews.isEmpty()
                ? null
                : mockInterviews.stream()
                        .mapToInt(InterviewSession::getOverallScore)
                        .max().orElse(0);
        final Integer mockInterviewTrend = mockInterviews.size() >= 2
                ? mockInterviews.getFirst().getOverallScore()
                - mockInterviews.get(1).getOverallScore()
                : null;
        final String gitHubUsername = user.getGithubUsername();
        final String leetCodeUsername = user.getLeetcodeUsername();

        // Calculate job application metrics
        final int totalJobApplications = jobApplications.size();
        final int interviewApplications = (int) jobApplications.stream()
                .filter(app -> ApplicationStatus.INTERVIEW.equals(app.getStatus()))
                .count();
        final int offerApplications = (int) jobApplications.stream()
                .filter(app -> ApplicationStatus.OFFER.equals(app.getStatus()))
                .count();
        final int assessmentApplications = (int) jobApplications.stream()
                .filter(app -> ApplicationStatus.ASSESSMENT.equals(app.getStatus()))
                .count();

        // Recent applications (most recent application date first) and the
        // next upcoming interview for the dashboard job tracker widget
        final List<RecentApplicationResponse> recentApplications = jobApplications.stream()
                .sorted(Comparator
                        .comparing(JobApplication::getApplicationDate,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(JobApplication::getId,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(app -> RecentApplicationResponse.builder()
                        .id(app.getId())
                        .companyName(app.getCompanyName())
                        .jobRole(app.getJobRole())
                        .status(app.getStatus())
                        .applicationDate(app.getApplicationDate())
                        .build())
                .toList();
        final UpcomingInterviewResponse upcomingInterview = buildUpcomingInterview(user);

        // Calculate study task metrics
        final int totalStudyTasks = studyPlanners.size();
        final int completedStudyTasks = (int) studyPlanners.stream()
                .filter(task -> StudyStatus.COMPLETED.equals(task.getStatus()))
                .count();
        final int studyCompletionRate = totalStudyTasks > 0
                ? (int) Math.round((double) completedStudyTasks / totalStudyTasks * 100)
                : 0;

        // Fetch GitHub statistics live (only when a GitHub username is linked)
        int githubRepositories = 0;
        String githubTopLanguage = null;
        Integer githubFollowers = null;
        Integer githubFollowing = null;

        if (gitHubUsername != null && !gitHubUsername.isBlank()) {
            try {
                final GitHubProfileResponse profile =
                        gitHubService.getGitHubProfile(gitHubUsername);
                githubRepositories = profile.getPublicRepositories() != null
                        ? profile.getPublicRepositories() : 0;
                githubFollowers = profile.getFollowers();
                githubFollowing = profile.getFollowing();

                final List<LanguageStatisticsResponse> languages =
                        gitHubService.getLanguageStatistics(gitHubUsername);
                githubTopLanguage = languages.isEmpty() ? null : languages.getFirst().getLanguage();
            } catch (RuntimeException e) {
                // GitHub API is unreachable, rate-limited, or the user is not
                // found — never let an external service failure break the
                // dashboard; fall back to empty defaults.
                githubRepositories = 0;
                githubFollowers = null;
                githubFollowing = null;
                githubTopLanguage = null;
            }
        }

        // Fetch LeetCode statistics live (only when a LeetCode username is linked)
        int leetcodeSolved = 0;
        Integer leetcodeEasySolved = null;
        Integer leetcodeMediumSolved = null;
        Integer leetcodeHardSolved = null;
        Integer leetcodeRanking = null;
        Double leetcodeAcceptanceRate = null;

        if (leetCodeUsername != null && !leetCodeUsername.isBlank()) {
            try {
                final LeetCodeProfileResponse profile =
                        leetCodeService.getLeetCodeProfile(leetCodeUsername);
                leetcodeSolved = profile.getTotalSolved() != null
                        ? profile.getTotalSolved() : 0;
                leetcodeEasySolved = profile.getEasySolved();
                leetcodeMediumSolved = profile.getMediumSolved();
                leetcodeHardSolved = profile.getHardSolved();
                leetcodeRanking = profile.getRanking();
                leetcodeAcceptanceRate = profile.getAcceptanceRate();
            } catch (RuntimeException e) {
                // LeetCode API is unreachable, rate-limited, or the user is not
                // found — never let an external service failure break the
                // dashboard; fall back to empty defaults.
                leetcodeSolved = 0;
                leetcodeEasySolved = null;
                leetcodeMediumSolved = null;
                leetcodeHardSolved = null;
                leetcodeRanking = null;
                leetcodeAcceptanceRate = null;
            }
        }

        // Calculate the overall placement readiness score
        final int placementReadiness = calculatePlacementReadiness(
                resumeCompletion,
                totalJobApplications,
                interviewApplications,
                totalStudyTasks,
                completedStudyTasks,
                githubRepositories,
                githubTopLanguage,
                leetcodeSolved,
                mockInterviewsCompleted,
                mockInterviewAverageScore);

        // Latest AI resume review for the dashboard ATS summary
        final ResumeReview latestReview = resumeReviewRepository
                .findByUserOrderByCreatedAtDesc(user).stream().findFirst().orElse(null);

        // ---- Smarter readiness summary (derived from real module data) ----
        final List<ReadinessModuleResponse> modules = buildModuleBreakdown(
                latestReview, resumeCompletion, mockInterviewsCompleted,
                mockInterviewAverageScore, githubRepositories, leetcodeSolved,
                leetCodeUsername, studyCompletionRate, totalJobApplications);
        final String strongestArea = strongestArea(modules);
        final String weakestArea = weakestArea(modules);

        final List<String> strengths = identifyStrengths(
                latestReview, resumeCompletion, mockInterviewsCompleted,
                githubRepositories, totalStudyTasks, studyCompletionRate,
                totalJobApplications);
        final List<String> improvements = identifyImprovements(
                latestReview, resumeCompletion, mockInterviewsCompleted,
                githubRepositories, gitHubUsername, leetcodeSolved,
                leetCodeUsername, totalStudyTasks, studyCompletionRate,
                totalJobApplications);
        final List<String> recommendations = buildRecommendations(
                latestReview, resumeCompletion, mockInterviewsCompleted,
                githubRepositories, leetcodeSolved, totalStudyTasks,
                studyCompletionRate, totalJobApplications);

        // ---- Progress tracking + milestone notifications ----
        final ProgressRecord progress = trackProgress(user, placementReadiness);

        return DashboardResponse.builder()
                .resumeCompletion(resumeCompletion)
                .totalJobApplications(totalJobApplications)
                .mockInterviewCount(mockInterviewsCompleted)
                .mockInterviewLatestScore(mockInterviewLatestScore)
                .mockInterviewAverageScore(mockInterviewAverageScore)
                .mockInterviewBestScore(mockInterviewBestScore)
                .mockInterviewTrend(mockInterviewTrend)
                .mockInterviewInsight(mockInterviewInsight(
                        mockInterviewsCompleted, mockInterviewAverageScore,
                        mockInterviewLatestScore))
                .interviewApplications(interviewApplications)
                .offerApplications(offerApplications)
                .assessmentApplications(assessmentApplications)
                .recentApplications(recentApplications)
                .upcomingInterview(upcomingInterview)
                .totalStudyTasks(totalStudyTasks)
                .completedStudyTasks(completedStudyTasks)
                .githubUsername(gitHubUsername)
                .githubRepositories(githubRepositories)
                .githubTopLanguage(githubTopLanguage)
                .githubFollowers(githubFollowers)
                .githubFollowing(githubFollowing)
                .leetcodeUsername(leetCodeUsername)
                .leetcodeSolved(leetcodeSolved)
                .leetcodeEasySolved(leetcodeEasySolved)
                .leetcodeMediumSolved(leetcodeMediumSolved)
                .leetcodeHardSolved(leetcodeHardSolved)
                .leetcodeRanking(leetcodeRanking)
                .leetcodeAcceptanceRate(leetcodeAcceptanceRate)
                .placementReadiness(placementReadiness)
                .atsScore(latestReview == null ? null : latestReview.getAtsScore())
                .atsReviewedAt(latestReview == null ? null : latestReview.getCreatedAt())
                .resumeQualityStatus(resumeQualityStatus(latestReview))
                .readinessStatus(readinessStatus(placementReadiness))
                .readinessPrevious(progress.previousScore())
                .readinessChange(progress.change())
                .readinessUpdatedAt(progress.updatedAt())
                .readinessStrongestArea(strongestArea)
                .readinessWeakestArea(weakestArea)
                .readinessNextGoal(nextGoal(weakestArea, latestReview,
                        mockInterviewsCompleted, gitHubUsername,
                        leetcodeSolved, leetCodeUsername,
                        totalJobApplications, totalStudyTasks))
                .readinessModules(modules)
                .readinessStrengths(strengths)
                .readinessImprovements(improvements)
                .readinessRecommendations(recommendations)
                .build();
    }

    /**
     * Resolves the next upcoming (future, non-cancelled) interview across
     * all of the user's applications, or {@code null} if none is scheduled.
     *
     * @param user the authenticated user
     * @return the upcoming interview summary, or {@code null}
     */
    private UpcomingInterviewResponse buildUpcomingInterview(final User user) {
        final List<InterviewSchedule> upcoming = interviewScheduleRepository
                .findByApplication_UserAndCancelledFalseAndScheduledDateGreaterThanEqualOrderByScheduledDateAscScheduledTimeAsc(
                        user, LocalDate.now());
        if (upcoming.isEmpty()) {
            return null;
        }
        final InterviewSchedule next = upcoming.getFirst();
        return UpcomingInterviewResponse.builder()
                .applicationId(next.getApplication().getId())
                .companyName(next.getApplication().getCompanyName())
                .jobRole(next.getApplication().getJobRole())
                .scheduledDate(next.getScheduledDate())
                .scheduledTime(next.getScheduledTime())
                .build();
    }

    /**
     * Builds the per-module breakdown of the readiness summary, each entry
     * aggregating real data from an existing platform module.
     *
     * @param latestReview           the most recent AI resume review, or null
     * @param resumeCompletion       the resume completion percentage
     * @param mockInterviews         the number of completed mock interviews
     * @param githubRepositories     the number of GitHub repositories
     * @param leetcodeSolved         the number of LeetCode problems solved
     * @param leetcodeUsername       the linked LeetCode username, or null
     * @param studyCompletionRate    the study task completion percentage
     * @param totalJobApplications   the total number of job applications
     * @return the ordered list of module summaries
     */
    private List<ReadinessModuleResponse> buildModuleBreakdown(
            final ResumeReview latestReview, final Integer resumeCompletion,
            final int mockInterviews, final Double mockInterviewAverageScore,
            final int githubRepositories, final int leetcodeSolved,
            final String leetcodeUsername, final int studyCompletionRate,
            final int totalJobApplications) {
        final List<ReadinessModuleResponse> modules = new ArrayList<>(7);

        // Resume ATS
        final Integer atsScore = latestReview == null ? null : latestReview.getAtsScore();
        modules.add(ReadinessModuleResponse.builder()
                .key("RESUME_ATS")
                .label("Resume ATS")
                .value(atsScore == null ? "Not Reviewed" : atsScore + " / 100")
                .score(atsScore == null ? 0 : atsScore)
                .build());

        // Resume Completion
        modules.add(ReadinessModuleResponse.builder()
                .key("RESUME_COMPLETION")
                .label("Resume Completion")
                .value(resumeCompletion + "%")
                .score(resumeCompletion)
                .build());

        // Mock Interview
        modules.add(ReadinessModuleResponse.builder()
                .key("MOCK_INTERVIEW")
                .label("Mock Interview")
                .value(mockInterviews + (mockInterviews == 1 ? " Interview" : " Interviews"))
                .score(mockInterviewScore(mockInterviews, mockInterviewAverageScore))
                .build());

        // GitHub
        modules.add(ReadinessModuleResponse.builder()
                .key("GITHUB")
                .label("GitHub")
                .value(githubRepositories
                        + (githubRepositories == 1 ? " Repository" : " Repositories"))
                .score(Math.min(githubRepositories * 10, 100))
                .build());

        // LeetCode
        final boolean leetCodeLinked = leetcodeUsername != null && !leetcodeUsername.isBlank();
        modules.add(ReadinessModuleResponse.builder()
                .key("LEETCODE")
                .label("LeetCode")
                .value(leetCodeLinked ? leetcodeSolved + " Problems" : "Not Connected")
                .score(Math.min(leetcodeSolved, 100))
                .build());

        // Study Planner
        modules.add(ReadinessModuleResponse.builder()
                .key("STUDY_PLANNER")
                .label("Study Planner")
                .value(studyCompletionRate + "%")
                .score(studyCompletionRate)
                .build());

        // Job Applications
        modules.add(ReadinessModuleResponse.builder()
                .key("JOB_APPLICATIONS")
                .label("Job Applications")
                .value(String.valueOf(totalJobApplications))
                .score(Math.min(totalJobApplications * 10, 100))
                .build());

        return modules;
    }

    /**
     * Identifies genuine strengths from the user's actual module data.
     *
     * @return the list of strengths, empty if the user has none yet
     */
    private List<String> identifyStrengths(final ResumeReview latestReview,
                                           final int resumeCompletion,
                                           final int mockInterviews,
                                           final int githubRepositories,
                                           final int totalStudyTasks,
                                           final int studyCompletionRate,
                                           final int totalJobApplications) {
        final List<String> strengths = new ArrayList<>();

        if (resumeCompletion == 100) {
            strengths.add("Resume completed");
        }
        final Integer atsScore = latestReview == null ? null : latestReview.getAtsScore();
        if (atsScore != null && atsScore >= STRONG_ATS_SCORE) {
            strengths.add("Strong ATS score");
        }
        if (mockInterviews > 0) {
            strengths.add("Interview practice");
        }
        if (githubRepositories > 0) {
            strengths.add("Active GitHub profile");
        }
        if (totalStudyTasks > 0 && studyCompletionRate >= 50) {
            strengths.add("Consistent study progress");
        }
        if (totalJobApplications > 0) {
            strengths.add("Actively applying to jobs");
        }

        return strengths;
    }

    /**
     * Identifies improvement areas strictly from the user's actual module
     * data — a module is only flagged when the underlying data shows a gap.
     *
     * @return the list of improvement areas, empty if none apply
     */
    private List<String> identifyImprovements(final ResumeReview latestReview,
                                              final int resumeCompletion,
                                              final int mockInterviews,
                                              final int githubRepositories,
                                              final String githubUsername,
                                              final int leetcodeSolved,
                                              final String leetcodeUsername,
                                              final int totalStudyTasks,
                                              final int studyCompletionRate,
                                              final int totalJobApplications) {
        final List<String> improvements = new ArrayList<>();

        final Integer atsScore = latestReview == null ? null : latestReview.getAtsScore();
        if (atsScore == null) {
            improvements.add("Run your first ATS resume review");
        } else if (atsScore < STRONG_ATS_SCORE) {
            improvements.add("Improve ATS score above " + STRONG_ATS_SCORE);
        }
        if (resumeCompletion < 100) {
            improvements.add("Complete your resume");
        }
        if (mockInterviews == 0) {
            improvements.add("Complete your first mock interview");
        }
        if (githubUsername == null || githubUsername.isBlank()) {
            improvements.add("Connect your GitHub account");
        } else if (githubRepositories == 0) {
            improvements.add("Add a GitHub project");
        }
        if (leetcodeUsername == null || leetcodeUsername.isBlank()) {
            improvements.add("Connect your LeetCode account");
        } else if (leetcodeSolved == 0) {
            improvements.add("Solve your first LeetCode problem");
        }
        if (totalStudyTasks == 0) {
            improvements.add("Create your first study plan");
        } else if (studyCompletionRate < 100) {
            improvements.add("Finish study planner tasks");
        }
        if (totalJobApplications == 0) {
            improvements.add("Apply for your first job");
        } else if (totalJobApplications < 5) {
            improvements.add("Apply for more jobs");
        }

        return improvements;
    }

    /**
     * Generates three to five personalized recommendations from the user's
     * module progress, ordered by priority so the first items are the most
     * impactful next steps.
     *
     * @return the prioritized recommendation list, capped at five items
     */
    private List<String> buildRecommendations(final ResumeReview latestReview,
                                              final int resumeCompletion,
                                              final int mockInterviews,
                                              final int githubRepositories,
                                              final int leetcodeSolved,
                                              final int totalStudyTasks,
                                              final int studyCompletionRate,
                                              final int totalJobApplications) {
        final List<String> recommendations = new ArrayList<>();

        if (mockInterviews == 0) {
            recommendations.add("Complete one mock interview.");
        }
        if (leetcodeSolved < 5) {
            recommendations.add("Solve five LeetCode problems.");
        }
        final Integer atsScore = latestReview == null ? null : latestReview.getAtsScore();
        if (atsScore == null) {
            recommendations.add("Run your first ATS resume review.");
        } else if (atsScore < STRONG_ATS_SCORE) {
            recommendations.add("Improve your ATS score above " + STRONG_ATS_SCORE + ".");
        }
        if (resumeCompletion < 100) {
            recommendations.add("Complete your resume fields.");
        }
        if (totalJobApplications < 3) {
            recommendations.add("Apply to three companies.");
        }
        if (githubRepositories < 3) {
            recommendations.add("Add another GitHub project.");
        }
        if (totalStudyTasks == 0) {
            recommendations.add("Create your first study plan.");
        } else if (studyCompletionRate < 100) {
            recommendations.add("Finish your pending study tasks.");
        }

        return recommendations.stream().limit(5).toList();
    }

    /**
     * Returns the name of the module with the highest normalized score, or
     * {@code null} when the user has no meaningful activity in any module.
     *
     * @param modules the module breakdown
     * @return the strongest module label, or {@code null}
     */
    private String strongestArea(final List<ReadinessModuleResponse> modules) {
        final ReadinessModuleResponse best = modules.stream()
                .max(Comparator.comparingInt(ReadinessModuleResponse::getScore))
                .orElse(null);
        return best == null || best.getScore() == 0 ? null : best.getLabel();
    }

    /**
     * Returns the name of the module with the lowest normalized score.
     *
     * @param modules the module breakdown
     * @return the weakest module label, or {@code null} if no modules exist
     */
    private String weakestArea(final List<ReadinessModuleResponse> modules) {
        final ReadinessModuleResponse worst = modules.stream()
                .min(Comparator.comparingInt(ReadinessModuleResponse::getScore))
                .orElse(null);
        return worst == null ? null : worst.getLabel();
    }

    /**
     * Derives an actionable next goal from the user's weakest module.
     *
     * @param weakestArea          the weakest module label, or {@code null}
     * @param latestReview         the most recent AI resume review, or null
     * @param mockInterviews       the number of completed mock interviews
     * @param githubUsername       the linked GitHub username, or null
     * @param leetcodeSolved       the number of LeetCode problems solved
     * @param leetcodeUsername     the linked LeetCode username, or null
     * @param totalJobApplications the total number of job applications
     * @param totalStudyTasks      the total number of study tasks
     * @return a short goal sentence
     */
    private String nextGoal(final String weakestArea, final ResumeReview latestReview,
                            final int mockInterviews, final String githubUsername,
                            final int leetcodeSolved, final String leetcodeUsername,
                            final int totalJobApplications, final int totalStudyTasks) {
        if (weakestArea == null) {
            return "Keep building your profile";
        }
        return switch (weakestArea) {
            case "Resume ATS" -> {
                if (latestReview == null) {
                    yield "Run your first ATS resume review";
                }
                yield latestReview.getAtsScore() < STRONG_ATS_SCORE
                        ? "Improve your ATS score above " + STRONG_ATS_SCORE
                        : "Keep your resume reviewed";
            }
            case "Resume Completion" -> "Complete your resume";
            case "Mock Interview" -> mockInterviews == 0
                    ? "Complete your first mock interview"
                    : "Keep practising mock interviews";
            case "GitHub" -> githubUsername == null || githubUsername.isBlank()
                    ? "Connect your GitHub account"
                    : "Add a GitHub project";
            case "LeetCode" -> leetcodeUsername == null || leetcodeUsername.isBlank()
                    ? "Connect your LeetCode account"
                    : leetcodeSolved == 0
                            ? "Solve your first LeetCode problem"
                            : "Solve more LeetCode problems";
            case "Study Planner" -> totalStudyTasks == 0
                    ? "Create your first study plan"
                    : "Finish your study planner tasks";
            case "Job Applications" -> totalJobApplications == 0
                    ? "Apply for your first job"
                    : "Apply for more jobs";
            default -> "Keep building your profile";
        };
    }

    /**
     * Compares the current readiness score against the user's latest
     * recorded snapshot, records a new snapshot when the score changed,
     * and raises a notification when the score improves significantly or
     * reaches a higher readiness level.
     *
     * @param user          the authenticated user
     * @param currentScore  the freshly computed readiness score
     * @return the previous score, change, and last-updated time for the card
     */
    private ProgressRecord trackProgress(final User user, final int currentScore) {
        final Optional<ReadinessSnapshot> latest =
                readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user);

        // First measurement — nothing to compare against yet, just record it.
        if (latest.isEmpty()) {
            readinessSnapshotRepository.save(ReadinessSnapshot.builder()
                    .user(user)
                    .score(currentScore)
                    .build());
            // The first score also feeds the gamification engine so the
            // Placement Ready badge can unlock on the very first measurement.
            eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                    new ActivityEvent(user.getId(), ActivityType.PLACEMENT_UPDATED,
                            currentScore, LocalDateTime.now()));
            return new ProgressRecord(null, null, null);
        }

        final ReadinessSnapshot previous = latest.get();

        if (previous.getScore() == currentScore) {
            // Unchanged — keep the existing snapshot as the last-updated record.
            return new ProgressRecord(previous.getScore(), 0, previous.getCreatedAt());
        }

        final int change = currentScore - previous.getScore();
        final boolean levelUp = readinessLevelRank(currentScore)
                > readinessLevelRank(previous.getScore());
        final boolean significant = change >= SIGNIFICANT_IMPROVEMENT_POINTS;

        if (levelUp) {
            eventPublisher.publish(EventTopics.READINESS_MILESTONE_KEY,
                    new NotificationEvent(user.getId(), NotificationType.READINESS,
                            "Placement readiness level up",
                            "Your placement readiness has reached \"" + readinessStatus(currentScore)
                                    + "\" (" + currentScore + "/100)."));
        } else if (significant) {
            eventPublisher.publish(EventTopics.READINESS_MILESTONE_KEY,
                    new NotificationEvent(user.getId(), NotificationType.READINESS,
                            "Placement readiness improved",
                            "Your placement readiness improved by " + change + " points to "
                                    + currentScore + "/100. Keep it up!"));
        }

        final ReadinessSnapshot snapshot = readinessSnapshotRepository.saveAndFlush(
                ReadinessSnapshot.builder()
                        .user(user)
                        .score(currentScore)
                        .build());

        // Publish the gamification activity; the consumer awards XP and
        // evaluates the Placement Ready achievement asynchronously.
        eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                new ActivityEvent(user.getId(), ActivityType.PLACEMENT_UPDATED,
                        currentScore, LocalDateTime.now()));

        return new ProgressRecord(previous.getScore(), change, snapshot.getCreatedAt());
    }

    /**
     * Returns the rank of a readiness score, used to detect level ups and to
     * derive the status label. Higher is better: Needs Improvement &lt;
     * Improving &lt; Placement Ready &lt; Excellent. This is the single
     * source of truth for the readiness level thresholds.
     *
     * @param score the placement readiness score (0–100)
     * @return the level rank (1–4)
     */
    private int readinessLevelRank(final int score) {
        if (score >= EXCELLENT_MIN_SCORE) {
            return 4;
        }
        if (score >= PLACEMENT_READY_MIN_SCORE) {
            return 3;
        }
        if (score >= IMPROVING_MIN_SCORE) {
            return 2;
        }
        return 1;
    }

    /**
     * Derives a human-readable readiness level from the overall score,
     * using the same thresholds as {@link #readinessLevelRank(int)}.
     *
     * @param score the placement readiness score (0–100)
     * @return the level label
     */
    private String readinessStatus(final int score) {
        return switch (readinessLevelRank(score)) {
            case 4 -> "Excellent";
            case 3 -> "Placement Ready";
            case 2 -> "Improving";
            default -> "Needs Improvement";
        };
    }

    /**
     * Derives a human-readable quality status from the latest resume review.
     *
     * @param latestReview the most recent resume review, or {@code null}
     * @return a status label describing the latest ATS score
     */
    private String resumeQualityStatus(final ResumeReview latestReview) {
        if (latestReview == null) {
            return "Not Reviewed";
        }
        final int score = latestReview.getAtsScore();
        if (score >= 80) {
            return "Excellent";
        }
        if (score >= 60) {
            return "Good";
        }
        if (score >= 40) {
            return "Needs Improvement";
        }
        return "Action Required";
    }

    /**
     * Calculates the resume completion percentage based on how many of the
     * five optional fields (headline, summary, LinkedIn URL, GitHub URL,
     * portfolio URL) are filled in the user's most complete resume.
     * <p>
     * If the user has no resumes, returns 0. Each of the five fields is
     * weighted equally at 20%.
     * </p>
     *
     * @param user the authenticated user
     * @return an integer between 0 and 100 representing the completion
     *         percentage of the most complete resume
     */
    private Integer calculateResumeCompletion(final User user) {
        final List<Resume> resumes = resumeRepository.findByUser(user);

        if (resumes.isEmpty()) {
            return 0;
        }

        // Find the highest completion score across all resumes
        int maxFilled = 0;

        for (final Resume resume : resumes) {
            int filled = 0;

            if (resume.getHeadline() != null && !resume.getHeadline().isBlank()) {
                filled++;
            }
            if (resume.getSummary() != null && !resume.getSummary().isBlank()) {
                filled++;
            }
            if (resume.getLinkedinUrl() != null && !resume.getLinkedinUrl().isBlank()) {
                filled++;
            }
            if (resume.getGithubUrl() != null && !resume.getGithubUrl().isBlank()) {
                filled++;
            }
            if (resume.getPortfolioUrl() != null && !resume.getPortfolioUrl().isBlank()) {
                filled++;
            }

            maxFilled = Math.max(maxFilled, filled);
        }

        // Scale to percentage (5 fields = 20% each)
        return (maxFilled * 100) / 5;
    }

    /**
     * Calculates the overall placement readiness score as a weighted composite
     * of the available module metrics.
     * <p>
     * The scoring breakdown is as follows:
     * <ul>
     *   <li><b>Resume completeness:</b> 25% — weighted directly from the
     *       resume completion percentage</li>
     *   <li><b>Job applications:</b> 15% — 10% for having at least one
     *       application, plus 5% for having at least one interview</li>
     *   <li><b>Study tasks:</b> 15% — weighted by the study task completion
     *       rate (completed / total), or 0 if no tasks exist</li>
     *   <li><b>GitHub presence:</b> 10% — 7% for having 1+ repository,
     *       plus 3% for having a detected top language</li>
     *   <li><b>LeetCode activity:</b> 10% — capped at 10% for 100+ problems
     *       solved, scaled linearly for fewer</li>
     *   <li><b>Mock interviews:</b> 25% — scaled by the average interview
     *       score and the number of completed interviews, so interview
     *       performance directly moves the overall score</li>
     * </ul>
     * The final score is clamped to the 0–100 range.
     * </p>
     *
     * @param resumeCompletion        the resume completion percentage (0–100)
     * @param totalApplications       the total number of job applications
     * @param interviewApps           the number of applications at interview stage
     * @param totalTasks              the total number of study tasks
     * @param completedTasks          the number of completed study tasks
     * @param githubRepos             the number of GitHub repositories
     * @param githubTopLanguage       the primary GitHub language (may be null)
     * @param leetcodeSolved          the number of LeetCode problems solved
     * @param mockInterviews          the number of completed mock interviews
     * @param mockInterviewAverage    the average mock interview score, or null
     * @return an integer between 0 and 100 representing placement readiness
     */
    private int calculatePlacementReadiness(final Integer resumeCompletion,
                                            final int totalApplications,
                                            final int interviewApps,
                                            final int totalTasks,
                                            final int completedTasks,
                                            final int githubRepos,
                                            final String githubTopLanguage,
                                            final int leetcodeSolved,
                                            final int mockInterviews,
                                            final Double mockInterviewAverage) {
        int score = 0;

        // 1. Resume completeness (max 25 points)
        score += (int) Math.round(resumeCompletion * 0.25);

        // 2. Job applications (max 15 points)
        if (totalApplications > 0) {
            score += 10;
        }
        if (interviewApps > 0) {
            score += 5;
        }

        // 3. Study tasks (max 15 points) — completion rate
        if (totalTasks > 0) {
            final int taskScore = (int) Math.round(
                    (double) completedTasks / totalTasks * 15);
            score += taskScore;
        }

        // 4. GitHub presence (max 10 points)
        if (githubRepos > 0) {
            score += Math.min(githubRepos, 7);
        }
        if (githubTopLanguage != null) {
            score += 3;
        }

        // 5. LeetCode problems solved (max 10 points)
        score += Math.min(leetcodeSolved, 10);

        // 6. Mock interview performance (max 25 points)
        score += mockInterviewReadinessPoints(mockInterviews, mockInterviewAverage);

        // Clamp to valid range
        return Math.min(Math.max(score, 0), 100);
    }

    /**
     * Computes the 0–100 mock interview module score from the number of
     * completed interviews and their average score. The score rewards both
     * performance (the average) and consistency (the count).
     *
     * @param mockInterviews       the number of completed mock interviews
     * @param mockInterviewAverage the average interview score, or null
     * @return the module score (0–100)
     */
    private int mockInterviewScore(final int mockInterviews,
                                   final Double mockInterviewAverage) {
        if (mockInterviews == 0 || mockInterviewAverage == null) {
            return 0;
        }
        final double average = mockInterviewAverage;
        final int performance = (int) Math.round(average * 0.5);
        final int consistency = Math.min(mockInterviews * 10, 50);
        return Math.min(performance + consistency, 100);
    }

    /**
     * Computes the mock interview contribution to the overall placement
     * readiness score (capped at 25 points). Rewards both performance (the
     * average score) and consistency (the number of interviews).
     *
     * @param mockInterviews       the number of completed mock interviews
     * @param mockInterviewAverage the average interview score, or null
     * @return the readiness points (0–25)
     */
    private int mockInterviewReadinessPoints(final int mockInterviews,
                                             final Double mockInterviewAverage) {
        if (mockInterviews == 0 || mockInterviewAverage == null) {
            return 0;
        }
        final double average = mockInterviewAverage;
        final int performance = (int) Math.round(average * 0.18);
        final int consistency = (int) Math.min(mockInterviews * 3.5, 7);
        return Math.min(performance + consistency, 25);
    }

    /**
     * Derives a short, data-driven recommendation from the user's mock
     * interview history for the dashboard widget.
     *
     * @param mockInterviews     the number of completed mock interviews
     * @param mockInterviewAverage the average interview score, or null
     * @param latestScore        the most recent interview score, or null
     * @return the recommendation text
     */
    private String mockInterviewInsight(final int mockInterviews,
                                        final Double mockInterviewAverage,
                                        final Integer latestScore) {
        if (mockInterviews == 0 || mockInterviewAverage == null) {
            return "Complete your first mock interview to unlock your interview readiness.";
        }
        if (latestScore != null && latestScore >= 80) {
            return "Great momentum — your latest interview scored " + latestScore
                    + "/100. Keep the streak going!";
        }
        if (mockInterviewAverage >= 70) {
            return "Consistent performance — your average is " + mockInterviewAverage
                    + "/100. Try a harder difficulty to level up.";
        }
        return "Keep practising — your average is " + mockInterviewAverage
                + "/100. Focus on your weakest topics each session.";
    }

    /**
     * Retrieves the currently authenticated user from the database.
     * <p>
     * Extracts the username (email) from the {@link SecurityContextHolder},
     * fetches the corresponding {@link User} entity from the repository,
     * and throws a {@link ResourceNotFoundException} if no matching user
     * is found.
     * </p>
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedUser() {
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

    /**
     * Immutable result of comparing the current readiness score with the
     * previously recorded snapshot.
     *
     * @param previousScore the previous recorded score, or {@code null}
     * @param change        the score difference, or {@code null}
     * @param updatedAt     the last-updated timestamp, or {@code null}
     */
    private record ProgressRecord(Integer previousScore, Integer change,
                                  LocalDateTime updatedAt) {
    }

}
