package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for creating a new certification record.
 * <p>
 * Contains the certification details required to add a certification entry
 * to a specific resume owned by the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCertificationRequest {

    /**
     * The name of the certification or credential.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Certification name is required")
    private String certificationName;

    /**
     * The organisation that issued the certification.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Issuing organization is required")
    private String issuingOrganization;

    /**
     * The date on which the certification was issued.
     */
    private LocalDate issueDate;

    /**
     * The date on which the certification expires, if applicable.
     */
    private LocalDate expiryDate;

    /**
     * The unique identifier or credential ID assigned by the issuing
     * organisation for this certification.
     */
    private String credentialId;

    /**
     * The URL to the certification verification page or credential
     * badge provided by the issuing organisation.
     */
    private String credentialUrl;

}
