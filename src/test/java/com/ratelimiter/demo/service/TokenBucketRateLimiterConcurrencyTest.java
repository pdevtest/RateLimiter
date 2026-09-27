package com.ratelimiter.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ratelimiter.demo.domain.RateLimit;
import com.ratelimiter.demo.store.InMemoryClientBucketStore;
import com.ratelimiter.demo.store.InMemoryClientLimitStore;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterConcurrencyTest {
    @Test
    void neverAllowsMoreThanCapacityUnderConcurrentRequests() throws Exception {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(
                new InMemoryClientLimitStore(), new InMemoryClientBucketStore(), Clock.systemUTC());
        limiter.configure("customerA", new RateLimit(100, Duration.ofHours(1)));
        ExecutorService executor = Executors.newFixedThreadPool(16);
        try {
            List<Callable<Boolean>> requests = new ArrayList<>();
            for (int i = 0; i < 1_000; i++) {
                requests.add(() -> limiter.allowRequest("customerA"));
            }
            List<Future<Boolean>> outcomes = executor.invokeAll(requests);
            long accepted = outcomes.stream().filter(outcome -> {
                try {
                    return outcome.get();
                } catch (Exception exception) {
                    throw new AssertionError(exception);
                }
            }).count();
            assertThat(accepted).isEqualTo(100);
        } finally {
            executor.shutdownNow();
        }
    }
}
