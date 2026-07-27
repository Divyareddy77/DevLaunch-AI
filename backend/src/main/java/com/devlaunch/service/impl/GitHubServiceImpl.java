package com.devlaunch.service.impl;

import com.devlaunch.dto.response.GitHubProfileResponse;
import com.devlaunch.dto.response.LanguageStatisticsResponse;
import com.devlaunch.dto.response.RepositoryResponse;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.service.interfaces.GitHubService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link GitHubService} providing GitHub profile
 * and repository retrieval by fetching live data from the GitHub REST API.
 * <p>
 * Uses Spring's {@link RestClient} to call the GitHub Users and Repositories
 * API endpoints and maps the JSON responses into {@link GitHubProfileResponse}
 * and {@link RepositoryResponse} DTOs. No data is persisted in the local
 * database — all information is retrieved directly from GitHub on each
 * request. If GitHub returns a 404 status, a
 * {@link ResourceNotFoundException} is thrown with a meaningful message.
 * No authentication is required for accessing public GitHub data.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class GitHubServiceImpl implements GitHubService {

    private final RestClient restClient;

    /**
     * Constructs the GitHub service with a default {@link RestClient}
     * instance for making HTTP requests to the GitHub REST API.
     */
    public GitHubServiceImpl() {
        this.restClient = RestClient.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitHubProfileResponse getGitHubProfile(final String username) {
        GitHubApiUserResponse apiResponse;

        try {
            apiResponse = restClient.get()
                    .uri("https://api.github.com/users/{username}", username)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ResourceNotFoundException(
                                        "GitHub user '" + username + "' not found");
                            })
                    .body(GitHubApiUserResponse.class);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException(
                    "GitHub user '" + username + "' not found");
        }

        if (apiResponse == null) {
            throw new ResourceNotFoundException(
                    "GitHub user '" + username + "' not found");
        }

        return GitHubProfileResponse.builder()
                .username(apiResponse.login)
                .name(apiResponse.name)
                .bio(apiResponse.bio)
                .avatarUrl(apiResponse.avatarUrl)
                .profileUrl(apiResponse.htmlUrl)
                .publicRepositories(apiResponse.publicRepos)
                .followers(apiResponse.followers)
                .following(apiResponse.following)
                .publicGists(apiResponse.publicGists)
                .company(apiResponse.company)
                .location(apiResponse.location)
                .blog(apiResponse.blog)
                .accountCreatedAt(apiResponse.createdAt)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RepositoryResponse> getRepositories(final String username) {
        GitHubApiRepoResponse[] apiResponses;

        try {
            apiResponses = restClient.get()
                    .uri("https://api.github.com/users/{username}/repos", username)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ResourceNotFoundException(
                                        "GitHub user '" + username + "' not found");
                            })
                    .body(GitHubApiRepoResponse[].class);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException(
                    "GitHub user '" + username + "' not found");
        }

        if (apiResponses == null) {
            return List.of();
        }

        return Arrays.stream(apiResponses)
                .map(repo -> RepositoryResponse.builder()
                        .name(repo.name)
                        .description(repo.description)
                        .language(repo.language)
                        .stars(repo.stargazersCount)
                        .forks(repo.forksCount)
                        .repositoryUrl(repo.htmlUrl)
                        .createdAt(repo.createdAt)
                        .updatedAt(repo.updatedAt)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LanguageStatisticsResponse> getLanguageStatistics(final String username) {
        GitHubApiRepoResponse[] apiResponses;

        try {
            apiResponses = restClient.get()
                    .uri("https://api.github.com/users/{username}/repos", username)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ResourceNotFoundException(
                                        "GitHub user '" + username + "' not found");
                            })
                    .body(GitHubApiRepoResponse[].class);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException(
                    "GitHub user '" + username + "' not found");
        }

        if (apiResponses == null) {
            return List.of();
        }

        return Arrays.stream(apiResponses)
                .filter(repo -> repo.language != null)
                .collect(Collectors.groupingBy(
                        repo -> repo.language,
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> LanguageStatisticsResponse.builder()
                        .language(entry.getKey())
                        .repositoryCount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Internal DTO for mapping the GitHub REST API user response.
     * <p>
     * Uses {@link JsonProperty} to map GitHub's snake_case JSON fields
     * (e.g. {@code public_repos}) to camelCase Java fields
     * (e.g. {@code publicRepos}). This class is strictly for
     * deserializing the GitHub API response and is not exposed outside
     * this service implementation.
     * </p>
     */
    private static class GitHubApiUserResponse {

        @JsonProperty("login")
        private String login;

        @JsonProperty("name")
        private String name;

        @JsonProperty("bio")
        private String bio;

        @JsonProperty("avatar_url")
        private String avatarUrl;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("public_repos")
        private Integer publicRepos;

        @JsonProperty("followers")
        private Integer followers;

        @JsonProperty("following")
        private Integer following;

        @JsonProperty("public_gists")
        private Integer publicGists;

        @JsonProperty("company")
        private String company;

        @JsonProperty("location")
        private String location;

        @JsonProperty("blog")
        private String blog;

        @JsonProperty("created_at")
        private String createdAt;

    }

    /**
     * Internal DTO for mapping the GitHub REST API repository response.
     * <p>
     * Uses {@link JsonProperty} to map GitHub's snake_case JSON fields
     * (e.g. {@code stargazers_count}) to camelCase Java fields
     * (e.g. {@code stargazersCount}). This class is strictly for
     * deserializing the GitHub API response and is not exposed outside
     * this service implementation.
     * </p>
     */
    private static class GitHubApiRepoResponse {

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("language")
        private String language;

        @JsonProperty("stargazers_count")
        private Integer stargazersCount;

        @JsonProperty("forks_count")
        private Integer forksCount;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

    }

}
