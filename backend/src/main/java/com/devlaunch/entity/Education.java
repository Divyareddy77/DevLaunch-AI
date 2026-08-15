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
 * Represents an educational qualification or degree associated with
 * a professional resume.
 * <p>
 * Each education record captures the institution attended, the degree
 * obtained, the field of study, and optional details such as grade,
 * dates of attendance, and a description. A resume may contain multiple
 * education records, each representing a distinct academic experience.
 * </p>
 */
@Entity
@Table(name = "educations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"resume"})
@EqualsAndHashCode(callSuper = true)
public class Education extends BaseEntity {

    /**
     * The name of the educational institution (e.g. university, college,
     * or training provider).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Institution name is required")
    @Column(name = "institution_name", nullable = false, length = 255)
    private String institutionName;

    /**
     * The degree or certification obtained (e.g. Bachelor of Science,
     * Master of Arts, Diploma).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Degree is required")
    @Column(name = "degree", nullable = false, length = 255)
    private String degree;

    /**
     * The field or discipline of study (e.g. Computer Science,
     * Business Administration, Mechanical Engineering).
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Field of study is required")
    @Column(name = "field_of_study", nullable = false, length = 255)
    private String fieldOfStudy;

    /**
     * The grade or GPA achieved for this qualification, if applicable.
     */
    @Column(name = "grade", length = 50)
    private String grade;

    /**
     * The date on which the educational programme started.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The date on which the educational programme ended (or is expected to end).
     * <p>
     * May be {@code null} if the user is currently studying.
     * </p>
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Indicates whether the user is currently studying at this institution.
     * <p>
     * When {@code true}, the {@code endDate} is typically not set or ignored.
     * </p>
     */
    @Column(name = "currently_studying")
    @Builder.Default
    private Boolean currentlyStudying = Boolean.FALSE;

    /**
     * A free-text description or additional details about this educational
     * experience, such as honours, activities, or relevant coursework.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * The resume to which this education record belongs.
     * <p>
     * Each education record must be associated with exactly one resume.
     * A resume may contain multiple education records.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

}
