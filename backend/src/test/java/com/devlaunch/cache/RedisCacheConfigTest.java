package com.devlaunch.cache;

import com.devlaunch.dto.response.AchievementSummaryResponse;
import com.devlaunch.dto.response.DashboardResponse;
import com.devlaunch.dto.response.InterviewScheduleResponse;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.dto.response.ReadinessModuleResponse;
import com.devlaunch.dto.response.StudyPlannerResponse;
import com.devlaunch.dto.response.TimelineEventResponse;
import com.devlaunch.dto.response.UnlockedAchievementResponse;
import com.devlaunch.entity.enums.ApplicationPriority;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.StudyPriority;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.entity.enums.TimelineEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.test.context.ActiveProfiles;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link RedisCacheConfig}.
 * <p>
 * Boots the full application context (which creates the Redis-backed cache
 * manager without connecting — connections are lazy) and verifies the cache
 * catalog, the per-cache TTLs, and that the configured JSON serializers
 * round-trip the cached value types (response DTOs and the scalar unread
 * count) without a live Redis.
 * </p>
 *
 * @author DevLaunch
 */
@SpringBootTest
@ActiveProfiles("test")
class RedisCacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("registers every expected cache name")
    void registersAllCacheNames() {
        final Set<String> expected = Set.of(
                CacheNames.DASHBOARD, CacheNames.RESUME, CacheNames.GITHUB,
                CacheNames.LEETCODE, CacheNames.STUDY, CacheNames.JOB,
                CacheNames.NOTIFICATIONS,
                CacheNames.ACHIEVEMENT_SUMMARY, CacheNames.ACHIEVEMENT_LIST,
                CacheNames.ACHIEVEMENT_PROGRESS, CacheNames.ACHIEVEMENT_UNLOCKS,
                CacheNames.ACHIEVEMENT_HISTORY);
        assertTrue(cacheManager.getCacheNames().containsAll(expected));
    }

    @Test
    @DisplayName("configures the per-cache TTLs from the cache policy")
    void configuresPerCacheTtl() {
        assertTtl(CacheNames.DASHBOARD, Duration.ofMinutes(5));
        assertTtl(CacheNames.RESUME, Duration.ofMinutes(15));
        assertTtl(CacheNames.GITHUB, Duration.ofMinutes(30));
        assertTtl(CacheNames.LEETCODE, Duration.ofMinutes(30));
        assertTtl(CacheNames.STUDY, Duration.ofMinutes(5));
        assertTtl(CacheNames.JOB, Duration.ofMinutes(5));
        assertTtl(CacheNames.NOTIFICATIONS, Duration.ofMinutes(2));
        assertTtl(CacheNames.ACHIEVEMENT_SUMMARY, Duration.ofMinutes(5));
        assertTtl(CacheNames.ACHIEVEMENT_LIST, Duration.ofMinutes(5));
        assertTtl(CacheNames.ACHIEVEMENT_PROGRESS, Duration.ofMinutes(5));
        assertTtl(CacheNames.ACHIEVEMENT_UNLOCKS, Duration.ofMinutes(5));
        assertTtl(CacheNames.ACHIEVEMENT_HISTORY, Duration.ofMinutes(5));
    }

    @Test
    @DisplayName("the achievement-summary serializer round-trips the gamification summary")
    void achievementsSerializerRoundTripsSummary() {
        final AchievementSummaryResponse summary = AchievementSummaryResponse.builder()
                .level(3)
                .levelTitle("Achiever")
                .totalXp(260)
                .currentLevelXp(250)
                .nextLevelXp(500)
                .nextLevel(4)
                .xpIntoLevel(10)
                .xpNeededForNext(240)
                .levelProgressPercent(4.0)
                .totalAchievements(17)
                .unlockedCount(2)
                .lockedCount(15)
                .completionPercent(12)
                .latestUnlock(UnlockedAchievementResponse.builder()
                        .id(1L)
                        .code("RESUME_EXPLORER")
                        .title("Resume Explorer")
                        .icon("📄")
                        .color("#6366f1")
                        .xpReward(100)
                        .unlockedAt(LocalDateTime.of(2026, 8, 7, 10, 0))
                        .build())
                .recentUnlocks(new ArrayList<>(List.of(UnlockedAchievementResponse.builder()
                        .id(1L)
                        .code("RESUME_EXPLORER")
                        .title("Resume Explorer")
                        .unlockedAt(LocalDateTime.of(2026, 8, 7, 10, 0))
                        .build())))
                .build();

        final byte[] bytes = writeValue(CacheNames.ACHIEVEMENT_SUMMARY, summary);
        final Object deserialized = readValue(CacheNames.ACHIEVEMENT_SUMMARY, bytes);

        assertTrue(deserialized instanceof AchievementSummaryResponse);
        final AchievementSummaryResponse result = (AchievementSummaryResponse) deserialized;
        assertEquals(3, result.getLevel());
        assertEquals("Achiever", result.getLevelTitle());
        assertEquals(260L, result.getTotalXp());
        assertEquals(17, result.getTotalAchievements());
        assertEquals(12, result.getCompletionPercent());
        assertEquals(LocalDateTime.of(2026, 8, 7, 10, 0), result.getLatestUnlock().getUnlockedAt());
        assertEquals(1, result.getRecentUnlocks().size());
    }

    @Test
    @DisplayName("the dashboard value serializer round-trips a full response DTO")
    void dashboardSerializerRoundTripsDto() {
        final DashboardResponse dto = DashboardResponse.builder()
                .placementReadiness(81)
                .readinessStatus("Placement Ready")
                .atsReviewedAt(LocalDateTime.of(2026, 8, 7, 10, 0))
                .readinessModules(List.of(ReadinessModuleResponse.builder()
                        .key("GITHUB").label("GitHub").value("28 Repositories").score(28).build()))
                .readinessStrengths(List.of("Resume completed"))
                .build();

        final byte[] bytes = writeValue(CacheNames.DASHBOARD, dto);
        final Object deserialized = readValue(CacheNames.DASHBOARD, bytes);

        assertTrue(deserialized instanceof DashboardResponse);
        final DashboardResponse result = (DashboardResponse) deserialized;
        assertEquals(81, result.getPlacementReadiness());
        assertEquals("Placement Ready", result.getReadinessStatus());
        assertEquals(LocalDateTime.of(2026, 8, 7, 10, 0), result.getAtsReviewedAt());
        assertEquals(1, result.getReadinessModules().size());
        assertEquals("GitHub", result.getReadinessModules().get(0).getLabel());
        assertEquals(List.of("Resume completed"), result.getReadinessStrengths());
    }

    @Test
    @DisplayName("the notifications value serializer round-trips the unread count as Long")
    void notificationsSerializerRoundTripsLong() {
        final byte[] bytes = writeValue(CacheNames.NOTIFICATIONS, 3L);
        final Object deserialized = readValue(CacheNames.NOTIFICATIONS, bytes);

        assertTrue(deserialized instanceof Long, "expected Long but got " + deserialized.getClass());
        assertEquals(3L, deserialized);
    }

    @Test
    @DisplayName("the job tracker serializer round-trips enriched application responses")
    void jobSerializerRoundTripsApplicationList() {
        // Mutable ArrayList mirrors what the service actually caches so the
        // bare-root list round-trip is proven.
        final List<JobApplicationResponse> applications = new ArrayList<>(List.of(JobApplicationResponse.builder()
                .id(7L)
                .companyName("Acme")
                .jobRole("Software Engineer")
                .applicationDate(LocalDate.of(2026, 8, 1))
                .status(ApplicationStatus.INTERVIEW)
                .priority(ApplicationPriority.HIGH)
                .notesCount(2)
                .attachmentCount(1)
                .timeline(List.of(TimelineEventResponse.builder()
                        .id(1L)
                        .eventType(TimelineEventType.INTERVIEW)
                        .title("Interview scheduled")
                        .occurredAt(LocalDateTime.of(2026, 8, 2, 9, 30))
                        .build()))
                .interviews(List.of(InterviewScheduleResponse.builder()
                        .id(3L)
                        .title("Technical")
                        .scheduledDate(LocalDate.of(2026, 8, 10))
                        .scheduledTime(LocalTime.of(14, 0))
                        .cancelled(Boolean.FALSE)
                        .build()))
                .build()));

        final Object deserialized = readValue(CacheNames.JOB,
                writeValue(CacheNames.JOB, applications));

        @SuppressWarnings("unchecked")
        final List<JobApplicationResponse> result = (List<JobApplicationResponse>) deserialized;
        assertEquals(1, result.size());
        final JobApplicationResponse app = result.get(0);
        assertEquals("Acme", app.getCompanyName());
        assertEquals(ApplicationStatus.INTERVIEW, app.getStatus());
        assertEquals(LocalDate.of(2026, 8, 1), app.getApplicationDate());
        assertEquals(1, app.getTimeline().size());
        assertEquals(TimelineEventType.INTERVIEW, app.getTimeline().get(0).getEventType());
        assertEquals(LocalDateTime.of(2026, 8, 2, 9, 30), app.getTimeline().get(0).getOccurredAt());
        assertEquals(1, app.getInterviews().size());
        assertEquals(LocalDate.of(2026, 8, 10), app.getInterviews().get(0).getScheduledDate());
        assertEquals(LocalTime.of(14, 0), app.getInterviews().get(0).getScheduledTime());
    }

    @Test
    @DisplayName("the study planner serializer round-trips planner responses")
    void studySerializerRoundTripsPlannerList() {
        final List<StudyPlannerResponse> planners = new ArrayList<>(List.of(StudyPlannerResponse.builder()
                .id(9L)
                .title("DSA practice")
                .studyDate(LocalDate.of(2026, 8, 7))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 30))
                .priority(StudyPriority.HIGH)
                .status(StudyStatus.COMPLETED)
                .build()));

        final Object deserialized = readValue(CacheNames.STUDY,
                writeValue(CacheNames.STUDY, planners));

        @SuppressWarnings("unchecked")
        final List<StudyPlannerResponse> result = (List<StudyPlannerResponse>) deserialized;
        assertEquals(1, result.size());
        final StudyPlannerResponse planner = result.get(0);
        assertEquals("DSA practice", planner.getTitle());
        assertEquals(StudyPriority.HIGH, planner.getPriority());
        assertEquals(StudyStatus.COMPLETED, planner.getStatus());
        assertEquals(LocalDate.of(2026, 8, 7), planner.getStudyDate());
        assertEquals(LocalTime.of(18, 0), planner.getStartTime());
        assertEquals(LocalTime.of(19, 30), planner.getEndTime());
    }

    private void assertTtl(final String name, final Duration expected) {
        assertEquals(expected, configuration(name).getTtl(), "TTL for cache " + name);
    }

    private RedisCacheConfiguration configuration(final String name) {
        return (RedisCacheConfiguration) nativeCache(name).getCacheConfiguration();
    }

    @SuppressWarnings("rawtypes")
    private byte[] writeValue(final String name, final Object value) {
        final SerializationPair pair = configuration(name).getValueSerializationPair();
        return toBytes(pair.write(value));
    }

    @SuppressWarnings("rawtypes")
    private Object readValue(final String name, final byte[] bytes) {
        final SerializationPair pair = configuration(name).getValueSerializationPair();
        return pair.read(ByteBuffer.wrap(bytes));
    }

    private byte[] toBytes(final ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private RedisCache nativeCache(final String name) {
        return (RedisCache) ((LoggingCacheManager) cacheManager).unwrapCache(name);
    }

}
