package com.devlaunch.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueRetrievalException;

import java.util.concurrent.Callable;

/**
 * {@link Cache} decorator that logs every access at debug level so cache
 * hit, miss, put, and eviction behavior is observable in the logs.
 * <p>
 * Spring Cache has no built-in hit/miss logging, so every cache returned by
 * {@link LoggingCacheManager} is wrapped with this decorator. The underlying
 * cache instance is shared (the wrapper is stateless), so wrapping on each
 * {@code getCache} lookup is safe.
 * </p>
 *
 * @author DevLaunch
 */
public class LoggingCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(LoggingCache.class);

    private final Cache delegate;

    /**
     * Constructs the decorator around an existing cache.
     *
     * @param delegate the cache to log for
     */
    public LoggingCache(final Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(final Object key) {
        final ValueWrapper value = delegate.get(key);
        if (value != null) {
            log.debug("Cache hit: cache={}, key={}", getName(), key);
        } else {
            log.debug("Cache miss: cache={}, key={}", getName(), key);
        }
        return value;
    }

    @Override
    public <T> T get(final Object key, final Class<T> type) {
        final ValueWrapper value = get(key);
        return value == null ? null : type.cast(value.get());
    }

    @Override
    public <T> T get(final Object key, final Callable<T> valueLoader) {
        final ValueWrapper value = get(key);
        if (value != null) {
            return (T) value.get();
        }
        try {
            final T loaded = valueLoader.call();
            put(key, loaded);
            return loaded;
        } catch (final Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(final Object key, final Object value) {
        log.debug("Cache put: cache={}, key={}", getName(), key);
        delegate.put(key, value);
    }

    @Override
    public void evict(final Object key) {
        log.debug("Cache evicted: cache={}, key={}", getName(), key);
        delegate.evict(key);
    }

    @Override
    public void clear() {
        log.debug("Cache cleared: cache={}", getName());
        delegate.clear();
    }

}
