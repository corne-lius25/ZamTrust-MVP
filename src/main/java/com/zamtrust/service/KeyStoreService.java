package com.zamtrust.service;

import com.zamtrust.exception.CryptoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;

@Service
public class KeyStoreService {

    @Value("${zamtrust.storage.keys-dir}")
    private String keysDir;

    private final CryptoService cryptoService;

    public KeyStoreService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public Path ensureKeyPair(Long userId) {
        try {
            Path dir = Paths.get(keysDir);
            Files.createDirectories(dir);

            Path priv = dir.resolve("user-" + userId + ".priv");
            Path pub  = dir.resolve("user-" + userId + ".pub");

            if (!Files.exists(priv) || !Files.exists(pub)) {
                KeyPair kp = cryptoService.generateRsaKeyPair();
                Files.write(priv, kp.getPrivate().getEncoded());
                Files.write(pub,  kp.getPublic().getEncoded());
            }
            return priv;
        } catch (Exception e) {
            throw new CryptoException("Key storage failure for user " + userId, e);
        }
    }

    public Path publicKeyPath(Long userId) {
        return Paths.get(keysDir, "user-" + userId + ".pub");
    }

    public Path privateKeyPath(Long userId) {
        return Paths.get(keysDir, "user-" + userId + ".priv");
    }
}