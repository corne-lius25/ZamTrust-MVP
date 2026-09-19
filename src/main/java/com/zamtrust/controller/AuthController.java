package com.zamtrust.controller;

import com.zamtrust.domain.Role;
import com.zamtrust.domain.User;
import com.zamtrust.dto.AuthResponse;
import com.zamtrust.dto.LoginRequest;
import com.zamtrust.dto.RegisterRequest;
import com.zamtrust.repository.UserRepository;
import com.zamtrust.security.JwtService;
import com.zamtrust.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.username()))
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        if (userRepository.existsByEmail(req.email()))
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));

        User u = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .organization(req.organization())
                .roles(Set.of(Role.DOCUMENT_USER))
                .build();
        userRepository.save(u);

        auditService.log(u, "REGISTER", "user:" + u.getId(), "new account", null);
        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return userRepository.findByUsername(req.username())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .filter(User::isEnabled)
                .<ResponseEntity<?>>map(u -> {
                    String token = jwtService.generate(u.getUsername());
                    auditService.log(u, "LOGIN", "user:" + u.getId(), "success", null);
                    Set<String> roles = u.getRoles().stream()
                            .map(Enum::name).collect(Collectors.toSet());
                    return ResponseEntity.ok(new AuthResponse(token, u.getUsername(), roles));
                })
                .orElseGet(() -> {
                    auditService.log(null, "LOGIN_FAILED", "user", req.username(), null);
                    return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
                });
    }
}
