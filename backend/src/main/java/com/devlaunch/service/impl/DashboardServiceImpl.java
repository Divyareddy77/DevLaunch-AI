package com.devlaunch.service.impl;

import com.devlaunch.dto.response.DashboardResponse;
import com.devlaunch.dto.response.GitHubProfileResponse;
import com.devlaunch.dto.response.LanguageStatisticsResponse;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.JobApplicationRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.StudyPlannerRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.DashboardService;
import com.devlaunch.service.interfaces.GitHubService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link DashboardService} providing a comprehensive
 * dashboard summary for the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain the
 * authenticated user's email, then queries existing repositories and
 * services to compile aggregate metrics across resumes, job applications,
 * study tasks, and GitHub activity. LeetCode metrics are reserved for a
 * future phase when LeetCode account linking is added to the user profile.
 * The placement readiness score is calculated on the fly using a weighted
 * formula. No new database tables or entities are introduced.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Pattern GITHUB_USERNAME_PATTERN =
            Pattern.compile("github\\.com/([^/?#]+)");

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final StudyPlannerRepository studyPlannerRepository;
    private final ResumeReviewRepository resumeReviewRepository;
    private final GitHubService gitHubService;

    /**
     * Constructs the dashboard service with the required repositories and
     * external service clients.
     *
     * @param userRepository           repository for user data access
     * @param resumeRepository         repository for resume data access
     * @param jobApplicationRepository repository for job application data access
     * @param studyPlannerRepository   repository for study planner data access
     * @param resumeReviewRepository   repository for AI resume review history
     * @param gitHubService            service for fetching GitHub profile and
     *                                 repository data
     */
    public DashboardServiceImpl(final UserRepository userRepository,
                                final ResumeRepository resumeRepository,
                                final JobApplicationRepository jobApplicationRepository,
                                final StudyPlannerRepository studyPlannerRepository,
                                final ResumeReviewRepository resumeReviewRepository,
                                final GitHubService gitHubService) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.studyPlannerRepository = studyPlannerRepository;
        this.resumeReviewRepository = resumeReviewRepository;
        this.gitHubService = gitHubService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        final User user = getAuthenticatedUser();

        // Gather data from each module
        final Integer resumeCompletion = calculateResumeCompletion(user);
        final List<JobApplication> jobApplications =
                jobApplicationRepository.findByUser(user);
        final List<StudyPlanner> studyPlanners =
                studyPlannerRepository.findByUser(user);
        final String gitHubUsername = extractGitHubUsername(user);

        // Calculate job application metrics
        final int totalJobApplications = jobApplications.size();
        final int interviewApplications = (int) jobApplications.stream()
                .filter(app -> ApplicationStatus.INTERVIEW.equals(app.getStatus()))
                .count();

        // Calculate study task metrics
        final int totalStudyTasks = studyPlanners.size();
        final int completedStudyTasks = (int) studyPlanners.stream()
                .filter(task -> StudyStatus.COMPLETED.equals(task.getStatus()))
                .count();

        // Fetch GitHub statistics (only if a GitHub username is available)
        int githubRepositories = 0;
        String githubTopLanguage = null;

        if (gitHubUsername != null && !gitHubUsername.isBlank()) {
            try {
                final GitHubProfileResponse profile =
                        gitHubService.getGitHubProfile(gitHubUsername);
                githubRepositories = profile.getPublicRepositories() != null
                        ? profile.getPublicRepositories() : 0;

                final List<LanguageStatisticsResponse> languages =
                        gitHubService.getLanguageStatistics(gitHubUsername);
                githubTopLanguage = languages.isEmpty() ? null : languages.getFirst().getLanguage();
            } catch (ResourceNotFoundException e) {
                // GitHub username is invalid or user not found; provide defaults
                githubRepositories = 0;
                githubTopLanguage = null;
            }
        }

        // LeetCode integration — reserved for a future phase.
        // When LeetCode account linking is added to the user profile, replace
        // this default with a live fetch from LeetCodeService using the linked
        // username.
        int leetcodeSolved = 0;

        // Calculate the overall placement readiness score
        final int placementReadiness = calculatePlacementReadiness(
                resumeCompletion,
                totalJobApplications,
                interviewApplications,
                totalStudyTasks,
                completedStudyTasks,
                githubRepositories,
                githubTopLanguage,
                leetcodeSolved);

        // Latest AI resume review for the dashboard ATS summary
        final ResumeReview latestReview = resumeReviewRepository
                .findByUserOrderByCreatedAtDesc(user).stream().findFirst().orElse(null);

        return DashboardResponse.builder()
                .resumeCompletion(resumeCompletion)
                .totalJobApplications(totalJobApplications)
                .interviewApplications(interviewApplications)
                .totalStudyTasks(totalStudyTasks)
                .completedStudyTasks(completedStudyTasks)
                .githubRepositories(githubRepositories)
                .githubTopLanguage(githubTopLanguage)
                .leetcodeSolved(leetcodeSolved)
                .placementReadiness(placementReadiness)
                .atsScore(latestReview == null ? null : latestReview.getAtsScore())
                .atsReviewedAt(latestReview == null ? null : latestReview.getCreatedAt())
                .resumeQualityStatus(resumeQualityStatus(latestReview))
                .build();
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
     *   <li><b>Resume completeness:</b> 30% — weighted directly from the
     *       resume completion percentage</li>
     *   <li><b>Job applications:</b> 20% — 10% for having at least one
     *       application, plus 10% for having at least one interview</li>
     *   <li><b>Study tasks:</b> 20% — weighted by the study task completion
     *       rate (completed / total), or 0 if no tasks exist</li>
     *   <li><b>GitHub presence:</b> 15% — 10% for having 1+ repository,
     *       plus 5% for having a detected top language</li>
     *   <li><b>LeetCode activity:</b> 15% — capped at 15% for 100+ problems
     *       solved, scaled linearly for fewer</li>
     * </ul>
     * The final score is clamped to the 0–100 range.
     * </p>
     *
     * @param resumeCompletion     the resume completion percentage (0–100)
     * @param totalApplications    the total number of job applications
     * @param interviewApps        the number of applications at interview stage
     * @param totalTasks           the total number of study tasks
     * @param completedTasks       the number of completed study tasks
     * @param githubRepos          the number of GitHub repositories
     * @param githubTopLanguage    the primary GitHub language (may be null)
     * @param leetcodeSolved       the number of LeetCode problems solved
     * @return an integer between 0 and 100 representing placement readiness
     */
    private int calculatePlacementReadiness(final Integer resumeCompletion,
                                            final int totalApplications,
                                            final int interviewApps,
                                            final int totalTasks,
                                            final int completedTasks,
                                            final int githubRepos,
                                            final String githubTopLanguage,
                                            final int leetcodeSolved) {
        int score = 0;

        // 1. Resume completeness (max 30 points)
        score += (int) Math.round(resumeCompletion * 0.30);

        // 2. Job applications (max 20 points)
        if (totalApplications > 0) {
            score += 10;
        }
        if (interviewApps > 0) {
            score += 10;
        }

        // 3. Study tasks (max 20 points) — completion rate
        if (totalTasks > 0) {
            final int taskScore = (int) Math.round(
                    (double) completedTasks / totalTasks * 20);
            score += taskScore;
        }

        // 4. GitHub presence (max 15 points)
        if (githubRepos > 0) {
            // 10 points for having repositories
            score += Math.min(githubRepos, 10);
        }
        if (githubTopLanguage != null) {
            score += 5;
        }

        // 5. LeetCode problems solved (max 15 points)
        score += Math.min(leetcodeSolved, 15);

        // Clamp to valid range
        return Math.min(Math.max(score, 0), 100);
    }

    /**
     * Extracts the GitHub username from the user's resume(s).
     * <p>
     * Iterates over all of the user's resumes and extracts the GitHub
     * username from the {@code githubUrl} field using a regular expression
     * matching {@code github.com/<username>}. Returns the first valid
     * username found, or {@code null} if none of the resumes contain a
     * valid GitHub URL.
     * </p>
     *
     * @param user the authenticated user
     * @return the extracted GitHub username, or {@code null} if not found
     */
    private String extractGitHubUsername(final User user) {
        final List<Resume> resumes = resumeRepository.findByUser(user);

        for (final Resume resume : resumes) {
            final String githubUrl = resume.getGithubUrl();
            if (githubUrl != null && !githubUrl.isBlank()) {
                final Matcher matcher = GITHUB_USERNAME_PATTERN.matcher(githubUrl);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }

        return null;
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

}
