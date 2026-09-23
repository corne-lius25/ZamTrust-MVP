# Roadmap: Rename ZamTrust → NetSig

**Status:** Deferred — do after Phase 3 or Phase 4 (before paid customer onboarding)

**Reason for deferral:**
- Focus on shipping features (Phase 2, 3, 4) without mid-flight churn
- Rename once, cleanly, when product shape is settled
- Avoid re-deploy cycles that reset Railway/Vercel state

## Name decision

**Final name:** `NetSig`

Rationale:
- Two syllables, one word, memorable over a phone call
- Suggests "network + signature" without being on-the-nose
- International (works in English, works across Africa)
- Distinct from ZamTrust (which reads informal + has "Zam" ambiguities)

## What will change

### Code
- Package: `com.zamtrust.*` → `com.netsig.*`
- Main class: `ZamTrustApplication` → `NetSigApplication`
- JAR name: `zamtrust-*.jar` → `netsig-*.jar`
- YAML properties: `zamtrust.*` → `netsig.*`
- Env vars: `ZAMTRUST_*` → `NETSIG_*`
- Frontend localStorage keys: `zamtrust_token` → `netsig_token`
- Frontend brand text everywhere

### Infrastructure
- GitHub repo display name: `ZamTrust-MVP` → `NetSig-MVP`
- Railway project: `zamtrust-mvp` → `netsig-production`
- Vercel project: `zamtrust-mvp` → `netsig`
- Domain: buy `netsig.dev` (or `.io`, `.app`, `.africa`)

### Docs
- `README.md`, `SECURITY.md`, blog posts, marketing
- `PHASE_1_COMPLETE.md` and other history — move to `docs/history/`, do not rewrite

### What does NOT change
- **Database schema** — table names stay generic (`users`, `documents`, `signatures`, etc.)
- **Architecture** — no redesign, just renames
- **Security posture** — same headers, same JWT, same crypto
- **API contracts** — same endpoints, same request/response shapes
  (unless we deliberately add a versioned API)

## When to do it

- Not now
- Not during Phase 2 (Plans + Usage Limits)
- Not during Phase 3a/b/c (Escrow, Forensic Certificate, SMS)
- **Before onboarding paying customers** — because B2B contracts need a
  real company name on them
- **Before buying a domain** — don't buy `zamtrust.dev` if you're going to
  change to `netsig.dev` in 2 months

## Before the rename, check availability

1. `netsig.dev`, `netsig.io`, `netsig.app`, `netsig.africa`, `netsig.co.zm`
2. `github.com/netsig`
3. `twitter.com/netsig`
4. `linkedin.com/company/netsig`
5. Trademark check (Zambia, plus USPTO for international)

If `netsig.com` and `netsig.dev` are taken by an active business, pick a
variant: `netsigapp.com`, `netsig.africa`, `getnetsig.com`, etc.

## Estimated effort

- Code rename (scripted + verified): 4 hours
- Infrastructure rename: 2 hours
- Docs + marketing copy: 2 hours
- Domain + DNS + email: 1 hour
- Local test + production verification: 2 hours

**Total: ~1.5 working days.**
