package com.ratelimiter.demo.store;

import com.ratelimiter.demo.domain.RateLimit;

import java.util.Optional;

public interface ClientLimitStore {
    Optional<RateLimit> find(String clientid);
    void put(String clientid, RateLimit rateLimit);
    void remove(String clientid);
}
