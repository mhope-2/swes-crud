package com.michaelhope.infrastructure.cache;

import com.michaelhope.api.response.SoftwareEngineerResponse;
import com.michaelhope.application.port.out.EngineerCacheStatistics;
import com.michaelhope.application.port.out.EngineerResponseCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;


@Component
@RequiredArgsConstructor
public class CaffeineEngineerResponseCache implements EngineerResponseCache, EngineerCacheStatistics {

    private final CacheManager cacheManager;

    @Override
    public void putAfterCommit(Integer id, SoftwareEngineerResponse response) {
        afterCommit(() -> cache().put(id, response));
    }

    @Override
    public void evictAfterCommit(Integer id) {
        afterCommit(() -> cache().evict(id));
    }

    @Override
    public Snapshot stats() {
        CacheStats stats = nativeCache().stats();
        return new Snapshot(
            CacheNames.ENGINEER_BY_ID,
            nativeCache().estimatedSize(),
            stats.hitCount(),
            stats.missCount(),
            stats.hitRate(),
            stats.missRate(),
            stats.loadSuccessCount(),
            stats.loadFailureCount(),
            stats.totalLoadTime(),
            stats.evictionCount()
        );
    }

    private Cache cache() {
        return Objects.requireNonNull(
            cacheManager.getCache(CacheNames.ENGINEER_BY_ID),
            "Engineer cache is not configured"
        );
    }

    @SuppressWarnings("unchecked")
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache() {
        Cache cache = cache();
        if (!(cache instanceof CaffeineCache caffeineCache)) {
            throw new IllegalStateException("Engineer cache must use Caffeine");
        }
        return (com.github.benmanes.caffeine.cache.Cache<Object, Object>) caffeineCache.getNativeCache();
    }

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
