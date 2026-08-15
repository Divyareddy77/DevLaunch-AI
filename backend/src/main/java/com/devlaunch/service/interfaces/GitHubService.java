package com.devlaunch.service.interfaces;

import com.devlaunch.dto.response.GitHubProfileResponse;
import com.devlaunch.dto.response.LanguageStatisticsResponse;
import com.devlaunch.dto.response.RepositoryResponse;

import java.util.List;

/**
 * Service interface for GitHub profile and repository retrieval operations.
 * <p>
 * Defines the contract for fetching public GitHub user profile and repository
 * data directly from the GitHub REST API. This service does not persist any
 * data in the local database; it retrieves live information from GitHub on
 * each request. No authentication is required for accessing public GitHub
 * data.
 * </p>
 *
 * @author DevLaunch
 */
public interface GitHubService {

    /**
     * Retrieves the public GitHub profile for the specified username.
     * <p>
     * Fetches profile data from the GitHub REST API, including the user's
     * name, bio, avatar URL, profile URL, public repository count, follower
     * and following counts, public gist count, company, location, blog, and
     * account creation date. If the specified username does not exist on
     * GitHub, a {@link com.devlaunch.exception.ResourceNotFoundException}
     * is thrown.
     * </p>
     *
     * @param username the GitHub username to look up (e.g. "octocat")
     * @return the GitHub profile data
     * @throws com.devlaunch.exception.ResourceNotFoundException if the GitHub
     *                                                           user is not found
     */
    GitHubProfileResponse getGitHubProfile(String username);

    /**
     * Retrieves the public repositories for the specified GitHub username.
     * <p>
     * Fetches repository data from the GitHub REST API, including the name,
     * description, primary language, star count, fork count, repository URL,
     * and creation and update timestamps for each repository. If the
     * specified username does not exist on GitHub, a
     * {@link com.devlaunch.exception.ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param username the GitHub username whose repositories to look up
     *                 (e.g. "octocat")
     * @return a list of repository summary data
     * @throws com.devlaunch.exception.ResourceNotFoundException if the GitHub
     *                                                           user is not found
     */
    List<RepositoryResponse> getRepositories(String username);

    /**
     * Retrieves language statistics for the specified GitHub username.
     * <p>
     * Fetches all public repositories for the user from the GitHub REST
     * API, groups them by their primary programming language, and counts
     * how many repositories belong to each language. Repositories with a
     * null language are excluded from the statistics. The result is sorted
     * by repository count in descending order. If the specified username
     * does not exist on GitHub, a
     * {@link com.devlaunch.exception.ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param username the GitHub username whose language statistics to
     *                 retrieve (e.g. "octocat")
     * @return a list of language statistics sorted by count descending
     * @throws com.devlaunch.exception.ResourceNotFoundException if the GitHub
     *                                                           user is not found
     */
    List<LanguageStatisticsResponse> getLanguageStatistics(String username);

}
