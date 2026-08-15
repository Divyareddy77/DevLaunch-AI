package com.devlaunch.cache;

/**
 * Central constants for the DevLaunch Redis cache names.
 * <p>
 * Every cached read belongs to exactly one of these user-scoped caches,
 * and every write that changes the underlying data evicts the affected
 * caches. Keeping the names here avoids string duplication between the
 * cache configuration (TTLs) and the {@code @Cacheable}/{@code @CacheEvict}
 * annotations in the services.
 * </p>
 *
 * @author DevLaunch
 */
public final class CacheNames {

    /** Dashboard statistics, placement readiness, and analytics snapshot. */
    public static final String DASHBOARD = "dashboard";

    /** Resume review data (latest review and per-user review reads). */
    public static final String RESUME = "resume";

    /** GitHub profile and repository statistics. */
    public static final String GITHUB = "github";

    /** LeetCode profile and solved-problem statistics. */
    public static final String LEETCODE = "leetcode";

    /** Study planner tasks and statistics. */
    public static final String STUDY = "study";

    /** Job application list and tracker statistics. */
    public static final String JOB = "job";

    /**
     * Job tracker analytics snapshot. Kept in its own cache so the list read
     * ({@link #JOB}) and the analytics read never share a Redis key — two
     * cache entries with different root types must never collide.
     */
    public static final String JOB_ANALYTICS = "job-analytics";

    /** Notification center counts. */
    public static final String NOTIFICATIONS = "notifications";

    /**
     * Gamification caches. Each cache holds exactly one response type so
     * Redis entries can never be read back as an incompatible type:
     * <ul>
     *   <li>{@code achievement-summary} — {@code AchievementSummaryResponse}</li>
     *   <li>{@code achievement-list} — the static catalog, {@code List<AchievementDefinitionResponse>}</li>
     *   <li>{@code achievement-progress} — {@code List<AchievementProgressResponse>}</li>
     *   <li>{@code achievement-unlocks} — {@code List<UnlockedAchievementResponse>}</li>
     *   <li>{@code achievement-history} — {@code List<XpHistoryResponse>}</li>
     * </ul>
     */
    public static final String ACHIEVEMENT_SUMMARY = "achievement-summary";
    public static final String ACHIEVEMENT_LIST = "achievement-list";
    public static final String ACHIEVEMENT_PROGRESS = "achievement-progress";
    public static final String ACHIEVEMENT_UNLOCKS = "achievement-unlocks";
    public static final String ACHIEVEMENT_HISTORY = "achievement-history";

    /**
     * Reserved for the future global leaderboard (ranked by total XP);
     * not yet wired into the cache manager.
     */
    public static final String LEADERBOARD = "leaderboard";

    private CacheNames() {
        // Constants holder — never instantiated.
    }

}
