package com.ratelimiter.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ratelimiter.demo.domain.RateLimit;
import com.ratelimiter.demo.store.InMemoryClientBucketStore;
import com.ratelimiter.demo.store.InMemoryClientLimitStore;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {
    private MutableClock clock;
    private TokenBucketRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        limiter = new TokenBucketRateLimiter(new InMemoryClientLimitStore(), new InMemoryClientBucketStore(), clock);
    }

    @Test
    void allowsCapacityThenRejects() {
        limiter.configure("customerA", new RateLimit(2, Duration.ofMinutes(1)));
        assertThat(limiter.allowRequest("customerA")).isTrue();
        assertThat(limiter.allowRequest("customerA")).isTrue();
        assertThat(limiter.allowRequest("customerA")).isFalse();
    }

    @Test
    void refillsTokensOverElapsedTime() {
        limiter.configure("customerA", new RateLimit(10, Duration.ofSeconds(10)));
        for (int i = 0; i < 10; i++) {
            assertThat(limiter.allowRequest("customerA")).isTrue();
        }
        assertThat(limiter.allowRequest("customerA")).isFalse();
        clock.advance(Duration.ofSeconds(3));
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.allowRequest("customerA")).isTrue();
        }
        assertThat(limiter.allowRequest("customerA")).isFalse();
    }

    @Test
    void keepsClientsIndependent() {
        limiter.configure("customerA", new RateLimit(1, Duration.ofMinutes(1)));
        limiter.configure("customerB", new RateLimit(2, Duration.ofMinutes(1)));
        assertThat(limiter.allowRequest("customerA")).isTrue();
        assertThat(limiter.allowRequest("customerA")).isFalse();
        assertThat(limiter.allowRequest("customerB")).isTrue();
        assertThat(limiter.allowRequest("customerB")).isTrue();
    }

    @Test
    void rejectsUnconfiguredClients() {
        assertThat(limiter.allowRequest("unknown")).isFalse();
    }

    @Test
    void appliesTighterConfigurationImmediately() {
        limiter.configure("customerA", new RateLimit(10, Duration.ofMinutes(1)));
        limiter.configure("customerA", new RateLimit(2, Duration.ofMinutes(1)));
        assertThat(limiter.allowRequest("customerA")).isTrue();
        assertThat(limiter.allowRequest("customerA")).isTrue();
        assertThat(limiter.allowRequest("customerA")).isFalse();
    }
}
