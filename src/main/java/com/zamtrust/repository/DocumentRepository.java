package com.zamtrust.repository;

import com.zamtrust.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByVerificationId(String verificationId);
    List<Document> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    long countByVerificationIdStartingWith(String prefix);
}