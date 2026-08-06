package com.devlaunch.service.impl;

import com.devlaunch.entity.enums.Difficulty;
import com.devlaunch.entity.enums.InterviewDifficulty;
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

    /** Fraction of a mixed interview reserved for easy questions. */
    private static final double MIXED_EASY_FRACTION = 0.3;

    /** Fraction of a mixed interview reserved for hard questions. */
    private static final double MIXED_HARD_FRACTION = 0.3;

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
    public List<InterviewQuestion> selectForInterview(final InterviewType category,
                                                      final InterviewDifficulty difficulty,
                                                      final int count) {
        validateAvailability(category, count);

        final DifficultyTargets targets = targetsFor(difficulty, count);
        final List<com.devlaunch.entity.InterviewQuestion> selected = new ArrayList<>();
        selected.addAll(pickRandom(category, Difficulty.EASY, targets.easy()));
        selected.addAll(pickRandom(category, Difficulty.MEDIUM, targets.medium()));
        selected.addAll(pickRandom(category, Difficulty.HARD, targets.hard()));

        fillRemaining(category, selected, count);

        // Mix difficulties so the interview does not start with a single
        // difficulty block.
        Collections.shuffle(selected);

        return selected.stream().map(this::toQuestion).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long countActive(final InterviewType category) {
        return questionRepository.countByCategoryAndActiveTrue(category);
    }

    /**
     * Verifies the category has enough active questions for an interview
     * of the requested length before any selection is attempted.
     *
     * @param category the interview category to validate
     * @param count    the number of questions requested
     * @throws IllegalArgumentException if fewer than {@code count} active
     *                                  questions exist for the category
     */
    private void validateAvailability(final InterviewType category, final int count) {
        final long available = questionRepository.countByCategoryAndActiveTrue(category);
        if (available < count) {
            throw new IllegalArgumentException(
                    "Not enough interview questions available for this category.");
        }
    }

    /**
     * Computes the per-difficulty selection targets for an interview.
     *
     * @param difficulty the interview difficulty mode
     * @param count      the total number of questions requested
     * @return the easy, medium, and hard selection targets
     */
    private DifficultyTargets targetsFor(final InterviewDifficulty difficulty, final int count) {
        return switch (difficulty) {
            case EASY -> new DifficultyTargets(count, 0, 0);
            case MEDIUM -> new DifficultyTargets(0, count, 0);
            case HARD -> new DifficultyTargets(0, 0, count);
            case MIXED -> {
                final int easy = (int) Math.round(count * MIXED_EASY_FRACTION);
                final int hard = (int) Math.round(count * MIXED_HARD_FRACTION);
                yield new DifficultyTargets(easy, count - easy - hard, hard);
            }
        };
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
     * slots are always satisfiable whenever the category holds at least
     * {@code count} active questions overall.
     * </p>
     *
     * @param category the interview category to draw from
     * @param selected the questions selected so far (mutated in place)
     * @param count    the total number of questions requested
     */
    private void fillRemaining(final InterviewType category,
                               final List<com.devlaunch.entity.InterviewQuestion> selected,
                               final int count) {
        final int missing = count - selected.size();
        if (missing <= 0) {
            return;
        }

        final Set<Long> selectedIds = selected.stream()
                .map(com.devlaunch.entity.InterviewQuestion::getId)
                .collect(Collectors.toSet());

        final List<com.devlaunch.entity.InterviewQuestion> pool = questionRepository
                .findRandomByCategoryAndActiveTrue(category, missing + selected.size());

        for (final com.devlaunch.entity.InterviewQuestion question : pool) {
            if (selected.size() >= count) {
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
     * session-scoped question id and carrying the bank difficulty through
     * for per-question badges.
     */
    private InterviewQuestion toQuestion(
            final com.devlaunch.entity.InterviewQuestion question) {
        return new InterviewQuestion(
                String.valueOf(question.getId()), question.getQuestion(), null,
                question.getDifficulty());
    }

    /**
     * The per-difficulty selection targets for one interview.
     *
     * @param easy   the number of easy questions to request
     * @param medium the number of medium questions to request
     * @param hard   the number of hard questions to request
     */
    private record DifficultyTargets(int easy, int medium, int hard) {
    }

}
