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
 * one session.
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
     * @return up to ten unique questions for the category, ordered for
     *         presentation
     * @throws IllegalStateException if the category has no questions in
     *                               the bank
     */
    List<InterviewQuestion> selectForInterview(InterviewType category);

}
