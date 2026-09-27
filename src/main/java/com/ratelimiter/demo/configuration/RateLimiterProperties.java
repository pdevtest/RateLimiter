package com.ratelimiter.demo.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {
    private Duration idleBucketTtl = Duration.ofMinutes(30);
    private int maxConfiguredClients = 10000;

    public Duration getIdleBucketTtl() {
        return idleBucketTtl;
    }

    public void setIdleBucketTtl(Duration idleBucketTtl) {
        this.idleBucketTtl = idleBucketTtl;
    }

    public int getMaxConfiguredClients() {
        return maxConfiguredClients;
    }

    public void setMaxConfiguredClients(int maxConfiguredClients) {
        if (maxConfiguredClients <= 0) {
            throw new IllegalArgumentException("maxConfiguredClients must be greater than 0");
        }
        this.maxConfiguredClients = maxConfiguredClients;
    }
}
