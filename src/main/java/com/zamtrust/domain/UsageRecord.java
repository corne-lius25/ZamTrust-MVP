package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Per-user, per-action, per-month usage counter.
 *
 * There is at most one row per (userId, action, periodKey).
 * The `count` field is incremented atomically on each use.
 *
 * Anonymous (IP-based) verification uses ipHash as the "userId" surrogate
 * and action="VERIFY_ANON".
 */
@Entity
@Table(name = "usage_records", indexes = {
        @Index(name = "idx_usage_lookup",
                columnList = "userId,action,periodKey", unique = true),
        @Index(name = "idx_usage_period", columnList = "periodKey")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null if the request is anonymous (see ipHash). */
    private Long userId;

    /** Hash of the client IP. Populated only for anonymous actions. */
    @Column(length = 64)
    private String ipHash;

    /** "SIGN", "VERIFY", "VERIFY_ANON", "API_CALL". */
    @Column(nullable = false, length = 32)
    private String action;

    /** "YYYY-MM" for the calendar month. */
    @Column(nullable = false, length = 7)
    private String periodKey;

    @Column(nullable = false)
    @Builder.Default
    private long count = 0L;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
