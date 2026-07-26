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
 * Represents a professional work experience entry associated with
 * a specific resume.
 * <p>
 * Each experience record captures a job or position held by the user,
 * including the company name, job title, employment period, and optional
 * details such as location, employment type, and a description. A resume
 * may contain multiple experience records, each representing a distinct
 * role or position.
 * </p>
 */
@Entity
@Table(name = "experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"resume"})
@EqualsAndHashCode(callSuper = true)
public class Experience extends BaseEntity {

    /**
     * The name of the company or organisation where the user worked.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Company name is required")
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    /**
     * The job title or position held by the user at the company.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Job title is required")
    @Column(name = "job_title", nullable = false, length = 255)
    private String jobTitle;

    /**
     * The type of employment (e.g. Full-time, Part-time, Contract,
     * Freelance, Internship).
     */
    @Column(name = "employment_type", length = 50)
    private String employmentType;

    /**
     * The geographical location where the job was performed
     * (e.g. city, state, country, or remote).
     */
    @Column(name = "location", length = 255)
    private String location;

    /**
     * The date on which the employment started.
     * <p>
     * Must not be null.
     * </p>
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * The date on which the employment ended.
     * <p>
     * May be {@code null} if the user is currently working in this role.
     * </p>
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Indicates whether the user is currently working in this role.
     * <p>
     * When {@code true}, the {@code endDate} is typically not set or ignored.
     * </p>
     */
    @Column(name = "currently_working")
    @Builder.Default
    private Boolean currentlyWorking = Boolean.FALSE;

    /**
     * A free-text description of the responsibilities, achievements,
     * and technologies used in this role.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * The resume to which this experience record belongs.
     * <p>
     * Each experience record must be associated with exactly one resume.
     * A resume may contain multiple experience records.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

}
