package com.zamtrust.service;

import com.zamtrust.domain.Document;
import com.zamtrust.domain.DocumentStatus;
import com.zamtrust.domain.User;
import com.zamtrust.exception.InvalidFileException;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final long MAX_SIZE_BYTES = 25L * 1024 * 1024; // 25MB

    private final DocumentRepository documentRepository;
    private final CryptoService cryptoService;

    @Value("${zamtrust.storage.documents-dir}")
    private String docsDir;

    public DocumentService(DocumentRepository documentRepository, CryptoService cryptoService) {
        this.documentRepository = documentRepository;
        this.cryptoService = cryptoService;
    }

    public Document upload(MultipartFile file, User owner, String title) {
        validate(file);
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
            throw new InvalidFileException("File exceeds 25MB limit");

        String name = file.getOriginalFilename();
        if (name == null || name.isBlank())
            throw new InvalidFileException("Missing file name");
        if (name.contains("..") || name.contains("/") || name.contains("\\"))
            throw new InvalidFileException("Illegal file name");
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String generateVerificationId() {
        int year = Year.now().getValue();
        String prefix = "ZT-" + year + "-";
        long seq = documentRepository.countByVerificationIdStartingWith(prefix) + 1;
        return String.format("%s%06d", prefix, seq);
    }
}