package com.devlaunch.service.impl;

import com.devlaunch.entity.enums.Difficulty;
import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.repository.InterviewQuestionRepository;
import com.devlaunch.service.ai.InterviewQuestion;
import com.devlaunch.service.interfaces.InterviewQuestionBankService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link InterviewQuestionBankService} backed by the
 * {@link InterviewQuestionRepository}.
 * <p>
 * Random selection happens at the database level with
 * {@code ORDER BY RAND() LIMIT n} native queries, so a bank of 500+
 * questions never needs to be loaded into memory and each interview costs
 * only a handful of small queries. Interviews prefer a balanced difficulty
 * mix of three easy, four medium, and three hard questions; when a
 * difficulty has fewer active questions than the target, the remaining
 * slots are filled from the category pool, so a partially populated bank
 * still yields a full interview whenever enough questions exist overall.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class InterviewQuestionBankServiceImpl implements InterviewQuestionBankService {

    /** The number of questions served in a single interview. */
    private static final int QUESTIONS_PER_INTERVIEW = 10;

    /** Preferred number of easy questions per interview. */
    private static final int EASY_TARGET = 3;

    /** Preferred number of medium questions per interview. */
    private static final int MEDIUM_TARGET = 4;

    /** Preferred number of hard questions per interview. */
    private static final int HARD_TARGET = 3;

    private final InterviewQuestionRepository questionRepository;

    /**
     * Constructs the bank service with the question repository.
     *
     * @param questionRepository repository for question bank data access
     */
    public InterviewQuestionBankServiceImpl(
            final InterviewQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<InterviewQuestion> selectForInterview(final InterviewType category) {
        validateAvailability(category);

        final List<com.devlaunch.entity.InterviewQuestion> selected = new ArrayList<>();
        selected.addAll(pickRandom(category, Difficulty.EASY, EASY_TARGET));
        selected.addAll(pickRandom(category, Difficulty.MEDIUM, MEDIUM_TARGET));
        selected.addAll(pickRandom(category, Difficulty.HARD, HARD_TARGET));

        fillRemaining(category, selected);

        // Mix difficulties so the interview does not start with a single
        // difficulty block.
        Collections.shuffle(selected);

        return selected.stream().map(this::toQuestion).toList();
    }

    /**
     * Verifies the category has enough active questions for a full
     * interview before any selection is attempted.
     *
     * @param category the interview category to validate
     * @throws IllegalArgumentException if fewer than ten active questions
     *                                  exist for the category
     */
    private void validateAvailability(final InterviewType category) {
        final long available = questionRepository.countByCategoryAndActiveTrue(category);
        if (available < QUESTIONS_PER_INTERVIEW) {
            throw new IllegalArgumentException(
                    "Not enough interview questions available for this category.");
        }
    }

    /**
     * Picks up to {@code limit} random active questions of the requested
     * difficulty directly from the database.
     *
     * @param category   the interview category to draw from
     * @param difficulty the difficulty level to draw
     * @param limit      the maximum number of questions to take
     * @return the randomly selected questions for the difficulty
     */
    private List<com.devlaunch.entity.InterviewQuestion> pickRandom(
            final InterviewType category, final Difficulty difficulty, final int limit) {
        return questionRepository.findRandomByCategoryAndDifficultyAndActiveTrue(
                category, difficulty, limit);
    }

    /**
     * Fills any remaining interview slots from the category pool without
     * duplicating questions already selected.
     * <p>
     * The pool is deliberately over-fetched (needed + already selected) and
     * already-selected ids are filtered out in memory, so the remaining
     * slots are always satisfiable whenever the category holds at least ten
     * active questions overall.
     * </p>
     *
     * @param category the interview category to draw from
     * @param selected the questions selected so far (mutated in place)
     */
    private void fillRemaining(final InterviewType category,
                               final List<com.devlaunch.entity.InterviewQuestion> selected) {
        final int missing = QUESTIONS_PER_INTERVIEW - selected.size();
        if (missing <= 0) {
            return;
        }

        final Set<Long> selectedIds = selected.stream()
                .map(com.devlaunch.entity.InterviewQuestion::getId)
                .collect(Collectors.toSet());

        final List<com.devlaunch.entity.InterviewQuestion> pool = questionRepository
                .findRandomByCategoryAndActiveTrue(category, missing + selected.size());

        for (final com.devlaunch.entity.InterviewQuestion question : pool) {
            if (selected.size() >= QUESTIONS_PER_INTERVIEW) {
                break;
            }
            if (!selectedIds.contains(question.getId())) {
                selected.add(question);
            }
        }
    }

    /**
     * Maps a bank entity to the internal question DTO consumed by the
     * mock interview providers, using the database identifier as the
     * session-scoped question id.
     */
    private InterviewQuestion toQuestion(
            final com.devlaunch.entity.InterviewQuestion question) {
        return new InterviewQuestion(
                String.valueOf(question.getId()), question.getQuestion(), null);
    }

}
