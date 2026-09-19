package com.zamtrust.repository;

import com.zamtrust.domain.Signature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignatureRepository extends JpaRepository<Signature, Long> {
    List<Signature> findByDocumentId(Long documentId);
    Optional<Signature> findFirstByDocumentIdOrderBySignedAtDesc(Long documentId);
}