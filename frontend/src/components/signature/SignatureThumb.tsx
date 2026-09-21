import { useEffect, useState, useRef } from 'react';
import { fetchSignatureImageBlobUrl } from '../../lib/signatures';

export function SignatureThumb({
  id,
  alt,
  className = '',
}: {
  id: number;
  alt: string;
  className?: string;
}) {
  const [url, setUrl] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);
  const urlRef = useRef<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      try {
        const objectUrl = await fetchSignatureImageBlobUrl(id);
        if (cancelled) {
          URL.revokeObjectURL(objectUrl);
          return;
        }
        // Revoke any previous URL before setting the new one
        if (urlRef.current && urlRef.current !== objectUrl) {
          URL.revokeObjectURL(urlRef.current);
        }
        urlRef.current = objectUrl;
        setUrl(objectUrl);
        setFailed(false);
      } catch {
        if (!cancelled) setFailed(true);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [id]);

  // Cleanup on unmount only
  useEffect(() => {
    return () => {
      if (urlRef.current) {
        URL.revokeObjectURL(urlRef.current);
        urlRef.current = null;
      }
    };
  }, []);

  if (failed) {
    return (
      <div className={`flex items-center justify-center text-caption text-ink-muted ${className}`}>
        Image unavailable
      </div>
    );
  }

  if (!url) {
    return (
      <div className={`flex items-center justify-center ${className}`}>
        <div className="w-6 h-6 border-2 border-border border-t-accent rounded-full animate-spin" />
      </div>
    );
  }

  return <img src={url} alt={alt} className={className} />;
}
