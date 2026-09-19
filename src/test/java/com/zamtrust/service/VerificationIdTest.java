package com.zamtrust.service;

import com.zamtrust.domain.Document;
import com.zamtrust.domain.User;
import com.zamtrust.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies generated verification IDs are random and unguessable.
 * Resolves finding #7.
 */
@SpringBootTest
@ActiveProfiles("test")
class VerificationIdTest {

    @Autowired
    DocumentService documentService;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Generated verification IDs are random and non-sequential")
    void idsAreRandom() {
        // Save the user first (Hibernate requires a managed entity)
        User owner = User.builder()
                .username("idtest")
                .email("idtest@example.com")
                .passwordHash("x")
                .fullName("ID Test")
                .organization("ZamTrust")
                .enabled(true)
                .roles(Set.of(com.zamtrust.domain.Role.DOCUMENT_USER))
                .build();
        owner = userRepository.save(owner);

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test" + i + ".txt", "text/plain",
                    ("content " + i).getBytes(StandardCharsets.UTF_8));

            Document doc = documentService.upload(file, owner, "Test " + i);
            String id = doc.getVerificationId();

            assertTrue(id.matches("^ZT-\\d{4}-[0-9A-F]{12}$"),
                    "Unexpected ID format: " + id);

            assertTrue(ids.add(id), "Duplicate ID: " + id);
        }
    }
}
