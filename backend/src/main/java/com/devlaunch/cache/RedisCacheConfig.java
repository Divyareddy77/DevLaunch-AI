package com.devlaunch.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Enables and configures the Redis caching layer.
 * <p>
 * Only expensive <em>read</em> operations are cached, always under
 * user-scoped keys derived from the authenticated user's ID (see
 * {@link UserCacheKeyGenerator}); login, JWT validation, password-reset
 * tokens, RabbitMQ messages, and database writes are never cached. Writes
 * that change cached data evict the affected caches via
 * {@code @CacheEvict}/{@code @Caching} in the owning services.
 * </p>
 * <p>     * Each cache has its own TTL tuned to how often its data changes:
 * dashboard 5m, resume review 15m, GitHub 30m, LeetCode 30m, study planner
 * 5m, job tracker 5m, notifications 2m, and the five gamification caches
 * (achievement-summary/list/progress/unlocks/history) 5m each — evicted
 * eagerly whenever XP or badges change. Values are stored as JSON
 * (keys stay as plain strings so Redis keys read like
 * {@code dashboard::42}); the {@code notifications} cache stores the
 * unread-count scalar with a typed serializer so it round-trips as
 * {@code Long}.
 * </p>
 * <p>
 * Keying: dashboard/study/job/notifications are keyed by the authenticated
 * user's ID. GitHub and LeetCode profiles are public data fetched for an
 * arbitrary profile username, so those caches are keyed by the profile
 * username instead — this both prevents cross-account cache poisoning and
 * lets the same public profile be shared between requesters. The GitHub
 * cache further prefixes keys by resource type ({@code profile:}/
 * {@code repos:}/{@code languages:}) so the three different response types
 * never share a Redis key. The {@code resume} cache is reserved for
 * per-user review reads and is evicted whenever a review completes; the
 * latest-review data itself is currently surfaced through the
 * {@code dashboard} cache.
 * </p>
 * <p>
 * Serialization constraint: cached values must have a non-final root type so
 * the JSON type metadata is written. Response DTOs qualify naturally; the
 * job and study tracker reads return a mutable {@code ArrayList} (not
 * {@code List.of()}/{@code toList()}) for exactly this reason.
 * </p>
 * <p>
 * Resilience: caches are decorated with debug-level hit/miss logging
 * ({@link LoggingCacheManager}), and every cache failure is logged and
 * swallowed by {@link GracefulCacheErrorHandler} so the application stays
 * fully functional — reads fall back to the database — when Redis is
 * unavailable.
 * </p>
 *
 * @author DevLaunch
 */
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    /** Fallback TTL for caches without an explicit configuration. */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private static final Duration DASHBOARD_TTL = Duration.ofMinutes(5);
    private static final Duration RESUME_TTL = Duration.ofMinutes(15);
    private static final Duration GITHUB_TTL = Duration.ofMinutes(30);
    private static final Duration LEETCODE_TTL = Duration.ofMinutes(30);
    private static final Duration STUDY_TTL = Duration.ofMinutes(5);
    private static final Duration JOB_TTL = Duration.ofMinutes(5);
    private static final Duration NOTIFICATIONS_TTL = Duration.ofMinutes(2);
    private static final Duration ACHIEVEMENTS_TTL = Duration.ofMinutes(5);

    /** Every gamification cache shares the same 5-minute TTL. */
    private static final String[] ACHIEVEMENT_CACHES = {
            CacheNames.ACHIEVEMENT_SUMMARY, CacheNames.ACHIEVEMENT_LIST,
            CacheNames.ACHIEVEMENT_PROGRESS, CacheNames.ACHIEVEMENT_UNLOCKS,
            CacheNames.ACHIEVEMENT_HISTORY
    };

    /**
     * Builds the Redis-backed cache manager with per-cache TTLs and JSON
     * value serialization. Backs off when another {@link CacheManager} bean
     * is provided (e.g. an in-memory one in tests).
     *
     * @param connectionFactory the Redis connection factory
     * @param objectMapper      the Spring-configured Jackson mapper (includes
     *                          the Java time module so {@code LocalDate}/
     *                          {@code LocalDateTime} values serialize as ISO
     *                          strings)
     * @return the logging cache manager
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(final RedisConnectionFactory connectionFactory,
                                     final ObjectMapper objectMapper) {
        final RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(jsonSerializer(objectMapper)));

        final Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(CacheNames.DASHBOARD, defaults.entryTtl(DASHBOARD_TTL));
        perCache.put(CacheNames.RESUME, defaults.entryTtl(RESUME_TTL));
        perCache.put(CacheNames.GITHUB, defaults.entryTtl(GITHUB_TTL));
        perCache.put(CacheNames.LEETCODE, defaults.entryTtl(LEETCODE_TTL));
        perCache.put(CacheNames.STUDY, defaults.entryTtl(STUDY_TTL));
        perCache.put(CacheNames.JOB, defaults.entryTtl(JOB_TTL));
        perCache.put(CacheNames.JOB_ANALYTICS, defaults.entryTtl(JOB_TTL));
        perCache.put(CacheNames.NOTIFICATIONS, defaults
                .entryTtl(NOTIFICATIONS_TTL)
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new Jackson2JsonRedisSerializer<>(Long.class))));
        for (final String cache : ACHIEVEMENT_CACHES) {
            perCache.put(cache, defaults.entryTtl(ACHIEVEMENTS_TTL));
        }

        final RedisCacheManager delegate = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();

        // The delegate is wrapped (not registered as a bean), so Spring never
        // runs its lifecycle; initialize it explicitly to pre-create the
        // configured caches with their per-cache TTLs and serializers.
        delegate.afterPropertiesSet();

        return new LoggingCacheManager(delegate);
    }

    /**
     * Default key generator for every cached read: user-scoped keys derived
     * from the authenticated user's ID.
     *
     * @return the user cache key generator
     */
    @Override
    public KeyGenerator keyGenerator() {
        return new UserCacheKeyGenerator();
    }

    /**
     * Cache error handler that logs and swallows cache failures so reads
     * fall back to the database when Redis is unavailable.
     *
     * @return the graceful cache error handler
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new GracefulCacheErrorHandler();
    }

    /**
     * JSON value serializer: Spring's {@link GenericJackson2JsonRedisSerializer}
     * built on a copy of the application {@link ObjectMapper} with default
     * typing enabled, so response DTOs round-trip with their concrete types
     * while date/time values keep the application-wide ISO format.
     *
     * @param objectMapper the Spring-configured Jackson mapper
     * @return the value serializer
     */
    private RedisSerializer<Object> jsonSerializer(final ObjectMapper objectMapper) {
        final ObjectMapper mapper = objectMapper.copy();
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

}
