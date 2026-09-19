package com.zamtrust.dto;

import com.zamtrust.domain.Signature;

import java.time.Instant;

public record SignatureResponse(
        Long id,
        Long documentId,
        String signerUsername,
        String algorithm,
        String documentHash,
        Instant signedAt
) {
    public static SignatureResponse from(Signature s) {
        return new SignatureResponse(
                s.getId(),
                s.getDocument().getId(),
                s.getSigner().getUsername(),
                s.getAlgorithm(),
                s.getDocumentHash(),
                s.getSignedAt()
        );
    }
}