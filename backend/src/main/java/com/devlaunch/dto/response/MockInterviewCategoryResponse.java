package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO describing a mock interview category on the landing page.
 * <p>
 * Combines the question bank size with the authenticated user's own
 * practice history for the category (attempt count, previous best score,
 * and last attempt date) so the landing page can show personalised
 * category cards.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewCategoryResponse {

    /**
     * The interview category.
     */
    private InterviewType interviewType;

    /**
     * The number of active questions available in the bank for this
     * category.
     */
    private Integer questionBankSize;

    /**
     * The number of interviews the user completed in this category.
     */
    private Integer attemptCount;

    /**
     * The user's best score in this category, or {@code null} when the
     * category has never been attempted.
     */
    private Integer previousBestScore;

    /**
     * The date of the user's last attempt in this category, or {@code null}
     * when the category has never been attempted.
     */
    private LocalDateTime lastAttemptAt;

}
