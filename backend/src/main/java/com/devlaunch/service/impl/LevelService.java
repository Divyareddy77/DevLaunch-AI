package com.devlaunch.service.impl;

import org.springframework.stereotype.Component;

/**
 * Computes the user level from a total XP value.
 * <p>
 * The first levels follow the explicit table from the product spec:
 * Level 1 at 0 XP, Level 2 at 100, Level 3 at 250, Level 4 at 500,
 * Level 5 at 900, Level 6 at 1400. From level 7 onward the curve
 * continues automatically: the XP needed to reach the next level grows
 * by 50 per level (level 7 needs 500 more, level 8 needs 550 more, …).
 * </p>
 * <p>
 * Thresholds are cumulative: a user at 1400 XP is Level 6 and needs 500
 * more XP to reach Level 7 (at 1900).
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class LevelService {

    /**
     * Cumulative XP thresholds for levels 1–6 (index = level - 1).
     * Mirrors the product spec exactly.
     */
    private static final long[] CUMULATIVE_THRESHOLDS = {0, 100, 250, 500, 900, 1400};

    /**
     * The base level from which the automatic curve takes over.
     */
    private static final int CURVE_START_LEVEL = CUMULATIVE_THRESHOLDS.length;

    /**
     * The cumulative threshold of {@link #CURVE_START_LEVEL}.
     */
    private static final long CURVE_START_THRESHOLD =
            CUMULATIVE_THRESHOLDS[CURVE_START_LEVEL - 1];

    /**
     * The XP increment from level 6 to level 7; the increment grows by 50
     * for every further level.
     */
    private static final long CURVE_BASE_INCREMENT = 500;

    /**
     * Human-readable titles for the first levels, used by the UI. Levels
     * beyond the list fall back to "Elite".
     */
    private static final String[] LEVEL_TITLES = {
            "Rookie", "Explorer", "Achiever", "Expert", "Master",
            "Grandmaster", "Legend", "Champion"
    };

    /**
     * Resolves the level for a total XP value.
     *
     * @param totalXp the user's total XP (never negative)
     * @return the level (at least 1)
     */
    public int levelFor(final long totalXp) {
        if (totalXp < 0) {
            return 1;
        }
        // Each threshold that the XP reached raises the level by one:
        // 0 XP -> level 1, 100 XP -> level 2, 250 XP -> level 3, ...
        int level = 0;
        for (final long threshold : CUMULATIVE_THRESHOLDS) {
            if (totalXp >= threshold) {
                level++;
            }
        }
        // Continue the curve beyond the explicit table.
        long threshold = CURVE_START_THRESHOLD;
        long increment = CURVE_BASE_INCREMENT;
        while (totalXp >= threshold + increment) {
            threshold += increment;
            increment += 50;
            level++;
        }
        return level;
    }

    /**
     * Builds the full level snapshot for a total XP value.
     *
     * @param totalXp the user's total XP
     * @return the level info (current level, next level, XP boundaries)
     */
    public LevelInfo levelInfo(final long totalXp) {
        final int level = levelFor(totalXp);
        final long currentThreshold = thresholdFor(level);
        final long nextThreshold = thresholdFor(level + 1);
        final long xpIntoLevel = Math.max(0, totalXp - currentThreshold);
        final long xpNeeded = Math.max(0, nextThreshold - totalXp);
        final long span = nextThreshold - currentThreshold;
        final double percent = span == 0 ? 0.0 : Math.min(100.0, (xpIntoLevel * 100.0) / span);
        return new LevelInfo(level, titleFor(level), totalXp, currentThreshold,
                nextThreshold, level + 1, xpIntoLevel, xpNeeded, percent);
    }

    /**
     * Returns the cumulative XP threshold at which the given level starts.
     *
     * @param level the level (at least 1)
     * @return the cumulative XP threshold of that level
     */
    public long thresholdFor(final int level) {
        if (level <= CUMULATIVE_THRESHOLDS.length) {
            return CUMULATIVE_THRESHOLDS[level - 1];
        }
        long threshold = CURVE_START_THRESHOLD;
        long increment = CURVE_BASE_INCREMENT;
        for (int current = CURVE_START_LEVEL; current < level; current++) {
            threshold += increment;
            increment += 50;
        }
        return threshold;
    }

    /**
     * Returns the display title for a level.
     *
     * @param level the level
     * @return the title, falling back to "Elite" beyond the known list
     */
    public String titleFor(final int level) {
        if (level >= 1 && level <= LEVEL_TITLES.length) {
            return LEVEL_TITLES[level - 1];
        }
        return "Elite";
    }

    /**
     * Immutable snapshot of a user's level state.
     *
     * @param level             the current level
     * @param levelTitle        the display title of the current level
     * @param totalXp           the user's total XP
     * @param currentLevelXp    the cumulative XP at which the current level starts
     * @param nextLevelXp       the cumulative XP at which the next level starts
     * @param nextLevel         the number of the next level
     * @param xpIntoLevel       XP earned within the current level
     * @param xpNeededForNext   XP still needed to reach the next level
     * @param progressPercent   percentage progress within the current level (0–100)
     * @author DevLaunch
     */
    public record LevelInfo(int level,
                            String levelTitle,
                            long totalXp,
                            long currentLevelXp,
                            long nextLevelXp,
                            int nextLevel,
                            long xpIntoLevel,
                            long xpNeededForNext,
                            double progressPercent) {
    }

}
