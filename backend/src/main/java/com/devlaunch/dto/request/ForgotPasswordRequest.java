package com.devlaunch.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for the forgot-password flow.
 * <p>
 * Contains only the email address of the account whose password should
 * be reset. The endpoint always responds with the same generic message
 * whether or not the email is registered, so account existence is never
 * disclosed.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {

    /**
     * The email address of the account requesting a password reset.
     * <p>
     * Must not be blank and must be a valid email format.
     * </p>
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

}
