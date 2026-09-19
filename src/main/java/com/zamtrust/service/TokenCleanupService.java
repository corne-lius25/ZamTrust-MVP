package com.zamtrust.service;

import com.zamtrust.repository.IssuedTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Periodically deletes expired issued-token rows (finding #8 housekeeping).
 */
@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final IssuedTokenRepository repo;

    public TokenCleanupService(IssuedTokenRepository repo) {
        this.repo = repo;
    }

    @Scheduled(fixedDelayString = "PT1H")
    @Transactional
    public void purgeExpired() {
        long deleted = repo.deleteByExpiresAtBefore(Instant.now());
        if (deleted > 0) {
            log.info("[TOKEN-CLEANUP] Purged {} expired issued-token rows", deleted);
        }
    }
}
