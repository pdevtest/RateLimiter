package com.ratelimiter.demo.service;

import com.ratelimiter.demo.configuration.RateLimiterProperties;
import com.ratelimiter.demo.store.ClientBucketStore;

import java.time.Clock;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Component
public class IdleBucketRemovalScheduler {

    private final ClientBucketStore bucketStore;
    private final RateLimiterProperties properties;
    private final Clock clock;


    public IdleBucketRemovalScheduler(ClientBucketStore bucketStore, RateLimiterProperties properties, Clock clock) {
        this.bucketStore = bucketStore;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${rate-limiter.cleanup-interval:PT5M}")
    public void cleanup() {
        bucketStore.evictIdleBefore(Instant.now(clock).minus(properties.getIdleBucketTtl()));
    }


}
