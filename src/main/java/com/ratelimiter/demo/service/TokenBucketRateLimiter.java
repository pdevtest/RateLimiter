package com.ratelimiter.demo.service;

import com.ratelimiter.demo.domain.RateLimit;
import com.ratelimiter.demo.store.ClientBucketStore;
import com.ratelimiter.demo.store.ClientLimitStore;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class TokenBucketRateLimiter implements RateLimiter, RateLimitConfigurationService {

    private final ClientLimitStore limitStore;
    private final ClientBucketStore bucketStore;
    private final Clock clock;

    public TokenBucketRateLimiter(ClientLimitStore limitStore, ClientBucketStore bucketStore, Clock clock) {
        this.limitStore = Objects.requireNonNull(limitStore);
        this.bucketStore = Objects.requireNonNull(bucketStore);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void configure(String clientId, RateLimit rateLimit) {
        limitStore.put(validateClientId(clientId), rateLimit);
    }

    @Override
    public void remove(String clientId) {
        String validateClientId = validateClientId(clientId);
        limitStore.remove(validateClientId);
        bucketStore.remove(validateClientId);
    }

    @Override
    public boolean allowRequest(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return false;
        }

        RateLimit limit = limitStore.find(clientId).orElse(null);
        if (limit == null) {
            return false;
        }

        Instant now = clock.instant();
        return bucketStore.getOrCreate(clientId, limit, now).tryConsume(now, limit);

    }

    public static String validateClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId cannot be blank");
        }
        return clientId;
    }
}
