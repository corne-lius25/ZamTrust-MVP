package com.zamtrust.controller;

import com.zamtrust.domain.Document;
import com.zamtrust.dto.VerificationResult;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.DocumentRepository;
import com.zamtrust.repository.SignedDocumentRepository;
import com.zamtrust.service.VerificationService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import com.zamtrust.domain.Plan;
import com.zamtrust.service.UsageService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/verifications")
public class VerificationController {

    private final VerificationService verificationService;
    private final DocumentRepository documentRepository;
    private final SignedDocumentRepository signedDocumentRepository;
    private final UsageService usageService;

    public VerificationController(VerificationService verificationService,
                                  DocumentRepository documentRepository,
                                  SignedDocumentRepository signedDocumentRepository,
                                  UsageService usageService) {
        this.verificationService = verificationService;
        this.documentRepository = documentRepository;
        this.signedDocumentRepository = signedDocumentRepository;
        this.usageService = usageService;
    }

    @GetMapping("/{verificationId}")
    public ResponseEntity<VerificationResult> verify(@PathVariable String verificationId,
                                                      HttpServletRequest http) {
        String ip = clientIp(http);
        String ipHash = usageService.hashIp(ip);
        usageService.recordAnonymousOrThrow(ipHash, "VERIFY_ANON",
                Plan.anonymousVerificationLimit());
        return ResponseEntity.ok(verificationService.verifyByVerificationId(verificationId));
    }

    @PostMapping(value = "/{verificationId}/upload", consumes = "multipart/form-data")
    public ResponseEntity<VerificationResult> verifyUpload(
            @PathVariable String verificationId,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(
                verificationService.verifyUpload(verificationId, file.getBytes()));
    }

    /**
     * Public download of the signed PDF for a given verification ID.
     * No auth required — verification is public by design.
     */
    @GetMapping("/{verificationId}/signed-pdf")
    public ResponseEntity<Resource> downloadSignedPdf(@PathVariable String verificationId) {
        Document doc = documentRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("No document for verification ID"));

        var signedDoc = signedDocumentRepository
                .findFirstByOriginalDocumentIdOrderByCreatedAtDesc(doc.getId())
                .orElseThrow(() -> new ResourceNotFoundException("This document has not been signed yet"));

        Path p = Path.of(signedDoc.getSignedStoragePath());
        if (!Files.exists(p)) {
            throw new ResourceNotFoundException("Signed PDF file is missing");
        }

        Resource resource = new FileSystemResource(p);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"signed-" + doc.getVerificationId() + ".pdf\"")
                .body(resource);
    }

    /**
     * Public inline preview of the signed PDF (opens in browser instead of downloading).
     */
    @GetMapping("/{verificationId}/signed-pdf-preview")
    public ResponseEntity<Resource> previewSignedPdf(@PathVariable String verificationId) {
        Document doc = documentRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("No document for verification ID"));

        var signedDoc = signedDocumentRepository
                .findFirstByOriginalDocumentIdOrderByCreatedAtDesc(doc.getId())
                .orElseThrow(() -> new ResourceNotFoundException("This document has not been signed yet"));

        Path p = Path.of(signedDoc.getSignedStoragePath());
        if (!Files.exists(p)) {
            throw new ResourceNotFoundException("Signed PDF file is missing");
        }

        Resource resource = new FileSystemResource(p);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }
}
