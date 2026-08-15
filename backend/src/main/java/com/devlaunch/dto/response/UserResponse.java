package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for user profile information.
 * Exposes safe user data excluding sensitive fields like password and internal timestamps.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    /**
     * The GitHub username linked to this account, or {@code null} if no
     * account is connected yet.
     */
    private String githubUsername;

    /**
     * The LeetCode username linked to this account, or {@code null} if no
     * account is connected yet.
     */
    private String leetcodeUsername;

    private String role;

    private Boolean isActive;

}
