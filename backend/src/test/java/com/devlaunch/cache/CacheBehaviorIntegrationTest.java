package com.devlaunch.cache;

import com.devlaunch.dto.request.CreateStudyPlannerRequest;
import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.entity.enums.StudyPriority;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.repository.NotificationRepository;
import com.devlaunch.repository.RoleRepository;
import com.devlaunch.repository.StudyPlannerRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.security.CustomUserDetails;
import com.devlaunch.service.interfaces.DashboardService;
import com.devlaunch.service.interfaces.NotificationService;
import com.devlaunch.service.interfaces.StudyPlannerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * End-to-end tests for the cache behavior driven through the real service
 * beans and annotations.
 * <p>
 * Uses an in-memory {@link ConcurrentMapCacheManager} (the Redis cache
 * manager bean backs off via {@code @ConditionalOnMissingBean}) so the
 * populate → hit → evict lifecycle is exercised without a live Redis, and
 * repository spies prove that a cached second request never re-queries the
 * database while an evicting update does.
 * </p>
 *
 * @author DevLaunch
 */
@SpringBootTest
@ActiveProfiles("test")
class CacheBehaviorIntegrationTest {

    @TestConfiguration
    static class InMemoryCacheConfiguration {

        @Bean
        @Primary
        CacheManager testCacheManager() {
            return new ConcurrentMapCacheManager(
                    CacheNames.DASHBOARD, CacheNames.RESUME, CacheNames.GITHUB,
                    CacheNames.LEETCODE, CacheNames.STUDY, CacheNames.JOB,
                    CacheNames.NOTIFICATIONS,
                    CacheNames.ACHIEVEMENT_SUMMARY, CacheNames.ACHIEVEMENT_LIST,
                    CacheNames.ACHIEVEMENT_PROGRESS, CacheNames.ACHIEVEMENT_UNLOCKS,
                    CacheNames.ACHIEVEMENT_HISTORY);
        }
    }

    private static final String USER_EMAIL_PREFIX = "cache-tester-";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private StudyPlannerService studyPlannerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoSpyBean
    private NotificationRepository notificationRepository;

    @MockitoSpyBean
    private StudyPlannerRepository studyPlannerRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        final Role role = roleRepository.findByRoleName(RoleType.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT role not seeded"));
        testUser = userRepository.save(User.builder()
                .firstName("Cache")
                .lastName("Tester")
                .email(USER_EMAIL_PREFIX + System.nanoTime() + "@example.com")
                .password("hashed-password")
                .phone("+1 555 000 0000")
                .isActive(true)
                .role(role)
                .build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(testUser), null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        notificationRepository.deleteAll();
        studyPlannerRepository.deleteAll();
    }

    @Test
    @DisplayName("unread count is populated on first read, served from cache, and evicted by updates")
    void unreadCountPopulatedHitAndEvicted() {
        notificationService.createNotification(testUser, NotificationType.SYSTEM, "Welcome", "Hello");

        // First read populates the cache; second read is served from it.
        assertEquals(1, notificationService.getUnreadCount());
        assertEquals(1, notificationService.getUnreadCount());
        verify(notificationRepository, times(1)).countByUserAndIsReadFalse(any(User.class));

        // The update evicts the cache, so the next read re-queries.
        notificationService.markAllAsRead();

        assertEquals(0, notificationService.getUnreadCount());
        verify(notificationRepository, times(2)).countByUserAndIsReadFalse(any(User.class));
    }

    @Test
    @DisplayName("dashboard snapshot is cached and evicted when study data changes")
    void dashboardPopulatedHitAndEvicted() {
        dashboardService.getDashboard();
        dashboardService.getDashboard();
        verify(studyPlannerRepository, times(1)).findByUser(any(User.class));

        // A study write evicts the dashboard and study caches.
        studyPlannerService.createStudyPlanner(CreateStudyPlannerRequest.builder()
                .title("DSA practice")
                .studyDate(LocalDate.now())
                .priority(StudyPriority.HIGH)
                .status(StudyStatus.PENDING)
                .build());

        dashboardService.getDashboard();
        verify(studyPlannerRepository, times(2)).findByUser(any(User.class));
    }

}
