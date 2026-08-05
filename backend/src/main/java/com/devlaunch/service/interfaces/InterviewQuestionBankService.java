package com.devlaunch.service.interfaces;

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
     *
     * @param category the interview category to draw questions from
     * @return exactly ten unique active questions for the category, ordered
     *         for presentation
     * @throws IllegalArgumentException if the category has fewer than ten
     *                                  active questions in the bank
     */
    List<InterviewQuestion> selectForInterview(InterviewType category);

}
