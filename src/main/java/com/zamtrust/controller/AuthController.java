package com.zamtrust.controller;

import com.zamtrust.domain.IssuedToken;
import com.zamtrust.domain.Role;
import com.zamtrust.domain.Subscription;
import com.zamtrust.domain.User;
import com.zamtrust.dto.AuthResponse;
import com.zamtrust.dto.LoginRequest;
import com.zamtrust.dto.RegisterRequest;
import com.zamtrust.repository.IssuedTokenRepository;
import com.zamtrust.repository.UserRepository;
import com.zamtrust.repository.SubscriptionRepository;
import com.zamtrust.security.JwtService;
import com.zamtrust.security.RateLimiter;
import com.zamtrust.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int REGISTER_LIMIT = 5;
    private static final int LOGIN_LIMIT = 30;

    private final UserRepository userRepository;
    private final IssuedTokenRepository issuedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final RateLimiter rateLimiter;
    private final SubscriptionRepository subscriptionRepository;

    public AuthController(UserRepository userRepository,
                          IssuedTokenRepository issuedTokenRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuditService auditService,
                          RateLimiter rateLimiter,
                          SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.issuedTokenRepository = issuedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.rateLimiter = rateLimiter;
        this.subscriptionRepository = subscriptionRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req,
                                      HttpServletRequest http) {
        String ip = clientIp(http);

        if (!rateLimiter.tryConsume("register:" + ip, REGISTER_LIMIT, WINDOW)) {
            auditService.log(null, "REGISTER_RATE_LIMITED", "auth", ip, ip);
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Too many registration attempts. Please try again later."));
        }

        if (userRepository.existsByUsername(req.username())
                || userRepository.existsByEmail(req.email())) {
            auditService.log(null, "REGISTER_FAILED", "auth", "duplicate user", ip);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration could not be completed. Please try again with different credentials."));
        }

        User u = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .organization(req.organization())
                .roles(Set.of(Role.DOCUMENT_USER))
                .build();
        userRepository.save(u);

        // Every user starts on the FREE plan with a subscription row
        subscriptionRepository.save(Subscription.builder()
                .userId(u.getId())
                .plan(com.zamtrust.domain.Plan.FREE)
                .active(true)
                .build());

        auditService.log(u, "REGISTER", "user:" + u.getId(), "new account", ip);
        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req,
                                   HttpServletRequest http) {
        String ip = clientIp(http);

        if (!rateLimiter.tryConsume("login:" + ip, LOGIN_LIMIT, WINDOW)) {
            auditService.log(null, "LOGIN_RATE_LIMITED", "auth", req.username(), ip);
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Too many login attempts. Please try again later."));
        }

        return userRepository.findByUsername(req.username())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .filter(User::isEnabled)
                .<ResponseEntity<?>>map(u -> {
                    JwtService.IssuedJwt issued = jwtService.generate(u.getUsername());

                    // Save issued token so it can be revoked later (finding #8)
                    issuedTokenRepository.save(IssuedToken.builder()
                            .jti(issued.jti())
                            .userId(u.getId())
                            .issuedAt(Instant.now())
                            .expiresAt(issued.expiresAt())
                            .revoked(false)
                            .build());

                    auditService.log(u, "LOGIN", "user:" + u.getId(), "success", ip);
                    Set<String> roles = u.getRoles().stream()
                            .map(Enum::name).collect(Collectors.toSet());
                    return ResponseEntity.ok(new AuthResponse(issued.token(), u.getUsername(), roles));
                })
                .orElseGet(() -> {
                    auditService.log(null, "LOGIN_FAILED", "user", req.username(), ip);
                    return ResponseEntity.status(401).body(Map.of(
                            "error", "Invalid username or password."));
                });
    }

    /**
     * Revokes the current token (finding #8).
     * Requires an authenticated request (Authorization header with the JWT to revoke).
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication auth, HttpServletRequest http) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String header = http.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing token"));
        }
        String token = header.substring(7);

        try {
            String jti = jwtService.extractJti(token);
            issuedTokenRepository.findByJti(jti).ifPresent(it -> {
                it.setRevoked(true);
                issuedTokenRepository.save(it);
            });
            auditService.log(null, "LOGOUT", "auth", auth.getName(), clientIp(http));
            return ResponseEntity.ok(Map.of("message", "Logged out"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }
}
