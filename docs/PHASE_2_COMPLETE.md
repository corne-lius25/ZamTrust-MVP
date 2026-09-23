# Phase 2 — Complete

**Date:** 2026-09-24
**Status:** ✅ Complete and verified in production

## Delivered

### Backend
- `Plan` enum (FREE/PERSONAL/BUSINESS/ENTERPRISE) with monthly limits
- `Subscription` entity — one per user, auto-created as FREE on register
- `UsageRecord` entity — per (userId, action, periodKey) counter
- `UsageService` — recordOrThrow, recordAnonymousOrThrow, planOf, hashIp
- `QuotaExceededException` → HTTP 402 with a structured response body
- `GET /api/usage/current` — plan + counters per action
- Anonymous verification quota (10 per IP per month)

### Frontend
- `/pricing` public page with four tiers, responsive
- `/app/upgrade` placeholder page (real Stripe flow in Phase 3)
- `UsageBar`, `PlanBadge`, `UsageCard`, `UpgradePrompt` components
- `lib/plans.ts` — plan metadata + fetchCurrentUsage()
- `lib/apiQuota.ts` — extractQuotaError from axios 402 responses
- SignDocument page catches 402 and shows UpgradePrompt inline
- AppLayout: UsageCard sidebar on lg+ screens
- App.tsx: /pricing and /app/upgrade routes

### Locked decisions implemented
- 1 signature = 1 sign-with-visible call
- Hard block at limit (402), no soft-overage
- Calendar-month reset (periodKey YYYY-MM)
- Anonymous verification capped at 10 per IP per month

## Verified in production

Fresh user (prodquota2, FREE plan):
- Signatures used: 0/3 → 3/3
- Attempt 4: 402 Payment Required with:
  - `used: 3`
  - `limit: 3`
  - `plan: FREE`
  - `action: SIGN`
  - `message: "You've used 3 of 3 signatures this month on the Free plan..."`

## Learnings

1. **Railway sometimes misses auto-deploy.** Empty commits (`git commit --allow-empty`) reliably trigger a redeploy.
2. **Filter ordering matters.** `/api/usage/current` initially returned "No static resource" because it wasn't yet in the running jar.
3. **`@RestController` on a stubbed file compiles but doesn't work.** Verifying the file size (~2.5KB) is a quick smoke test.

## Next: Phase 3

Three sub-phases aligned with the strategy doc:

**Phase 3a — Escrow + Signature Lock ("Pay-Before-Sign")**
Flutterwave / MTN / Airtel Money integration. Sign button unlocks only
after the tenant completes the deposit. Real value for property managers,
vehicle sellers, freelancers.

**Phase 3b — Certificate of Forensic Completion**
Append a distinct page to every signed PDF with signer IP, user-agent,
network timestamp, verification ID, signed PDF SHA-256. Legal-grade
evidence aligned with Zambia ECT Act.

**Phase 3c — SMS-based signing links**
Lightweight USSD alternative: send signed link via SMS. Works on any phone.
