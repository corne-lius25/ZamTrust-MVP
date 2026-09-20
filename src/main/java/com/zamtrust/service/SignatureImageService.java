package com.zamtrust.service;

import com.zamtrust.domain.SignatureImage;
import com.zamtrust.exception.InvalidFileException;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.SignatureImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Manages user signature images (drawn or typed).
 * Resolves the visible-signature requirement of Phase 1.
 */
@Service
public class SignatureImageService {

    private static final long MAX_BYTES = 512 * 1024; // 512 KB
    private static final int MAX_PER_USER = 5;

    private final SignatureImageRepository repository;
    private final CryptoService cryptoService;

    @Value("${zamtrust.storage.signatures-dir:./storage/signatures}")
    private String signaturesDir;

    public SignatureImageService(SignatureImageRepository repository,
                                 CryptoService cryptoService) {
        this.repository = repository;
        this.cryptoService = cryptoService;
    }

    @Transactional(readOnly = true)
    public List<SignatureImage> list(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public SignatureImage require(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Signature not found"));
    }

    /**
     * Saves a signature image from base64 PNG data.
     * The image must be a PNG (we validate magic bytes).
     */
    @Transactional
    public SignatureImage saveFromBase64(Long userId,
                                         String label,
                                         String base64Png,
                                         int widthPx,
                                         int heightPx,
                                         boolean makeDefault) {
        if (repository.countByUserId(userId) >= MAX_PER_USER) {
            throw new InvalidFileException("You can save at most " + MAX_PER_USER + " signatures.");
        }

        // Strip the data URI prefix if present
        String clean = base64Png.contains(",") ? base64Png.substring(base64Png.indexOf(',') + 1) : base64Png;

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(clean);
        } catch (IllegalArgumentException e) {
            throw new InvalidFileException("Signature data is not valid base64.");
        }

        if (bytes.length == 0) throw new InvalidFileException("Signature image is empty.");
        if (bytes.length > MAX_BYTES) throw new InvalidFileException("Signature image exceeds 512 KB.");

        // Magic bytes: PNG starts with 89 50 4E 47
        if (bytes.length < 8
                || (bytes[0] & 0xFF) != 0x89
                || bytes[1] != 0x50
                || bytes[2] != 0x4E
                || bytes[3] != 0x47) {
            throw new InvalidFileException("Signature image must be a PNG.");
        }

        try {
            Path dir = Paths.get(signaturesDir);
            Files.createDirectories(dir);

            String fileName = "user-" + userId + "-" + UUID.randomUUID() + ".png";
            Path target = dir.resolve(fileName);
            Files.write(target, bytes);

            String sha = cryptoService.sha256Hex(bytes);

            SignatureImage image = SignatureImage.builder()
                    .userId(userId)
                    .label(label == null || label.isBlank() ? "Signature" : label.trim())
                    .storagePath(target.toString())
                    .sha256(sha)
                    .widthPx(widthPx > 0 ? widthPx : 300)
                    .heightPx(heightPx > 0 ? heightPx : 100)
                    .isDefault(false)
                    .build();

            SignatureImage saved = repository.save(image);

            if (makeDefault || repository.findByUserIdAndIsDefaultTrue(userId).isEmpty()) {
                setDefault(saved.getId(), userId);
            }

            return saved;
        } catch (Exception e) {
            if (e instanceof InvalidFileException) throw (InvalidFileException) e;
            throw new RuntimeException("Signature storage failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SignatureImage saveFromMultipart(Long userId,
                                            String label,
                                            MultipartFile file,
                                            int widthPx,
                                            int heightPx,
                                            boolean makeDefault) {
        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return saveFromBase64(userId, label, base64, widthPx, heightPx, makeDefault);
        } catch (Exception e) {
            throw new InvalidFileException("Failed to read signature image.");
        }
    }

    @Transactional
    public void setDefault(Long id, Long userId) {
        SignatureImage target = require(id, userId);
        repository.findByUserIdAndIsDefaultTrue(userId).ifPresent(current -> {
            if (!current.getId().equals(target.getId())) {
                current.setDefault(false);
                repository.save(current);
            }
        });
        target.setDefault(true);
        repository.save(target);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        SignatureImage image = require(id, userId);
        try {
            Files.deleteIfExists(Path.of(image.getStoragePath()));
        } catch (Exception ignored) { /* best effort */ }
        repository.delete(image);

        // If we just deleted the default, promote the most recent remaining
        if (image.isDefault()) {
            List<SignatureImage> remaining = repository.findByUserIdOrderByCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                SignatureImage next = remaining.get(0);
                next.setDefault(true);
                repository.save(next);
            }
        }
    }

    @Transactional(readOnly = true)
    public byte[] readFile(SignatureImage image) {
        try {
            return Files.readAllBytes(Path.of(image.getStoragePath()));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Signature image file not found");
        }
    }
}
