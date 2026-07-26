package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing certification record.
 * <p>
 * Contains the updated certification details that the authenticated
 * user wishes to apply to an existing certification entry on their resume.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCertificationRequest {

    /**
     * The updated name of the certification or credential.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Certification name is required")
    private String certificationName;

    /**
     * The updated organisation that issued the certification.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Issuing organization is required")
    private String issuingOrganization;

    /**
     * The updated date on which the certification was issued.
     */
    private LocalDate issueDate;

    /**
     * The updated date on which the certification expires, if applicable.
     */
    private LocalDate expiryDate;

    /**
     * The updated unique identifier or credential ID assigned by the
     * issuing organisation for this certification.
     */
    private String credentialId;

    /**
     * The updated URL to the certification verification page or
     * credential badge provided by the issuing organisation.
     */
    private String credentialUrl;

}
