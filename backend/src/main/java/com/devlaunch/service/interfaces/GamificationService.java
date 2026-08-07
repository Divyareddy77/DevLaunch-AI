package com.devlaunch.service.interfaces;

import com.devlaunch.dto.response.AchievementDefinitionResponse;
import com.devlaunch.dto.response.AchievementProgressResponse;
import com.devlaunch.dto.response.AchievementSummaryResponse;
import com.devlaunch.dto.response.UnlockedAchievementResponse;
import com.devlaunch.dto.response.XpHistoryResponse;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ActivityType;

import java.util.List;

/**
 * Service contract for the gamification module.
 * <p>
 * The write path ({@link #recordActivity(User, ActivityType, Integer)}) is
 * invoked by the messaging consumer whenever a platform module changes: it
 * awards activity XP, evaluates every achievement badge, unlocks newly
 * satisfied badges (awarding their XP), and raises the unlock / level-up
 * notifications — all in a single transaction. The read paths serve the
 * achievements API and are cached in Redis.
 * </p>
 *
 * @author DevLaunch
 */
public interface GamificationService {

    /**
     * Processes a platform activity: awards the activity XP, evaluates the
     * achievement catalog, unlocks newly satisfied badges, and raises the
     * unlock / level-up notifications.
     * <p>
     * Safe to call multiple times for the same activity — the unique
     * constraint on {@code user_achievements} guarantees a badge is never
     * unlocked (or rewarded) twice.
     * </p>
     *
     * @param user  the user who performed the activity
     * @param type  the activity type
     * @param value the optional activity value (ATS score, interview score,
     *              study streak, readiness score), or {@code null}
     */
    void recordActivity(User user, ActivityType type, Integer value);

    /**
     * The gamification summary for the authenticated user: level state,
     * XP totals, badge completion, and recent unlocks.
     *
     * @return the summary
     */
    AchievementSummaryResponse getSummary();

    /**
     * Per-badge progress for the authenticated user (locked and unlocked),
     * in catalog order.
     *
     * @return the progress list
     */
    List<AchievementProgressResponse> getProgress();

    /**
     * The badges the authenticated user has unlocked, newest first.
     *
     * @return the unlocked badges
     */
    List<UnlockedAchievementResponse> getUserAchievements();

    /**
     * The full static achievement catalog.
     *
     * @return the catalog
     */
    List<AchievementDefinitionResponse> getAchievementCatalog();

    /**
     * The authenticated user's recent XP ledger, newest first.
     *
     * @return the XP history entries
     */
    List<XpHistoryResponse> getXpHistory();

}
