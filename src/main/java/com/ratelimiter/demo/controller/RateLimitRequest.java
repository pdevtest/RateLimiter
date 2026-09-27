package com.ratelimiter.demo.controller;

import jakarta.validation.constraints.Min;

public record RateLimitRequest(
        @Min(1) long capacity,
        @Min(1) long periodSeconds) { }
