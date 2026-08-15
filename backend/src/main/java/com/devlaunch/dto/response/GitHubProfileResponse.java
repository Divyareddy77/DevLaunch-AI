package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for GitHub user profile information.
 * <p>
 * Exposes public GitHub profile data fetched from the GitHub REST API,
 * including the user's name, bio, avatar, repository count, follower
 * statistics, and other public metadata. Internal fields such as user
 * association and timestamps are excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubProfileResponse {

    private String username;

    private String name;

    private String bio;

    private String avatarUrl;

    private String profileUrl;

    private Integer publicRepositories;

    private Integer followers;

    private Integer following;

    private Integer publicGists;

    private String company;

    private String location;

    private String blog;

    private String accountCreatedAt;

}
