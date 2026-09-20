package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A saved signature image belonging to a user.
 *
 * Users may save multiple signatures (e.g. initials, full signature,
 * with/without middle initial) and choose which to use when signing.
 * Exactly one is marked default.
 *
 * Resolves the "visible signature" requirement of Phase 1.
 */
@Entity
@Table(name = "signature_images", indexes = {
        @Index(name = "idx_sigimg_user", columnList = "userId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SignatureImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String label;

    /** PNG file path on disk (or storage path once we move to S3/R2). */
    @Column(nullable = false, length = 512)
    private String storagePath;

    /** SHA-256 of the image file for integrity checks. */
    @Column(nullable = false, length = 64)
    private String sha256;

    /** Rendered width/height in CSS pixels for consistent placement. */
    private int widthPx;
    private int heightPx;

    /** Only one signature per user can be default. Enforced in service layer. */
    @Column(nullable = false)
    private boolean isDefault = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
