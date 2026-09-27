package com.ratelimiter.demo.domain;

import java.time.Duration;
import java.time.Instant;

public class TokenBucket {

    private double availableTokens;
    private Instant lastRefill;
    private Instant lastAccess;
    private RateLimit appliedRateLimit;


    public TokenBucket(RateLimit limit, Instant now) {
        this.availableTokens = limit.capacity();
        this.lastRefill = now;
        this.lastAccess = now;
        this.appliedRateLimit = limit;
    }

    public static TokenBucket of(RateLimit limit, Instant now) {
        return new TokenBucket(limit, now);
    }

    public synchronized boolean tryConsume(Instant now, RateLimit configuredLimit) {
        refill(now);

        // Configuration changed in between
        if (!configuredLimit.equals(appliedRateLimit)) {
            availableTokens = Math.min(availableTokens, configuredLimit.capacity());
            appliedRateLimit = configuredLimit;
        }

        lastAccess = now;
        if (availableTokens < 1) {
            return false;
        }
        availableTokens -= 1;
        return true;
    }

    public synchronized boolean previousAccessBeforeCutOff(Instant cutOff) {
        return lastAccess.isBefore(cutOff);
    }

    private void refill(Instant now) {
        // In multiple instances deployement time zone should be synchronized
        if (now.isBefore(lastRefill)) {
            return;
        }
        long elapsedTimeinMillis = Duration.between(lastRefill, now).toMillis();
        double refillableTokens = availableTokens + (elapsedTimeinMillis * appliedRateLimit.tokensPerMilliSec());
        if (elapsedTimeinMillis > 0) {
            availableTokens = Math.min(appliedRateLimit.capacity(), refillableTokens);
            lastRefill = now;
        }
    }

}
