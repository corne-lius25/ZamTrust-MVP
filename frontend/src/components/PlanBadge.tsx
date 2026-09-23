type Plan = 'FREE' | 'PERSONAL' | 'BUSINESS' | 'ENTERPRISE';

const styles: Record<Plan, string> = {
  FREE: 'bg-surface-muted text-ink-secondary border-border',
  PERSONAL: 'bg-accent-subtle text-accent border-accent/20',
  BUSINESS: 'bg-accent-subtle text-accent border-accent/20',
  ENTERPRISE: 'bg-ink text-white border-ink',
};

export function PlanBadge({ plan, className = '' }: { plan: Plan; className?: string }) {
  const label = plan === 'FREE' ? 'Free' : plan.charAt(0) + plan.slice(1).toLowerCase();
  return (
    <span
      className={
        'inline-flex items-center h-5 px-1.5 rounded-[3px] ' +
        'text-caption uppercase tracking-wider border font-medium ' +
        styles[plan] + ' ' + className
      }
    >
      {label}
    </span>
  );
}
