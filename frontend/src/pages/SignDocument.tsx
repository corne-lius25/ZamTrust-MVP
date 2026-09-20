import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { Alert } from '../components/Alert';
import { Skeleton } from '../components/Skeleton';
import { PdfPlacement, type Placement } from '../components/pdf/PdfPlacement';
import { getDocument, type DocumentRow } from '../lib/documents';
import { listSignatures, signatureImageUrl, type SignatureDto } from '../lib/signatures';
import { api } from '../lib/api';
import { toMessage } from '../lib/errors';

export default function SignDocument() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [doc, setDoc] = useState<DocumentRow | null>(null);
  const [sigs, setSigs] = useState<SignatureDto[] | null>(null);
  const [selectedSigId, setSelectedSigId] = useState<number | null>(null);
  const [placement, setPlacement] = useState<Placement>({
    page: 1,
    x: 0.55,
    y: 0.72,
    width: 0.35,
    height: 0.12,
  });
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const [d, s] = await Promise.all([getDocument(id), listSignatures()]);
        setDoc(d);
        setSigs(s);
        const def = s.find((x) => x.isDefault) ?? s[0];
        if (def) setSelectedSigId(def.id);
      } catch (e) {
        setError(toMessage(e));
      }
    })();
  }, [id]);

  // fetch the original PDF via axios so we include the auth header
  const [pdfBlobUrl, setPdfBlobUrl] = useState<string | null>(null);
  useEffect(() => {
    if (!doc) return;
    let cancelled = false;
    let url: string | null = null;

    (async () => {
      try {
        const res = await api.get(`/api/documents/${doc.id}/original-pdf`, {
          responseType: 'blob',
        });
        url = URL.createObjectURL(res.data);
        if (!cancelled) setPdfBlobUrl(url);
      } catch (e) {
        if (!cancelled) setError(toMessage(e));
      }
    })();

    return () => {
      cancelled = true;
      if (url) URL.revokeObjectURL(url);
    };
  }, [doc]);

  // fetch selected signature image
  const [sigBlobUrl, setSigBlobUrl] = useState<string | null>(null);
  useEffect(() => {
    if (!selectedSigId) return;
    let cancelled = false;
    let url: string | null = null;

    (async () => {
      try {
        const res = await api.get(`/api/signatures/${selectedSigId}/image`, {
          responseType: 'blob',
        });
        url = URL.createObjectURL(res.data);
        if (!cancelled) setSigBlobUrl(url);
      } catch (e) {
        if (!cancelled) setError(toMessage(e));
      }
    })();

    return () => {
      cancelled = true;
      if (url) URL.revokeObjectURL(url);
    };
  }, [selectedSigId]);

  async function apply() {
    if (!doc || !selectedSigId) return;
    setSubmitting(true);
    setError(null);
    try {
      await api.post(`/api/documents/${doc.id}/sign-with-visible`, {
        signatureImageId: selectedSigId,
        page: placement.page,
        x: placement.x,
        y: placement.y,
        width: placement.width,
        height: placement.height,
      });
      navigate(`/app/documents/${doc.id}`, { replace: true });
    } catch (e) {
      setError(toMessage(e));
    } finally {
      setSubmitting(false);
    }
  }

  if (!doc) {
    return <Skeleton className="h-96 w-full" />;
  }

  return (
    <>
      <div className="mb-6">
        <button
          onClick={() => navigate(`/app/documents/${doc.id}`)}
          className="text-small text-ink-muted hover:text-ink transition-colors"
        >
          ← Back to document
        </button>
      </div>

      <div className="grid lg:grid-cols-3 gap-6 items-start">
        <div className="lg:col-span-2 space-y-4">
          <Card>
            <div className="px-6 py-4 border-b border-border">
              <h1 className="text-h4 text-ink">Place your signature</h1>
              <p className="mt-1 text-small text-ink-secondary">
                Drag it to where you want to sign. The cryptographic signature is applied
                separately on the server and cannot be forged by editing this image.
              </p>
            </div>
            <div className="p-6">
              {error && <Alert variant="danger" className="mb-4">{error}</Alert>}
              {pdfBlobUrl && sigBlobUrl ? (
                <PdfPlacement
                  pdfUrl={pdfBlobUrl}
                  signatureImageUrl={sigBlobUrl}
                  onChange={setPlacement}
                />
              ) : (
                <Skeleton className="h-[600px] w-full" />
              )}
            </div>
          </Card>
        </div>

        <aside className="space-y-4">
          <Card>
            <div className="px-6 py-4 border-b border-border">
              <h2 className="text-h4 text-ink">Signature</h2>
            </div>
            <div className="p-6 space-y-3">
              {sigs && sigs.length > 0 ? (
                sigs.map((s) => (
                  <button
                    key={s.id}
                    type="button"
                    onClick={() => setSelectedSigId(s.id)}
                    className={
                      'w-full rounded-card border p-3 text-left transition-all ' +
                      (selectedSigId === s.id
                        ? 'border-accent bg-accent-subtle'
                        : 'border-border hover:border-border-strong')
                    }
                  >
                    <img
                      src={signatureImageUrl(s.id)}
                      alt={s.label}
                      className="h-12 mb-2 object-contain"
                      onError={(e) => {
                        (e.target as HTMLImageElement).style.display = 'none';
                      }}
                    />
                    <span className="text-small font-medium text-ink">{s.label}</span>
                  </button>
                ))
              ) : (
                <p className="text-small text-ink-muted">
                  No signatures yet.{' '}
                  <button
                    type="button"
                    onClick={() => navigate('/app/signatures')}
                    className="text-accent hover:text-accent-hover underline"
                  >
                    Create one
                  </button>
                </p>
              )}
            </div>
          </Card>

          <Button
            size="lg"
            className="w-full"
            onClick={apply}
            loading={submitting}
            disabled={submitting || !selectedSigId || !pdfBlobUrl}
          >
            {submitting ? 'Signing…' : 'Apply signature'}
          </Button>
        </aside>
      </div>
    </>
  );
}
