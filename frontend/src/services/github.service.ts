/**
 * GitHub Analytics service — retrieval of public GitHub profile,
 * repository, and language statistics.
 *
 * Communicates with the backend GitHubController. All data is
 * fetched live from the GitHub REST API by the backend.
 *
 * @see backend/src/main/java/com/devlaunch/controller/GitHubController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { GITHUB } from '../api/endpoints';
import type {
  GitHubProfileResponse,
  RepositoryResponse,
  LanguageStatisticsResponse,
} from '../types/github';

export const githubService = {
  /** GET /api/github/{username} — Fetch a public GitHub user profile. */
  getProfile: (username: string) =>
    apiClient.get<GitHubProfileResponse>(GITHUB.PROFILE(username)).then((r) => r.data),

  /** GET /api/github/{username}/repositories — Fetch a user's public repositories. */
  getRepositories: (username: string) =>
    apiClient.get<RepositoryResponse[]>(GITHUB.REPOSITORIES(username)).then((r) => r.data),

  /** GET /api/github/{username}/languages — Fetch language usage statistics. */
  getLanguageStatistics: (username: string) =>
    apiClient.get<LanguageStatisticsResponse[]>(GITHUB.LANGUAGES(username)).then((r) => r.data),
};
