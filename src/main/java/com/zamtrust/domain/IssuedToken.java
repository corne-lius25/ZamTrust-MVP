package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Records every JWT issued by the server so it can be revoked.
 * Resolves finding #8.
 */
@Entity
@Table(name = "issued_tokens", indexes = {
        @Index(name = "idx_issued_tokens_jti", columnList = "jti", unique = true),
        @Index(name = "idx_issued_tokens_user", columnList = "userId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IssuedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String jti;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;
}
