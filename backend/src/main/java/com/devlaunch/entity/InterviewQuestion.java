package com.devlaunch.entity;

import com.devlaunch.entity.enums.Difficulty;
import com.devlaunch.entity.enums.InterviewType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A question in the database-backed interview question bank.
 * <p>
 * Each record captures the interview category the question belongs to,
 * the question text, and its difficulty level. The bank is seeded from
 * SQL and is intended to be managed at runtime by the future admin module
 * (add, edit, delete, filter, import) without backend code changes. The
 * unique key on the category and question text keeps seeding idempotent.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "interview_questions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_interview_question_category_question",
                columnNames = {"category", "question"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InterviewQuestion extends BaseEntity {

    /**
     * The interview category this question belongs to.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private InterviewType category;

    /**
     * The question text presented to the user.
     * <p>
     * Must not be blank. Stored as a capped string so the unique key with
     * the category remains indexable by MySQL.
     * </p>
     */
    @NotBlank(message = "Question is required")
    @Column(name = "question", nullable = false, length = 500)
    private String question;

    /**
     * The difficulty level of this question.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Difficulty is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 10)
    private Difficulty difficulty;

    /**
     * Whether this question is available for selection in new interviews.
     * <p>
     * Inactive questions are excluded from interview assembly but remain
     * in the bank, so the future admin module can re-enable them without
     * data loss. Defaults to {@code true} so seeded and newly created
     * questions go live immediately.
     * </p>
     */
    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;

}
