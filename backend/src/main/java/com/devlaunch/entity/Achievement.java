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
 * Represents a notable achievement or accomplishment associated with
 * a specific resume.
 * <p>
 * Each achievement record captures the title and optional details such as
 * a description and the date it was achieved. A resume may contain multiple
 * achievements, each representing a distinct professional or personal
 * accomplishment.
 * </p>
 */
@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"resume"})
@EqualsAndHashCode(callSuper = true)
public class Achievement extends BaseEntity {

    /**
     * The title or name of the achievement (e.g. Employee of the Month,
     * Published Research Paper, Won Hackathon).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * A free-text description providing additional details about
     * the achievement, such as context, impact, or recognition received.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * The date on which the achievement was attained or recognised.
     * <p>
     * May be {@code null} if the exact date is not known.
     * </p>
     */
    @Column(name = "date_achieved")
    private LocalDate dateAchieved;

    /**
     * The resume to which this achievement belongs.
     * <p>
     * Each achievement record must be associated with exactly one resume.
     * A resume may contain multiple achievement records.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

}
