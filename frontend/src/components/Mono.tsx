export function Mono({
  children,
  className = '',
  truncate = false,
}: {
  children: React.ReactNode;
  className?: string;
  truncate?: boolean;
}) {
  return (
    <code
      className={
        'font-mono text-small text-ink-secondary bg-surface-muted ' +
        'px-2 py-0.5 rounded-input border border-border ' +
        (truncate ? 'inline-block max-w-[220px] truncate align-middle ' : '') +
        className
      }
    >
      {children}
    </code>
  );
}
