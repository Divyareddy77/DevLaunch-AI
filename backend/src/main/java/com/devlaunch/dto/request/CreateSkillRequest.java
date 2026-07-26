package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a new skill record.
 * <p>
 * Contains the skill details required to add a skill entry
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
public class CreateSkillRequest {

    /**
     * The name of the skill (e.g. Java, Project Management, Public Speaking).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Skill name is required")
    private String skillName;

    /**
     * The proficiency level for this skill (e.g. Beginner, Intermediate,
     * Advanced, Expert).
     */
    private String proficiency;

}
