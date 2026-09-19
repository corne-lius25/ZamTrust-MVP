package com.zamtrust.repository;

import com.zamtrust.domain.IssuedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IssuedTokenRepository extends JpaRepository<IssuedToken, Long> {

    Optional<IssuedToken> findByJti(String jti);

    List<IssuedToken> findByUserIdAndRevokedFalse(Long userId);

    long deleteByExpiresAtBefore(Instant cutoff);
}
