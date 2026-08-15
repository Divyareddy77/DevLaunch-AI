package com.devlaunch.service.impl;

import com.devlaunch.entity.InterviewQuestion;
import com.devlaunch.entity.enums.Difficulty;
import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.repository.InterviewQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link InterviewQuestionBankServiceImpl}.
 * <p>
 * Verifies that interviews are assembled from the database-backed question
 * bank with ten unique questions, the requested category, and the preferred
 * balanced difficulty mix (3 easy / 4 medium / 3 hard), including graceful
 * fill when a difficulty has fewer questions than the target and the
 * validation error when fewer than ten active questions exist.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class InterviewQuestionBankServiceImplTest {

    private static final int EASY_TARGET = 3;
    private static final int MEDIUM_TARGET = 4;
    private static final int HARD_TARGET = 3;

    @Mock
    private InterviewQuestionRepository questionRepository;

    private InterviewQuestionBankServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterviewQuestionBankServiceImpl(questionRepository);
    }

    /**
     * Builds a bank entity with the given id and difficulty.
     */
    private InterviewQuestion entity(final long id, final Difficulty difficulty) {
        final InterviewQuestion question = InterviewQuestion.builder()
                .category(InterviewType.REACT)
                .question("Question " + id)
                .difficulty(difficulty)
                .build();
        question.setId(id);
        return question;
    }

    /**
     * Builds a pool of {@code count} questions for each difficulty.
     */
    private List<InterviewQuestion> pool(final int easy, final int medium, final int hard) {
        final List<InterviewQuestion> pool = new ArrayList<>();
        long id = 1;
        for (int i = 0; i < easy; i++) {
            pool.add(entity(id++, Difficulty.EASY));
        }
        for (int i = 0; i < medium; i++) {
            pool.add(entity(id++, Difficulty.MEDIUM));
        }
        for (int i = 0; i < hard; i++) {
            pool.add(entity(id++, Difficulty.HARD));
        }
        return pool;
    }

    /**
     * Stubs the three per-difficulty random queries. Each stub mirrors the
     * database {@code LIMIT} behaviour and returns at most the difficulty
     * target number of questions.
     */
    private void stubDifficultyQueries(final List<InterviewQuestion> easy,
                                       final List<InterviewQuestion> medium,
                                       final List<InterviewQuestion> hard) {
        when(questionRepository.findRandomByCategoryAndDifficultyAndActiveTrue(
                InterviewType.REACT, Difficulty.EASY, EASY_TARGET))
                .thenReturn(easy.stream().limit(EASY_TARGET).toList());
        when(questionRepository.findRandomByCategoryAndDifficultyAndActiveTrue(
                InterviewType.REACT, Difficulty.MEDIUM, MEDIUM_TARGET))
                .thenReturn(medium.stream().limit(MEDIUM_TARGET).toList());
        when(questionRepository.findRandomByCategoryAndDifficultyAndActiveTrue(
                InterviewType.REACT, Difficulty.HARD, HARD_TARGET))
                .thenReturn(hard.stream().limit(HARD_TARGET).toList());
    }

    /**
     * Counts the difficulties of the selected questions using the bank
     * entities' ids preserved in the returned DTOs.
     */
    private Map<Difficulty, Integer> difficultyCounts(
            final List<InterviewQuestion> pool,
            final List<com.devlaunch.service.ai.InterviewQuestion> selected) {
        final Map<Long, Difficulty> byId = new java.util.HashMap<>();
        for (final InterviewQuestion question : pool) {
            byId.put(question.getId(), question.getDifficulty());
        }

        final Map<Difficulty, Integer> counts = new EnumMap<>(Difficulty.class);
        for (final com.devlaunch.service.ai.InterviewQuestion question : selected) {
            counts.merge(byId.get(Long.parseLong(question.id())), 1, Integer::sum);
        }
        return counts;
    }

    @Test
    @DisplayName("a full bank yields exactly ten unique questions")
    void selectsTenUniqueQuestions() {
        final List<InterviewQuestion> pool = pool(7, 7, 6);
        when(questionRepository.countByCategoryAndActiveTrue(InterviewType.REACT))
                .thenReturn(20L);
        stubDifficultyQueries(
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.EASY).toList(),
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.MEDIUM).toList(),
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.HARD).toList());

        final List<com.devlaunch.service.ai.InterviewQuestion> selected =
                service.selectForInterview(InterviewType.REACT);

        assertEquals(10, selected.size());
        final Set<String> ids = new HashSet<>();
        for (final com.devlaunch.service.ai.InterviewQuestion question : selected) {
            assertTrue(ids.add(question.id()), "duplicate question id: " + question.id());
        }
    }

    @Test
    @DisplayName("questions are drawn from the requested category")
    void selectsFromRequestedCategory() {
        final List<InterviewQuestion> pool = pool(7, 7, 6);
        when(questionRepository.countByCategoryAndActiveTrue(InterviewType.REACT))
                .thenReturn(20L);
        stubDifficultyQueries(
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.EASY).toList(),
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.MEDIUM).toList(),
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.HARD).toList());

        service.selectForInterview(InterviewType.REACT);

        verify(questionRepository).countByCategoryAndActiveTrue(InterviewType.REACT);
        verify(questionRepository).findRandomByCategoryAndDifficultyAndActiveTrue(
                InterviewType.REACT, Difficulty.EASY, EASY_TARGET);
    }

    @Test
    @DisplayName("a full bank prefers three easy, four medium, three hard")
    void prefersBalancedDifficultyMix() {
        final List<InterviewQuestion> pool = pool(7, 7, 6);
        when(questionRepository.countByCategoryAndActiveTrue(InterviewType.REACT))
                .thenReturn(20L);
        stubDifficultyQueries(
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.EASY).toList(),
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.MEDIUM).toList(),
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.HARD).toList());

        final List<com.devlaunch.service.ai.InterviewQuestion> selected =
                service.selectForInterview(InterviewType.REACT);
        final Map<Difficulty, Integer> counts = difficultyCounts(pool, selected);

        assertEquals(3, counts.get(Difficulty.EASY));
        assertEquals(4, counts.get(Difficulty.MEDIUM));
        assertEquals(3, counts.get(Difficulty.HARD));
    }

    @Test
    @DisplayName("missing difficulties are filled from the remaining questions")
    void fillsMissingDifficultiesFromRemainder() {
        // Only one easy question available — the remaining easy slots must
        // be filled from the medium and hard questions.
        final List<InterviewQuestion> pool = pool(1, 4, 5);
        when(questionRepository.countByCategoryAndActiveTrue(InterviewType.REACT))
                .thenReturn(10L);
        stubDifficultyQueries(
                List.of(pool.get(0)),
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.MEDIUM).toList(),
                pool.stream().filter(q -> q.getDifficulty() == Difficulty.HARD)
                        .limit(HARD_TARGET).toList());
        // The fill query returns the whole bank; already-selected questions
        // must be filtered out so no duplicates appear.
        when(questionRepository.findRandomByCategoryAndActiveTrue(InterviewType.REACT, 10))
                .thenReturn(pool);

        final List<com.devlaunch.service.ai.InterviewQuestion> selected =
                service.selectForInterview(InterviewType.REACT);
        final Map<Difficulty, Integer> counts = difficultyCounts(pool, selected);

        assertEquals(10, selected.size());
        assertEquals(1, counts.get(Difficulty.EASY));
        assertEquals(4, counts.get(Difficulty.MEDIUM));
        assertEquals(5, counts.get(Difficulty.HARD));
        final Set<String> ids = new HashSet<>();
        for (final com.devlaunch.service.ai.InterviewQuestion question : selected) {
            assertTrue(ids.add(question.id()), "duplicate question id: " + question.id());
        }
    }

    @Test
    @DisplayName("a category with fewer than ten active questions throws a validation error")
    void throwsWhenFewerThanTenActiveQuestions() {
        when(questionRepository.countByCategoryAndActiveTrue(InterviewType.REACT))
                .thenReturn(7L);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.selectForInterview(InterviewType.REACT));

        assertEquals("Not enough interview questions available for this category.",
                exception.getMessage());
    }

    @Test
    @DisplayName("an empty category throws the same validation error")
    void throwsWhenCategoryHasNoActiveQuestions() {
        when(questionRepository.countByCategoryAndActiveTrue(InterviewType.SQL))
                .thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> service.selectForInterview(InterviewType.SQL));
    }

}
