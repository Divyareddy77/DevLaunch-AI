package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating an existing skill record.
 * <p>
 * Contains the updated skill details that the authenticated
 * user wishes to apply to an existing skill entry on their resume.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSkillRequest {

    /**
     * The updated name of the skill.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Skill name is required")
    private String skillName;

    /**
     * The updated proficiency level for this skill.
     */
    private String proficiency;

}
