import type { DocumentStatus } from '../lib/documents';

const styles: Record<DocumentStatus, { bg: string; dot: string; label: string }> = {
  UPLOADED: {
    bg: 'bg-surface-muted text-ink-secondary',
    dot: 'bg-ink-subtle',
    label: 'Uploaded',
  },
  PENDING_SIGNATURE: {
    bg: 'bg-warning-subtle text-warning',
    dot: 'bg-warning',
    label: 'Pending',
  },
  SIGNED: {
    bg: 'bg-success-subtle text-success',
    dot: 'bg-success',
    label: 'Signed',
  },
  REVOKED: {
    bg: 'bg-danger-subtle text-danger',
    dot: 'bg-danger',
    label: 'Revoked',
  },
};

export function StatusPill({ status }: { status: DocumentStatus }) {
  const s = styles[status] ?? styles.UPLOADED;
  return (
    <span
      className={
        'inline-flex items-center gap-1.5 h-6 px-2 rounded-input text-caption font-medium ' +
        s.bg
      }
    >
      <span className={'w-1.5 h-1.5 rounded-full ' + s.dot} aria-hidden="true" />
      {s.label}
    </span>
  );
}
