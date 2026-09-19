type CardProps = {
  variant?: 'default' | 'elevated' | 'interactive';
  className?: string;
  children: React.ReactNode;
};

export function Card({ variant = 'default', className = '', children }: CardProps) {
  const base = 'rounded-card bg-surface';

  const variants = {
    default: 'border border-border',
    elevated: 'border border-border shadow-sm',
    interactive:
      'border border-border shadow-xs hover:border-border-strong ' +
      'hover:shadow-sm transition-all duration-150 ease-natural',
  } as const;

  return (
    <div className={`${base} ${variants[variant]} ${className}`}>{children}</div>
  );
}

export function CardHeader({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <div className={`px-6 py-4 border-b border-border ${className}`}>{children}</div>;
}

export function CardBody({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <div className={`px-6 py-5 ${className}`}>{children}</div>;
}

export function CardFooter({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <div className={`px-6 py-4 border-t border-border ${className}`}>{children}</div>;
}
