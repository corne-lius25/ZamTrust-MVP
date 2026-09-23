type Props = {
  used: number;
  limit: number;
  unlimited?: boolean;
  label: string;
  unit?: string;
};

export function UsageBar({ used, limit, unlimited, label, unit = '' }: Props) {
  if (unlimited) {
    return (
      <div>
        <div className="flex items-center justify-between text-small mb-1.5">
          <span className="text-ink-secondary">{label}</span>
          <span className="font-medium text-ink tabular-nums">{used} / ∞</span>
        </div>
        <div className="h-1.5 w-full rounded-full bg-surface-muted overflow-hidden">
          <div className="h-full bg-accent/30 w-full" />
        </div>
      </div>
    );
  }

  const pct = limit === 0 ? 0 : Math.min(100, Math.round((used / limit) * 100));
  const nearLimit = pct >= 80;
  const atLimit = used >= limit;

  const barColor = atLimit
    ? 'bg-danger'
    : nearLimit
      ? 'bg-warning'
      : 'bg-accent';

  return (
    <div>
      <div className="flex items-center justify-between text-small mb-1.5">
        <span className="text-ink-secondary">{label}</span>
        <span className={`font-medium tabular-nums ${atLimit ? 'text-danger' : 'text-ink'}`}>
          {used} / {limit}{unit ? ` ${unit}` : ''}
        </span>
      </div>
      <div className="h-1.5 w-full rounded-full bg-surface-muted overflow-hidden">
        <div
          className={`h-full transition-all duration-300 ${barColor}`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}
