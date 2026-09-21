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
 * Regression test: verifies every security header is set on every response,
 * even after we disabled Spring Security's built-in header management and
 * moved responsibility to SecurityHeadersFilter.
 *
 * This test is the executable guarantee that disabling Spring Security's
 * headers did NOT weaken the security posture.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityHeadersRegressionTest {

    @Autowired
    MockMvc mockMvc;

    private static final String JSON_ENDPOINT = "/api/verifications/ZT-2026-000000000000";

    @Test
    @DisplayName("All security headers present on a JSON endpoint")
    void allHeadersOnJsonEndpoint() throws Exception {
        mockMvc.perform(get(JSON_ENDPOINT))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("Referrer-Policy"))
                .andExpect(header().exists("Permissions-Policy"))
                .andExpect(header().exists("Cross-Origin-Resource-Policy"))
                .andExpect(header().exists("Cross-Origin-Opener-Policy"))
                .andExpect(header().exists("X-Robots-Tag"));
    }

    @Test
    @DisplayName("JSON endpoint uses strict CSP with frame-ancestors 'none'")
    void strictCspOnJsonEndpoint() throws Exception {
        mockMvc.perform(get(JSON_ENDPOINT))
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName("Verification endpoints have X-Robots-Tag: noindex")
    void verificationHasNoIndex() throws Exception {
        mockMvc.perform(get(JSON_ENDPOINT))
                .andExpect(header().string("X-Robots-Tag",
                        "noindex, nofollow, noarchive, nosnippet"));
    }

    @Test
    @DisplayName("Verification endpoints have private, no-store cache control")
    void verificationHasNoStoreCache() throws Exception {
        mockMvc.perform(get(JSON_ENDPOINT))
                .andExpect(header().string("Cache-Control", "private, no-store, max-age=0"));
    }
}
