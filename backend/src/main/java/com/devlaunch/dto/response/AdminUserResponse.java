package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a user as seen by an administrator.
 * <p>
 * Extends the standard user profile with the registration timestamp so the
 * admin panel can display account age and recent registrations. Sensitive
 * fields such as the password hash are never exposed.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String role;

    private Boolean isActive;

    private LocalDateTime createdAt;

}
