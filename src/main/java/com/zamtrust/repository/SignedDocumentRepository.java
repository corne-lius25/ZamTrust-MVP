package com.zamtrust.repository;

import com.zamtrust.domain.SignedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignedDocumentRepository extends JpaRepository<SignedDocument, Long> {

    Optional<SignedDocument> findFirstByOriginalDocumentIdOrderByCreatedAtDesc(Long originalDocumentId);

    boolean existsByOriginalDocumentId(Long originalDocumentId);
}
