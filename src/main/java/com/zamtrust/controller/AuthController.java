package com.zamtrust.controller;

import com.zamtrust.domain.Role;
import com.zamtrust.domain.User;
import com.zamtrust.dto.AuthResponse;
import com.zamtrust.dto.LoginRequest;
import com.zamtrust.dto.RegisterRequest;
import com.zamtrust.repository.UserRepository;
import com.zamtrust.security.JwtService;
import com.zamtrust.security.RateLimiter;
import com.zamtrust.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int REGISTER_LIMIT = 5;
    private static final int LOGIN_LIMIT = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final RateLimiter rateLimiter;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuditService auditService,
                          RateLimiter rateLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.rateLimiter = rateLimiter;
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

        // Generic message to prevent user enumeration (finding #5).
        // Attacker cannot distinguish "username taken" from "email taken".
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
                    String token = jwtService.generate(u.getUsername());
                    auditService.log(u, "LOGIN", "user:" + u.getId(), "success", ip);
                    Set<String> roles = u.getRoles().stream()
                            .map(Enum::name).collect(Collectors.toSet());
                    return ResponseEntity.ok(new AuthResponse(token, u.getUsername(), roles));
                })
                .orElseGet(() -> {
                    auditService.log(null, "LOGIN_FAILED", "user", req.username(), ip);
                    return ResponseEntity.status(401).body(Map.of(
                            "error", "Invalid username or password."));
                });
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
