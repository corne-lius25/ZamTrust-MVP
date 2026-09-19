import { forwardRef, type ButtonHTMLAttributes } from 'react';
import { Link } from 'react-router-dom';

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger';
type Size = 'sm' | 'md' | 'lg';

const base =
  'inline-flex items-center justify-center font-medium rounded-btn transition-all ' +
  'duration-150 ease-natural disabled:cursor-not-allowed disabled:opacity-50 ' +
  'no-select whitespace-nowrap';

const variants: Record<Variant, string> = {
  primary:
    'bg-accent text-white hover:bg-accent-hover active:bg-accent-hover ' +
    'shadow-xs hover:shadow-sm',
  secondary:
    'bg-surface text-ink border border-border hover:border-border-strong ' +
    'hover:bg-surface-subtle shadow-xs',
  ghost:
    'bg-transparent text-ink-secondary hover:text-ink hover:bg-surface-muted',
  danger:
    'bg-danger text-white hover:brightness-95 shadow-xs',
};

const sizes: Record<Size, string> = {
  sm: 'h-8 px-3 text-small gap-1.5',
  md: 'h-10 px-4 text-small gap-2',
  lg: 'h-11 px-5 text-body gap-2',
};

type ButtonProps = {
  variant?: Variant;
  size?: Size;
  loading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
} & ButtonHTMLAttributes<HTMLButtonElement>;

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    { variant = 'primary', size = 'md', loading, leftIcon, rightIcon, className = '', children, disabled, ...rest },
    ref,
  ) => {
    return (
      <button
        ref={ref}
        disabled={disabled || loading}
        className={`${base} ${variants[variant]} ${sizes[size]} ${className}`}
        {...rest}
      >
        {loading ? (
          <Spinner />
        ) : (
          leftIcon
        )}
        <span>{children}</span>
        {!loading && rightIcon}
      </button>
    );
  },
);
Button.displayName = 'Button';

function Spinner() {
  return (
    <svg
      className="animate-spin h-4 w-4"
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
      aria-hidden="true"
    >
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="3"
      />
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
      />
    </svg>
  );
}

/* Link variant — same visual, but renders <Link> */
type ButtonLinkProps = {
  to: string;
  variant?: Variant;
  size?: Size;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  className?: string;
  children: React.ReactNode;
};

export function ButtonLink({
  to,
  variant = 'primary',
  size = 'md',
  leftIcon,
  rightIcon,
  className = '',
  children,
}: ButtonLinkProps) {
  return (
    <Link
      to={to}
      className={`${base} ${variants[variant]} ${sizes[size]} ${className}`}
    >
      {leftIcon}
      <span>{children}</span>
      {rightIcon}
    </Link>
  );
}
