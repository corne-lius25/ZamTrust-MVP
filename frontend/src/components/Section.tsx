type Props = {
  eyebrow?: string;
  title?: string;
  description?: string;
  children: React.ReactNode;
  className?: string;
  align?: 'left' | 'center';
  width?: 'content' | 'wide';
};

export function Section({
  eyebrow,
  title,
  description,
  children,
  className = '',
  align = 'left',
  width = 'content',
}: Props) {
  const alignClass = align === 'center' ? 'text-center mx-auto max-w-2xl' : 'max-w-2xl';
  const widthClass = width === 'wide' ? 'max-w-wide' : 'max-w-content';

  return (
    <section className={`py-20 lg:py-28 ${className}`}>
      <div className={`${widthClass} mx-auto px-6 lg:px-8`}>
        {(eyebrow || title || description) && (
          <header className={`${alignClass} mb-12 lg:mb-16`}>
            {eyebrow && <p className="eyebrow mb-3">{eyebrow}</p>}
            {title && <h2 className="text-h2 text-ink">{title}</h2>}
            {description && (
              <p className="mt-4 text-body-lg text-ink-secondary">{description}</p>
            )}
          </header>
        )}
        {children}
      </div>
    </section>
  );
}
