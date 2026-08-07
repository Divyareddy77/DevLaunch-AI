package com.devlaunch.repository;

import com.devlaunch.entity.AchievementDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the static gamification achievement catalog.
 *
 * @author DevLaunch
 */
@Repository
public interface AchievementDefinitionRepository extends JpaRepository<AchievementDefinition, Long> {

    /**
     * Loads the full catalog in display order.
     *
     * @return all achievement definitions ordered by their display sort order
     */
    List<AchievementDefinition> findAllByOrderBySortOrderAsc();

    /**
     * Finds a single definition by its stable code.
     *
     * @param code the achievement code (e.g. {@code ATS_EXPERT})
     * @return the matching definition, or an empty {@link Optional}
     */
    Optional<AchievementDefinition> findByCode(String code);

}
