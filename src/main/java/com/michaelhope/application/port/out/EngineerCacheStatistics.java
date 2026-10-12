package com.michaelhope.application.port.out;

public interface EngineerCacheStatistics {

    Snapshot stats();

    record Snapshot(
        String cacheName,
        long estimatedSize,
        long hitCount,
        long missCount,
        double hitRate,
        double missRate,
        long loadSuccessCount,
        long loadFailureCount,
        long totalLoadTimeNanos,
        long evictionCount
    ) {
    }
}
