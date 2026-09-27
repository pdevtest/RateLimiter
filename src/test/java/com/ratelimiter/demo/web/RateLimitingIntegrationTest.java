package com.ratelimiter.demo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returns429AfterConfiguredCapacityIsExhausted() throws Exception {
        mockMvc.perform(put("/admin/clients/integration-client/rate-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacity\":2,\"periodSeconds\":3600}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/example").header("X-Client-Id", "integration-client")).andExpect(status().isOk());
        mockMvc.perform(get("/api/example").header("X-Client-Id", "integration-client")).andExpect(status().isOk());
        mockMvc.perform(get("/api/example").header("X-Client-Id", "integration-client"))
                .andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After", "1"));
    }

    @Test
    void requiresClientIdForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/example")).andExpect(status().isBadRequest());
    }
}
