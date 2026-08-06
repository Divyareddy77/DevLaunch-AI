/**
 * Type definitions for the achievement dashboard.
 *
 * Mirrors the backend response DTO in
 * com.devlaunch.dto.response.DashboardResponse.
 *
 * @see backend/src/main/java/com/devlaunch/dto/response/DashboardResponse.java
 * @author DevLaunch
 */

/**
 * Aggregated dashboard summary returned from GET /api/dashboard.
 *
 * Contains key metrics from across the DevLaunch platform, compiled
 * live by the backend from the authenticated user's existing data.
 */
export interface DashboardResponse {
  /** Completion percentage of the user's most complete resume (0–100). */
  resumeCompletion: number;

  /** Total number of job applications created. */
  totalJobApplications: number;

  /** Number of job applications that reached the interview stage. */
  interviewApplications: number;

  /** Total number of study planner tasks created. */
  totalStudyTasks: number;

  /** Number of study planner tasks marked as completed. */
  completedStudyTasks: number;

  /** Number of public GitHub repositories (0 if no GitHub linked). */
  githubRepositories: number;

  /** Primary programming language across GitHub repos, or null. */
  githubTopLanguage: string | null;

  /** Number of LeetCode problems solved (reserved for future use; currently 0). */
  leetcodeSolved: number;

  /** Overall placement readiness score (0–100). */
  placementReadiness: number;

  /** ATS score of the most recent AI resume review, or null if none yet. */
  atsScore: number | null;

  /** ISO-8601 timestamp of the most recent AI resume review, or null. */
  atsReviewedAt: string | null;

  /** Human-readable quality status from the latest ATS score. */
  resumeQualityStatus: string | null;
}
