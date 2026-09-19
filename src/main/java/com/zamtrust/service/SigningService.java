package com.zamtrust.service;

import com.zamtrust.domain.Document;
import com.zamtrust.domain.DocumentStatus;
import com.zamtrust.domain.Signature;
import com.zamtrust.domain.User;
import com.zamtrust.exception.CryptoException;
import com.zamtrust.repository.DocumentRepository;
import com.zamtrust.repository.SignatureRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.Instant;

@Service
public class SigningService {

    private final CryptoService cryptoService;
    private final KeyStoreService keyStoreService;
    private final DocumentRepository documentRepository;
    private final SignatureRepository signatureRepository;

    public SigningService(CryptoService cryptoService,
                          KeyStoreService keyStoreService,
                          DocumentRepository documentRepository,
                          SignatureRepository signatureRepository) {
        this.cryptoService = cryptoService;
        this.keyStoreService = keyStoreService;
        this.documentRepository = documentRepository;
        this.signatureRepository = signatureRepository;
    }

    public Signature sign(Document doc, User signer) {
        try {
            // 1. Ensure signer has an RSA key pair
            Path privPath = keyStoreService.ensureKeyPair(signer.getId());
            Path pubPath  = keyStoreService.publicKeyPath(signer.getId());

            PrivateKey privateKey = cryptoService.privateKeyFromBytes(Files.readAllBytes(privPath));
            String publicKeyB64 = cryptoService.encode(Files.readAllBytes(pubPath));

            // 2. Read the current document bytes and hash
            byte[] docBytes = Files.readAllBytes(Path.of(doc.getStoragePath()));
            String docHash = cryptoService.sha256Hex(docBytes);

            // 3. Sign the raw bytes
            byte[] signatureBytes = cryptoService.sign(docBytes, privateKey);

            // 4. Persist signature
            Signature sig = Signature.builder()
                    .document(doc)
                    .signer(signer)
                    .documentHash(docHash)
                    .signatureBase64(cryptoService.encode(signatureBytes))
                    .publicKeyBase64(publicKeyB64)
                    .algorithm(CryptoService.SIGN_ALGO)
                    .build();
            signatureRepository.save(sig);

            // 5. Update document status
            doc.setStatus(DocumentStatus.SIGNED);
            doc.setSignedAt(Instant.now());
            documentRepository.save(doc);

            return sig;
        } catch (Exception e) {
            throw new CryptoException("Signing failed for document " + doc.getId(), e);
        }
    }
}