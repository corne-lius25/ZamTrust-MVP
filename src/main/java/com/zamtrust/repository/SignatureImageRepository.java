package com.zamtrust.repository;

import com.zamtrust.domain.SignatureImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignatureImageRepository extends JpaRepository<SignatureImage, Long> {

    List<SignatureImage> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<SignatureImage> findByIdAndUserId(Long id, Long userId);

    Optional<SignatureImage> findByUserIdAndIsDefaultTrue(Long userId);

    long countByUserId(Long userId);
}
