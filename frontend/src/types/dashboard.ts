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
 * The stable identifiers of the modules contributing to the placement
 * readiness summary.
 */
export type ReadinessModuleKey =
  | 'RESUME_ATS'
  | 'RESUME_COMPLETION'
  | 'MOCK_INTERVIEW'
  | 'GITHUB'
  | 'LEETCODE'
  | 'STUDY_PLANNER'
  | 'JOB_APPLICATIONS';

/**
 * One module's contribution to the placement readiness summary.
 *
 * Aggregates real data from an existing platform module into a display
 * value plus a normalized 0–100 score used for colour coding.
 */
export interface ReadinessModule {
  /** Stable module identifier, e.g. 'RESUME_ATS' or 'MOCK_INTERVIEW'. */
  key: ReadinessModuleKey;
  /** Human-readable module name, e.g. 'Resume ATS'. */
  label: string;
  /** Display value built from real user data, e.g. '67 / 100' or 'Not Connected'. */
  value: string;
  /** Normalized 0–100 score used for colour coding (not part of the readiness formula). */
  score: number;
}

/**
 * A recent job application shown on the dashboard job tracker widget.
 */
export interface RecentApplication {
  id: number;
  companyName: string;
  jobRole: string;
  status: ApplicationStatusValue;
  applicationDate: string | null;
}

/**
 * The next upcoming interview shown on the dashboard job tracker widget.
 */
export interface UpcomingInterview {
  applicationId: number;
  companyName: string;
  jobRole: string;
  scheduledDate: string;
  scheduledTime: string | null;
}

/** Job application status values used by the dashboard widget. */
export type ApplicationStatusValue =
  | 'WISHLIST'
  | 'APPLIED'
  | 'ASSESSMENT'
  | 'INTERVIEW'
  | 'OFFER'
  | 'REJECTED';

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

  /** Number of mock interviews completed. */
  mockInterviewCount: number;

  /** Overall score of the most recent mock interview, or null. */
  mockInterviewLatestScore: number | null;

  /** Average mock interview score, or null when none completed yet. */
  mockInterviewAverageScore: number | null;

  /** Best mock interview score achieved, or null. */
  mockInterviewBestScore: number | null;

  /** Score difference between the two most recent interviews, or null. */
  mockInterviewTrend: number | null;

  /** Short data-driven recommendation from the interview history. */
  mockInterviewInsight: string | null;

  /** Number of job applications that reached the interview stage. */
  interviewApplications: number;

  /** Number of job applications that reached the offer stage. */
  offerApplications: number | null;

  /** Number of job applications currently in the assessment stage. */
  assessmentApplications: number | null;

  /** The three most recently added job applications, or an empty list. */
  recentApplications: RecentApplication[];

  /** The next upcoming interview across all applications, or null. */
  upcomingInterview: UpcomingInterview | null;

  /** Total number of study planner tasks created. */
  totalStudyTasks: number;

  /** Number of study planner tasks marked as completed. */
  completedStudyTasks: number;

  /** GitHub username linked to the account, or null if none connected. */
  githubUsername: string | null;

  /** Number of public GitHub repositories (0 if no GitHub linked). */
  githubRepositories: number;

  /** Primary programming language across GitHub repos, or null. */
  githubTopLanguage: string | null;

  /** Number of GitHub followers, or null if no account linked. */
  githubFollowers: number | null;

  /** Number of GitHub users followed, or null if no account linked. */
  githubFollowing: number | null;

  /** LeetCode username linked to the account, or null if none connected. */
  leetcodeUsername: string | null;

  /** Number of LeetCode problems solved (0 if no LeetCode linked). */
  leetcodeSolved: number;

  /** Number of easy-difficulty LeetCode problems solved, or null. */
  leetcodeEasySolved: number | null;

  /** Number of medium-difficulty LeetCode problems solved, or null. */
  leetcodeMediumSolved: number | null;

  /** Number of hard-difficulty LeetCode problems solved, or null. */
  leetcodeHardSolved: number | null;

  /** Global LeetCode ranking, or null if unavailable/not linked. */
  leetcodeRanking: number | null;

  /** LeetCode acceptance rate percentage, or null if unavailable/not linked. */
  leetcodeAcceptanceRate: number | null;

  /** Overall placement readiness score (0–100). */
  placementReadiness: number;

  /** ATS score of the most recent AI resume review, or null if none yet. */
  atsScore: number | null;

  /** ISO-8601 timestamp of the most recent AI resume review, or null. */
  atsReviewedAt: string | null;

  /** Human-readable quality status from the latest ATS score. */
  resumeQualityStatus: string | null;

  /** Placement readiness level: Excellent / Placement Ready / Improving / Needs Improvement. */
  readinessStatus: string | null;

  /** Previously recorded readiness score, or null if none exists yet. */
  readinessPrevious: number | null;

  /** Current minus previous readiness score, or null if no previous score exists. */
  readinessChange: number | null;

  /** ISO-8601 timestamp of the last readiness score update, or null. */
  readinessUpdatedAt: string | null;

  /** Name of the module contributing most to readiness, or null. */
  readinessStrongestArea: string | null;

  /** Name of the module contributing least to readiness, or null. */
  readinessWeakestArea: string | null;

  /** Short actionable goal derived from the weakest module, or null. */
  readinessNextGoal: string | null;

  /** Per-module breakdown of the readiness summary. */
  readinessModules: ReadinessModule[];

  /** Automatically identified strengths from real module data. */
  readinessStrengths: string[];

  /** Automatically identified improvement areas from real module data. */
  readinessImprovements: string[];

  /** Three to five personalized recommendations, ordered by priority. */
  readinessRecommendations: string[];
}
