package com.zamtrust.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates JWTs.
 *
 * Findings resolved:
 *   #8 - jti claim (enables per-token revocation)
 *   #9 - iss and aud claims (defence-in-depth against token confusion)
 */
@Service
public class JwtService {

    private static final String ISSUER = "zamtrust";
    private static final String AUDIENCE = "zamtrust-api";

    @Value("${zamtrust.jwt.secret}")
    private String secret;

    @Value("${zamtrust.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Generates a token with a fresh jti, iss, and aud. Returns both token and jti. */
    public IssuedJwt generate(String username) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .id(jti)
                .subject(username)
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();

        return new IssuedJwt(token, jti, exp.toInstant());
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public String extractJti(String token) {
        return parse(token).getId();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record IssuedJwt(String token, String jti, java.time.Instant expiresAt) {}
}
