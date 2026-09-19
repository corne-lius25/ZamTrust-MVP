type Props = {
  title: string;
  description: string;
  action?: React.ReactNode;
};

export function EmptyState({ title, description, action }: Props) {
  return (
    <div className="text-center py-16 px-6">
      <div className="mx-auto w-10 h-10 rounded-full border border-border grid place-items-center mb-4">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <path
            d="M3 2h7l3 3v9a1 1 0 01-1 1H3a1 1 0 01-1-1V3a1 1 0 011-1z"
            stroke="currentColor"
            strokeWidth="1.25"
            strokeLinejoin="round"
            className="text-ink-muted"
          />
          <path
            d="M10 2v3h3"
            stroke="currentColor"
            strokeWidth="1.25"
            strokeLinejoin="round"
            className="text-ink-muted"
          />
        </svg>
      </div>
      <h3 className="text-h4 text-ink">{title}</h3>
      <p className="mt-2 text-small text-ink-secondary max-w-sm mx-auto">
        {description}
      </p>
      {action && <div className="mt-6">{action}</div>}
    </div>
  );
}
