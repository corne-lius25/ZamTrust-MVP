package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_doc_verification_id", columnList = "verificationId", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String verificationId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 128)
    private String contentType;

    private long sizeBytes;

    @Column(nullable = false, length = 512)
    private String storagePath;

    @Column(nullable = false, length = 64)
    private String originalHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(length = 255)
    private String title;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant signedAt;
}
