package com.devlaunch.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for user registration.
 * Contains all required fields to create a new user account.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * The account password.
     * <p>
     * Must be 8–100 characters and contain at least one uppercase letter,
     * one lowercase letter, one digit, and one special character — the same
     * policy enforced on {@link ResetPasswordRequest}. The raw value is
     * hashed with the configured BCrypt encoder before persisting.
     * </p>
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, "
                    + "one number, and one special character"
    )
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phone;

}
