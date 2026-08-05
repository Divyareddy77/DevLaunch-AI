package com.devlaunch.repository;

import com.devlaunch.entity.InterviewQuestion;
import com.devlaunch.entity.enums.Difficulty;
import com.devlaunch.entity.enums.InterviewType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link InterviewQuestionRepository} backed by an
 * embedded H2 database.
 * <p>
 * Verifies the database-level random selection and counting behaviour used
 * to assemble interviews: random queries return only active questions of
 * the requested category (and difficulty), honour the limit, never repeat a
 * question within one call, and the count query reflects only active
 * questions.
 * </p>
 *
 * @author DevLaunch
 */
@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
class InterviewQuestionRepositoryTest {

    @Autowired
    private InterviewQuestionRepository repository;

    /**
     * Saves a question with the given attributes and returns it.
     */
    private InterviewQuestion save(final InterviewType category,
                                   final Difficulty difficulty,
                                   final boolean active,
                                   final String text) {
        return repository.save(InterviewQuestion.builder()
                .category(category)
                .question(text)
                .difficulty(difficulty)
                .active(active)
                .build());
    }

    @Test
    @DisplayName("the active count excludes inactive and other-category questions")
    void countReflectsOnlyActiveQuestionsOfTheCategory() {
        save(InterviewType.REACT, Difficulty.EASY, true, "React easy 1");
        save(InterviewType.REACT, Difficulty.EASY, true, "React easy 2");
        save(InterviewType.REACT, Difficulty.MEDIUM, false, "React inactive medium");
        save(InterviewType.JAVA, Difficulty.EASY, true, "Java easy");

        assertEquals(2, repository.countByCategoryAndActiveTrue(InterviewType.REACT));
        assertEquals(1, repository.countByCategoryAndActiveTrue(InterviewType.JAVA));
        assertEquals(0, repository.countByCategoryAndActiveTrue(InterviewType.SQL));
    }

    @Test
    @DisplayName("random selection returns only active questions of the category and honours the limit")
    void randomByCategoryReturnsOnlyActiveQuestionsOfTheCategory() {
        for (int i = 1; i <= 6; i++) {
            save(InterviewType.REACT, Difficulty.values()[i % 3], true,
                    "React question " + i);
        }
        save(InterviewType.REACT, Difficulty.EASY, false, "React inactive question");
        save(InterviewType.JAVA, Difficulty.EASY, true, "Java question");

        final List<InterviewQuestion> result =
                repository.findRandomByCategoryAndActiveTrue(InterviewType.REACT, 5);

        assertEquals(5, result.size());
        assertTrue(result.stream()
                        .allMatch(question -> question.getCategory() == InterviewType.REACT),
                "only the requested category should be returned");
        assertTrue(result.stream().allMatch(InterviewQuestion::isActive),
                "inactive questions must never be returned");
        assertDistinctIds(result);
    }

    @Test
    @DisplayName("random selection by difficulty filters both category and difficulty")
    void randomByCategoryAndDifficultyFiltersBothDimensions() {
        save(InterviewType.REACT, Difficulty.EASY, true, "React easy 1");
        save(InterviewType.REACT, Difficulty.EASY, true, "React easy 2");
        save(InterviewType.REACT, Difficulty.MEDIUM, true, "React medium");
        save(InterviewType.REACT, Difficulty.HARD, true, "React hard");
        save(InterviewType.REACT, Difficulty.EASY, false, "React inactive easy");
        save(InterviewType.JAVA, Difficulty.EASY, true, "Java easy");

        final List<InterviewQuestion> result = repository
                .findRandomByCategoryAndDifficultyAndActiveTrue(
                        InterviewType.REACT, Difficulty.EASY, 10);

        assertEquals(2, result.size());
        assertTrue(result.stream()
                        .allMatch(question -> question.getCategory() == InterviewType.REACT
                                && question.getDifficulty() == Difficulty.EASY),
                "only the requested category and difficulty should be returned");
        assertTrue(result.stream().allMatch(InterviewQuestion::isActive),
                "inactive questions must never be returned");
    }

    @Test
    @DisplayName("random selection never exceeds the requested limit")
    void randomSelectionHonoursTheLimit() {
        for (int i = 1; i <= 5; i++) {
            save(InterviewType.REACT, Difficulty.EASY, true, "React question " + i);
        }

        final List<InterviewQuestion> result =
                repository.findRandomByCategoryAndActiveTrue(InterviewType.REACT, 3);

        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("an empty category returns no random questions")
    void randomSelectionOnEmptyCategoryReturnsNothing() {
        assertTrue(repository.findRandomByCategoryAndActiveTrue(InterviewType.SQL, 10)
                .isEmpty());
        assertTrue(repository.findRandomByCategoryAndDifficultyAndActiveTrue(
                InterviewType.SQL, Difficulty.EASY, 10).isEmpty());
    }

    /**
     * Asserts every question id in the list is unique.
     */
    private void assertDistinctIds(final List<InterviewQuestion> questions) {
        final Set<Long> ids = new HashSet<>();
        for (final InterviewQuestion question : questions) {
            assertTrue(ids.add(question.getId()),
                    "duplicate id in random selection: " + question.getId());
        }
    }

}
