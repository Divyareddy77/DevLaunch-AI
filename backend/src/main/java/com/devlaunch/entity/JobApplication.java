package com.devlaunch.entity;

import com.devlaunch.entity.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Represents a job application tracked by a registered user.
 * <p>
 * Each record captures the company and role applied to, the current
 * status of the application, and optional details such as location,
 * job type, salary, application date, job URL, and personal notes.
 * A user may have many job applications, and each application may
 * optionally be linked to a resume used when applying.
 * </p>
 */
@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"user", "resume"})
@EqualsAndHashCode(callSuper = true)
public class JobApplication extends BaseEntity {

    /**
     * The name of the company to which the user applied or intends to apply.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Company name is required")
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    /**
     * The title of the job role or position applied for.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Job role is required")
    @Column(name = "job_role", nullable = false, length = 255)
    private String jobRole;

    /**
     * The geographical location of the company or position
     * (e.g. city, state, country, or remote).
     */
    @Column(name = "company_location", length = 255)
    private String companyLocation;

    /**
     * The type of employment (e.g. Full-time, Part-time, Contract,
     * Freelance, Internship).
     */
    @Column(name = "job_type", length = 50)
    private String jobType;

    /**
     * The salary or compensation range associated with the position,
     * if known (e.g. "$80,000 - $100,000").
     */
    @Column(name = "salary", length = 100)
    private String salary;

    /**
     * The date on which the application was submitted.
     * <p>
     * May be {@code null} if the application has not yet been submitted
     * (e.g. the job is still on the wishlist).
     * </p>
     */
    @Column(name = "application_date")
    private LocalDate applicationDate;

    /**
     * The current status of this job application within the hiring pipeline.
     * <p>
     * Must not be null. Defaults to {@link ApplicationStatus#WISHLIST}.
     * </p>
     */
    @NotNull(message = "Application status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.WISHLIST;

    /**
     * The URL to the job posting or application page.
     */
    @Column(name = "job_url", length = 500)
    private String jobUrl;

    /**
     * Free-text notes about the application, such as preparation notes,
     * follow-up reminders, or feedback received.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * The user who owns this job application.
     * <p>
     * A user may have many job applications. Each application must be
     * associated with exactly one user.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The resume used when submitting this job application.
     * <p>
     * An application may optionally be linked to the resume that was
     * used when applying. A resume may be associated with many job
     * applications.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

}
