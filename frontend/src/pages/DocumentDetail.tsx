import { api } from '../lib/api';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { Alert } from '../components/Alert';
import { Badge } from '../components/Badge';
import { Divider } from '../components/Divider';
import { StatusPill } from '../components/StatusPill';
import { VerificationCard } from '../components/VerificationCard';
import { Mono } from '../components/Mono';
import { CopyButton } from '../components/CopyButton';
import { Skeleton } from '../components/Skeleton';
import {
  getDocument,
  verifyDocument,
  formatBytes,
  formatDate,
  type DocumentRow,
  type VerificationData,
} from '../lib/documents';
import { toMessage } from '../lib/errors';

export default function DocumentDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [doc, setDoc] = useState<DocumentRow | null>(null);
  const [verification, setVerification] = useState<VerificationData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  async function load() {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const d = await getDocument(id);
      setDoc(d);
      try {
        const v = await verifyDocument(d.verificationId);
        setVerification(v);
      } catch {
        setVerification(null);
      }
    } catch (err) {
      setError(toMessage(err));
    } finally {
      setLoading(false);
    }
  }

  async function downloadSignedPdf(id: number) {
    try {
      const res = await api.get(`/api/documents/${id}/signed-pdf`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(res.data);
      const a = document.createElement("a");
      a.href = url;
      a.download = `signed-${id}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError(toMessage(e));
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-40" />
        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-4">
            <Skeleton className="h-32 w-full" />
            <Skeleton className="h-48 w-full" />
          </div>
          <Skeleton className="h-72 w-full" />
        </div>
      </div>
    );
  }

  if (error && !doc) {
    return (
      <>
        <button
          onClick={() => navigate('/app')}
          className="text-small text-ink-muted hover:text-ink mb-4"
        >
          ← Back to documents
        </button>
        <Alert variant="danger">{error}</Alert>
      </>
    );
  }

  if (!doc) return null;

  const verifyUrl = `${window.location.origin}/v/${doc.verificationId}`;

  return (
    <>
      <div className="mb-6">
        <Link to="/app" className="text-small text-ink-muted hover:text-ink transition-colors">
          ← Documents
        </Link>
      </div>

      <div className="flex flex-wrap items-start justify-between gap-6 mb-8">
        <div className="min-w-0">
          <h1 className="text-h3 text-ink truncate">{doc.title || doc.fileName}</h1>
          <div className="mt-2 flex items-center gap-3 flex-wrap">
            <StatusPill status={doc.status} />
            <span className="text-small text-ink-muted">{formatBytes(doc.sizeBytes)}</span>
            <span className="text-small text-ink-muted">·</span>
            <span className="text-small text-ink-muted">{formatDate(doc.createdAt)}</span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {doc.status !== 'SIGNED' && (
            <Button onClick={() => navigate(`/app/documents/${doc.id}/sign`)}>
              Sign document
            </Button>
          )}
          {doc.status === 'SIGNED' && (
            <Button onClick={() => downloadSignedPdf(doc.id)}>
              Download signed PDF
            </Button>
          )}
          <Button
            variant="secondary"
            onClick={() => {
              navigator.clipboard.writeText(verifyUrl);
            }}
          >
            Copy verification link
          </Button>
        </div>
      </div>

      {error && <Alert variant="danger" className="mb-6">{error}</Alert>}

      <div className="grid lg:grid-cols-3 gap-6 items-start">
        {/* Main */}
        <div className="lg:col-span-2 space-y-6">
          {/* Verification */}
          <Card>
            <div className="px-6 py-4 border-b border-border flex items-center justify-between">
              <div>
                <h2 className="text-h4 text-ink">Verification</h2>
                <p className="mt-0.5 text-small text-ink-secondary">
                  Independent integrity check on the stored document.
                </p>
              </div>
              {verification && (
                <Badge variant={verification.message === 'VALID' ? 'success' : 'danger'}>
                  {verification.message}
                </Badge>
              )}
            </div>
            <div className="px-6 py-5">
              {verification ? (
                <div className="grid sm:grid-cols-2 gap-x-8 gap-y-4">
                  <Field
                    label="Integrity"
                    value={verification.integrityValid ? 'Verified' : 'Failed'}
                    ok={verification.integrityValid}
                  />
                  <Field
                    label="Signature"
                    value={verification.signatureValid ? 'Valid' : 'Invalid'}
                    ok={verification.signatureValid}
                  />
                  <Field label="Signer" value={verification.signer ?? 'Not signed yet'} />
                  <Field label="Signed at" value={formatDate(verification.signedAt)} />
                  <Field label="Algorithm" value={verification.algorithm ?? '—'} mono />
                </div>
              ) : (
                <p className="text-small text-ink-muted">
                  Not signed yet. Verify after signing to see the cryptographic result.
                </p>
              )}
            </div>
          </Card>

          {/* Document fingerprint */}
          <Card>
            <div className="px-6 py-4 border-b border-border">
              <h2 className="text-h4 text-ink">Document fingerprint</h2>
              <p className="mt-0.5 text-small text-ink-secondary">
                Computed on upload. Any change to the file changes this hash.
              </p>
            </div>
            <div className="px-6 py-5">
              <div className="flex items-center justify-between gap-4 mb-3">
                <span className="text-caption uppercase tracking-wider text-ink-muted">
                  SHA-256
                </span>
                <CopyButton value={doc.originalHash} />
              </div>
              <Mono className="break-all whitespace-pre-wrap block">
                {doc.originalHash}
              </Mono>
            </div>
          </Card>

          {/* Preview (browser-rendered) */}
          {doc.contentType?.startsWith('image/') && (
            <Card>
              <div className="px-6 py-4 border-b border-border">
                <h2 className="text-h4 text-ink">Preview</h2>
              </div>
              <div className="p-6">
                <img
                  src={`/api/documents/${doc.id}/raw`}
                  alt={doc.fileName}
                  className="max-h-96 mx-auto rounded-input border border-border"
                  onError={(e) => {
                    (e.target as HTMLImageElement).style.display = 'none';
                  }}
                />
              </div>
            </Card>
          )}
        </div>

        {/* Sidebar */}
        <aside className="space-y-6">
          <Card>
            <div className="px-6 py-4 border-b border-border">
              <h2 className="text-h4 text-ink">Verification ID</h2>
            </div>
            <div className="px-6 py-5">
              <div className="flex items-center justify-between gap-3">
                <code className="text-body font-mono text-ink tracking-tight">
                  {doc.verificationId}
                </code>
                <CopyButton value={doc.verificationId} />
              </div>
              <p className="mt-3 text-caption text-ink-muted">
                Share this with anyone who needs to verify the document.
              </p>

              <Divider className="my-4" />

              <div className="flex items-center justify-between gap-3">
                <span className="text-caption text-ink-muted truncate">{verifyUrl}</span>
                <CopyButton value={verifyUrl} />
              </div>
            </div>
          </Card>

          <Card>
            <div className="px-6 py-4 border-b border-border">
              <h2 className="text-h4 text-ink">Live verification card</h2>
            </div>
            <div className="p-5">
              {verification ? (
                <VerificationCard
                  data={{
                    verificationId: verification.verificationId,
                    fileName: verification.fileName,
                    integrityValid: verification.integrityValid,
                    signatureValid: verification.signatureValid,
                    signer: verification.signer,
                    signedAt: verification.signedAt,
                    algorithm: verification.algorithm,
                    message: verification.message,
                  }}
                />
              ) : (
                <p className="text-small text-ink-muted">
                  Available after the document is signed.
                </p>
              )}
            </div>
          </Card>
        </aside>
      </div>
    </>
  );
}

function Field({
  label,
  value,
  ok,
  mono,
}: {
  label: string;
  value: string;
  ok?: boolean;
  mono?: boolean;
}) {
  return (
    <div>
      <p className="text-caption uppercase tracking-wider text-ink-muted mb-1">
        {label}
      </p>
      <p
        className={
          'text-small font-medium truncate ' +
          (mono ? 'font-mono tracking-tight ' : '') +
          (ok === undefined ? 'text-ink' : ok ? 'text-success' : 'text-danger')
        }
      >
        {value}
      </p>
    </div>
  );
}
