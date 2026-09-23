import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Card } from './Card';
import { UsageBar } from './UsageBar';
import { PlanBadge } from './PlanBadge';
import { Skeleton } from './Skeleton';
import { fetchCurrentUsage, type CurrentUsage } from '../lib/plans';

export function UsageCard() {
  const [usage, setUsage] = useState<CurrentUsage | null>(null);
  const [error, setError] = useState(false);

  const location = useLocation();

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const u = await fetchCurrentUsage();
        if (!cancelled) {
          setUsage(u);
          setError(false);
        }
      } catch {
        if (!cancelled) setError(true);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [location.pathname]);

  if (error) return null;

  if (!usage) {
    return (
      <Card>
        <div className="p-5 space-y-4">
          <Skeleton className="h-4 w-24" />
          <Skeleton className="h-8 w-full" />
          <Skeleton className="h-8 w-full" />
        </div>
      </Card>
    );
  }

  const atLimit = usage.signatures.remaining === 0 && !usage.signatures.unlimited;

  return (
    <Card>
      <div className="px-5 py-4 border-b border-border flex items-center justify-between">
        <div>
          <p className="text-small font-medium text-ink">Usage</p>
          <p className="text-caption text-ink-muted">{usage.periodKey}</p>
        </div>
        <PlanBadge plan={usage.plan as any} />
      </div>

      <div className="p-5 space-y-4">
        <UsageBar
          label="Signatures"
          used={usage.signatures.used}
          limit={usage.signatures.limit}
          unlimited={usage.signatures.unlimited}
        />
        <UsageBar
          label="API calls"
          used={usage.apiCalls.used}
          limit={usage.apiCalls.limit}
          unlimited={usage.apiCalls.limit > 1_000_000}
        />
      </div>

      {(atLimit || usage.plan === 'FREE') && (
        <div className="px-5 py-3 border-t border-border bg-surface-subtle">
          <Link
            to="/pricing"
            className="text-small font-medium text-accent hover:text-accent-hover transition-colors"
          >
            {atLimit ? 'Upgrade to continue →' : 'Compare plans →'}
          </Link>
        </div>
      )}
    </Card>
  );
}

