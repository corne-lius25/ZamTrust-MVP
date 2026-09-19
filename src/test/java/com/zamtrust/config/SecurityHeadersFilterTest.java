package com.zamtrust.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * Verifies the security headers are set on every response.
 * Resolves findings #1, #2, #3.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityHeadersFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Content-Security-Policy is present and restrictive")
    void cspHeader() throws Exception {
        mockMvc.perform(get("/api/verifications/ZT-2026-999999"))
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"));
    }

    @Test
    @DisplayName("Referrer-Policy is set")
    void referrerPolicyHeader() throws Exception {
        mockMvc.perform(get("/api/verifications/ZT-2026-999999"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    @DisplayName("Permissions-Policy is set")
    void permissionsPolicyHeader() throws Exception {
        mockMvc.perform(get("/api/verifications/ZT-2026-999999"))
                .andExpect(header().string("Permissions-Policy",
                        "geolocation=(), microphone=(), camera=(), payment=(), usb=()"));
    }

    @Test
    @DisplayName("Cross-Origin-Resource-Policy is set")
    void corpHeader() throws Exception {
        mockMvc.perform(get("/api/verifications/ZT-2026-999999"))
                .andExpect(header().string("Cross-Origin-Resource-Policy", "same-site"));
    }
}
