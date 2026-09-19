import { Badge } from './Badge';

export type VerificationData = {
  verificationId: string;
  fileName: string;
  integrityValid: boolean;
  signatureValid: boolean;
  signer: string | null;
  signedAt: string | null;
  algorithm: string | null;
  message: string;
};

type Props = {
  data: VerificationData;
  className?: string;
};

export function VerificationCard({ data, className = '' }: Props) {
  const statusVariant =
    data.message === 'VALID'
      ? 'success'
      : data.message === 'NOT_SIGNED'
        ? 'warning'
        : 'danger';

  const statusLabel =
    data.message === 'VALID'
      ? 'Valid'
      : data.message === 'NOT_SIGNED'
        ? 'Not signed'
        : 'Invalid';

  return (
    <div
      className={
        'rounded-card border border-border bg-surface overflow-hidden ' +
        className
      }
    >
      {/* Head */}
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-border">
        <code className="font-mono text-small text-ink-muted tracking-tight">
          {data.verificationId}
        </code>
        <Badge variant={statusVariant}>{statusLabel}</Badge>
      </div>

      {/* Body */}
      <div className="px-5 py-5">
        <p className="text-small text-ink-muted mb-0.5">Document</p>
        <p className="text-body font-medium text-ink truncate">{data.fileName}</p>

        <dl className="mt-5 space-y-3 text-small">
          <Row label="Integrity" value={data.integrityValid ? 'Verified' : 'Failed'} ok={data.integrityValid} />
          <Row label="Signature" value={data.signatureValid ? 'Valid' : 'Invalid'} ok={data.signatureValid} />
          <Row label="Signer" value={data.signer ?? '—'} />
          <Row label="Signed at" value={fmtDate(data.signedAt)} />
          {data.algorithm && <Row label="Algorithm" value={data.algorithm} mono />}
        </dl>
      </div>
    </div>
  );
}

function Row({
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
    <div className="flex items-center justify-between gap-4">
      <dt className="text-ink-muted">{label}</dt>
      <dd
        className={
          'font-medium truncate max-w-[60%] ' +
          (mono ? 'font-mono text-caption tracking-tight ' : '') +
          (ok === undefined
            ? 'text-ink'
            : ok
              ? 'text-success'
              : 'text-danger')
        }
      >
        {value}
      </dd>
    </div>
  );
}

function fmtDate(iso: string | null) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  } catch {
    return iso;
  }
}
