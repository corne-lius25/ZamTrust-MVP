import { Link } from 'react-router-dom';

export type QuotaInfo = {
  action: string;
  used: number;
  limit: number;
  plan: string;
  message: string;
  upgradeUrl?: string;
};

type Props = {
  quota: QuotaInfo;
  className?: string;
};

/**
 * Renders a quota-exceeded response (HTTP 402) as a friendly inline callout
 * with an upgrade CTA. Used anywhere a sign or verify action can hit a limit.
 */
export function UpgradePrompt({ quota, className = '' }: Props) {
  const label = labelFor(quota.action);

  return (
    <div
      className={
        'rounded-card border border-warning/30 bg-warning-subtle px-5 py-4 ' + className
      }
      role="alert"
    >
      <div className="flex items-start gap-3">
        <span
          aria-hidden="true"
          className="flex-shrink-0 w-6 h-6 rounded-full bg-warning text-white grid place-items-center mt-0.5"
        >
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path
              d="M6 2v4M6 8.5h.01"
              stroke="currentColor"
              strokeWidth="1.75"
              strokeLinecap="round"
            />
          </svg>
        </span>

        <div className="min-w-0 flex-1">
          <p className="text-small font-medium text-ink">
            {label} limit reached
          </p>
          <p className="mt-1 text-small text-ink-secondary">
            You've used <strong className="text-ink">{quota.used}</strong> of{' '}
            <strong className="text-ink">{quota.limit}</strong> {label.toLowerCase()} this
            month on the{' '}
            <strong className="text-ink">{prettyPlan(quota.plan)}</strong> plan.
          </p>

          <div className="mt-3 flex flex-wrap items-center gap-2">
            <Link
              to={quota.upgradeUrl || '/pricing'}
              className="inline-flex items-center h-8 px-3 rounded-btn bg-accent text-white text-small font-medium hover:bg-accent-hover transition-colors"
            >
              Compare plans →
            </Link>
            <span className="text-caption text-ink-muted">
              Resets on the 1st of next month
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

function labelFor(action: string) {
  switch (action) {
    case 'SIGN': return 'Signature';
    case 'VERIFY':
    case 'VERIFY_ANON': return 'Verification';
    case 'API_CALL': return 'API call';
    default: return 'Usage';
  }
}

function prettyPlan(plan: string) {
  if (plan === 'FREE') return 'Free';
  return plan.charAt(0) + plan.slice(1).toLowerCase();
}
