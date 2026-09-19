type Variant = 'neutral' | 'success' | 'warning' | 'danger' | 'accent';

const styles: Record<Variant, string> = {
  neutral: 'bg-surface-muted text-ink-secondary border-border',
  success: 'bg-success-subtle text-success border-success/20',
  warning: 'bg-warning-subtle text-warning border-warning/20',
  danger:  'bg-danger-subtle text-danger border-danger/20',
  accent:  'bg-accent-subtle text-accent border-accent/20',
};

export function Badge({
  variant = 'neutral',
  children,
  className = '',
}: {
  variant?: Variant;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <span
      className={
        'inline-flex items-center gap-1.5 h-6 px-2 rounded-input ' +
        'text-caption uppercase tracking-wider border ' +
        styles[variant] + ' ' + className
      }
    >
      {children}
    </span>
  );
}
