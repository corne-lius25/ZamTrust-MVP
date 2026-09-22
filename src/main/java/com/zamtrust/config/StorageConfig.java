package com.zamtrust.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Value("${zamtrust.storage.documents-dir}")
    private String documentsDir;

    @Value("${zamtrust.storage.keys-dir}")
    private String keysDir;

    @Value("${zamtrust.storage.signatures-dir:./storage/signatures}")
    private String signaturesDir;

    @PostConstruct
    public void init() {
        ensureDirectory(documentsDir, "documents");
        ensureDirectory(keysDir, "keys");
        ensureDirectory(signaturesDir, "signatures");
    }

    private void ensureDirectory(String pathStr, String label) {
        try {
            Path path = Paths.get(pathStr);
            Files.createDirectories(path);
            if (!Files.isWritable(path)) {
                log.warn("[STORAGE] {} directory {} exists but is NOT writable", label, path);
            } else {
                log.info("[STORAGE] {} directory ready: {}", label, path);
            }
        } catch (Exception e) {
            log.error("[STORAGE] Cannot prepare {} directory {}: {}",
                    label, pathStr, e.getMessage());
            // Don't crash — log and continue. The app can still serve
            // most endpoints. Only operations that write to this dir will fail.
        }
    }
}
