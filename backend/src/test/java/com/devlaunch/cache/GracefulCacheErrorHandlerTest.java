package com.devlaunch.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GracefulCacheErrorHandler}.
 * <p>
 * Verifies that every cache operation failure (read, write, evict, clear) is
 * swallowed so the intercepted method simply falls back to the database —
 * the behavior that keeps the application functional without Redis.
 * </p>
 *
 * @author DevLaunch
 */
class GracefulCacheErrorHandlerTest {

    private final GracefulCacheErrorHandler handler = new GracefulCacheErrorHandler();

    private final Cache cache = mock(Cache.class);

    private final RedisConnectionFailureException failure =
            new RedisConnectionFailureException("Redis unavailable");

    @Test
    @DisplayName("a failed cache read is logged and swallowed")
    void failedReadIsSwallowed() {
        when(cache.getName()).thenReturn("dashboard");
        assertDoesNotThrow(() -> handler.handleCacheGetError(failure, cache, "user:1"));
    }

    @Test
    @DisplayName("a failed cache write is logged and swallowed")
    void failedWriteIsSwallowed() {
        when(cache.getName()).thenReturn("dashboard");
        assertDoesNotThrow(() -> handler.handleCachePutError(failure, cache, "user:1", "value"));
    }

    @Test
    @DisplayName("a failed eviction is logged and swallowed")
    void failedEvictionIsSwallowed() {
        when(cache.getName()).thenReturn("notifications");
        assertDoesNotThrow(() -> handler.handleCacheEvictError(failure, cache, "user:1"));
    }

    @Test
    @DisplayName("a failed clear is logged and swallowed")
    void failedClearIsSwallowed() {
        when(cache.getName()).thenReturn("github");
        assertDoesNotThrow(() -> handler.handleCacheClearError(failure, cache));
    }

}
