import { Link, useSearchParams } from 'react-router-dom';
import { Card } from '../components/Card';
import { ButtonLink } from '../components/Button';

export default function Upgrade() {
  const [params] = useSearchParams();
  const planKey = params.get('plan') || 'PERSONAL';
  const planLabel = planKey.charAt(0) + planKey.slice(1).toLowerCase();

  return (
    <>
      <div className="mb-6">
        <Link to="/pricing" className="text-small text-ink-muted hover:text-ink transition-colors">
          ← Back to pricing
        </Link>
      </div>

      <Card>
        <div className="px-8 py-12 text-center max-w-lg mx-auto">
          <p className="eyebrow mb-3">Upgrade to {planLabel}</p>
          <h1 className="text-h3 text-ink">Card payments coming soon</h1>
          <p className="mt-4 text-body text-ink-secondary">
            Self-service upgrades will be available shortly. In the meantime,
            contact us to move your account to the <strong>{planLabel}</strong> plan
            and we'll set it up manually.
          </p>

          <div className="mt-8 flex flex-wrap gap-3 justify-center">
            <a
              href={`mailto:sales@zamtrust.dev?subject=Upgrade to ${planLabel}`}
              className="inline-flex items-center h-10 px-4 rounded-btn bg-accent text-white text-small font-medium hover:bg-accent-hover transition-colors"
            >
              Contact sales
            </a>
            <ButtonLink to="/app" variant="secondary">
              Back to dashboard
            </ButtonLink>
          </div>

          <p className="mt-6 text-caption text-ink-muted">
            Already have a payment method on file? Email support@zamtrust.dev.
          </p>
        </div>
      </Card>
    </>
  );
}
