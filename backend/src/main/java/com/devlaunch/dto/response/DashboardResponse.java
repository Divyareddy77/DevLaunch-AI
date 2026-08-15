package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for the achievement dashboard overview.
 * <p>
 * Aggregates key metrics from across the DevLaunch platform into a single
 * summary object, including resume completeness, job application status,
 * study task progress, GitHub statistics, LeetCode activity, and an overall
 * placement readiness score. This data is calculated live from the
 * authenticated user's existing data and requires no additional persistence.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    /**
     * The completion percentage of the user's most complete resume,
     * calculated as the proportion of filled optional fields (headline,
     * summary, LinkedIn URL, GitHub URL, portfolio URL) out of the total
     * possible fields. Ranges from 0 to 100.
     */
    private Integer resumeCompletion;

    /**
     * The total number of job applications created by the user.
     */
    private Integer totalJobApplications;

    /**
     * The number of mock interviews the user has completed.
     */
    private Integer mockInterviewCount;

    /**
     * The overall score of the user's most recent mock interview,
     * or {@code null} when no interview has been completed yet.
     */
    private Integer mockInterviewLatestScore;

    /**
     * The average mock interview score across all completed interviews,
     * or {@code null} when no interview has been completed yet.
     */
    private Double mockInterviewAverageScore;

    /**
     * The best mock interview score achieved, or {@code null} when no
     * interview has been completed yet.
     */
    private Integer mockInterviewBestScore;

    /**
     * The score difference between the two most recent interviews (latest
     * minus previous), or {@code null} when fewer than two interviews have
     * been completed.
     */
    private Integer mockInterviewTrend;

    /**
     * A short, data-driven recommendation derived from the user's mock
     * interview history.
     */
    private String mockInterviewInsight;

    /**
     * The number of job applications that have reached the interview stage.
     */
    private Integer interviewApplications;

    /**
     * The total number of study planner tasks created by the user.
     */
    private Integer totalStudyTasks;

    /**
     * The number of study planner tasks that have been marked as completed.
     */
    private Integer completedStudyTasks;

    /**
     * The GitHub username linked to the user's account, or {@code null}
     * if no GitHub account is connected yet. The dashboard only fetches
     * live GitHub statistics when this is present.
     */
    private String githubUsername;

    /**
     * The total number of public GitHub repositories associated with the
     * user's linked GitHub account. Retrieved live from the GitHub API
     * using the linked username.
     */
    private Integer githubRepositories;

    /**
     * The number of job applications that reached the offer stage.
     */
    private Integer offerApplications;

    /**
     * The number of job applications currently in the assessment stage.
     */
    private Integer assessmentApplications;

    /**
     * The three most recently added job applications, used by the dashboard
     * job tracker widget. Empty when the user has no applications.
     */
    private List<RecentApplicationResponse> recentApplications;

    /**
     * The next upcoming (future, non-cancelled) interview across all of the
     * user's applications, or {@code null} if none is scheduled.
     */
    private UpcomingInterviewResponse upcomingInterview;

    /**
     * The primary programming language used across the user's GitHub
     * repositories, determined by the language with the highest repository
     * count. May be {@code null} if no GitHub account is linked or if no
     * language data is available.
     */
    private String githubTopLanguage;

    /**
     * The number of followers of the user's linked GitHub account,
     * or {@code null} if no account is linked.
     */
    private Integer githubFollowers;

    /**
     * The number of users the linked GitHub account follows,
     * or {@code null} if no account is linked.
     */
    private Integer githubFollowing;

    /**
     * The LeetCode username linked to the user's account, or {@code null}
     * if no LeetCode account is connected yet. The dashboard only fetches
     * live LeetCode statistics when this is present.
     */
    private String leetcodeUsername;

    /**
     * The total number of LeetCode problems solved by the user, retrieved
     * live from the LeetCode GraphQL API using the linked username.
     */
    private Integer leetcodeSolved;

    /**
     * The number of easy-difficulty LeetCode problems solved by the user,
     * or {@code null} if no LeetCode account is linked.
     */
    private Integer leetcodeEasySolved;

    /**
     * The number of medium-difficulty LeetCode problems solved by the user,
     * or {@code null} if no LeetCode account is linked.
     */
    private Integer leetcodeMediumSolved;

    /**
     * The number of hard-difficulty LeetCode problems solved by the user,
     * or {@code null} if no LeetCode account is linked.
     */
    private Integer leetcodeHardSolved;

    /**
     * The user's global LeetCode ranking, or {@code null} if no account is
     * linked or the ranking is unavailable.
     */
    private Integer leetcodeRanking;

    /**
     * The user's LeetCode acceptance rate as a percentage, or {@code null}
     * if no account is linked or the value could not be determined.
     */
    private Double leetcodeAcceptanceRate;

    /**
     * An overall placement readiness score from 0 to 100, calculated as a
     * weighted composite of resume completeness, job application activity,
     * study task progress, GitHub presence, and LeetCode problem-solving.
     * A higher score indicates greater readiness for the job market.
     */
    private Integer placementReadiness;

    /**
     * The Applicant Tracking System score of the user's most recent AI
     * resume review, or {@code null} if no review has been completed yet.
     */
    private Integer atsScore;

    /**
     * The date and time of the user's most recent AI resume review,
     * or {@code null} if no review has been completed yet.
     */
    private LocalDateTime atsReviewedAt;

    /**
     * A human-readable quality status derived from the latest ATS score
     * (e.g. "Excellent", "Good", "Needs Improvement", "Not Reviewed").
     */
    private String resumeQualityStatus;

    /**
     * A human-readable placement readiness level derived from the overall
     * score: "Excellent" (90–100), "Placement Ready" (75–89),
     * "Improving" (60–74), or "Needs Improvement" (below 60).
     */
    private String readinessStatus;

    /**
     * The placement readiness score recorded on the previous measurement,
     * or {@code null} if the user has no prior snapshot yet.
     */
    private Integer readinessPrevious;

    /**
     * The difference between the current and previous readiness scores
     * (current minus previous), or {@code null} if no previous score
     * exists.
     */
    private Integer readinessChange;

    /**
     * The date and time the readiness score was last updated (i.e. the
     * most recent measurement in which the score changed), or
     * {@code null} if no snapshot has been recorded yet.
     */
    private LocalDateTime readinessUpdatedAt;

    /**
     * The name of the module with the highest readiness contribution
     * (e.g. "Resume Completion"), or {@code null} when the user has no
     * meaningful activity in any module yet.
     */
    private String readinessStrongestArea;

    /**
     * The name of the module with the lowest readiness contribution
     * (e.g. "Mock Interview"), or {@code null} if no modules exist.
     */
    private String readinessWeakestArea;

    /**
     * A short, actionable goal derived from the user's weakest module
     * (e.g. "Complete your first mock interview").
     */
    private String readinessNextGoal;

    /**
     * The per-module breakdown of the readiness summary, each entry
     * aggregating real data from an existing platform module.
     */
    private List<ReadinessModuleResponse> readinessModules;

    /**
     * The automatically identified strengths, derived strictly from the
     * user's actual module data (e.g. "Resume completed").
     */
    private List<String> readinessStrengths;

    /**
     * The automatically identified improvement areas, derived strictly
     * from the user's actual module data (e.g. "Complete your first
     * mock interview").
     */
    private List<String> readinessImprovements;

    /**
     * Three to five personalized recommendations generated from the
     * user's module progress, ordered by priority.
     */
    private List<String> readinessRecommendations;

}
