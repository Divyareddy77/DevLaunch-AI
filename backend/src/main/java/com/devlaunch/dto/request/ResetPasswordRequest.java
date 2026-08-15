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
 * Request DTO for completing a password reset.
 * <p>
 * Carries the one-time reset token from the emailed link together with
 * the new password and its confirmation. Password strength is enforced
 * here via {@link Pattern} so weak passwords are rejected before they
 * reach the service layer; matching of the two password fields is
 * verified in the service.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    /**
     * The one-time token received in the password reset email.
     */
    @NotBlank(message = "Reset token is required")
    private String token;

    /**
     * The new password to set.
     * <p>
     * Must be 8–100 characters and contain at least one uppercase letter,
     * one lowercase letter, one digit, and one special character. The raw
     * value is hashed with the configured BCrypt encoder before persisting.
     * </p>
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, "
                    + "one number, and one special character"
    )
    private String newPassword;

    /**
     * Confirmation of the new password; must equal {@link #newPassword}.
     */
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

}
