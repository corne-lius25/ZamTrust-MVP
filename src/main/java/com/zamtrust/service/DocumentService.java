package com.zamtrust.service;

import com.zamtrust.domain.Document;
import com.zamtrust.domain.DocumentStatus;
import com.zamtrust.domain.User;
import com.zamtrust.exception.FileTooLargeException;
import com.zamtrust.exception.InvalidFileException;
import com.zamtrust.exception.InvalidFileException;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final long MAX_SIZE_BYTES = 25L * 1024 * 1024; // 25MB
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MAX_ID_ATTEMPTS = 5;

    private final DocumentRepository documentRepository;
    private final CryptoService cryptoService;
    private final FileTypeValidator fileTypeValidator;

    @Value("${zamtrust.storage.documents-dir}")
    private String docsDir;

    public DocumentService(DocumentRepository documentRepository,
                           CryptoService cryptoService,
                           FileTypeValidator fileTypeValidator) {
        this.documentRepository = documentRepository;
        this.cryptoService = cryptoService;
        this.fileTypeValidator = fileTypeValidator;
    }

    public Document upload(MultipartFile file, User owner, String title) {
        validate(file);
        fileTypeValidator.validate(file);
        try {
            Path dir = Paths.get(docsDir);
            Files.createDirectories(dir);

            String safeName = sanitize(file.getOriginalFilename());
            Path target = dir.resolve(UUID.randomUUID() + "-" + safeName);
            file.transferTo(target);

            String hash = cryptoService.sha256Hex(target);

            Document doc = Document.builder()
                    .verificationId(generateVerificationId())
                    .fileName(safeName)
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .storagePath(target.toString())
                    .originalHash(hash)
                    .status(DocumentStatus.UPLOADED)
                    .owner(owner)
                    .title(title != null ? title : safeName)
                    .build();

            return documentRepository.save(doc);
        } catch (Exception e) {
            throw new InvalidFileException("Upload failed: " + e.getMessage());
        }
    }

    public Document get(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document " + id + " not found"));
    }

    public Document getByVerificationId(String verificationId) {
        return documentRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No document for verification ID " + verificationId));
    }

    public List<Document> listByOwner(User owner) {
        return documentRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId());
    }

    public Path pathOf(Document doc) {
        return Path.of(doc.getStoragePath());
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new InvalidFileException("File is empty");
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new FileTooLargeException(
                    "File exceeds the 25 MB upload limit. Please reduce the file size and try again.");

        String name = file.getOriginalFilename();
        if (name == null || name.isBlank())
            throw new InvalidFileException("Missing file name");
        if (name.contains("..") || name.contains("/") || name.contains("\\"))
            throw new InvalidFileException("Illegal file name");
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Generates a random verification ID of the form ZT-YYYY-XXXXXXXXXXXX.
     * The 12-hex-char suffix is drawn from SecureRandom (48 bits of entropy),
     * making IDs unguessable and preventing enumeration across tenants.
     * Resolves finding #7.
     *
     * Retries on collision (extremely unlikely but handled).
     */
    private String generateVerificationId() {
        int year = Year.now().getValue();
        String prefix = "ZT-" + year + "-";
        for (int attempt = 0; attempt < MAX_ID_ATTEMPTS; attempt++) {
            byte[] bytes = new byte[6]; // 48 bits
            SECURE_RANDOM.nextBytes(bytes);
            StringBuilder hex = new StringBuilder(12);
            for (byte b : bytes) hex.append(String.format("%02x", b));
            String candidate = prefix + hex.toString().toUpperCase();
            if (documentRepository.findByVerificationId(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Failed to generate a unique verification ID after " + MAX_ID_ATTEMPTS + " attempts");
    }
}