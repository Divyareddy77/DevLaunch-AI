package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Represents a professional certification or credential associated with
 * a specific resume.
 * <p>
 * Each certification record captures the certification name, the issuing
 * organisation, and optional details such as issue and expiry dates,
 * a credential identifier, and a URL for verification. A resume may
 * contain multiple certifications, each representing a distinct
 * professional credential.
 * </p>
 */
@Entity
@Table(name = "certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"resume"})
@EqualsAndHashCode(callSuper = true)
public class Certification extends BaseEntity {

    /**
     * The name of the certification or credential (e.g. AWS Solutions
     * Architect, PMP, Certified Scrum Master).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Certification name is required")
    @Column(name = "certification_name", nullable = false, length = 255)
    private String certificationName;

    /**
     * The organisation that issued the certification (e.g. Amazon Web
     * Services, Project Management Institute, Scrum Alliance).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Issuing organization is required")
    @Column(name = "issuing_organization", nullable = false, length = 255)
    private String issuingOrganization;

    /**
     * The date on which the certification was issued.
     */
    @Column(name = "issue_date")
    private LocalDate issueDate;

    /**
     * The date on which the certification expires, if applicable.
     * <p>
     * May be {@code null} if the certification does not expire.
     * </p>
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * The unique identifier or credential ID assigned by the issuing
     * organisation for this certification.
     */
    @Column(name = "credential_id", length = 255)
    private String credentialId;

    /**
     * The URL to the certification verification page or credential
     * badge provided by the issuing organisation.
     */
    @Column(name = "credential_url", length = 500)
    private String credentialUrl;

    /**
     * The resume to which this certification belongs.
     * <p>
     * Each certification record must be associated with exactly one resume.
     * A resume may contain multiple certification records.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

}
