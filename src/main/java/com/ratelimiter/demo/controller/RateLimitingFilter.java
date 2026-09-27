package com.ratelimiter.demo.controller;

import com.ratelimiter.demo.service.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Enforces client limits before a protected controller performs work. */
@Component
public final class RateLimitingFilter extends OncePerRequestFilter {
    private static final String CLIENT_ID_HEADER = "X-Client-Id";
    private final RateLimiter rateLimiter;

    public RateLimitingFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/admin/") || path.equals("/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientId = request.getHeader(CLIENT_ID_HEADER);
        if (clientId == null || clientId.isBlank()) {
            writeError(response, HttpStatus.BAD_REQUEST.value(), "X-Client-Id header is required");
            return;
        }
        if (!rateLimiter.allowRequest(clientId)) {
            response.setHeader("Retry-After", "1");
            writeError(response, HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
