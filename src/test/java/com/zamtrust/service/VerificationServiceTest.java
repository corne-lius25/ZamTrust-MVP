package com.zamtrust.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class VerificationServiceTest {

    private final CryptoService crypto = new CryptoService();

    @Test
    void integrityCheck_detectsTamperedBytes() {
        byte[] original = "original contract".getBytes(StandardCharsets.UTF_8);
        String storedHash = crypto.sha256Hex(original);

        byte[] tampered = "original contract!".getBytes(StandardCharsets.UTF_8);
        assertNotEquals(storedHash, crypto.sha256Hex(tampered));
    }

    @Test
    void signatureVerify_endToEnd() {
        KeyPair kp = crypto.generateRsaKeyPair();
        byte[] original = "signed contract".getBytes(StandardCharsets.UTF_8);

        byte[] sigBytes = crypto.sign(original, kp.getPrivate());
        assertTrue(crypto.verify(original, sigBytes, kp.getPublic()));

        byte[] tampered = "signed contract2".getBytes(StandardCharsets.UTF_8);
        assertFalse(crypto.verify(tampered, sigBytes, kp.getPublic()));
    }
}