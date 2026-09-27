package com.ratelimiter.demo.store;

import static org.assertj.core.api.Assertions.assertThat;

import com.ratelimiter.demo.domain.RateLimit;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class InMemoryClientBucketStoreTest {
    @Test
    void evictsOnlyIdleBuckets() {
        InMemoryClientBucketStore store = new InMemoryClientBucketStore();
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        RateLimit limit = new RateLimit(1, Duration.ofMinutes(1));
        store.getOrCreate("idle", limit, start);
        store.getOrCreate("active", limit, start.plusSeconds(100));
        store.evictIdleBefore(start.plusSeconds(50));
        assertThat(store.size()).isEqualTo(1);
    }
}
