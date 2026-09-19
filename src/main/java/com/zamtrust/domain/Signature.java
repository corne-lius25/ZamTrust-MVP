package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "signatures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "signer_id")
    private User signer;

    @Column(nullable = false, length = 64)
    private String documentHash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signatureBase64;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKeyBase64;

    @Column(nullable = false, length = 64)
    private String algorithm;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant signedAt = Instant.now();
}
