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
 * Verifies that interviews are assembled from the question bank with ten
 * unique questions, the requested category, and the preferred balanced
 * difficulty mix (3 easy / 4 medium / 3 hard), including graceful fill
 * when a difficulty has fewer questions than the target.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class InterviewQuestionBankServiceImplTest {

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
        when(questionRepository.findByCategory(InterviewType.REACT)).thenReturn(pool);

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
        when(questionRepository.findByCategory(InterviewType.REACT)).thenReturn(pool);

        service.selectForInterview(InterviewType.REACT);

        verify(questionRepository).findByCategory(InterviewType.REACT);
    }

    @Test
    @DisplayName("a full bank prefers three easy, four medium, three hard")
    void prefersBalancedDifficultyMix() {
        final List<InterviewQuestion> pool = pool(7, 7, 6);
        when(questionRepository.findByCategory(InterviewType.REACT)).thenReturn(pool);

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
        when(questionRepository.findByCategory(InterviewType.REACT)).thenReturn(pool);

        final List<com.devlaunch.service.ai.InterviewQuestion> selected =
                service.selectForInterview(InterviewType.REACT);
        final Map<Difficulty, Integer> counts = difficultyCounts(pool, selected);

        assertEquals(10, selected.size());
        assertEquals(1, counts.get(Difficulty.EASY));
        assertEquals(4, counts.get(Difficulty.MEDIUM));
        assertEquals(5, counts.get(Difficulty.HARD));
    }

    @Test
    @DisplayName("a bank smaller than ten returns all available questions")
    void returnsAllAvailableWhenBankIsSmall() {
        final List<InterviewQuestion> pool = pool(3, 4, 0);
        when(questionRepository.findByCategory(InterviewType.REACT)).thenReturn(pool);

        final List<com.devlaunch.service.ai.InterviewQuestion> selected =
                service.selectForInterview(InterviewType.REACT);

        assertEquals(7, selected.size());
        final Set<String> ids = new HashSet<>();
        for (final com.devlaunch.service.ai.InterviewQuestion question : selected) {
            assertTrue(ids.add(question.id()), "duplicate question id: " + question.id());
        }
    }

    @Test
    @DisplayName("an empty category throws so the caller can surface a clear error")
    void throwsWhenCategoryHasNoQuestions() {
        when(questionRepository.findByCategory(InterviewType.SQL)).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> service.selectForInterview(InterviewType.SQL));
    }

}
