# ZamTrust — Security Assessment

**Assessment type:** Internal penetration test
**Target:** https://zamtrust-mvp-production.up.railway.app (production)
**Date:** 2026-09-19
**Methodology:** OWASP WSTG / OWASP Top 10 (2021)
**Tester:** Corne (project owner)

---

## Executive summary

Eleven security findings were identified during this assessment:

| # | Title | Severity | Status |
|---|-------|----------|--------|
| 1 | Missing Content-Security-Policy header | Medium | **Fixed** |
| 2 | Missing Referrer-Policy header | Low | **Fixed** |
| 3 | Missing Permissions-Policy header | Low | **Fixed** |
| 4 | Server header discloses technology fingerprint | Info | Open |
| 5 | User enumeration via /api/auth/register | Medium | **Fixed** |
| 6 | Incorrect HTTP status for auth failures (403 vs 401) | Info | **Fixed** |
| 7 | Sequential verification IDs allow enumeration | Low | Open |
| 8 | JWT lacks jti claim (no revocation) | Medium | Open |
| 9 | JWT missing iss and aud claims | Info | Open |
| 10 | No MIME type or extension whitelist on upload | High | **Fixed** |
| 11 | Oversized upload returns HTTP 500 instead of 413 | Medium | **Fixed** |

---

## Findings


### Finding 1 — Missing Content-Security-Policy header

**Severity:** Medium
**CVSS v3.1:** 5.4
**OWASP Top 10:** A05:2021 - Security Misconfiguration
**CWE:** CWE-693

**Description:** API responses do not include a Content-Security-Policy header.

**Evidence:**
    $ curl -s -D - -o /dev/null https://zamtrust-mvp-production.up.railway.app/api/verifications/ZT-2026-000001
    HTTP/2 200
    ... (no content-security-policy header)

**Remediation:** Implemented in SecurityHeadersFilter.java.

**Status:** Fixed — 2026-09-19

---


### Finding 2 — Missing Referrer-Policy header

**Severity:** Low
**CVSS v3.1:** 3.1
**OWASP Top 10:** A05:2021
**CWE:** CWE-200

**Description:** No Referrer-Policy header. Browsers fall back to defaults, potentially leaking full verification URLs to third-party resources.

**Remediation:** Implemented in SecurityHeadersFilter.java.

**Status:** Fixed — 2026-09-19

---


### Finding 3 — Missing Permissions-Policy header

**Severity:** Low
**CVSS v3.1:** 3.1
**OWASP Top 10:** A05:2021
**CWE:** CWE-693

**Description:** No Permissions-Policy header present. Browser defaults apply.

**Remediation:** Implemented in SecurityHeadersFilter.java.

**Status:** Fixed — 2026-09-19

---


### Finding 4 — Server header discloses technology fingerprint

**Severity:** Info
**OWASP Top 10:** A05:2021
**CWE:** CWE-200

**Description:** Responses include 'server: railway-hikari', disclosing the hosting platform.

**Remediation:** Suppress or genericize the Server header via reverse proxy.

**Status:** Open (low priority)

---


### Finding 5 — User enumeration via /api/auth/register

**Severity:** Medium
**CVSS v3.1:** 5.3
**OWASP Top 10:** A07:2021 - Identification and Authentication Failures
**CWE:** CWE-204

**Description:** The registration endpoint returns distinct error messages for "username already taken" versus a successful registration. An attacker can systematically probe for valid usernames. Combined with the absence of rate limiting, this enables username harvesting at scale.

**Evidence:**
    $ curl -s -X POST https://zamtrust-mvp-production.up.railway.app/api/auth/register \
        -H "Content-Type: application/json" \
        -d '{"username":"clouduser","email":"different@example.com","password":"StrongPass1!","fullName":"Dup","organization":"X"}'
    {"error":"Username already taken"}

**Remediation:** Registration now returns a single generic error for both duplicate usernames and duplicate emails. RateLimiter (sliding-window, per-IP) applied to /api/auth/register (5/15min) and /api/auth/login (10/15min). HTTP 429 on limit exceeded. 3 unit tests.

**Status:** Fixed — 2026-09-19

---


### Finding 6 — Incorrect HTTP status code for authentication failures

**Severity:** Info
**OWASP Top 10:** A05:2021
**CWE:** CWE-392

**Description:** Requests with a missing or malformed Authorization header return HTTP 403 Forbidden. The semantically correct response is HTTP 401 Unauthorized. HTTP 403 should be reserved for authenticated-but-unauthorized cases.

**Evidence:**
    $ curl -s -D - -o /dev/null -H "Authorization: Bearer not.a.real.jwt" \
        https://zamtrust-mvp-production.up.railway.app/api/documents
    HTTP/2 403

