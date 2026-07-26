package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response DTO for certification information.
 * <p>
 * Exposes certification data including the certification name, issuing
 * organisation, issue and expiry dates, credential ID, and credential URL.
 * Internal fields such as the resume association and timestamps are
 * excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationResponse {

    private Long id;

    private String certificationName;

    private String issuingOrganization;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private String credentialId;

    private String credentialUrl;

}
