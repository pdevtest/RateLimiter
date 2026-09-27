package com.ratelimiter.demo.service;

import com.ratelimiter.demo.domain.RateLimit;
import com.ratelimiter.demo.store.ClientBucketStore;

public interface RateLimitConfigurationService {
    void configure(String clientId, RateLimit rateLimit);
    void remove(String clientId);
}
