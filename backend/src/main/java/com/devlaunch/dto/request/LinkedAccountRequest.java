package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for linking an external platform account (GitHub or
 * LeetCode) to the authenticated user.
 * <p>
 * Contains only the username to persist. No OAuth flow is performed —
 * the username is stored verbatim so the dashboard and analytics pages
 * can reuse the linked account across devices.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkedAccountRequest {

    /**
     * The username of the account to link (e.g. "octocat").
     * <p>
     * Must not be blank and may only contain letters, digits, and the
     * hyphen character, matching the GitHub/LeetCode username rules.
     * Leading and trailing whitespace is trimmed by the service layer.
     * </p>
     */
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Username may only contain letters, digits, hyphens, and underscores"
    )
    private String username;

}
