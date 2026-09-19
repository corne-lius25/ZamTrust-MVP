package com.zamtrust.controller;

import com.zamtrust.domain.Document;
import com.zamtrust.domain.Signature;
import com.zamtrust.domain.User;
import com.zamtrust.dto.DocumentResponse;
import com.zamtrust.dto.SignatureResponse;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.UserRepository;
import com.zamtrust.service.AuditService;
import com.zamtrust.service.DocumentService;
import com.zamtrust.service.SigningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final SigningService signingService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public DocumentController(DocumentService documentService,
                              SigningService signingService,
                              UserRepository userRepository,
                              AuditService auditService) {
        this.documentService = documentService;
        this.signingService = signingService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "title", required = false) String title,
                                                   Authentication auth) {
        User user = currentUser(auth);
        Document doc = documentService.upload(file, user, title);
        auditService.log(user, "DOCUMENT_UPLOAD", doc.getVerificationId(), doc.getFileName(), null);
        return ResponseEntity.ok(DocumentResponse.from(doc));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> list(Authentication auth) {
        User user = currentUser(auth);
        return ResponseEntity.ok(documentService.listByOwner(user).stream()
                .map(DocumentResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> get(@PathVariable Long id, Authentication auth) {
        Document doc = documentService.get(id);
        auditService.log(currentUser(auth), "DOCUMENT_VIEW", doc.getVerificationId(), null, null);
        return ResponseEntity.ok(DocumentResponse.from(doc));
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<SignatureResponse> sign(@PathVariable Long id, Authentication auth) {
        User user = currentUser(auth);
        Document doc = documentService.get(id);
        Signature sig = signingService.sign(doc, user);
        auditService.log(user, "DOCUMENT_SIGN", doc.getVerificationId(), "signed", null);
        return ResponseEntity.ok(SignatureResponse.from(sig));
    }
}