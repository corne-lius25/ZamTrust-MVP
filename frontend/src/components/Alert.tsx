type Variant = 'info' | 'success' | 'warning' | 'danger';

const styles: Record<Variant, string> = {
  info: 'bg-accent-subtle border-accent/20 text-ink',
  success: 'bg-success-subtle border-success/20 text-ink',
  warning: 'bg-warning-subtle border-warning/20 text-ink',
  danger: 'bg-danger-subtle border-danger/20 text-ink',
};

const dot: Record<Variant, string> = {
  info: 'bg-accent',
  success: 'bg-success',
  warning: 'bg-warning',
  danger: 'bg-danger',
};

export function Alert({
  variant = 'info',
  title,
  children,
  className = '',
}: {
  variant?: Variant;
  title?: string;
  children?: React.ReactNode;
  className?: string;
}) {
  return (
    <div
      role="alert"
      className={
        'flex gap-3 rounded-card border px-4 py-3 ' +
        styles[variant] + ' ' + className
      }
    >
      <span
        aria-hidden="true"
        className={'mt-1.5 w-1.5 h-1.5 rounded-full flex-shrink-0 ' + dot[variant]}
      />
      <div className="text-small">
        {title && <p className="font-medium mb-0.5">{title}</p>}
        {children && <div className="text-ink-secondary">{children}</div>}
      </div>
    </div>
  );
}
