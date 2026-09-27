package com.ratelimiter.demo.configuration;

import com.ratelimiter.demo.domain.TokenBucket;
import com.ratelimiter.demo.service.TokenBucketRateLimiter;
import com.ratelimiter.demo.store.ClientBucketStore;
import com.ratelimiter.demo.store.ClientLimitStore;
import com.ratelimiter.demo.store.InMemoryClientBucketStore;
import com.ratelimiter.demo.store.InMemoryClientLimitStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateLimiterConfiguration {

    @Bean
    Clock rateLimiterClock() {
        return Clock.systemUTC();
    }

    @Bean
    ClientLimitStore clientLimitStore(RateLimiterProperties properties) {
        return new InMemoryClientLimitStore(properties.getMaxConfiguredClients());
    }

    @Bean
    ClientBucketStore clientBucketStore(Clock rateLimiterClock) {
        return new InMemoryClientBucketStore();
    }

    @Bean
    TokenBucketRateLimiter tokenBucketRateLimiter(
            ClientLimitStore limitStore, ClientBucketStore bucketStore, Clock rateLimiterClock) {
        return new TokenBucketRateLimiter(limitStore, bucketStore, rateLimiterClock);
    }

}
