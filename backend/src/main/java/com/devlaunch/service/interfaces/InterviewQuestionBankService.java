package com.devlaunch.service.interfaces;

import com.devlaunch.entity.enums.InterviewDifficulty;
import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.service.ai.InterviewQuestion;

import java.util.List;

/**
 * Service for selecting questions from the database-backed interview
 * question bank.
 * <p>
 * Implementations assemble the question set for a new interview session,
 * drawing randomly from the bank while keeping a balanced difficulty mix,
 * so interviews vary between sessions and never repeat questions within
 * one session. Random selection is performed by the database so the
 * service scales to banks of hundreds of questions without loading them
 * into memory.
 * </p>
 *
 * @author DevLaunch
 */
public interface InterviewQuestionBankService {

    /**
     * Selects a random, balanced set of unique questions for an interview
     * of the given category.
     * <p>
     * Convenience overload equivalent to
     * {@code selectForInterview(category, InterviewDifficulty.MIXED, 10)}.
     * </p>
     *
     * @param category the interview category to draw questions from
     * @return exactly ten unique active questions for the category, ordered
     *         for presentation
     * @throws IllegalArgumentException if the category has fewer than ten
     *                                  active questions in the bank
     */
    default List<InterviewQuestion> selectForInterview(InterviewType category) {
        return selectForInterview(category, InterviewDifficulty.MIXED, 10);
    }

    /**
     * Selects a random, unique set of questions for an interview of the
     * given category, difficulty, and length.
     * <p>
     * {@code MIXED} difficulty assembles a balanced mix of easy, medium,
     * and hard questions; the single-difficulty modes prefer questions of
     * that difficulty and top up any shortfall from the category pool so a
     * partially populated bank still yields a full interview whenever
     * enough questions exist overall.
     * </p>
     *
     * @param category   the interview category to draw questions from
     * @param difficulty the difficulty mode of the interview
     * @param count      the number of questions to select
     * @return exactly {@code count} unique active questions for the
     *         category, ordered for presentation
     * @throws IllegalArgumentException if the category has fewer than
     *                                  {@code count} active questions in
     *                                  the bank
     */
    List<InterviewQuestion> selectForInterview(InterviewType category,
                                               InterviewDifficulty difficulty,
                                               int count);

    /**
     * Counts the active questions available in the bank for a category.
     *
     * @param category the interview category to query
     * @return the number of active questions for the category
     */
    long countActive(InterviewType category);

}
