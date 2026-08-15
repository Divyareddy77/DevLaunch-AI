package com.devlaunch.service.impl;

import com.devlaunch.cache.CacheNames;
import com.devlaunch.dto.response.AchievementDefinitionResponse;
import com.devlaunch.dto.response.AchievementProgressResponse;
import com.devlaunch.dto.response.AchievementSummaryResponse;
import com.devlaunch.dto.response.UnlockedAchievementResponse;
import com.devlaunch.dto.response.XpHistoryResponse;
import com.devlaunch.entity.AchievementDefinition;
import com.devlaunch.entity.ReadinessSnapshot;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.User;
import com.devlaunch.entity.UserAchievement;
import com.devlaunch.entity.XpHistory;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.entity.enums.XpReason;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.GamificationMapper;
import com.devlaunch.messaging.consumer.NotificationEventProcessor;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.AchievementDefinitionRepository;
import com.devlaunch.repository.InterviewSessionRepository;
import com.devlaunch.repository.JobApplicationRepository;
import com.devlaunch.repository.ReadinessSnapshotRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.StudyPlannerRepository;
import com.devlaunch.repository.UserAchievementRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.repository.XpHistoryRepository;
import com.devlaunch.service.interfaces.GamificationService;
import com.devlaunch.service.interfaces.GitHubService;
import com.devlaunch.service.interfaces.LeetCodeService;
import com.devlaunch.service.interfaces.StudyPlannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link GamificationService}.
 * <p>
 * <strong>Write path</strong> ({@link #recordActivity(User, ActivityType, Integer)}):
 * called by the messaging consumer on every platform activity. It awards the
 * activity XP defined for the activity type, then evaluates the full badge
 * catalog — computing each badge's progress from real user data (repository
 * counts, event values, and the cached GitHub/LeetCode profiles) — and
 * unlocks every badge whose target is reached. Unlocking persists a
 * {@link UserAchievement} row (guarded by the unique constraint for
 * duplicate prevention), awards the badge's XP reward, and raises an
 * achievement notification. A level-up notification is added whenever the
 * user's level rises. Everything runs in one transaction; the achievement
 * caches are evicted afterwards.
 * </p>
 * <p>
 * <strong>Read path</strong>: the summary, progress, unlocked badges, and XP
 * history endpoints are cached in Redis, each under its own type-specific
 * cache ({@link CacheNames#ACHIEVEMENT_SUMMARY}, {@link CacheNames#ACHIEVEMENT_LIST},
 * {@link CacheNames#ACHIEVEMENT_PROGRESS}, {@link CacheNames#ACHIEVEMENT_UNLOCKS},
 * {@link CacheNames#ACHIEVEMENT_HISTORY}) keyed by the authenticated user's ID,
 * so the dashboard, profile, and achievements page reads stay fast and
 * consistent and cache entries can never be read back as a different type.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class GamificationServiceImpl implements GamificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationServiceImpl.class);

    /** How many recent unlocks are included in the summary timeline. */
    private static final int RECENT_UNLOCKS_LIMIT = 5;

    /** Achievement codes used by the evaluation logic (seed constants). */
    private static final String CODE_RESUME_EXPLORER = "RESUME_EXPLORER";
    private static final String CODE_ATS_EXPERT = "ATS_EXPERT";
    private static final String CODE_ATS_MASTER = "ATS_MASTER";
    private static final String CODE_FIRST_APPLICATION = "FIRST_APPLICATION";
    private static final String CODE_JOB_HUNTER = "JOB_HUNTER";
    private static final String CODE_INTERVIEW_BEGINNER = "INTERVIEW_BEGINNER";
    private static final String CODE_INTERVIEW_EXPERT = "INTERVIEW_EXPERT";
    private static final String CODE_COMMUNICATION_PRO = "COMMUNICATION_PRO";
    private static final String CODE_STUDY_STARTER = "STUDY_STARTER";
    private static final String CODE_CONSISTENCY_CHAMPION = "CONSISTENCY_CHAMPION";
    private static final String CODE_GITHUB_CONNECTED = "GITHUB_CONNECTED";
    private static final String CODE_GITHUB_CONTRIBUTOR = "GITHUB_CONTRIBUTOR";
    private static final String CODE_LEETCODE_BEGINNER = "LEETCODE_BEGINNER";
    private static final String CODE_LEETCODE_INTERMEDIATE = "LEETCODE_INTERMEDIATE";
    private static final String CODE_LEETCODE_MASTER = "LEETCODE_MASTER";
    private static final String CODE_PLACEMENT_READY = "PLACEMENT_READY";
    private static final String CODE_POWER_USER = "POWER_USER";

    /** The number of distinct modules that count towards Power User. */
    private static final int POWER_USER_MODULE_COUNT = 8;

    /**
     * The XP awarded for each platform activity, mirroring the product spec.
     */
    private static final Map<ActivityType, Integer> ACTIVITY_XP;

    static {
        final Map<ActivityType, Integer> xp = new EnumMap<>(ActivityType.class);
        xp.put(ActivityType.RESUME_CREATED, 100);
        xp.put(ActivityType.RESUME_REVIEWED, 50);
        xp.put(ActivityType.JOB_APPLICATION_CREATED, 20);
        xp.put(ActivityType.INTERVIEW_COMPLETED, 40);
        xp.put(ActivityType.STUDY_TASK_COMPLETED, 15);
        xp.put(ActivityType.GITHUB_CONNECTED, 25);
        xp.put(ActivityType.LEETCODE_SYNCED, 10);
        xp.put(ActivityType.PLACEMENT_UPDATED, 30);
        ACTIVITY_XP = Map.copyOf(xp);
    }

    private final AchievementDefinitionRepository definitionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final XpHistoryRepository xpHistoryRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final StudyPlannerRepository studyPlannerRepository;
    private final ResumeReviewRepository resumeReviewRepository;
    private final ReadinessSnapshotRepository readinessSnapshotRepository;
    private final UserRepository userRepository;
    private final GitHubService gitHubService;
    private final LeetCodeService leetCodeService;
    private final StudyPlannerService studyPlannerService;
    private final LevelService levelService;
    private final GamificationMapper achievementMapper;
    private final NotificationEventProcessor notificationProcessor;

    /**
     * Constructs the gamification service.
     *
     * @param definitionRepository        the achievement catalog repository
     * @param userAchievementRepository   the unlock records repository
     * @param xpHistoryRepository         the XP ledger repository
     * @param resumeRepository            repository for resume counts
     * @param jobApplicationRepository    repository for application counts
     * @param interviewSessionRepository  repository for interview stats
     * @param studyPlannerRepository      repository for study task stats
     * @param resumeReviewRepository      repository for review counts
     * @param readinessSnapshotRepository repository for readiness snapshots
     * @param userRepository              repository for user lookups
     * @param gitHubService               service for GitHub profile data
     * @param leetCodeService             service for LeetCode profile data
     * @param studyPlannerService         service for the study streak calculation
     * @param levelService                computes levels from total XP
     * @param achievementMapper           mapper for entity-to-DTO conversion
     * @param notificationProcessor       shared notification persistence logic
     */
    public GamificationServiceImpl(final AchievementDefinitionRepository definitionRepository,
                                   final UserAchievementRepository userAchievementRepository,
                                   final XpHistoryRepository xpHistoryRepository,
                                   final ResumeRepository resumeRepository,
                                   final JobApplicationRepository jobApplicationRepository,
                                   final InterviewSessionRepository interviewSessionRepository,
                                   final StudyPlannerRepository studyPlannerRepository,
                                   final ResumeReviewRepository resumeReviewRepository,
                                   final ReadinessSnapshotRepository readinessSnapshotRepository,
                                   final UserRepository userRepository,
                                   final GitHubService gitHubService,
                                   final LeetCodeService leetCodeService,
                                   final StudyPlannerService studyPlannerService,
                                   final LevelService levelService,
                                   final GamificationMapper achievementMapper,
                                   final NotificationEventProcessor notificationProcessor) {
        this.definitionRepository = definitionRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.xpHistoryRepository = xpHistoryRepository;
        this.resumeRepository = resumeRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.studyPlannerRepository = studyPlannerRepository;
        this.resumeReviewRepository = resumeReviewRepository;
        this.readinessSnapshotRepository = readinessSnapshotRepository;
        this.userRepository = userRepository;
        this.gitHubService = gitHubService;
        this.leetCodeService = leetCodeService;
        this.studyPlannerService = studyPlannerService;
        this.levelService = levelService;
        this.achievementMapper = achievementMapper;
        this.notificationProcessor = notificationProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.ACHIEVEMENT_SUMMARY, key = "#user.id"),
            @CacheEvict(cacheNames = CacheNames.ACHIEVEMENT_PROGRESS, key = "#user.id"),
            @CacheEvict(cacheNames = CacheNames.ACHIEVEMENT_UNLOCKS, key = "#user.id"),
            @CacheEvict(cacheNames = CacheNames.ACHIEVEMENT_HISTORY, key = "#user.id"),
            @CacheEvict(cacheNames = CacheNames.ACHIEVEMENT_LIST, key = "'catalog'")
    })
    public void recordActivity(final User user, final ActivityType type, final Integer value) {
        final long totalXpBefore = xpHistoryRepository.sumByUser(user);
        final int levelBefore = levelService.levelFor(totalXpBefore);

        // 1. Award the activity XP (resume created, interview completed, …).
        final int activityXp = ACTIVITY_XP.getOrDefault(type, 0);
        if (activityXp > 0) {
            xpHistoryRepository.save(XpHistory.builder()
                    .user(user)
                    .amount(activityXp)
                    .reason(XpReason.fromActivity(type))
                    .description(activityDescription(type))
                    .build());
        }

        // 2. Evaluate the full badge catalog and unlock newly satisfied badges.
        final Set<String> unlockedCodes = userAchievementRepository
                .findByUserOrderByUnlockedAtDesc(user).stream()
                .map(ua -> ua.getAchievementDefinition().getCode())
                .collect(Collectors.toSet());

        final long resumeCount = resumeRepository.countByUser(user);
        final long jobCount = jobApplicationRepository.countByUser(user);
        final long interviewCount = interviewSessionRepository.countByUser(user);
        final long studyCompletedCount = studyPlannerRepository
                .countByUserAndStatus(user, StudyStatus.COMPLETED);
        final double averageInterviewScore = interviewSessionRepository.averageScoreByUser(user);

        final LocalDateTime now = LocalDateTime.now();
        for (final AchievementDefinition definition : definitionRepository.findAllByOrderBySortOrderAsc()) {
            if (unlockedCodes.contains(definition.getCode())) {
                continue;
            }
            final int progress = computeProgress(definition, user, value,
                    resumeCount, jobCount, interviewCount, studyCompletedCount,
                    averageInterviewScore);
            if (progress < definition.getTargetValue()) {
                continue;
            }
            unlockAchievement(user, definition, now);
        }

        // 3. Level-up detection after every XP award (activity + unlocks).
        final long totalXpAfter = xpHistoryRepository.sumByUser(user);
        final int levelAfter = levelService.levelFor(totalXpAfter);
        if (levelAfter > levelBefore) {
            notificationProcessor.process(new NotificationEvent(user.getId(),
                    NotificationType.ACHIEVEMENT,
                    "Level Up!",
                    "Congratulations! You reached Level " + levelAfter
                            + " (" + levelService.titleFor(levelAfter) + ")."));
            log.info("User id={} reached Level {}", user.getId(), levelAfter);
        }

        log.info("Activity processed for user id={}: type={}, activityXp={}, unlockedBadges={}",
                user.getId(), type, activityXp);
    }

    /**
     * Persists the unlock (guarded against duplicates), awards the badge XP,
     * and raises the achievement notification.
     *
     * @param user       the user who unlocked the badge
     * @param definition the unlocked badge
     * @param unlockedAt the unlock timestamp
     */
    private void unlockAchievement(final User user, final AchievementDefinition definition,
                                   final LocalDateTime unlockedAt) {
        try {
            userAchievementRepository.saveAndFlush(UserAchievement.builder()
                    .user(user)
                    .achievementDefinition(definition)
                    .unlockedAt(unlockedAt)
                    .build());
        } catch (final DataIntegrityViolationException ex) {
            // A concurrent consumer or a re-delivered message already
            // unlocked this badge — skip the reward and notification.
            log.info("Achievement '{}' already unlocked for user id={}; skipping duplicate",
                    definition.getCode(), user.getId());
            return;
        }

        xpHistoryRepository.save(XpHistory.builder()
                .user(user)
                .amount(definition.getXpReward())
                .reason(XpReason.ACHIEVEMENT_UNLOCKED)
                .description("Unlocked " + definition.getTitle())
                .build());

        notificationProcessor.process(new NotificationEvent(user.getId(),
                NotificationType.ACHIEVEMENT,
                "Achievement Unlocked",
                "\uD83C\uDFC6 " + definition.getTitle() + "\n+" + definition.getXpReward() + " XP"));

        log.info("Achievement '{}' unlocked for user id={}: +{} XP",
                definition.getCode(), user.getId(), definition.getXpReward());
    }

    /**
     * Computes a badge's current progress for the user. Count-based badges
     * read the pre-fetched repository counts; score-based badges read the
     * event value (ATS score, interview score, readiness score) or the
     * live average; account badges read the user's linked usernames and the
     * cached GitHub/LeetCode profiles; Power User counts the distinct
     * modules the user has engaged with.
     *
     * @param definition           the badge definition
     * @param user                 the user
     * @param value                the event value, or {@code null}
     * @param resumeCount          the user's resume count
     * @param jobCount             the user's job application count
     * @param interviewCount       the user's interview session count
     * @param studyCompletedCount  the user's completed study task count
     * @param averageInterviewScore the user's average interview score
     * @return the current progress towards the badge target
     */
    private int computeProgress(final AchievementDefinition definition, final User user,
                                final Integer value,
                                final long resumeCount, final long jobCount,
                                final long interviewCount, final long studyCompletedCount,
                                final double averageInterviewScore) {
        return switch (definition.getCode()) {
            case CODE_RESUME_EXPLORER -> (int) Math.min(Integer.MAX_VALUE, resumeCount);
            // The event carries the freshest score; the read path falls back
            // to the latest persisted review so progress is always accurate.
            case CODE_ATS_EXPERT, CODE_ATS_MASTER -> value != null ? value : latestAtsScore(user);
            case CODE_FIRST_APPLICATION, CODE_JOB_HUNTER ->
                    (int) Math.min(Integer.MAX_VALUE, jobCount);
            case CODE_INTERVIEW_BEGINNER, CODE_INTERVIEW_EXPERT ->
                    (int) Math.min(Integer.MAX_VALUE, interviewCount);
            case CODE_COMMUNICATION_PRO -> (int) Math.round(averageInterviewScore);
            case CODE_STUDY_STARTER -> (int) Math.min(Integer.MAX_VALUE, studyCompletedCount);
            case CODE_CONSISTENCY_CHAMPION -> value != null ? value : studyPlannerService.getCurrentStudyStreak();
            case CODE_GITHUB_CONNECTED -> user.getGithubUsername() == null ? 0 : 1;
            case CODE_GITHUB_CONTRIBUTOR -> githubRepositoryCount(user);
            case CODE_LEETCODE_BEGINNER, CODE_LEETCODE_INTERMEDIATE, CODE_LEETCODE_MASTER ->
                    leetcodeSolvedCount(user);
            case CODE_PLACEMENT_READY -> value != null ? value : latestReadinessScore(user);
            case CODE_POWER_USER -> modulesUsed(user, resumeCount, jobCount, interviewCount,
                    studyCompletedCount);
            default -> 0;
        };
    }

    /**
     * The ATS score of the user's most recent AI resume review, or 0.
     *
     * @param user the user
     * @return the latest ATS score
     */
    private int latestAtsScore(final User user) {
        return resumeReviewRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .map(ResumeReview::getAtsScore)
                .orElse(0);
    }

    /**
     * The user's latest recorded placement readiness score, or 0.
     *
     * @param user the user
     * @return the latest readiness score
     */
    private int latestReadinessScore(final User user) {
        return readinessSnapshotRepository.findTopByUserOrderByCreatedAtDesc(user)
                .map(ReadinessSnapshot::getScore)
                .orElse(0);
    }

    /**
     * The number of public repositories of the user's linked GitHub account.
     * <p>
     * Reads the cached GitHub profile; any failure (invalid username,
     * GitHub API outage) yields 0 so the badge simply stays locked and the
     * event processing never fails.
     * </p>
     *
     * @param user the user
     * @return the repository count, or 0 when unavailable
     */
    private int githubRepositoryCount(final User user) {
        if (user.getGithubUsername() == null) {
            return 0;
        }
        try {
            final Integer repositories = gitHubService
                    .getGitHubProfile(user.getGithubUsername()).getPublicRepositories();
            return repositories == null ? 0 : repositories;
        } catch (final RuntimeException e) {
            log.warn("Could not fetch GitHub profile for '{}': {}", user.getGithubUsername(),
                    e.getMessage());
            return 0;
        }
    }

    /**
     * The number of problems solved on the user's linked LeetCode account.
     * <p>
     * Reads the cached LeetCode profile; any failure yields 0 (see
     * {@link #githubRepositoryCount(User)}).
     * </p>
     *
     * @param user the user
     * @return the solved count, or 0 when unavailable
     */
    private int leetcodeSolvedCount(final User user) {
        if (user.getLeetcodeUsername() == null) {
            return 0;
        }
        try {
            final Integer solved = leetCodeService
                    .getLeetCodeProfile(user.getLeetcodeUsername()).getTotalSolved();
            return solved == null ? 0 : solved;
        } catch (final RuntimeException e) {
            log.warn("Could not fetch LeetCode profile for '{}': {}", user.getLeetcodeUsername(),
                    e.getMessage());
            return 0;
        }
    }

    /**
     * Counts the distinct platform modules the user has engaged with:
     * resume builder, AI resume review, job tracker, mock interview,
     * study planner, GitHub, LeetCode, and the placement dashboard.
     *
     * @param user                 the user
     * @param resumeCount          pre-fetched resume count
     * @param jobCount             pre-fetched application count
     * @param interviewCount       pre-fetched interview count
     * @param studyCompletedCount  pre-fetched completed study task count
     * @return the number of modules used (0–8)
     */
    private int modulesUsed(final User user, final long resumeCount, final long jobCount,
                            final long interviewCount, final long studyCompletedCount) {
        int modules = 0;
        if (resumeCount > 0) {
            modules++;
        }
        if (resumeReviewRepository.countByUser(user) > 0) {
            modules++;
        }
        if (jobCount > 0) {
            modules++;
        }
        if (interviewCount > 0) {
            modules++;
        }
        if (studyCompletedCount > 0) {
            modules++;
        }
        if (user.getGithubUsername() != null) {
            modules++;
        }
        if (user.getLeetcodeUsername() != null) {
            modules++;
        }
        if (readinessSnapshotRepository.countByUser(user) > 0) {
            modules++;
        }
        return modules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = CacheNames.ACHIEVEMENT_SUMMARY)
    @Transactional(readOnly = true)
    public AchievementSummaryResponse getSummary() {
        final User user = getAuthenticatedUser();
        final long totalXp = xpHistoryRepository.sumByUser(user);
        final LevelService.LevelInfo levelInfo = levelService.levelInfo(totalXp);
        final List<AchievementDefinition> catalog =
                definitionRepository.findAllByOrderBySortOrderAsc();
        final List<UserAchievement> unlocks =
                userAchievementRepository.findByUserOrderByUnlockedAtDesc(user);

        final int unlockedCount = unlocks.size();
        final int total = catalog.size();
        final int completionPercent = total == 0 ? 0
                : (int) Math.round((unlockedCount * 100.0) / total);

        final List<UnlockedAchievementResponse> recentUnlocks = unlocks.stream()
                .limit(RECENT_UNLOCKS_LIMIT)
                .map(achievementMapper::toUnlockedResponse)
                .collect(Collectors.toCollection(ArrayList::new));

        return AchievementSummaryResponse.builder()
                .level(levelInfo.level())
                .levelTitle(levelInfo.levelTitle())
                .totalXp(totalXp)
                .currentLevelXp(levelInfo.currentLevelXp())
                .nextLevelXp(levelInfo.nextLevelXp())
                .nextLevel(levelInfo.nextLevel())
                .xpIntoLevel(levelInfo.xpIntoLevel())
                .xpNeededForNext(levelInfo.xpNeededForNext())
                .levelProgressPercent(levelInfo.progressPercent())
                .totalAchievements(total)
                .unlockedCount(unlockedCount)
                .lockedCount(total - unlockedCount)
                .completionPercent(completionPercent)
                .latestUnlock(unlocks.isEmpty() ? null
                        : achievementMapper.toUnlockedResponse(unlocks.getFirst()))
                .recentUnlocks(recentUnlocks)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = CacheNames.ACHIEVEMENT_PROGRESS)
    @Transactional(readOnly = true)
    public List<AchievementProgressResponse> getProgress() {
        final User user = getAuthenticatedUser();
        final List<AchievementDefinition> catalog =
                definitionRepository.findAllByOrderBySortOrderAsc();
        // One query feeds both the unlocked-code set and the unlock timestamps.
        final List<UserAchievement> unlocks =
                userAchievementRepository.findByUserOrderByUnlockedAtDesc(user);
        final Set<String> unlockedCodes = unlocks.stream()
                .map(ua -> ua.getAchievementDefinition().getCode())
                .collect(Collectors.toSet());
        final Map<String, LocalDateTime> unlockedAtByCode = unlocks.stream()
                .collect(Collectors.toMap(
                        ua -> ua.getAchievementDefinition().getCode(),
                        UserAchievement::getUnlockedAt));

        final long resumeCount = resumeRepository.countByUser(user);
        final long jobCount = jobApplicationRepository.countByUser(user);
        final long interviewCount = interviewSessionRepository.countByUser(user);
        final long studyCompletedCount = studyPlannerRepository
                .countByUserAndStatus(user, StudyStatus.COMPLETED);
        final double averageInterviewScore = interviewSessionRepository.averageScoreByUser(user);

        return catalog.stream().map(definition -> {
            final int progress = computeProgress(definition, user, null,
                    resumeCount, jobCount, interviewCount, studyCompletedCount,
                    averageInterviewScore);
            final boolean unlocked = unlockedCodes.contains(definition.getCode());
            final int progressPercent = definition.getTargetValue() <= 0 ? 100
                    : Math.min(100, (progress * 100) / definition.getTargetValue());
            return AchievementProgressResponse.builder()
                    .id(definition.getId())
                    .code(definition.getCode())
                    .category(definition.getCategory())
                    .title(definition.getTitle())
                    .description(definition.getDescription())
                    .icon(definition.getIcon())
                    .color(definition.getColor())
                    .xpReward(definition.getXpReward())
                    .targetValue(definition.getTargetValue())
                    .progress(Math.min(progress, definition.getTargetValue()))
                    .unlocked(unlocked)
                    .unlockedAt(unlockedAtByCode.get(definition.getCode()))
                    .progressPercent(unlocked ? 100 : progressPercent)
                    .build();
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = CacheNames.ACHIEVEMENT_UNLOCKS)
    @Transactional(readOnly = true)
    public List<UnlockedAchievementResponse> getUserAchievements() {
        final User user = getAuthenticatedUser();
        return userAchievementRepository.findByUserOrderByUnlockedAtDesc(user).stream()
                .map(achievementMapper::toUnlockedResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = CacheNames.ACHIEVEMENT_LIST, key = "'catalog'")
    @Transactional(readOnly = true)
    public List<AchievementDefinitionResponse> getAchievementCatalog() {
        return definitionRepository.findAllByOrderBySortOrderAsc().stream()
                .map(achievementMapper::toAchievementDefinitionResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = CacheNames.ACHIEVEMENT_HISTORY)
    @Transactional(readOnly = true)
    public List<XpHistoryResponse> getXpHistory() {
        final User user = getAuthenticatedUser();
        return xpHistoryRepository.findTop50ByUserOrderByCreatedAtDesc(user).stream()
                .map(achievementMapper::toXpHistoryResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * A human-readable description of an activity XP award.
     *
     * @param type the activity type
     * @return the description
     */
    private String activityDescription(final ActivityType type) {
        return switch (type) {
            case RESUME_CREATED -> "Created a resume";
            case RESUME_REVIEWED -> "Completed an ATS resume review";
            case JOB_APPLICATION_CREATED -> "Added a job application";
            case INTERVIEW_COMPLETED -> "Completed a mock interview";
            case STUDY_TASK_COMPLETED -> "Completed a study task";
            case GITHUB_CONNECTED -> "Connected a GitHub account";
            case LEETCODE_SYNCED -> "Connected a LeetCode account";
            case PLACEMENT_UPDATED -> "Updated placement readiness";
        };
    }

    /**
     * Retrieves the currently authenticated user from the database.
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found
     */
    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

}
