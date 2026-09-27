package com.ratelimiter.demo.domain;

import java.time.Duration;

public record RateLimit(long capacity, Duration period) {
    public RateLimit {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Ensure capacity must be greater than zero");
        }
        if (period == null || period.isZero() || period.isNegative()) {
            throw new IllegalArgumentException("Rate limit period must be greater than zero or positive");
        }
    }

    public double tokensPerMilliSec() {
        return (double) capacity / period.toMillis();
    }
}
