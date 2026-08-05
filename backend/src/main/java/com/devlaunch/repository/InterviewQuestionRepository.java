package com.devlaunch.repository;

import com.devlaunch.entity.InterviewQuestion;
import com.devlaunch.entity.enums.Difficulty;
import com.devlaunch.entity.enums.InterviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link InterviewQuestion} entity operations.
 * <p>
 * Provides standard CRUD operations and the query methods required to
 * assemble interviews from the question bank. Random selection is done at
 * the database level ({@code ORDER BY RAND() LIMIT n}) so a bank of 500+
 * questions never needs to be loaded into memory, keeping interview
 * assembly constant-time regardless of bank size. The future admin module
 * can reuse the inherited CRUD methods to add, edit, delete, and filter
 * bank questions without backend code changes.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    /**
     * Counts the active bank questions belonging to the specified category.
     *
     * @param category the interview category to query
     * @return the number of active questions for the category
     */
    long countByCategoryAndActiveTrue(InterviewType category);

    /**
     * Selects up to {@code limit} random active questions of the specified
     * category, ordered randomly at the database level.
     * <p>
     * The category and difficulty are bound by their enum names via SpEL so
     * the native queries work identically on MySQL and on the embedded H2
     * test database (H2 does not bind enum parameters directly in native
     * queries).
     * </p>
     *
     * @param category the interview category to draw from
     * @param limit    the maximum number of questions to return
     * @return a random subset of active questions for the category
     */
    @Query(value = "SELECT * FROM interview_questions "
            + "WHERE category = :#{#category.name()} AND active = TRUE "
            + "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<InterviewQuestion> findRandomByCategoryAndActiveTrue(
            @Param("category") InterviewType category, @Param("limit") int limit);

    /**
     * Selects up to {@code limit} random active questions of the specified
     * category and difficulty, ordered randomly at the database level.
     *
     * @param category   the interview category to draw from
     * @param difficulty the difficulty level to filter by
     * @param limit      the maximum number of questions to return
     * @return a random subset of active questions matching the filters
     */
    @Query(value = "SELECT * FROM interview_questions "
            + "WHERE category = :#{#category.name()} "
            + "AND difficulty = :#{#difficulty.name()} AND active = TRUE "
            + "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<InterviewQuestion> findRandomByCategoryAndDifficultyAndActiveTrue(
            @Param("category") InterviewType category,
            @Param("difficulty") Difficulty difficulty,
            @Param("limit") int limit);

    /**
     * Finds all active bank questions belonging to the specified category.
     *
     * @param category the interview category to query
     * @return a list of active questions for the category,
     *         or an empty list if none exist
     */
    List<InterviewQuestion> findByCategoryAndActiveTrue(InterviewType category);

    /**
     * Finds all active bank questions belonging to the specified category
     * and difficulty.
     *
     * @param category   the interview category to query
     * @param difficulty the difficulty level to filter by
     * @return a list of matching questions, or an empty list if none exist
     */
    List<InterviewQuestion> findByCategoryAndDifficultyAndActiveTrue(
            InterviewType category, Difficulty difficulty);

}
