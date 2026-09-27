package com.ratelimiter.demo.store;

import com.ratelimiter.demo.domain.RateLimit;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryClientLimitStore implements ClientLimitStore {
    ConcurrentHashMap<String, RateLimit> limits = new ConcurrentHashMap<>();
    public final int maxConfiguredClients;

    public InMemoryClientLimitStore(int maxConfiguredClients) {
        this.maxConfiguredClients = maxConfiguredClients;
    }

    public InMemoryClientLimitStore() {
        this(10000);
    }

    @Override
    public Optional<RateLimit> find(String clientid) {
        return Optional.ofNullable(limits.get(clientid));
    }

    @Override
    public void put(String clientid, RateLimit rateLimit) {
        if (!limits.containsKey(clientid) && limits.size() >= maxConfiguredClients) {
            throw new IllegalStateException("Configure client capacity reached");
        }
        limits.put(clientid, rateLimit);
    }

    @Override
    public void remove(String clientid) {
        limits.remove(clientid);
    }
}
