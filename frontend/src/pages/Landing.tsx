import { Link } from 'react-router-dom';
import { Badge } from '../components/Badge';
import { ButtonLink } from '../components/Button';
import { Mono } from '../components/Mono';
import { Section } from '../components/Section';
import { VerificationCard } from '../components/VerificationCard';
import { Divider } from '../components/Divider';
import { CopyButton } from '../components/CopyButton';

export default function Landing() {
  return (
    <div className="bg-surface">
      <Header />
      <Hero />
      <HowItWorks />
      <SpecSheet />
      <DeveloperExample />
      <FinalCTA />
      <Footer />
    </div>
  );
}

/* -------------------------------------------------------------------------- */
/*                                   HEADER                                   */
/* -------------------------------------------------------------------------- */

function Header() {
  return (
    <header className="sticky top-0 z-40 bg-surface/85 backdrop-blur-md border-b border-border">
      <div className="max-w-wide mx-auto px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 no-underline">
          <Mark />
          <span className="text-body font-semibold text-ink tracking-tight">
            ZamTrust
          </span>
        </Link>

        <nav className="hidden md:flex items-center gap-8">
          <a href="#how" className="text-small text-ink-secondary hover:text-ink transition-colors">
            How it works
          </a>
          <a href="#tech" className="text-small text-ink-secondary hover:text-ink transition-colors">
            Technology
          </a>
          <a href="#developers" className="text-small text-ink-secondary hover:text-ink transition-colors">
            Developers
          </a>
        </nav>

        <div className="flex items-center gap-3">
          <Link to="/login" className="text-small text-ink-secondary hover:text-ink transition-colors">
            Sign in
          </Link>
          <ButtonLink to="/register" size="sm">
            Get started
          </ButtonLink>
        </div>
      </div>
    </header>
  );
}

function Mark() {
  return (
    <span
      aria-hidden="true"
      className="grid place-items-center w-7 h-7 rounded-btn bg-ink text-white font-semibold text-small"
    >
      Z
    </span>
  );
}

/* -------------------------------------------------------------------------- */
/*                                    HERO                                    */
/* -------------------------------------------------------------------------- */

