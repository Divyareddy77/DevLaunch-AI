package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for changing the authenticated user's password.
 * <p>
 * Contains the current password for identity verification and the
 * new password to be set. The new password must meet minimum length
 * requirements before being hashed and persisted.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    /**
     * The user's current password, used to verify their identity
     * before permitting the password change.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    /**
     * The new password the user wishes to set.
     * <p>
     * Must not be blank and must be between 8 and 100 characters
     * in length. The raw value is hashed via the configured
     * {@link org.springframework.security.crypto.password.PasswordEncoder}
     * before being persisted.
     * </p>
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    private String newPassword;

}
