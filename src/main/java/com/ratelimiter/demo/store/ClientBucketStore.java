package com.ratelimiter.demo.store;

import com.ratelimiter.demo.domain.RateLimit;
import com.ratelimiter.demo.domain.TokenBucket;

import java.time.Instant;

public interface ClientBucketStore {
    TokenBucket getOrCreate(String clientid, RateLimit rateLimit, Instant now);
    void remove(String clientid);
    void evictIdleBefore(Instant cutOff);
    int size();

}
