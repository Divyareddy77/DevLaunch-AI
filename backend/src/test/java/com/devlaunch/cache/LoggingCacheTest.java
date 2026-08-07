package com.devlaunch.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoggingCache}.
 * <p>
 * Verifies that the logging decorator transparently delegates hits, misses,
 * puts, and evictions to the wrapped cache so logging never changes cache
 * behavior.
 * </p>
 *
 * @author DevLaunch
 */
class LoggingCacheTest {

    private final Cache delegate = mock(Cache.class);

    private final LoggingCache cache = new LoggingCache(delegate);

    @Test
    @DisplayName("a hit is delegated and returned unchanged")
    void hitDelegatesAndReturnsValue() {
        final Cache.ValueWrapper wrapper = () -> "value";
        when(delegate.get("key")).thenReturn(wrapper);

        assertSame(wrapper, cache.get("key"));
        verify(delegate).get("key");
    }

    @Test
    @DisplayName("a miss is delegated and returned as null")
    void missDelegatesAndReturnsNull() {
        when(delegate.get("key")).thenReturn(null);

        assertNull(cache.get("key"));
        verify(delegate).get("key");
    }

    @Test
    @DisplayName("puts are delegated")
    void putDelegates() {
        cache.put("key", "value");

        verify(delegate).put("key", "value");
    }

    @Test
    @DisplayName("evictions are delegated")
    void evictDelegates() {
        cache.evict("key");

        verify(delegate).evict("key");
    }

}