**Remediation:** Custom JsonAuthenticationEntryPoint returns HTTP 401 Unauthorized with a JSON body (using Spring's autoconfigured ObjectMapper for correct JavaTimeModule support). 403 is now reserved for authorization failures only. 2 tests verify the behaviour.

**Status:** Fixed — 2026-09-19

---


### Finding 7 — Sequential verification IDs allow enumeration

**Severity:** Low
**CVSS v3.1:** 3.7
**OWASP Top 10:** A01:2021 - Broken Access Control
**CWE:** CWE-200

**Description:** Verification IDs follow a predictable sequential pattern (ZT-YYYY-NNNNNN). An attacker with one valid ID can enumerate adjacent IDs to discover other organisations' documents.

**Evidence:**
    ZT-2026-000001 -> 200
    ZT-2026-000002 -> 200
    ZT-2026-000003 -> 404
    ZT-2026-000004 -> 404

**Remediation:** Replace sequential counter with a cryptographically random 12-hex-char suffix (48 bits) generated by SecureRandom.

**Status:** Open

---


### Finding 8 — JWT lacks jti claim (no token revocation)

**Severity:** Medium
**CVSS v3.1:** 5.0
**OWASP Top 10:** A07:2021
**CWE:** CWE-613

**Description:** The JWT payload contains only sub, iat, exp. No jti (JWT ID) claim means a stolen token remains valid until expiry (currently 1 hour), even after logout or password change. There is no mechanism to revoke individual tokens without rotating the global signing secret.

**Evidence:**
    {
      "sub": "clouduser",
      "iat": 1789835566,
      "exp": 1789839166
    }

**Remediation:**
1. Add a random jti (UUID v4) on issue
2. Store issued token IDs in issued_tokens(jti, user_id, expires_at, revoked)
3. Verify jti existence on each request
4. Revoke tokens on logout / password change
5. Purge expired rows periodically

**Status:** Open

---


### Finding 9 — JWT missing iss and aud claims

**Severity:** Info
**OWASP Top 10:** A02:2021 - Cryptographic Failures
**CWE:** CWE-345

**Description:** The JWT does not declare iss (issuer) or aud (audience). Adding and validating these claims now is cheap defence-in-depth for future multi-service scenarios.

**Remediation:**
    generate(): .issuer("zamtrust").audience().add("zamtrust-api").and()
    parse():    .requireIssuer("zamtrust").requireAudience("zamtrust-api")

**Status:** Open

---


### Finding 10 — No MIME type or file extension whitelist on upload

**Severity:** High
**CVSS v3.1:** 8.1
**OWASP Top 10:** A04:2021 - Insecure Design
**CWE:** CWE-434

**Description:** The document upload endpoint accepts any file type. HTML files containing inline scripts and SVG files with onload handlers both uploaded successfully. If a document preview feature is added (which enterprises will require), these become stored XSS vectors.

**Evidence:**
    HTML upload:   Status 200
    SVG  upload:   Status 200
    ZIP  upload with spoofed Content-Type (image/png):
        The server trusts the client-supplied MIME type and does not
        validate actual file contents by reading magic bytes. It is
        possible to upload a file whose declared MIME type does not
        match its real content. Combined with any feature that renders
        files based on contentType, this is exploitable.

**Remediation:** Implemented in FileTypeValidator.java with an extension whitelist, MIME type cross-check, and magic byte verification. HTML/SVG/executables are rejected. HTTP status: 400 with a clear message.

**Status:** Fixed — 2026-09-19

---


### Finding 11 — Oversized upload returns HTTP 500 instead of 413

**Severity:** Medium
**CVSS v3.1:** 5.3
**OWASP Top 10:** A05:2021
**CWE:** CWE-391

**Description:** Uploading a 30 MB file (above the 25 MB limit) causes Spring Boot to throw MaxUploadSizeExceededException, which isn't caught by the exception handler, returning HTTP 500.

**Evidence:**
    $ curl -X POST .../api/documents -F "file=@big.bin" ...
    Status: 500

**Remediation:** Implemented via FileTooLargeException and a corresponding @ExceptionHandler in GlobalExceptionHandler.java. Returns HTTP 413 Payload Too Large.

**Status:** Fixed — 2026-09-19

---


## Methodology notes

Tested endpoints:
- POST /api/auth/register
- POST /api/auth/login
- GET  /api/documents
- POST /api/documents
- POST /api/documents/{id}/sign
- GET  /api/verifications/{verificationId}

Tools used:
- curl (manual testing)
- Python (JWT decoding, payload generation)
- Burp Suite Community and OWASP ZAP (pending automated scan)

---

## Remediation roadmap

| Fix | Resolves | Priority |
|-----|----------|----------|
| Security headers filter | 1, 2, 3 | High |
| 413 exception handler | 11 | Medium |
| Upload MIME whitelist + magic bytes | 10 | High |
| Generic registration errors + rate limit | 5 | High |
| 401 vs 403 handling | 6 | Medium |
| Random verification IDs | 7 | Medium |
| JWT hardening (jti, iss, aud) | 8, 9 | Medium |
