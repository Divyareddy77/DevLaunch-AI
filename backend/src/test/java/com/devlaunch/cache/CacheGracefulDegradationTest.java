package com.devlaunch.cache;

import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.repository.RoleRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.security.CustomUserDetails;
import com.devlaunch.service.interfaces.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the application stays fully functional when Redis is
 * unavailable.
 * <p>
 * The cache manager in this test simulates a Redis outage by throwing a
 * {@link RedisConnectionFailureException} on every operation. Through the
 * real {@code @Cacheable} interceptor and {@link GracefulCacheErrorHandler},
 * the intercepted read must fall back to the database and still return a
 * correct result.
 * </p>
 *
 * @author DevLaunch
 */
@SpringBootTest
@ActiveProfiles("test")
class CacheGracefulDegradationTest {

    @TestConfiguration
    static class UnavailableCacheConfiguration {

        @Bean
        @Primary
        CacheManager testCacheManager() {
            return new BrokenCacheManager();
        }
    }

    private static final String USER_EMAIL = "graceful-tester-" + System.nanoTime() + "@example.com";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        final Role role = roleRepository.findByRoleName(RoleType.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT role not seeded"));
        final User user = userRepository.save(User.builder()
                .firstName("Graceful")
                .lastName("Tester")
                .email(USER_EMAIL)
                .password("hashed-password")
                .phone("+1 555 000 0000")
                .isActive(true)
                .role(role)
                .build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user), null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("a read still succeeds from the database when the cache throws")
    void fallsBackToDatabaseWhenRedisIsDown() {
        assertEquals(0, notificationService.getUnreadCount());
    }

    /**
     * Cache manager that simulates a Redis outage on every operation.
     */
    private static final class BrokenCacheManager implements CacheManager {

        @Override
        public Cache getCache(final String name) {
            return new BrokenCache(name);
        }

        @Override
        public Collection<String> getCacheNames() {
            return Set.of(CacheNames.DASHBOARD, CacheNames.RESUME, CacheNames.GITHUB,
                    CacheNames.LEETCODE, CacheNames.STUDY, CacheNames.JOB,
                    CacheNames.JOB_ANALYTICS,
                    CacheNames.NOTIFICATIONS,
                    CacheNames.ACHIEVEMENT_SUMMARY, CacheNames.ACHIEVEMENT_LIST,
                    CacheNames.ACHIEVEMENT_PROGRESS, CacheNames.ACHIEVEMENT_UNLOCKS,
                    CacheNames.ACHIEVEMENT_HISTORY);
        }
    }

    /**
     * Cache that always throws, mimicking an unreachable Redis.
     */
    private static final class BrokenCache implements Cache {

        private final String name;

        private BrokenCache(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return this;
        }

        @Override
        public ValueWrapper get(final Object key) {
            throw unavailable();
        }

        @Override
        public <T> T get(final Object key, final Class<T> type) {
            throw unavailable();
        }

        @Override
        public <T> T get(final Object key, final Callable<T> valueLoader) {
            throw unavailable();
        }

        @Override
        public void put(final Object key, final Object value) {
            throw unavailable();
        }

        @Override
        public void evict(final Object key) {
            throw unavailable();
        }

        @Override
        public void clear() {
            throw unavailable();
        }

        private RedisConnectionFailureException unavailable() {
            return new RedisConnectionFailureException("Redis unavailable");
        }
    }

}
