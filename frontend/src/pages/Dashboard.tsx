import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { EmptyState } from '../components/EmptyState';
import { UploadModal } from '../components/UploadModal';
import { TableSkeleton } from '../components/Skeleton';
import { StatusPill } from '../components/StatusPill';
import { Alert } from '../components/Alert';
import { listDocuments, formatBytes, formatDate, type DocumentRow } from '../lib/documents';
import { toMessage } from '../lib/errors';

export default function Dashboard() {
  const navigate = useNavigate();
  const [docs, setDocs] = useState<DocumentRow[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [uploadOpen, setUploadOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const rows = await listDocuments();
        if (!cancelled) setDocs(rows);
      } catch (err) {
        if (!cancelled) setError(toMessage(err));
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <>
      <div className="flex items-start justify-between gap-6 mb-8">
        <div>
          <h1 className="text-h3 text-ink">Documents</h1>
          <p className="mt-1 text-small text-ink-secondary">
            Uploaded, signed, and auditable. Click a document to sign or verify it.
          </p>
        </div>
        <Button onClick={() => setUploadOpen(true)}>Upload document</Button>
      </div>

      {error && <Alert variant="danger" className="mb-6">{error}</Alert>}

      <Card>
        {docs === null && !error && <TableSkeleton rows={4} />}

        {docs !== null && docs.length === 0 && (
          <EmptyState
            title="No documents yet"
            description="Upload your first document to compute its SHA-256 fingerprint and prepare it for signing."
            action={
              <Button onClick={() => setUploadOpen(true)}>
                Upload your first document
              </Button>
            }
          />
        )}

        {docs !== null && docs.length > 0 && (
          <table className="w-full">
            <thead>
              <tr className="border-b border-border text-left">
                <Th>Document</Th>
                <Th>Status</Th>
                <Th>Uploaded</Th>
                <Th>Verification ID</Th>
                <Th align="right">Size</Th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {docs.map((doc) => (
                <tr
                  key={doc.id}
                  onClick={() => navigate(`/app/documents/${doc.id}`)}
                  className="cursor-pointer hover:bg-surface-muted/60 transition-colors"
                >
                  <td className="px-6 py-3.5">
                    <p className="text-small font-medium text-ink truncate max-w-xs">
                      {doc.title || doc.fileName}
                    </p>
                    <p className="text-caption text-ink-muted truncate max-w-xs">
                      {doc.fileName}
                    </p>
                  </td>
                  <td className="px-6 py-3.5">
                    <StatusPill status={doc.status} />
                  </td>
                  <td className="px-6 py-3.5 text-small text-ink-secondary whitespace-nowrap">
                    {formatDate(doc.createdAt)}
                  </td>
                  <td className="px-6 py-3.5">
                    <code className="text-caption font-mono text-ink-muted">
                      {doc.verificationId}
                    </code>
                  </td>
                  <td className="px-6 py-3.5 text-small text-ink-secondary text-right whitespace-nowrap">
                    {formatBytes(doc.sizeBytes)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <UploadModal
        open={uploadOpen}
        onClose={() => setUploadOpen(false)}
        onUploaded={(doc) => {
          setDocs((prev) => (prev ? [doc, ...prev] : [doc]));
        }}
      />
    </>
  );
}

function Th({
  children,
  align = 'left',
}: {
  children: React.ReactNode;
  align?: 'left' | 'right';
}) {
  return (
    <th
      className={
        'px-6 py-3 text-caption uppercase tracking-wider font-medium text-ink-muted ' +
        (align === 'right' ? 'text-right' : 'text-left')
      }
    >
      {children}
    </th>
  );
}
