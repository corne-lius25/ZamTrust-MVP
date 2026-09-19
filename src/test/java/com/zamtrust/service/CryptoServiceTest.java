package com.zamtrust.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    private final CryptoService crypto = new CryptoService();

    @Test
    void sha256_isDeterministic() {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        assertEquals(crypto.sha256Hex(data), crypto.sha256Hex(data));
    }

    @Test
    void sha256_changesOnByteFlip() {
        byte[] a = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] b = "hellp".getBytes(StandardCharsets.UTF_8);
        assertNotEquals(crypto.sha256Hex(a), crypto.sha256Hex(b));
    }

    @Test
    void signAndVerify_success() {
        KeyPair kp = crypto.generateRsaKeyPair();
        byte[] data = "document".getBytes(StandardCharsets.UTF_8);

        byte[] sig = crypto.sign(data, kp.getPrivate());
        assertTrue(crypto.verify(data, sig, kp.getPublic()));
    }

    @Test
    void verify_failsOnTamperedData() {
        KeyPair kp = crypto.generateRsaKeyPair();
        byte[] data = "document".getBytes(StandardCharsets.UTF_8);
        byte[] tampered = "documXnt".getBytes(StandardCharsets.UTF_8);

        byte[] sig = crypto.sign(data, kp.getPrivate());
        assertFalse(crypto.verify(tampered, sig, kp.getPublic()));
    }
}