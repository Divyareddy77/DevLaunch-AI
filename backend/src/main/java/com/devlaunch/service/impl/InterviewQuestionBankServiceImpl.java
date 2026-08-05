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

/**
 * Implementation of {@link InterviewQuestionBankService} backed by the
 * {@link InterviewQuestionRepository}.
 * <p>
 * Interviews are assembled by shuffling the category's bank questions and
 * preferring a balanced difficulty mix of three easy, four medium, and
 * three hard questions. When a difficulty has fewer questions than the
 * target, the remaining slots are filled from the leftover pool, so a
 * partially populated bank still yields a full interview whenever enough
 * questions exist overall.
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
        final List<com.devlaunch.entity.InterviewQuestion> pool =
                new ArrayList<>(questionRepository.findByCategory(category));

        if (pool.isEmpty()) {
            throw new IllegalStateException(
                    "No interview questions found for category " + category);
        }

        Collections.shuffle(pool);

        final List<com.devlaunch.entity.InterviewQuestion> selected = new ArrayList<>();
        selected.addAll(takeByDifficulty(pool, Difficulty.EASY, EASY_TARGET));
        selected.addAll(takeByDifficulty(pool, Difficulty.MEDIUM, MEDIUM_TARGET));
        selected.addAll(takeByDifficulty(pool, Difficulty.HARD, HARD_TARGET));

        // Fill any remaining slots from the leftover pool so a bank with
        // fewer questions in one difficulty still yields a full interview.
        final int target = Math.min(QUESTIONS_PER_INTERVIEW, pool.size());
        if (selected.size() < target) {
            for (final com.devlaunch.entity.InterviewQuestion question : pool) {
                if (selected.size() >= target) {
                    break;
                }
                if (!selected.contains(question)) {
                    selected.add(question);
                }
            }
        }

        // Mix difficulties so the interview does not start with a single
        // difficulty block.
        Collections.shuffle(selected);

        return selected.stream().map(this::toQuestion).toList();
    }

    /**
     * Takes up to {@code max} questions of the requested difficulty from
     * the shuffled pool.
     */
    private List<com.devlaunch.entity.InterviewQuestion> takeByDifficulty(
            final List<com.devlaunch.entity.InterviewQuestion> pool,
            final Difficulty difficulty, final int max) {
        return pool.stream()
                .filter(question -> question.getDifficulty() == difficulty)
                .limit(max)
                .toList();
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
