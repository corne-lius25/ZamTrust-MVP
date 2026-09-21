package com.zamtrust.service;

import com.zamtrust.domain.Document;
import com.zamtrust.domain.DocumentStatus;
import com.zamtrust.domain.Role;
import com.zamtrust.domain.Signature;
import com.zamtrust.domain.User;
import com.zamtrust.repository.DocumentRepository;
import com.zamtrust.repository.SignatureRepository;
import com.zamtrust.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: exercises the cryptographic signing flow with
 * persisted entities (so Hibernate has real IDs to work with).
 */
@SpringBootTest
@ActiveProfiles("test")
class SigningServiceTest {

    @Autowired CryptoService cryptoService;
    @Autowired SigningService signingService;
    @Autowired UserRepository userRepository;
    @Autowired DocumentRepository documentRepository;
    @Autowired SignatureRepository signatureRepository;

    @Test
    void signAndPersist() throws Exception {
        // 1. Write a file to disk for the "document"
        Path file = Paths.get("./target/test-docs/sample.txt");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "contract body");

        // 2. Save a real user
        User signer = userRepository.save(User.builder()
                .username("sigtest-" + System.nanoTime())
                .email("sigtest-" + System.nanoTime() + "@example.com")
                .passwordHash("x")
                .fullName("Sig Tester")
                .organization("ZamTrust")
                .enabled(true)
                .roles(Set.of(Role.SIGNER))
                .build());

        // 3. Save a real document
        Document doc = documentRepository.save(Document.builder()
                .verificationId("ZT-TEST-" + System.nanoTime())
                .fileName("sample.txt")
                .storagePath(file.toString())
                .originalHash(cryptoService.sha256Hex(file))
                .status(DocumentStatus.UPLOADED)
                .owner(signer)
                .build());

        // 4. Sign
        Signature s = signingService.sign(doc, signer);

        // 5. Assertions
        assertNotNull(s.getId(), "Signature should be persisted");
        assertEquals(CryptoService.SIGN_ALGO, s.getAlgorithm());
        assertEquals(DocumentStatus.SIGNED, doc.getStatus());
        assertNotNull(s.getSignatureBase64());
        assertNotNull(s.getPublicKeyBase64());
        assertEquals(cryptoService.sha256Hex(file), s.getDocumentHash());
    }
}
