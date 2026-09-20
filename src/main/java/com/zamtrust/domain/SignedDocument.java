package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Records that a document was signed with a visible signature,
 * producing a new "signed PDF" file alongside the original.
 *
 * The original is preserved; the signed version is the canonical
 * download that users share.
 *
 * Resolves Phase 1's visible-signature requirement.
 */
@Entity
@Table(name = "signed_documents", indexes = {
        @Index(name = "idx_signed_original", columnList = "originalDocumentId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SignedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long originalDocumentId;

    @Column(nullable = false)
    private Long signatureId;

    @Column(nullable = false, length = 512)
    private String signedStoragePath;

    /** SHA-256 of the signed PDF. Any change to the PDF after signing breaks this. */
    @Column(nullable = false, length = 64)
    private String signedSha256;

    private long signedSizeBytes;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
