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
 * Represents a persisted AI resume review performed by a user.
 * <p>
 * Each record captures the reviewed resume, the optional target role, and the
 * two scores produced by the AI review (overall resume quality and ATS
 * compatibility). The review history is surfaced in the admin module's AI
 * monitoring section. The record is a summary snapshot — it intentionally does
 * not duplicate the full review text or suggestion list.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "resume_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"user", "resume"})
@EqualsAndHashCode(callSuper = true)
public class ResumeReview extends BaseEntity {

    /**
     * The user who requested this resume review.
     * <p>
     * A user may perform many resume reviews over time.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The resume that was reviewed.
     * <p>
     * A resume may be reviewed many times.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    /**
     * The optional job role the review was tailored towards.
     */
    @Column(name = "target_role", length = 100)
    private String targetRole;

    /**
     * The overall resume quality score, ranging from 0 to 100.
     */
    @Column(name = "resume_score", nullable = false)
    private int resumeScore;

    /**
     * The Applicant Tracking System compatibility score, ranging from 0 to 100.
     */
    @Column(name = "ats_score", nullable = false)
    private int atsScore;

}
