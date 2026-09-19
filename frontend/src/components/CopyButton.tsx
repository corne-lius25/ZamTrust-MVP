import { useState } from 'react';

export function CopyButton({ value, label = 'Copy' }: { value: string; label?: string }) {
  const [copied, setCopied] = useState(false);

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      setTimeout(() => setCopied(false), 1600);
    } catch {
      /* clipboard unavailable */
    }
  }

  return (
    <button
      type="button"
      onClick={handleCopy}
      className={
        'inline-flex items-center h-7 px-2 rounded-input text-caption uppercase ' +
        'tracking-wider border transition-colors duration-150 ' +
        (copied
          ? 'bg-success-subtle text-success border-success/20'
          : 'bg-surface text-ink-muted border-border hover:text-ink hover:border-border-strong')
      }
      aria-live="polite"
    >
      {copied ? 'Copied' : label}
    </button>
  );
}
