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
 * Request DTO for updating an existing user's profile.
 * <p>
 * Contains the editable profile fields that a user is allowed
 * to modify after account creation. The email and role fields
 * are excluded to prevent unauthorized changes to identity or
 * access level.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    /**
     * The user's updated first name.
     * <p>
     * Must not be blank and must not exceed 50 characters.
     * </p>
     */
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    /**
     * The user's updated last name.
     * <p>
     * Must not be blank and must not exceed 50 characters.
     * </p>
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    /**
     * The user's updated phone number.
     * <p>
     * Must not be blank and must match a valid international phone
     * number pattern (optional leading '+', followed by 7 to 15 digits).
     * </p>
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    private String phone;

}
