package com.zamtrust.dto;

import com.zamtrust.domain.Document;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String verificationId,
        String fileName,
        String title,
        String contentType,
        long sizeBytes,
        String originalHash,
        String status,
        String ownerUsername,
        Instant createdAt,
        Instant signedAt
) {
    public static DocumentResponse from(Document d) {
        return new DocumentResponse(
                d.getId(),
                d.getVerificationId(),
                d.getFileName(),
                d.getTitle(),
                d.getContentType(),
                d.getSizeBytes(),
                d.getOriginalHash(),
                d.getStatus().name(),
                d.getOwner() != null ? d.getOwner().getUsername() : null,
                d.getCreatedAt(),
                d.getSignedAt()
        );
    }
}