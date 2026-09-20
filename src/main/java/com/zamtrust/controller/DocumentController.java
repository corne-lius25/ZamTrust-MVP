package com.zamtrust.controller;

import com.zamtrust.domain.Document;
import com.zamtrust.domain.Signature;
import com.zamtrust.domain.SignaturePlacement;
import com.zamtrust.domain.User;
import com.zamtrust.dto.DocumentResponse;
import com.zamtrust.dto.SignatureResponse;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.UserRepository;
import com.zamtrust.service.AuditService;
import com.zamtrust.service.DocumentService;
import com.zamtrust.service.DocumentSigningService;
import com.zamtrust.service.SigningService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final SigningService signingService;
    private final DocumentSigningService documentSigningService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public DocumentController(DocumentService documentService,
                              SigningService signingService,
                              DocumentSigningService documentSigningService,
                              UserRepository userRepository,
                              AuditService auditService) {
        this.documentService = documentService;
        this.signingService = signingService;
        this.documentSigningService = documentSigningService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    // ------------------------------------------------------------------
    // Existing endpoints
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Visible-signature endpoints (Phase 1)
    // ------------------------------------------------------------------

    /**
     * Sign a document with a visible signature.
     * Body: { signatureImageId, page, x, y, width, height } — fractions of page dimensions.
     */
    @PostMapping("/{id}/sign-with-visible")
    public ResponseEntity<?> signWithVisible(@PathVariable Long id,
                                             @RequestBody SignWithVisibleRequest req,
                                             Authentication auth) {
        User user = currentUser(auth);

        SignaturePlacement placement = SignaturePlacement.builder()
                .page(req.page() == null ? 1 : req.page())
                .x(req.x() == null ? 0.55 : req.x())
                .y(req.y() == null ? 0.75 : req.y())
                .width(req.width() == null ? 0.35 : req.width())
                .height(req.height() == null ? 0.12 : req.height())
                .build();

        DocumentSigningService.SignResult result =
                documentSigningService.signWithVisible(id, user, req.signatureImageId(), placement);

        auditService.log(user, "DOCUMENT_SIGN_VISIBLE",
                result.document().getVerificationId(), "signed with visible signature", null);

        return ResponseEntity.ok(java.util.Map.of(
                "documentId", result.document().getId(),
                "verificationId", result.document().getVerificationId(),
                "signatureId", result.signature().getId(),
                "signedDocumentId", result.signedDocument().getId(),
                "signedPdfUrl", "/api/documents/" + id + "/signed-pdf",
                "downloadUrl", "/api/documents/" + id + "/signed-pdf"
        ));
    }

    /**
     * Stream the original PDF (before any signature is applied).
     * Used by the placement UI to render the document.
     */
    @GetMapping("/{id}/original-pdf")
    public ResponseEntity<Resource> downloadOriginalPdf(@PathVariable Long id, Authentication auth) {
        User user = currentUser(auth);
        Document doc = documentService.get(id);
        Path p = documentService.pathOf(doc);
        Resource resource = new FileSystemResource(p);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Stream the original PDF (before any signature is applied).
     * Used by the placement UI to render the document.
     */
    @GetMapping("/{id}/signed-pdf")
    public ResponseEntity<Resource> downloadSignedPdf(@PathVariable Long id) {
        Path p = documentSigningService.signedPdfPath(id);
        Resource resource = new FileSystemResource(p);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"signed-" + id + ".pdf\"")
                .body(resource);
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    public record SignWithVisibleRequest(
            Long signatureImageId,
            Integer page,
            Double x,
            Double y,
            Double width,
            Double height
    ) {}
}
