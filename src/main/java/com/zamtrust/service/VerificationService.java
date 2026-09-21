package com.zamtrust.service;

import com.zamtrust.domain.Document;
import com.zamtrust.domain.Signature;
import com.zamtrust.domain.SignedDocument;
import com.zamtrust.dto.VerificationResult;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.DocumentRepository;
import com.zamtrust.repository.SignatureRepository;
import com.zamtrust.repository.SignedDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Optional;

@Service
public class VerificationService {

    private final DocumentRepository documentRepository;
    private final SignatureRepository signatureRepository;
    private final SignedDocumentRepository signedDocumentRepository;
    private final CryptoService cryptoService;

    public VerificationService(DocumentRepository documentRepository,
                               SignatureRepository signatureRepository,
                               SignedDocumentRepository signedDocumentRepository,
                               CryptoService cryptoService) {
        this.documentRepository = documentRepository;
        this.signatureRepository = signatureRepository;
        this.signedDocumentRepository = signedDocumentRepository;
        this.cryptoService = cryptoService;
    }

    @Transactional(readOnly = true)
    public VerificationResult verifyByVerificationId(String verificationId) {
        Document doc = documentRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No document for verification ID " + verificationId));
        try {
            byte[] bytes = Files.readAllBytes(Path.of(doc.getStoragePath()));
            return verifyBytes(doc, bytes);
        } catch (Exception e) {
            return new VerificationResult(doc.getVerificationId(), doc.getFileName(),
                    false, false, null, null, null, "ERROR: " + e.getMessage(), false, null);
        }
    }

    @Transactional(readOnly = true)
    public VerificationResult verifyUpload(String verificationId, byte[] uploadedBytes) {
        Document doc = documentRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No document for verification ID " + verificationId));
        return verifyBytes(doc, uploadedBytes);
    }

    private VerificationResult verifyBytes(Document doc, byte[] bytes) {
        String currentHash = cryptoService.sha256Hex(bytes);
        boolean integrityValid = currentHash.equals(doc.getOriginalHash());

        Optional<Signature> sigOpt =
                signatureRepository.findFirstByDocumentIdOrderBySignedAtDesc(doc.getId());

        boolean signatureValid = false;
        String signer = null;
        String algorithm = null;
        java.time.Instant signedAt = null;

        if (sigOpt.isPresent()) {
            Signature s = sigOpt.get();
            algorithm = s.getAlgorithm();
            signedAt = s.getSignedAt();
            signer = s.getSigner().getOrganization() != null
                    ? s.getSigner().getOrganization()
                    : s.getSigner().getUsername();

            try {
                PublicKey pub = cryptoService.publicKeyFromBase64(s.getPublicKeyBase64());
                byte[] sigBytes = cryptoService.decode(s.getSignatureBase64());
                signatureValid = cryptoService.verify(bytes, sigBytes, pub);
            } catch (Exception ignored) {
                signatureValid = false;
            }
        }

        String message = (integrityValid && signatureValid)
                ? "VALID"
                : (sigOpt.isEmpty() ? "NOT_SIGNED" : "INVALID");

        // Check if a signed PDF exists
        Optional<SignedDocument> signedOpt =
                signedDocumentRepository.findFirstByOriginalDocumentIdOrderByCreatedAtDesc(doc.getId());

        boolean hasSignedPdf = signedOpt.isPresent();
        String signedHash = signedOpt.map(SignedDocument::getSignedSha256).orElse(null);

        return new VerificationResult(
                doc.getVerificationId(),
                doc.getFileName(),
                integrityValid,
                signatureValid,
                signer,
                signedAt,
                algorithm,
                message,
                hasSignedPdf,
                signedHash);
    }
}
