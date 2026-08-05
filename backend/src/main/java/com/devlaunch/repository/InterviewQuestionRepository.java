package com.devlaunch.repository;

import com.devlaunch.entity.InterviewQuestion;
import com.devlaunch.entity.enums.Difficulty;
import com.devlaunch.entity.enums.InterviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link InterviewQuestion} entity operations.
 * <p>
 * Provides standard CRUD operations and the query methods required to
 * assemble interviews from the question bank. The future admin module can
 * reuse the inherited CRUD methods to add, edit, delete, and filter bank
 * questions without backend code changes.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    /**
     * Finds all bank questions belonging to the specified category.
     *
     * @param category the interview category to query
     * @return a list of questions for the category,
     *         or an empty list if none exist
     */
    List<InterviewQuestion> findByCategory(InterviewType category);

    /**
     * Finds all bank questions belonging to the specified category and
     * difficulty.
     *
     * @param category   the interview category to query
     * @param difficulty the difficulty level to filter by
     * @return a list of matching questions, or an empty list if none exist
     */
    List<InterviewQuestion> findByCategoryAndDifficulty(InterviewType category, Difficulty difficulty);

}
