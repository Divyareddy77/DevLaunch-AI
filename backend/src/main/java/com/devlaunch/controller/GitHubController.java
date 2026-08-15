package com.devlaunch.controller;

import com.devlaunch.dto.response.GitHubProfileResponse;
import com.devlaunch.dto.response.LanguageStatisticsResponse;
import com.devlaunch.dto.response.RepositoryResponse;
import com.devlaunch.service.interfaces.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for GitHub profile and repository retrieval operations.
 * <p>
 * Exposes endpoints for fetching public GitHub user profile data and
 * repository lists from the GitHub REST API. These endpoints do not
 * require authentication and operate without persisting any data in
 * the local database. All data is fetched live from GitHub on each
 * request.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;

    /**
     * Retrieves the public GitHub profile for the specified username.
     * <p>
     * Delegates to {@link GitHubService#getGitHubProfile(String)} to
     * fetch profile data including the user's name, bio, avatar URL,
     * profile URL, public repository count, follower and following
     * counts, public gist count, company, location, blog, and account
     * creation date.
     * </p>
     *
     * @param username the GitHub username to look up (e.g. "octocat")
     * @return a {@link ResponseEntity} containing the GitHub profile data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{username}")
    public ResponseEntity<GitHubProfileResponse> getGitHubProfile(
            @PathVariable final String username) {
        GitHubProfileResponse response = gitHubService.getGitHubProfile(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the public repositories for the specified GitHub username.
     * <p>
     * Delegates to {@link GitHubService#getRepositories(String)} to
     * fetch repository data including the name, description, primary
     * language, star count, fork count, repository URL, and creation
     * and update timestamps for each repository.
     * </p>
     *
     * @param username the GitHub username whose repositories to look up
     *                 (e.g. "octocat")
     * @return a {@link ResponseEntity} containing a list of repository
     *         summary data with HTTP status 200 (OK)
     */
    @GetMapping("/{username}/repositories")
    public ResponseEntity<List<RepositoryResponse>> getRepositories(
            @PathVariable final String username) {
        List<RepositoryResponse> response = gitHubService.getRepositories(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves language statistics for the specified GitHub username.
     * <p>
     * Delegates to {@link GitHubService#getLanguageStatistics(String)} to
     * aggregate repositories by their primary programming language and
     * return a count for each language, sorted in descending order by
     * repository count. Repositories with a null language are excluded.
     * </p>
     *
     * @param username the GitHub username whose language statistics to
     *                 retrieve (e.g. "octocat")
     * @return a {@link ResponseEntity} containing a list of language
     *         statistics sorted by count descending with HTTP status
     *         200 (OK)
     */
    @GetMapping("/{username}/languages")
    public ResponseEntity<List<LanguageStatisticsResponse>> getLanguageStatistics(
            @PathVariable final String username) {
        List<LanguageStatisticsResponse> response = gitHubService.getLanguageStatistics(username);
        return ResponseEntity.ok(response);
    }

}
