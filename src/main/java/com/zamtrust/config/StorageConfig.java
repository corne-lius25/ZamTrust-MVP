package com.zamtrust.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class StorageConfig {

    @Value("${zamtrust.storage.documents-dir}")
    private String documentsDir;

    @Value("${zamtrust.storage.keys-dir}")
    private String keysDir;

    @PostConstruct
    public void init() throws Exception {
        Files.createDirectories(Path.of(documentsDir));
        Files.createDirectories(Path.of(keysDir));
    }
}