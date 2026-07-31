/**
 * Type definitions for the GitHub Analytics module.
 *
 * Mirrors the backend response DTOs in
 * com.devlaunch.dto.response.GitHubProfileResponse,
 * com.devlaunch.dto.response.RepositoryResponse, and
 * com.devlaunch.dto.response.LanguageStatisticsResponse.
 *
 * @see backend/src/main/java/com/devlaunch/controller/GitHubController.java
 * @author DevLaunch
 */

/**
 * GitHub user profile response from GET /api/github/{username}.
 *
 * Contains public profile data fetched live from the GitHub REST API.
 */
export interface GitHubProfileResponse {
  /** The GitHub login/username (e.g. "octocat"). */
  username: string;

  /** The user's display name, or null if not set. */
  name: string | null;

  /** The user's bio, or null if not set. */
  bio: string | null;

  /** URL of the user's avatar image, or null if unavailable. */
  avatarUrl: string | null;

  /** URL of the user's GitHub profile page. */
  profileUrl: string;

  /** Total number of public repositories. */
  publicRepositories: number;

  /** Total number of followers. */
  followers: number;

  /** Total number of users this user follows. */
  following: number;

  /** Total number of public gists. */
  publicGists: number;

  /** The user's company, or null if not set. */
  company: string | null;

  /** The user's location, or null if not set. */
  location: string | null;

  /** The user's personal blog/website URL, or null if not set. */
  blog: string | null;

  /** ISO-8601 timestamp of when the account was created. */
  accountCreatedAt: string | null;
}

/**
 * Repository summary response from GET /api/github/{username}/repositories.
 *
 * Contains public repository metadata fetched live from the GitHub REST API.
 */
export interface RepositoryResponse {
  /** The repository name. */
  name: string;

  /** The repository description, or null if not set. */
  description: string | null;

  /** The primary programming language, or null if not set. */
  language: string | null;

  /** Number of stars the repository has. */
  stars: number;

  /** Number of forks of the repository. */
  forks: number;

  /** URL of the repository on GitHub. */
  repositoryUrl: string;

  /** ISO-8601 timestamp of when the repository was created. */
  createdAt: string;

  /** ISO-8601 timestamp of when the repository was last updated. */
  updatedAt: string;
}

/**
 * Language usage statistic response from GET /api/github/{username}/languages.
 *
 * Aggregates a user's public repositories by primary programming language,
 * sorted by repository count descending.
 */
export interface LanguageStatisticsResponse {
  /** The programming language name. */
  language: string;

  /** Number of repositories that use this language as their primary language. */
  repositoryCount: number;
}
