package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Represents a predefined resume template that users can apply to their
 * resumes.
 * <p>
 * Templates are predefined by the system and provide different visual styles
 * (e.g. Professional, Modern, Minimal, Creative). Each resume may reference
 * exactly one template, and many resumes may share the same template. Users
 * do not create or delete templates — they are seeded at application startup.
 * </p>
 */
@Entity
@Table(name = "resume_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ResumeTemplate extends BaseEntity {

    /**
     * The display name of the template (e.g. Professional, Modern, Minimal,
     * Creative).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Template name is required")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * A brief description of the template's visual style and intended use.
     * <p>
     * Provides guidance on when this template is best suited.
     * </p>
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * An optional URL pointing to a preview image of the template.
     * <p>
     * This can be used in the frontend to display a visual preview before
     * the user selects a template.
     * </p>
     */
    @Column(name = "preview_image_url", length = 500)
    private String previewImageUrl;

}
