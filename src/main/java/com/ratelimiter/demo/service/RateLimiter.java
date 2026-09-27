package com.ratelimiter.demo.service;

public interface RateLimiter {
    boolean allowRequest(String clientId);
}
