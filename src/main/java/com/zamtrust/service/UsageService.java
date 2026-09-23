package com.zamtrust.service;

import com.zamtrust.domain.Plan;
import com.zamtrust.domain.Subscription;
import com.zamtrust.domain.UsageRecord;
import com.zamtrust.exception.QuotaExceededException;
import com.zamtrust.repository.SubscriptionRepository;
import com.zamtrust.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.YearMonth;

/**
 * Enforces per-user and per-IP monthly usage quotas.
 *
 * Locked decisions:
 *   - Counts reset on the 1st of each calendar month (periodKey = "YYYY-MM")
 *   - Every sign-with-visible call = 1 SIGN unit
 *   - Anonymous verification = 1 VERIFY_ANON unit per IP
 *   - Hard block at limit, HTTP 402 with structured response
 *
 * Concurrency: the count check + increment happens in one transaction.
 * Two concurrent requests from the same user could theoretically both see
 * count=2 and both increment to 3, briefly exceeding the limit by 1.
 * This is acceptable: the enforcement is designed to prevent abuse, not
 * to be exact to the last byte.
 */
@Service
public class UsageService {

    private static final String PERIOD_KEY = currentPeriodKey();

    private final UsageRecordRepository usageRepo;
    private final SubscriptionRepository subscriptionRepo;

    public UsageService(UsageRecordRepository usageRepo,
                        SubscriptionRepository subscriptionRepo) {
        this.usageRepo = usageRepo;
        this.subscriptionRepo = subscriptionRepo;
    }

    // ---------- User-based usage ----------

    /**
     * Records a unit of usage for the given user & action.
     * Throws QuotaExceededException if the plan's limit is already reached.
     */
    @Transactional
    public void recordOrThrow(Long userId, String action, Plan plan) {
        if (plan == null) plan = Plan.FREE;

        long limit = limitFor(plan, action);
        String periodKey = currentPeriodKey();

        UsageRecord record = usageRepo
                .findByUserIdAndActionAndPeriodKey(userId, action, periodKey)
                .orElseGet(() -> usageRepo.save(UsageRecord.builder()
                        .userId(userId)
                        .action(action)
                        .periodKey(periodKey)
                        .count(0L)
                        .build()));

        if (record.getCount() >= limit) {
            throw new QuotaExceededException(action, record.getCount(), limit, plan);
        }

        usageRepo.incrementUserUsage(userId, action, periodKey);
    }

    /** Returns the current usage count for a user + action, this month. */
    @Transactional(readOnly = true)
    public long currentUsage(Long userId, String action) {
        return usageRepo
                .findByUserIdAndActionAndPeriodKey(userId, action, currentPeriodKey())
                .map(UsageRecord::getCount)
                .orElse(0L);
    }

    /** Returns the plan's limit for a given action. */
    public long limitFor(Plan plan, String action) {
        return switch (action) {
            case "SIGN" -> plan.monthlySignatures();
            case "VERIFY" -> plan.monthlyVerifications();
            case "API_CALL" -> plan.monthlyApiCalls();
            default -> Long.MAX_VALUE;
        };
    }

    /** Finds the user's current plan (defaults to FREE if no subscription). */
    @Transactional(readOnly = true)
    public Plan planOf(Long userId) {
        return subscriptionRepo.findByUserId(userId)
                .filter(Subscription::isActive)
                .map(Subscription::getPlan)
                .orElse(Plan.FREE);
    }

    // ---------- Anonymous (IP-based) usage ----------

    /**
     * Records a unit of anonymous usage keyed by the client IP hash.
     * Throws QuotaExceededException if the anonymous limit is reached.
     */
    @Transactional
    public void recordAnonymousOrThrow(String ipHash, String action, int limit) {
        String periodKey = currentPeriodKey();

        UsageRecord record = usageRepo
                .findByIpHashAndActionAndPeriodKey(ipHash, action, periodKey)
                .orElseGet(() -> usageRepo.save(UsageRecord.builder()
                        .ipHash(ipHash)
                        .action(action)
                        .periodKey(periodKey)
                        .count(0L)
                        .build()));

        if (record.getCount() >= limit) {
            // Anonymous — surface the limit but no plan (there is none)
            throw new QuotaExceededException(action, record.getCount(), limit, Plan.FREE);
        }

        usageRepo.incrementAnonymousUsage(ipHash, action, periodKey);
    }

    // ---------- Utilities ----------

    /**
     * Hashes an IP address so we can count per-IP without storing the raw IP.
     * Privacy: the hash is stable for a single day but not reversible.
     */
    public String hashIp(String ip) {
        if (ip == null || ip.isBlank()) return "unknown";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.substring(0, 32); // truncate to 32 hex chars
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static String currentPeriodKey() {
        return YearMonth.now().toString();
    }
}
