package com.zamtrust.exception;

import com.zamtrust.domain.Plan;

/**
 * Thrown when a user (or anonymous IP) exceeds their monthly quota
 * for a specific action. Mapped to HTTP 402 Payment Required.
 */
public class QuotaExceededException extends RuntimeException {

    private final String action;
    private final long used;
    private final long limit;
    private final Plan currentPlan;

    public QuotaExceededException(String action, long used, long limit, Plan currentPlan) {
        super(buildMessage(action, used, limit, currentPlan));
        this.action = action;
        this.used = used;
        this.limit = limit;
        this.currentPlan = currentPlan;
    }

    public String action() { return action; }
    public long used() { return used; }
    public long limit() { return limit; }
    public Plan currentPlan() { return currentPlan; }

    private static String buildMessage(String action, long used, long limit, Plan plan) {
        String label = switch (action) {
            case "SIGN" -> "signatures";
            case "VERIFY" -> "verifications";
            case "VERIFY_ANON" -> "anonymous verifications";
            case "API_CALL" -> "API calls";
            default -> action.toLowerCase();
        };
        return String.format(
                "You've used %d of %d %s this month on the %s plan. " +
                        "Upgrade your plan to continue.",
                used, limit, label, plan.displayName());
    }
}
