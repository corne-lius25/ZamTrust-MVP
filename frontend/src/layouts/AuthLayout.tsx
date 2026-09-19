import { Link } from 'react-router-dom';
import { VerificationCard } from '../components/VerificationCard';

type Props = {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
};

export default function AuthLayout({ title, subtitle, children, footer }: Props) {
  return (
    <div className="min-h-screen grid lg:grid-cols-2">
      {/* Left: form */}
      <div className="flex flex-col px-6 lg:px-10 py-8 lg:py-10">
        <Link to="/" className="flex items-center gap-2 no-underline w-fit">
          <Mark />
          <span className="text-body font-semibold text-ink tracking-tight">
            ZamTrust
          </span>
        </Link>

        <div className="flex-1 flex items-center">
          <div className="w-full max-w-sm mx-auto py-12">
            <h1 className="text-h2 text-ink">{title}</h1>
            {subtitle && (
              <p className="mt-2 text-body text-ink-secondary">{subtitle}</p>
            )}
            <div className="mt-8">{children}</div>
            {footer && <div className="mt-8">{footer}</div>}
          </div>
        </div>

        <p className="text-caption text-ink-muted text-center">
          © {new Date().getFullYear()} ZamTrust
        </p>
      </div>

      {/* Right: product context */}
      <div className="hidden lg:flex items-center justify-center bg-surface-subtle border-l border-border p-12">
        <div className="w-full max-w-sm">
          <p className="eyebrow mb-5">A live verification result</p>
          <VerificationCard
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
            className="shadow-sm"
          />
          <p className="mt-4 text-small text-ink-muted leading-relaxed">
            Every document you sign on ZamTrust gets a public verification
            link. Anyone — with or without an account — can confirm it has
            not been altered.
          </p>
        </div>
      </div>
    </div>
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
