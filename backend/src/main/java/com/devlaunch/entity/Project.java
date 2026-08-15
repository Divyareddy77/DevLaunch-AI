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
 * Represents a project associated with a specific resume.
 * <p>
 * Each project record captures the name, description, technologies used,
 * and optional links to the source code and live deployment. A resume may
 * contain multiple projects, each representing a distinct portfolio work.
 * </p>
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"resume"})
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseEntity {

    /**
     * The name of the project.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Project name is required")
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    /**
     * A detailed description of the project, including its purpose,
     * features, and the user's role or contributions.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * The technologies, frameworks, and tools used in the project.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Technologies are required")
    @Column(name = "technologies", nullable = false, length = 500)
    private String technologies;

    /**
     * The URL to the project's source code repository (e.g. GitHub, GitLab).
     */
    @Column(name = "github_url", length = 500)
    private String githubUrl;

    /**
     * The URL to the live deployment or demo of the project.
     */
    @Column(name = "live_url", length = 500)
    private String liveUrl;

    /**
     * The date on which the project was started.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The date on which the project was completed.
     * <p>
     * May be {@code null} if the project is ongoing.
     * </p>
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Indicates whether the project is currently ongoing.
     * <p>
     * When {@code true}, the {@code endDate} is typically not set or ignored.
     * </p>
     */
    @Column(name = "currently_working")
    @Builder.Default
    private Boolean currentlyWorking = Boolean.FALSE;

    /**
     * The resume to which this project belongs.
     * <p>
     * Each project must be associated with exactly one resume.
     * A resume may contain multiple projects.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

}
