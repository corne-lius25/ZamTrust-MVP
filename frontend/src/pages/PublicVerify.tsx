import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { Badge } from '../components/Badge';
import { ButtonLink } from '../components/Button';
import { Card } from '../components/Card';
import { CopyButton } from '../components/CopyButton';
import { Divider } from '../components/Divider';
import { Mono } from '../components/Mono';
import { Skeleton } from '../components/Skeleton';
import { verifyDocument, formatDate, type VerificationData } from '../lib/documents';

type EnrichedVerification = VerificationData & {
  hasSignedPdf?: boolean;
  signedPdfSha256?: string | null;
};

export default function PublicVerify() {
  const { verificationId } = useParams<{ verificationId: string }>();
  const [data, setData] = useState<EnrichedVerification | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    if (!verificationId) return;

    setLoading(true);
    setError(null);
    (async () => {
      try {
        const result = await verifyDocument(verificationId);
        if (!cancelled) setData(result as EnrichedVerification);
      } catch (err: any) {
        if (!cancelled) {
          const status = err?.response?.status;
          setError(status === 404
            ? 'No document matches this verification ID.'
            : 'Could not load verification details. Please try again.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [verificationId]);

  return (
    <div className="min-h-screen bg-surface-subtle">
      <Header />
      <main className="max-w-4xl mx-auto px-6 py-12 lg:py-20">
        {loading ? (
          <LoadingState />
        ) : error ? (
          <NotFound message={error} />
        ) : data ? (
          <Result data={data} />
        ) : null}
      </main>
      <Footer />
    </div>
  );
}

/* -------------------------------------------------------------------------- */

function Header() {
  return (
    <header className="border-b border-border bg-surface">
      <div className="max-w-wide mx-auto px-6 lg:px-8 h-14 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 no-underline">
          <span
            aria-hidden="true"
            className="grid place-items-center w-6 h-6 rounded-btn bg-ink text-white font-semibold text-caption"
          >
            Z
          </span>
          <span className="text-body font-semibold text-ink tracking-tight">
            ZamTrust
          </span>
        </Link>
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

function Footer() {
  return (
    <footer className="border-t border-border mt-20 py-8">
      <div className="max-w-4xl mx-auto px-6 text-center">
        <p className="text-caption text-ink-muted">
          Verified by ZamTrust · Cryptographic integrity for every document
        </p>
      </div>
    </footer>
  );
}

/* -------------------------------------------------------------------------- */

function LoadingState() {
  return (
    <div className="space-y-4">
      <Skeleton className="h-6 w-40" />
      <Skeleton className="h-12 w-3/4" />
      <Skeleton className="h-32 w-full" />
      <Skeleton className="h-48 w-full" />
    </div>
  );
}

function NotFound({ message }: { message: string }) {
  return (
    <Card className="text-center py-16">
      <div className="px-6">
        <div className="mx-auto w-12 h-12 rounded-full border border-danger/20 bg-danger-subtle grid place-items-center mb-4">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path
              d="M10 6v4M10 14h.01"
              stroke="currentColor"
              strokeWidth="1.75"
              strokeLinecap="round"
              className="text-danger"
            />
            <circle cx="10" cy="10" r="8" stroke="currentColor" strokeWidth="1.25" className="text-danger" />
          </svg>
        </div>
        <h1 className="text-h3 text-ink">Document not found</h1>
        <p className="mt-3 text-body text-ink-secondary max-w-md mx-auto">{message}</p>
        <div className="mt-6 flex justify-center gap-3">
          <ButtonLink to="/" variant="secondary">
            Return home
          </ButtonLink>
        </div>
      </div>
    </Card>
  );
}

/* -------------------------------------------------------------------------- */

function Result({ data }: { data: EnrichedVerification }) {
  const isValid = data.message === 'VALID';
  const isNotSigned = data.message === 'NOT_SIGNED';
  const statusVariant = isValid ? 'success' : isNotSigned ? 'warning' : 'danger';
  const statusLabel = isValid ? 'Valid' : isNotSigned ? 'Not signed' : 'Invalid';
  const headline = isValid
    ? 'This document is authentic.'
    : isNotSigned
      ? 'This document has not been signed yet.'
      : 'This document has been altered.';

  const verifyUrl = typeof window !== 'undefined' ? window.location.href : '';
  const signedPdfUrl = `/api/verifications/${data.verificationId}/signed-pdf`;
  const previewUrl = `/api/verifications/${data.verificationId}/signed-pdf-preview`;

  return (
    <div className="space-y-8">
      {/* Status banner */}
      <div
        className={
          'rounded-card border px-6 py-5 flex items-start gap-4 ' +
          (isValid
            ? 'bg-success-subtle border-success/20'
            : isNotSigned
              ? 'bg-warning-subtle border-warning/20'
              : 'bg-danger-subtle border-danger/20')
        }
      >
        <div
          className={
            'flex-shrink-0 w-8 h-8 rounded-full grid place-items-center ' +
            (isValid
              ? 'bg-success text-white'
              : isNotSigned
                ? 'bg-warning text-white'
                : 'bg-danger text-white')
          }
          aria-hidden="true"
        >
          {isValid ? <CheckIcon /> : isNotSigned ? <PendingIcon /> : <CrossIcon />}
        </div>
        <div className="min-w-0">
          <div className="flex items-center gap-3 mb-1">
            <h1 className="text-h4 text-ink">{headline}</h1>
            <Badge variant={statusVariant}>{statusLabel}</Badge>
          </div>
          <p className="text-small text-ink-secondary">
            {isValid
              ? 'The SHA-256 hash and RSA signature match. The document has not changed since it was signed.'
              : isNotSigned
                ? 'The document exists but has not yet been signed by any party.'
                : 'The current file contents do not match what was signed. Do not rely on this document.'}
          </p>
        </div>
      </div>

      {/* Verification metadata + QR */}
      <Card>
        <div className="grid md:grid-cols-5 gap-6 p-6">
          <div className="md:col-span-3 space-y-4">
            <div>
              <p className="text-caption uppercase tracking-wider text-ink-muted mb-1">
                Verification ID
              </p>
              <div className="flex items-center gap-2">
                <code className="text-body font-mono text-ink tracking-tight">
                  {data.verificationId}
                </code>
                <CopyButton value={data.verificationId} />
              </div>
            </div>

            <div>
              <p className="text-caption uppercase tracking-wider text-ink-muted mb-1">
                Document
              </p>
              <p className="text-body text-ink truncate">{data.fileName}</p>
            </div>

            <div className="grid grid-cols-2 gap-4 pt-2">
              <div>
                <p className="text-caption uppercase tracking-wider text-ink-muted mb-1">
                  Signer
                </p>
                <p className="text-small font-medium text-ink truncate">
                  {data.signer ?? '—'}
                </p>
              </div>
              <div>
                <p className="text-caption uppercase tracking-wider text-ink-muted mb-1">
                  Signed at
                </p>
                <p className="text-small font-medium text-ink">
                  {formatDate(data.signedAt)}
                </p>
              </div>
            </div>
          </div>

          <div className="md:col-span-2 flex flex-col items-center justify-center">
            <div className="p-3 bg-surface border border-border rounded-card">
              <QRCodeSVG value={verifyUrl} size={140} level="M" bgColor="#ffffff" fgColor="#0a0a0a" />
            </div>
            <p className="mt-2 text-caption text-ink-muted text-center">
              Scan to re-verify
            </p>
          </div>
        </div>

        {data.hasSignedPdf && (
          <>
            <Divider />
            <div className="px-6 py-4 flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="text-small font-medium text-ink">Signed document</p>
                <p className="text-caption text-ink-muted">
                  Download or preview the PDF with the visible signature.
                </p>
              </div>
              <div className="flex gap-2">
                <a
                  href={previewUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center h-9 px-3 rounded-btn border border-border bg-surface text-small text-ink-secondary hover:text-ink hover:border-border-strong transition-colors"
                >
                  Preview
                </a>
                <a
                  href={signedPdfUrl}
                  className="inline-flex items-center h-9 px-4 rounded-btn bg-accent text-white text-small font-medium hover:bg-accent-hover transition-colors"
                >
                  Download PDF
                </a>
              </div>
            </div>
          </>
        )}
      </Card>

      {/* Signed PDF preview */}
      {data.hasSignedPdf && (
        <Card>
          <div className="px-6 py-4 border-b border-border">
            <h2 className="text-h4 text-ink">Signed PDF preview</h2>
            <p className="mt-0.5 text-small text-ink-secondary">
              This is the actual signed document, with the visible signature baked in.
            </p>
          </div>
          <div className="p-4">
            <iframe
              src={`${previewUrl}#toolbar=0&navpanes=0`}
              title="Signed PDF preview"
              className="w-full rounded-input border border-border bg-surface-muted"
              style={{ height: '600px' }}
            />
          </div>
        </Card>
      )}

      {/* Cryptographic checks */}
      <Card>
        <div className="px-6 py-4 border-b border-border">
          <h2 className="text-h4 text-ink">Cryptographic checks</h2>
          <p className="mt-0.5 text-small text-ink-secondary">
            Each check is performed independently when this page loads.
          </p>
        </div>
        <div className="px-6 py-5">
          <dl className="space-y-4">
            <Check
              label="Original document integrity"
              description="SHA-256 hash of the current file matches the hash recorded at upload."
              ok={data.integrityValid}
            />
            <Check
              label="Digital signature"
              description={
                data.algorithm
                  ? `Signature verified against the signer's public key using ${data.algorithm}.`
                  : 'Signature verified against the signer’s public key.'
              }
              ok={data.signatureValid}
            />
          </dl>

          <Divider className="my-5" />

          <div className="space-y-3">
            <Row label="Algorithm" value={data.algorithm ?? '—'} mono />
            <Row label="Verification ID" value={data.verificationId} mono />
            <Row label="Status" value={data.message} mono />
            {data.signedPdfSha256 && (
              <div className="flex items-start justify-between gap-4">
                <span className="text-small text-ink-muted flex-shrink-0">
                  Signed PDF hash
                </span>
                <div className="flex items-center gap-2 min-w-0">
                  <Mono truncate>{data.signedPdfSha256}</Mono>
                  <CopyButton value={data.signedPdfSha256} />
                </div>
              </div>
            )}
          </div>
        </div>
      </Card>

      {/* How this works */}
      <div className="text-center">
        <p className="text-small text-ink-secondary max-w-xl mx-auto">
          ZamTrust verifies documents using public-key cryptography. Anyone can
          verify a signed document without access to the signer's private key
          — only their public key is required.
        </p>
        <div className="mt-5 flex justify-center gap-3">
          <ButtonLink to="/register" variant="secondary" size="sm">
            Learn more about ZamTrust
          </ButtonLink>
        </div>
      </div>
    </div>
  );
}

/* -------------------------------------------------------------------------- */

function Check({
  label,
  description,
  ok,
}: {
  label: string;
  description: string;
  ok: boolean;
}) {
  return (
    <div className="flex items-start gap-3">
      <span
        className={
          'flex-shrink-0 w-5 h-5 rounded-full grid place-items-center mt-0.5 ' +
          (ok ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger')
        }
        aria-hidden="true"
      >
        {ok ? (
          <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
            <path
              d="M1.5 5.5l2 2 5-5"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        ) : (
          <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
            <path
              d="M2 2l6 6M8 2l-6 6"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
            />
          </svg>
        )}
      </span>
      <div className="min-w-0">
        <p className="text-small font-medium text-ink">{label}</p>
        <p className="mt-0.5 text-small text-ink-secondary">{description}</p>
      </div>
    </div>
  );
}

function Row({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between gap-4">
      <span className="text-small text-ink-muted">{label}</span>
      {mono ? (
        <Mono>{value}</Mono>
      ) : (
        <span className="text-small text-ink">{value}</span>
      )}
    </div>
  );
}

/* -------------------------------------------------------------------------- */

function CheckIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
      <path
        d="M2 7.5l3.5 3.5L12 4"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function PendingIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
      <circle cx="7" cy="7" r="5" stroke="currentColor" strokeWidth="1.75" />
      <path
        d="M7 4v3.5l2 1"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function CrossIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
      <path
        d="M3 3l8 8M11 3l-8 8"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  );
}
