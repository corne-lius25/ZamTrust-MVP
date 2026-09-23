package com.zamtrust.domain;

/**
 * Subscription plans and their monthly limits.
 *
 * Locked decisions:
 *   - Signatures reset on the 1st of each calendar month
 *   - Every sign-with-visible call consumes exactly 1 signature
 *   - Anonymous verification is capped separately (by IP, not by plan)
 *   - Limits are hard-enforced (402 Payment Required when exceeded)
 */
public enum Plan {

    FREE("Free", 0, 3, 10, 0, false, false),

    PERSONAL("Personal", 900, 50, 500, 0, false, true),   // $9.00

    BUSINESS("Business", 4900, 500, 5000, 10_000, true, true), // $49.00

    ENTERPRISE("Enterprise", 49900, Integer.MAX_VALUE, Integer.MAX_VALUE,
               1_000_000, true, true); // $499.00

    private final String displayName;
    private final int priceCents;
    private final int monthlySignatures;
    private final int monthlyVerifications;
    private final int monthlyApiCalls;
    private final boolean apiEnabled;
    private final boolean persistentIdentity;

    Plan(String displayName,
         int priceCents,
         int monthlySignatures,
         int monthlyVerifications,
         int monthlyApiCalls,
         boolean apiEnabled,
         boolean persistentIdentity) {
        this.displayName = displayName;
        this.priceCents = priceCents;
        this.monthlySignatures = monthlySignatures;
        this.monthlyVerifications = monthlyVerifications;
        this.monthlyApiCalls = monthlyApiCalls;
        this.apiEnabled = apiEnabled;
        this.persistentIdentity = persistentIdentity;
    }

    public String displayName() { return displayName; }
    public int priceCents() { return priceCents; }
    public int monthlySignatures() { return monthlySignatures; }
    public int monthlyVerifications() { return monthlyVerifications; }
    public int monthlyApiCalls() { return monthlyApiCalls; }
    public boolean apiEnabled() { return apiEnabled; }
    public boolean persistentIdentity() { return persistentIdentity; }

    public boolean isFree() { return this == FREE; }
    public boolean isUnlimited() { return this == ENTERPRISE; }

    /**
     * Anonymous verification limit per IP per month. Not plan-based —
     * applied globally for unauthenticated requests.
     */
    public static int anonymousVerificationLimit() {
        return 10;
    }
}
