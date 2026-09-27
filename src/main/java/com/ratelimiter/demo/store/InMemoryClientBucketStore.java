package com.ratelimiter.demo.store;

import com.ratelimiter.demo.domain.RateLimit;
import com.ratelimiter.demo.domain.TokenBucket;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryClientBucketStore implements ClientBucketStore {
    ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public TokenBucket getOrCreate(String clientid, RateLimit rateLimit, Instant now) {
        return buckets.computeIfAbsent(clientid, id -> TokenBucket.of(rateLimit, now));
    }

    @Override
    public void remove(String clientid) {
        buckets.remove(clientid);
    }

    @Override
    public void evictIdleBefore(Instant cutOff) {
        buckets.entrySet().removeIf(entry -> entry.getValue().previousAccessBeforeCutOff(cutOff));
    }

    @Override
    public int size() {
        return buckets.size();
    }
}
