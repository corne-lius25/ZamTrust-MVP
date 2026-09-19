package com.zamtrust.service;

import com.zamtrust.exception.CryptoException;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class CryptoService {

    public static final String SIGN_ALGO = "SHA256withRSA";
    public static final String KEY_ALGO  = "RSA";
    public static final int    KEY_SIZE  = 2048;

    public String sha256Hex(Path file) {
        try {
            return sha256Hex(Files.readAllBytes(file));
        } catch (Exception e) {
            throw new CryptoException("Hashing failed for " + file, e);
        }
    }

    public String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(data));
        } catch (Exception e) {
            throw new CryptoException("Hashing failed", e);
        }
    }

    public KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGO);
            kpg.initialize(KEY_SIZE, new SecureRandom());
            return kpg.generateKeyPair();
        } catch (Exception e) {
            throw new CryptoException("Key generation failed", e);
        }
    }

    public byte[] sign(byte[] data, PrivateKey privateKey) {
        try {
            Signature sig = Signature.getInstance(SIGN_ALGO);
            sig.initSign(privateKey);
            sig.update(data);
            return sig.sign();
        } catch (Exception e) {
            throw new CryptoException("Signing failed", e);
        }
    }

    public boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance(SIGN_ALGO);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    public String encode(byte[] data) { return Base64.getEncoder().encodeToString(data); }
    public byte[] decode(String b64)  { return Base64.getDecoder().decode(b64); }

    public PrivateKey privateKeyFromBytes(byte[] keyBytes) {
        try {
            return KeyFactory.getInstance(KEY_ALGO)
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new CryptoException("Invalid private key", e);
        }
    }

    public PublicKey publicKeyFromBase64(String b64) {
        try {
            return KeyFactory.getInstance(KEY_ALGO)
                    .generatePublic(new X509EncodedKeySpec(decode(b64)));
        } catch (Exception e) {
            throw new CryptoException("Invalid public key", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}