import { forwardRef, useId, type InputHTMLAttributes } from 'react';

type InputProps = {
  label?: string;
  hint?: string;
  error?: string;
  leftIcon?: React.ReactNode;
} & InputHTMLAttributes<HTMLInputElement>;

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, hint, error, leftIcon, className = '', id, ...rest }, ref) => {
    const autoId = useId();
    const inputId = id ?? autoId;
    const errorId = error ? `${inputId}-error` : undefined;
    const hintId = hint && !error ? `${inputId}-hint` : undefined;

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={inputId}
            className="block text-small font-medium text-ink mb-1.5"
          >
            {label}
          </label>
        )}
        <div className="relative">
          {leftIcon && (
            <div className="absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted pointer-events-none">
              {leftIcon}
            </div>
          )}
          <input
            ref={ref}
            id={inputId}
            aria-invalid={!!error}
            aria-describedby={errorId ?? hintId}
            className={
              'w-full h-10 rounded-input border bg-surface text-body px-3 ' +
              'transition-colors duration-150 ' +
              (leftIcon ? 'pl-9 ' : '') +
              (error
                ? 'border-danger focus:border-danger '
                : 'border-border hover:border-border-strong focus:border-accent ') +
              'focus:outline-none focus:ring-2 focus:ring-accent/15 ' +
              'disabled:bg-surface-muted disabled:text-ink-muted disabled:cursor-not-allowed ' +
              'placeholder:text-ink-subtle ' +
              className
            }
            {...rest}
          />
        </div>
        {error ? (
          <p id={errorId} className="mt-1.5 text-small text-danger">
            {error}
          </p>
        ) : hint ? (
          <p id={hintId} className="mt-1.5 text-small text-ink-muted">
            {hint}
          </p>
        ) : null}
      </div>
    );
  },
);
Input.displayName = 'Input';
