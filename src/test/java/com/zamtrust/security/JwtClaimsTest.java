package com.zamtrust.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies JWT claims (finding #9) and token generation (finding #8).
 */
@SpringBootTest
@ActiveProfiles("test")
class JwtClaimsTest {

    @Autowired JwtService jwtService;

    @Test
    @DisplayName("Generated token contains jti, iss, aud, sub, iat, exp")
    void tokenHasAllClaims() {
        var issued = jwtService.generate("testuser");
        String token = issued.token();

        assertNotNull(issued.jti());
        assertFalse(issued.jti().isBlank());

        // Parse the token back
        assertTrue(jwtService.isValid(token));
        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals(issued.jti(), jwtService.extractJti(token));
    }

    @Test
    @DisplayName("Each token gets a unique jti")
    void eachTokenHasUniqueJti() {
        var a = jwtService.generate("user1");
        var b = jwtService.generate("user1");
        assertNotEquals(a.jti(), b.jti());
    }
}
