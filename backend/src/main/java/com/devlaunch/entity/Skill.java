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

/**
 * Represents a professional skill associated with a specific resume.
 * <p>
 * Each skill record captures the name of the skill and an optional
 * proficiency level (e.g. Beginner, Intermediate, Advanced, Expert).
 * A resume may contain multiple skills, each representing a distinct
 * technical or professional capability.
 * </p>
 */
@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"resume"})
@EqualsAndHashCode(callSuper = true)
public class Skill extends BaseEntity {

    /**
     * The name of the skill (e.g. Java, Project Management, Public Speaking).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Skill name is required")
    @Column(name = "skill_name", nullable = false, length = 255)
    private String skillName;

    /**
     * The proficiency level for this skill.
     * <p>
     * May indicate the user's expertise level, such as Beginner,
     * Intermediate, Advanced, or Expert. This field is optional.
     * </p>
     */
    @Column(name = "proficiency", length = 100)
    private String proficiency;

    /**
     * The resume to which this skill record belongs.
     * <p>
     * Each skill record must be associated with exactly one resume.
     * A resume may contain multiple skill records.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

}