function Hero() {
  return (
    <section className="border-b border-border">
      <div className="max-w-wide mx-auto px-6 lg:px-8 py-20 lg:py-28">
        <div className="grid lg:grid-cols-12 gap-12 lg:gap-16 items-center">
          {/* Text */}
          <div className="lg:col-span-7">
            <p className="eyebrow mb-5">Document integrity for enterprises</p>

            <h1 className="text-h1 lg:text-display text-ink max-w-2xl text-balance">
              Cryptographic proof for every document your business signs.
            </h1>

            <p className="mt-6 text-body-lg text-ink-secondary max-w-xl">
              ZamTrust hashes, signs, and verifies documents with SHA-256
              and RSA-2048. Every signature is provable. Every change is
              detectable. Every action is audited.
            </p>

            <div className="mt-8 flex flex-wrap items-center gap-3">
              <ButtonLink to="/register" size="lg" rightIcon={<ArrowRight />}>
                Start signing
              </ButtonLink>
              <ButtonLink to="/login" variant="secondary" size="lg">
                Sign in
              </ButtonLink>
            </div>

            <p className="mt-5 text-small text-ink-muted">
              Verify a document without an account — every signed file comes
              with a public verification link.
            </p>
          </div>

          {/* Live preview card — same component used in the app */}
          <div className="lg:col-span-5 lg:justify-self-end w-full max-w-md">
            <VerificationCard
              className="shadow-sm"
              data={{
                verificationId: 'ZT-2026-000184',
                fileName: 'employment-agreement.pdf',
                integrityValid: true,
                signatureValid: true,
                signer: 'ABC Limited',
                signedAt: '2026-09-15T10:30:00Z',
                algorithm: 'SHA256withRSA',
                message: 'VALID',
              }}
            />
            <p className="mt-3 text-caption text-ink-muted text-center">
              A live verification result. Same view the public link produces.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}

function ArrowRight() {
  return (
    <svg width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
      <path
        d="M3 8h10M9 4l4 4-4 4"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

/* -------------------------------------------------------------------------- */
/*                                 HOW IT WORKS                               */
/* -------------------------------------------------------------------------- */

const STEPS = [
  {
    n: '01',
    title: 'Upload',
    body:
      'A user uploads a document. ZamTrust computes its SHA-256 fingerprint the moment it lands on the server.',
    visual: <VisualHash />,
  },
  {
    n: '02',
    title: 'Sign',
    body:
      'A designated signer approves the document. The server signs its hash with the signer’s RSA-2048 private key.',
    visual: <VisualSign />,
  },
  {
    n: '03',
    title: 'Verify',
    body:
      'Anyone with the verification ID can confirm the document has not changed since it was signed.',
    visual: <VisualVerify />,
  },
];

function HowItWorks() {
  return (
    <Section
      eyebrow="How it works"
      title="Three steps. Cryptographic guarantees at each."
      description="No manual processes. No print-sign-scan. No way to alter a document without detection."
      className="border-b border-border"
    >
      <div id="how" className="grid lg:grid-cols-12 gap-12 lg:gap-20">
        {/* Left: sticky step text */}
        <div className="lg:col-span-5 space-y-14">
          {STEPS.map((s) => (
            <div key={s.n} className="grid grid-cols-[auto_1fr] gap-5">
              <div className="text-caption font-mono text-ink-muted pt-1.5">
                {s.n}
              </div>
              <div>
                <h3 className="text-h4 text-ink mb-2">{s.title}</h3>
                <p className="text-body text-ink-secondary">{s.body}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Right: visuals */}
        <div className="lg:col-span-7 space-y-6">
          {STEPS.map((s) => (
            <div key={s.n} id={`step-${s.n}`}>
              {s.visual}
            </div>
          ))}
        </div>
      </div>
    </Section>
  );
}

function VisualHash() {
  return (
    <div className="rounded-card border border-border bg-surface-subtle p-5">
      <div className="flex items-center justify-between mb-4">
        <span className="text-small text-ink-muted">document.pdf · 12 KB</span>
        <Badge variant="neutral">Uploaded</Badge>
      </div>
      <Divider className="mb-4" />
      <div className="flex items-center justify-between gap-4">
        <span className="text-small text-ink-muted">SHA-256</span>
        <Mono>a3f9…c812</Mono>
      </div>
    </div>
  );
}

function VisualSign() {
  return (
    <div className="rounded-card border border-border bg-surface-subtle p-5">
      <div className="flex items-center justify-between mb-4">
        <span className="text-small text-ink-muted">
          Signer · ABC Limited
        </span>
        <Badge variant="accent">Signing</Badge>
      </div>
      <Divider className="mb-4" />
      <div className="space-y-2.5">
        <div className="flex items-center justify-between gap-4">
          <span className="text-small text-ink-muted">Algorithm</span>
          <Mono>SHA256withRSA</Mono>
        </div>
        <div className="flex items-center justify-between gap-4">
          <span className="text-small text-ink-muted">Signature</span>
          <Mono truncate>MEUCIQDf2…+A==</Mono>
        </div>
      </div>
    </div>
  );
}

function VisualVerify() {
  return (
    <div className="rounded-card border border-border bg-surface-subtle p-5">
      <div className="flex items-center justify-between mb-4">
        <span className="text-small text-ink-muted">
          ZT-2026-000184
        </span>
        <Badge variant="success">Valid</Badge>
      </div>
      <Divider className="mb-4" />
      <ul className="space-y-2 text-small">
        <li className="flex items-center justify-between">
          <span className="text-ink-muted">Integrity</span>
          <span className="text-success font-medium">Verified</span>
        </li>
        <li className="flex items-center justify-between">
          <span className="text-ink-muted">Signature</span>
          <span className="text-success font-medium">Valid</span>
        </li>
        <li className="flex items-center justify-between">
          <span className="text-ink-muted">Tamper test</span>
          <span className="text-ink font-medium">Detected</span>
        </li>
      </ul>
    </div>
  );
}

/* -------------------------------------------------------------------------- */
/*                                 SPEC SHEET                                 */
/* -------------------------------------------------------------------------- */

const SPECS: { section: string; rows: [string, string][] }[] = [
  {
    section: 'Cryptography',
    rows: [
      ['Hash algorithm', 'SHA-256'],
      ['Signature algorithm', 'SHA256withRSA'],
      ['Key size', '2048-bit'],
      ['Signature format', 'PKCS#1 v1.5'],
      ['Verification', 'Public-key only'],
    ],
  },
  {
    section: 'Storage & audit',
    rows: [
      ['Document storage', 'Encrypted at rest'],
      ['Audit trail', 'Append-only'],
      ['Retention', 'Configurable per organisation'],
      ['Identifier format', 'ZT-YYYY-NNNNNN'],
    ],
  },
  {
    section: 'Compliance (planned)',
    rows: [
      ['ECTA (Zambia)', 'Aligned'],
      ['ISO 27001', 'Roadmap'],
      ['SOC 2 Type I', 'Roadmap'],
    ],
  },
];

function SpecSheet() {
  return (
    <Section
      eyebrow="Technology"
      title="What is actually running under the hood."
      description="A technical summary, not a marketing summary. Same details we publish in our security documentation."
      className="border-b border-border"
    >
      <div id="tech" className="grid md:grid-cols-3 gap-10 lg:gap-16">
        {SPECS.map((spec) => (
          <div key={spec.section}>
            <h3 className="text-caption uppercase tracking-wider text-ink-muted mb-4">
              {spec.section}
            </h3>
            <dl className="space-y-3">
              {spec.rows.map(([k, v]) => (
                <div
                  key={k}
                  className="flex items-baseline justify-between gap-4 border-b border-border-subtle pb-2.5"
                >
                  <dt className="text-small text-ink-secondary">{k}</dt>
                  <dd className="text-small font-medium text-ink text-right">
                    {v}
                  </dd>
                </div>
              ))}
            </dl>
          </div>
        ))}
      </div>
    </Section>
  );
}

/* -------------------------------------------------------------------------- */
/*                              DEVELOPER EXAMPLE                             */
/* -------------------------------------------------------------------------- */

const CURL_SNIPPET = `curl -X POST https://zamtrust.app/api/documents/42/sign \\
  -H "Authorization: Bearer $TOKEN"`;

const JSON_SNIPPET = `{
  "documentId": 42,
  "algorithm": "SHA256withRSA",
  "documentHash": "a3f9…c812",
  "signedAt": "2026-09-15T10:30:00Z"
}`;

function DeveloperExample() {
  return (
    <Section
      eyebrow="For developers"
      title="A REST API you can build on."
      description="Every action in the UI is available over HTTP. Verification is public. Everything else is authenticated with a JWT."
      className="border-b border-border bg-surface-subtle"
    >
      <div id="developers" className="grid lg:grid-cols-2 gap-6">
        <CodeBlock language="bash" label="Request" code={CURL_SNIPPET} />
        <CodeBlock language="json" label="Response" code={JSON_SNIPPET} />
      </div>

      <p className="mt-6 text-small text-ink-muted max-w-2xl">
        Full OpenAPI documentation and SDKs are on the roadmap. Today the API is
        documented in-app and accessible from any HTTP client.
      </p>
    </Section>
  );
}

function CodeBlock({
  language,
  label,
  code,
}: {
  language: string;
  label: string;
  code: string;
}) {
  return (
    <div className="rounded-card border border-border bg-surface overflow-hidden">
      <div className="flex items-center justify-between px-4 py-2.5 border-b border-border">
        <span className="text-caption uppercase tracking-wider text-ink-muted">
          {label} · {language}
        </span>
        <CopyButton value={code} />
      </div>
      <pre className="px-4 py-4 text-mono text-ink-secondary overflow-x-auto">
        <code>{code}</code>
      </pre>
    </div>
  );
}

/* -------------------------------------------------------------------------- */
/*                                   CTA                                      */
/* -------------------------------------------------------------------------- */

function FinalCTA() {
  return (
    <section className="border-b border-border">
      <div className="max-w-content mx-auto px-6 lg:px-8 py-20 lg:py-24 text-center">
        <h2 className="text-h2 text-ink max-w-2xl mx-auto">
          Sign one document today and verify it in a browser.
        </h2>
        <p className="mt-4 text-body-lg text-ink-secondary max-w-xl mx-auto">
          Creating an account takes under a minute. Your first document is
          signed and verifiable in two.
        </p>
        <div className="mt-8 flex flex-wrap gap-3 justify-center">
          <ButtonLink to="/register" size="lg" rightIcon={<ArrowRight />}>
            Create account
          </ButtonLink>
          <ButtonLink to="/login" variant="secondary" size="lg">
            Sign in
          </ButtonLink>
        </div>
      </div>
    </section>
  );
}

/* -------------------------------------------------------------------------- */
/*                                  FOOTER                                    */
/* -------------------------------------------------------------------------- */

function Footer() {
  return (
    <footer className="bg-surface">
      <div className="max-w-wide mx-auto px-6 lg:px-8 py-14">
        <div className="grid md:grid-cols-4 gap-10">
          <div>
            <div className="flex items-center gap-2 mb-3">
              <Mark />
              <span className="text-body font-semibold text-ink tracking-tight">
                ZamTrust
              </span>
            </div>
            <p className="text-small text-ink-muted max-w-xs">
              Document trust infrastructure for enterprises that need
              provable signing and verification.
            </p>
          </div>

          <FooterCol
            title="Product"
            links={[
              ['How it works', '#how'],
              ['Technology', '#tech'],
              ['Verification', '/v/ZT-2026-000184'],
            ]}
          />
          <FooterCol
            title="Developers"
            links={[
              ['API reference', '#developers'],
              ['Sign in', '/login'],
              ['Create account', '/register'],
            ]}
          />
          <FooterCol
            title="Company"
            links={[
              ['Sign in', '/login'],
              ['Get started', '/register'],
            ]}
          />
        </div>

        <div className="mt-12 pt-6 border-t border-border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <p className="text-caption text-ink-muted">
            © {new Date().getFullYear()} ZamTrust
          </p>
          <p className="text-caption text-ink-muted">
            Built in Zambia. Verifiable everywhere.
          </p>
        </div>
      </div>
    </footer>
  );
}

function FooterCol({
  title,
  links,
}: {
  title: string;
  links: [string, string][];
}) {
  return (
    <div>
      <h3 className="text-caption uppercase tracking-wider text-ink-muted mb-3">
        {title}
      </h3>
      <ul className="space-y-2">
        {links.map(([label, href]) => (
          <li key={label}>
            {href.startsWith('#') || href.startsWith('/') ? (
              <Link to={href} className="link-quiet text-small">
                {label}
              </Link>
            ) : (
              <a href={href} className="link-quiet text-small">
                {label}
              </a>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
