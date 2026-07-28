package com.devlaunch.service.impl;

import com.devlaunch.dto.response.LeetCodeProfileResponse;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.service.interfaces.LeetCodeService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Implementation of {@link LeetCodeService} providing LeetCode user profile
 * statistics by fetching live data from the LeetCode GraphQL API.
 * <p>
 * Uses Spring's {@link RestClient} to call the LeetCode GraphQL API endpoint
 * and maps the JSON response into a {@link LeetCodeProfileResponse} DTO.
 * No data is persisted in the local database — all information is retrieved
 * directly from LeetCode on each request. If the LeetCode API returns a
 * response indicating the user was not found, a
 * {@link ResourceNotFoundException} is thrown with a meaningful message.
 * No authentication is required for accessing public LeetCode profile data.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class LeetCodeServiceImpl implements LeetCodeService {

    private static final String LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";

    private static final String LEETCODE_PROFILE_QUERY = """
            query getUserProfile($username: String!) {
              matchedUser(username: $username) {
                username
                submitStats: submitStatsGlobal {
                  acSubmissionNum {
                    difficulty
                    count
                  }
                }
                profile {
                  ranking
                }
              }
            }
            """;

    private final RestClient restClient;

    /**
     * Constructs the LeetCode service with a default {@link RestClient}
     * instance for making HTTP requests to the LeetCode GraphQL API.
     */
    public LeetCodeServiceImpl() {
        this.restClient = RestClient.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LeetCodeProfileResponse getLeetCodeProfile(final String username) {
        GraphQLResponse response;

        try {
            GraphQLRequest request = new GraphQLRequest();
            request.query = LEETCODE_PROFILE_QUERY;
            request.variables = new GraphQLVariables();
            request.variables.username = username;

            response = restClient.post()
                    .uri(LEETCODE_GRAPHQL_URL)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(GraphQLResponse.class);
        } catch (Exception e) {
            throw new ResourceNotFoundException(
                    "LeetCode user '" + username + "' not found");
        }

        if (response == null
                || response.data == null
                || response.data.matchedUser == null) {
            throw new ResourceNotFoundException(
                    "LeetCode user '" + username + "' not found");
        }

        MatchedUser matchedUser = response.data.matchedUser;

        int totalSolved = 0;
        int easySolved = 0;
        int mediumSolved = 0;
        int hardSolved = 0;

        if (matchedUser.submitStats != null
                && matchedUser.submitStats.acSubmissionNum != null) {
            for (SubmissionNum submission : matchedUser.submitStats.acSubmissionNum) {
                int count = submission.count != null ? submission.count : 0;
                String difficulty = submission.difficulty;

                if ("All".equals(difficulty)) {
                    totalSolved = count;
                } else if ("Easy".equals(difficulty)) {
                    easySolved = count;
                } else if ("Medium".equals(difficulty)) {
                    mediumSolved = count;
                } else if ("Hard".equals(difficulty)) {
                    hardSolved = count;
                }
            }
        }

        Integer ranking = matchedUser.profile != null
                ? matchedUser.profile.ranking
                : null;

        return LeetCodeProfileResponse.builder()
                .username(matchedUser.username)
                .totalSolved(totalSolved)
                .easySolved(easySolved)
                .mediumSolved(mediumSolved)
                .hardSolved(hardSolved)
                .ranking(ranking)
                .build();
    }

    /**
     * Internal DTO for the LeetCode GraphQL request body.
     * <p>
     * Contains the query string and its variables. This class is strictly
     * for serializing the GraphQL request and is not exposed outside this
     * service implementation.
     * </p>
     */
    private static class GraphQLRequest {

        @JsonProperty("query")
        private String query;

        @JsonProperty("variables")
        private GraphQLVariables variables;

    }

    /**
     * Internal DTO for the LeetCode GraphQL request variables.
     * <p>
     * Contains the username variable passed to the GraphQL query. This
     * class is strictly for serializing the request and is not exposed
     * outside this service implementation.
     * </p>
     */
    private static class GraphQLVariables {

        @JsonProperty("username")
        private String username;

    }

    /**
     * Internal DTO for the LeetCode GraphQL API response.
     * <p>
     * Maps the top-level response structure containing the data object
     * with the matched user information. This class is strictly for
     * deserializing the GraphQL API response and is not exposed outside
     * this service implementation.
     * </p>
     */
    private static class GraphQLResponse {

        @JsonProperty("data")
        private GraphQLData data;

    }

    /**
     * Internal DTO for the {@code data} field of the LeetCode GraphQL response.
     * <p>
     * Contains the {@code matchedUser} object representing the found user's
     * profile data. This class is strictly for deserializing the GraphQL API
     * response and is not exposed outside this service implementation.
     * </p>
     */
    private static class GraphQLData {

        @JsonProperty("matchedUser")
        private MatchedUser matchedUser;

    }

    /**
     * Internal DTO for the {@code matchedUser} object from the LeetCode
     * GraphQL response.
     * <p>
     * Contains the username, submission statistics grouped by difficulty,
     * and profile information including ranking. This class is strictly
     * for deserializing the GraphQL API response and is not exposed
     * outside this service implementation.
     * </p>
     */
    private static class MatchedUser {

        @JsonProperty("username")
        private String username;

        @JsonProperty("submitStats")
        private SubmitStats submitStats;

        @JsonProperty("profile")
        private Profile profile;

    }

    /**
     * Internal DTO for the {@code submitStats} object representing global
     * submission statistics.
     * <p>
     * Contains a list of submission numbers broken down by difficulty level.
     * This class is strictly for deserializing the GraphQL API response and
     * is not exposed outside this service implementation.
     * </p>
     */
    private static class SubmitStats {

        @JsonProperty("acSubmissionNum")
        private List<SubmissionNum> acSubmissionNum;

    }

    /**
     * Internal DTO for an individual submission count entry.
     * <p>
     * Represents the count of accepted submissions for a specific difficulty
     * level (Easy, Medium, or Hard). This class is strictly for deserializing
     * the GraphQL API response and is not exposed outside this service
     * implementation.
     * </p>
     */
    private static class SubmissionNum {

        @JsonProperty("difficulty")
        private String difficulty;

        @JsonProperty("count")
        private Integer count;

    }

    /**
     * Internal DTO for the {@code profile} object within a matched user.
     * <p>
     * Contains the user's global ranking on the LeetCode platform. This
     * class is strictly for deserializing the GraphQL API response and is
     * not exposed outside this service implementation.
     * </p>
     */
    private static class Profile {

        @JsonProperty("ranking")
        private Integer ranking;

    }

}
