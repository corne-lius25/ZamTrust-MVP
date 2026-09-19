package com.zamtrust.dto;

import java.time.Instant;

public record VerificationResult(
        String verificationId,
        String fileName,
        boolean integrityValid,
        boolean signatureValid,
        String signer,
        Instant signedAt,
        String algorithm,
        String message
) {}