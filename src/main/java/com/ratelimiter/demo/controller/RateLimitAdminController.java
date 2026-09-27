package com.ratelimiter.demo.controller;

import com.ratelimiter.demo.domain.RateLimit;
import com.ratelimiter.demo.service.RateLimitConfigurationService;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/clients")
public class RateLimitAdminController {
    private final RateLimitConfigurationService configurationService;

    public RateLimitAdminController(RateLimitConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PutMapping("/{clientId}/rate-limit")
    public ResponseEntity<Void> configure(@PathVariable String clientId, @Valid @RequestBody RateLimitRequest request) {
        configurationService.configure(clientId, new RateLimit(request.capacity(), Duration.ofSeconds(request.periodSeconds())));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{clientId}/rate-limit")
    public ResponseEntity<Void> remove(@PathVariable String clientId) {
        configurationService.remove(clientId);
        return ResponseEntity.noContent().build();
    }
}
