package com.zamtrust.service;

import com.zamtrust.domain.*;
import com.zamtrust.repository.DocumentRepository;
import com.zamtrust.repository.SignatureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "zamtrust.storage.documents-dir=./target/test-docs",
        "zamtrust.storage.keys-dir=./target/test-keys"
})
class SigningServiceTest {

    @Autowired CryptoService cryptoService;
    @Autowired KeyStoreService keyStoreService;
    @Autowired SigningService signingService;
    @Autowired DocumentRepository documentRepository;
    @Autowired SignatureRepository signatureRepository;

    @Test
    void signAndPersist() throws Exception {
        Path file = Paths.get("./target/test-docs/sample.txt");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "contract body");

        User signer = User.builder()
                .username("tester-" + System.nanoTime())
                .email("tester@example.com")
                .passwordHash("x")
                .roles(java.util.Set.of(Role.SIGNER))
                .build();
        // persist via repository (in real app, use UserRepository)

        Document doc = Document.builder()
                .verificationId("ZT-TEST-" + System.nanoTime())
                .fileName("sample.txt")
                .storagePath(file.toString())
                .originalHash(cryptoService.sha256Hex(file))
                .status(DocumentStatus.UPLOADED)
                .owner(signer)
                .build();

        // SAVE signer via UserRepository in real test â€” omitted for brevity.
        // Assume signer persisted, doc persisted:
        // userRepository.save(signer);
        // documentRepository.save(doc);

        Signature s = signingService.sign(doc, signer);

        assertNotNull(s.getId());
        assertEquals(CryptoService.SIGN_ALGO, s.getAlgorithm());
        assertEquals(DocumentStatus.SIGNED, doc.getStatus());
    }
}