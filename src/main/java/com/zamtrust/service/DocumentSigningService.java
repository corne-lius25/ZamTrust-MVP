package com.zamtrust.service;

import com.zamtrust.domain.*;
import com.zamtrust.exception.CryptoException;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.DocumentRepository;
import com.zamtrust.repository.SignatureRepository;
import com.zamtrust.repository.SignedDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

/**
 * Orchestrates a full "sign with visible signature" operation:
 *   1. Persists the cryptographic signature (SigningService)
 *   2. Bakes the visible signature into a signed PDF (PdfSignatureService)
 *   3. Records the resulting file as a SignedDocument
 *
 * Resolves Phase 1's visible-signature requirement.
 */
@Service
public class DocumentSigningService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSigningService.class);

    private final DocumentRepository documentRepository;
    private final SignedDocumentRepository signedDocumentRepository;
    private final SignatureRepository signatureRepository;
    private final SignatureImageService signatureImageService;
    private final SigningService signingService;
    private final PdfSignatureService pdfSignatureService;
    private final CryptoService cryptoService;

    @Value("${zamtrust.storage.documents-dir:./storage/documents}")
    private String documentsDir;

    @Value("${zamtrust.base-url:http://localhost:8080}")
    private String baseUrl;

    public DocumentSigningService(DocumentRepository documentRepository,
                                  SignedDocumentRepository signedDocumentRepository,
                                  SignatureRepository signatureRepository,
                                  SignatureImageService signatureImageService,
                                  SigningService signingService,
                                  PdfSignatureService pdfSignatureService,
                                  CryptoService cryptoService) {
        this.documentRepository = documentRepository;
        this.signedDocumentRepository = signedDocumentRepository;
        this.signatureRepository = signatureRepository;
        this.signatureImageService = signatureImageService;
        this.signingService = signingService;
        this.pdfSignatureService = pdfSignatureService;
        this.cryptoService = cryptoService;
    }

    /**
     * Signs a document with both a cryptographic signature and a visible signature.
     *
     * @param documentId      the document to sign
     * @param signer          the user performing the signing
     * @param signatureImageId  which of the user's saved signatures to use
     * @param placement       where to place the visible signature
     */
    @Transactional
    public SignResult signWithVisible(Long documentId,
                                      User signer,
                                      Long signatureImageId,
                                      SignaturePlacement placement) {

        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (doc.getStatus() == DocumentStatus.SIGNED) {
            throw new CryptoException("Document is already signed");
        }

        SignatureImage image = signatureImageService.require(signatureImageId, signer.getId());
        Path signaturePngPath = Path.of(image.getStoragePath());
        if (!Files.exists(signaturePngPath)) {
            throw new CryptoException("Signature image file is missing");
        }

        // 1. Cryptographic signature (reuses existing SigningService)
        Signature cryptoSignature = signingService.sign(doc, signer);

        // 2. Visible signature → signed PDF
        Path originalPdf = Path.of(doc.getStoragePath());
        Path signedPdfPath = Paths.get(documentsDir)
                .resolve("signed-" + UUID.randomUUID() + "-" + doc.getFileName());

        String verifyUrl = baseUrl + "/v/" + doc.getVerificationId();

        pdfSignatureService.applyVisibleSignature(
                originalPdf,
                signaturePngPath,
                placement,
                signer.getFullName() != null ? signer.getFullName() : signer.getUsername(),
                null, // role — future field
                signer.getOrganization(),
                doc.getVerificationId(),
                verifyUrl,
                Instant.now(),
                signedPdfPath
        );

        // 3. Hash the signed PDF and record it
        String signedHash = cryptoService.sha256Hex(signedPdfPath);
        long signedSize = 0;
        try {
            signedSize = Files.size(signedPdfPath);
        } catch (Exception ignored) { }

        SignedDocument signedDocument = SignedDocument.builder()
                .originalDocumentId(doc.getId())
                .signatureId(cryptoSignature.getId())
                .signedStoragePath(signedPdfPath.toString())
                .signedSha256(signedHash)
                .signedSizeBytes(signedSize)
                .build();
        signedDocumentRepository.save(signedDocument);

        // Reload the doc (SigningService updated its status)
        Document updated = documentRepository.findById(doc.getId()).orElseThrow();

        log.info("[SIGN-VISIBLE] Signed document {} → {} ({} bytes)",
                doc.getVerificationId(), signedPdfPath, signedSize);

        return new SignResult(updated, signedDocument, cryptoSignature);
    }

    public Path signedPdfPath(Long originalDocumentId) {
        SignedDocument sd = signedDocumentRepository
                .findFirstByOriginalDocumentIdOrderByCreatedAtDesc(originalDocumentId)
                .orElseThrow(() -> new ResourceNotFoundException("No signed PDF for this document"));
        Path p = Path.of(sd.getSignedStoragePath());
        if (!Files.exists(p)) throw new ResourceNotFoundException("Signed PDF file is missing");
        return p;
    }

    public record SignResult(Document document, SignedDocument signedDocument, Signature signature) {}
}
