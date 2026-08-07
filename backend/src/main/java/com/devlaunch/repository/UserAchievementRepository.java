package com.devlaunch.repository;

import com.devlaunch.entity.AchievementDefinition;
import com.devlaunch.entity.User;
import com.devlaunch.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for per-user unlocked achievement badges.
 *
 * @author DevLaunch
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    /**
     * Finds every badge a user unlocked, newest first.
     *
     * @param user the user whose badges to retrieve
     * @return the unlocked badges, or an empty list if none
     */
    List<UserAchievement> findByUserOrderByUnlockedAtDesc(User user);

    /**
     * Checks whether a user already unlocked a specific badge.
     *
     * @param user       the user
     * @param definition the badge definition
     * @return {@code true} if already unlocked, {@code false} otherwise
     */
    boolean existsByUserAndAchievementDefinition(User user, AchievementDefinition definition);

    /**
     * Counts the badges a user has unlocked.
     *
     * @param user the user
     * @return the number of unlocked badges
     */
    long countByUser(User user);

}
