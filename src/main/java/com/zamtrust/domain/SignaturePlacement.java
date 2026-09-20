package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores the position of a visible signature on a specific page of a PDF.
 *
 * Coordinates are expressed as fractions of the page dimensions (0.0 to 1.0)
 * so they survive any change to the PDF's rendered resolution.
 *
 * Resolves the visible-signature requirement of Phase 1.
 */
@Entity
@Table(name = "signature_placements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SignaturePlacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Page number, 1-based. */
    @Column(nullable = false)
    private int page;

    /** X position as fraction of page width (0.0 to 1.0). */
    @Column(nullable = false)
    private double x;

    /** Y position as fraction of page height (0.0 to 1.0), measured from the top. */
    @Column(nullable = false)
    private double y;

    /** Width as fraction of page width. */
    @Column(nullable = false)
    private double width;

    /** Height as fraction of page height. */
    @Column(nullable = false)
    private double height;

    public void validate() {
        if (page < 1) throw new IllegalArgumentException("Page must be >= 1");
        if (x < 0 || y < 0) throw new IllegalArgumentException("Coordinates must be non-negative");
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Size must be positive");
        if (x + width > 1.001 || y + height > 1.001)
            throw new IllegalArgumentException("Signature exceeds page bounds");
    }
}
