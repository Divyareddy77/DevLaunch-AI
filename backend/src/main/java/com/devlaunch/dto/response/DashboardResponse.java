package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
     * The total number of public GitHub repositories associated with the
     * user's linked GitHub account. Retrieved live from the GitHub API
     * using the username extracted from the user's resume.
     */
    private Integer githubRepositories;

    /**
     * The primary programming language used across the user's GitHub
     * repositories, determined by the language with the highest repository
     * count. May be {@code null} if no GitHub account is linked or if no
     * language data is available.
     */
    private String githubTopLanguage;

    /**
     * The total number of LeetCode problems solved by the user.
     * <p>
     * This field is reserved for a future phase when LeetCode account
     * linking is added to the user profile. Currently defaults to 0.
     * Once a {@code leetcodeUsername} field is available on the
     * {@link com.devlaunch.entity.User} entity, this value will be
     * populated live from the LeetCode GraphQL API.
     * </p>
     */
    private Integer leetcodeSolved;

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

}
