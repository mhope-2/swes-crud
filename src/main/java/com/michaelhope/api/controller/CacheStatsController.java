package com.michaelhope.api.controller;

import com.michaelhope.api.response.EngineerCacheStatsResponse;
import com.michaelhope.application.port.out.EngineerCacheStatistics;
import com.michaelhope.application.port.out.EngineerCacheStatistics.Snapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/cache")
@RequiredArgsConstructor
public class CacheStatsController {

    private final EngineerCacheStatistics engineerCacheStatistics;

    @GetMapping("engineer-by-id/stats")
    public ResponseEntity<EngineerCacheStatsResponse> getEngineerCacheStats() {
        Snapshot stats = engineerCacheStatistics.stats();
        return ResponseEntity.ok(new EngineerCacheStatsResponse(
            stats.cacheName(), stats.estimatedSize(), stats.hitCount(), stats.missCount(),
            stats.hitRate(), stats.missRate(), stats.loadSuccessCount(), stats.loadFailureCount(),
            stats.totalLoadTimeNanos(), stats.evictionCount()
        ));
    }
}
