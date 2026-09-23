package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.YearMonth;

/**
 * A user's active subscription.
 * Every user has exactly one active subscription row.
 * Free users have a Subscription with plan=FREE and no Stripe fields.
 */
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_sub_user", columnList = "userId", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private Plan plan = Plan.FREE;

    /** For future Stripe integration. Null for free-tier users. */
    @Column(length = 64)
    private String stripeCustomerId;

    @Column(length = 64)
    private String stripeSubscriptionId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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

    /**
     * The current calendar month, in "YYYY-MM" format.
     * Used as the periodKey in UsageRecord for counting.
     */
    public static String currentPeriodKey() {
        return YearMonth.now().toString();
    }
}
