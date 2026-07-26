package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a professional resume associated with a registered user.
 * <p>
 * Each resume contains a user's career headline, professional summary,
 * and links to their online profiles. A user may own multiple resumes,
 * each representing a different role, target company, or career track.
 * </p>
 */
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"user"})
@EqualsAndHashCode(callSuper = true)
public class Resume extends BaseEntity {

    /**
     * A short professional headline or tagline summarising the user's role.
     */
    @Column(name = "headline", nullable = false, length = 255)
    private String headline;

    /**
     * A detailed professional summary describing the user's experience and
     * career objectives.
     */
    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    /**
     * URL to the user's LinkedIn profile.
     */
    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    /**
     * URL to the user's GitHub profile.
     */
    @Column(name = "github_url", length = 500)
    private String githubUrl;

    /**
     * URL to the user's personal portfolio or website.
     */
    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    /**
     * The user who owns this resume.
     * <p>
     * A user may create multiple resumes, each targeting a different
     * role, company, or career objective.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The template applied to this resume.
     * <p>
     * A resume may optionally be assigned a predefined template that
     * controls its visual style. Many resumes may share the same
     * template, and changing the template does not affect the resume's
     * content.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ResumeTemplate template;

}
