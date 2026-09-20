import { useEffect, useRef, useState, useCallback } from 'react';
import * as pdfjsLib from 'pdfjs-dist';
import type { PDFDocumentProxy } from 'pdfjs-dist';
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url';
import { Button } from '../Button';
import { Skeleton } from '../Skeleton';

pdfjsLib.GlobalWorkerOptions.workerSrc = workerUrl;

export type Placement = {
  page: number;
  x: number;      // fraction of page width
  y: number;      // fraction of page height, from top
  width: number;  // fraction of page width
  height: number; // fraction of page height
};

type Props = {
  pdfUrl: string;
  signatureImageUrl: string;
  initial?: Placement;
  onChange: (p: Placement) => void;
};

export function PdfPlacement({ pdfUrl, signatureImageUrl, initial, onChange }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [pdf, setPdf] = useState<PDFDocumentProxy | null>(null);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState<{ w: number; h: number } | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [placement, setPlacement] = useState<Placement>(
    initial ?? { page: 1, x: 0.55, y: 0.72, width: 0.35, height: 0.12 }
  );

  // load PDF
  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    (async () => {
      try {
        const doc = await pdfjsLib.getDocument({ url: pdfUrl }).promise;
        if (cancelled) return;
        setPdf(doc);
      } catch (e: any) {
        if (!cancelled) setError(e?.message ?? 'Could not load PDF');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [pdfUrl]);

  // render page
  useEffect(() => {
    if (!pdf) return;
    let cancelled = false;

    (async () => {
      const page = await pdf.getPage(pageNum);
      if (cancelled) return;

      const containerWidth = containerRef.current?.clientWidth ?? 800;
      const base = page.getViewport({ scale: 1 });
      const scale = containerWidth / base.width;
      const viewport = page.getViewport({ scale });

      const canvas = canvasRef.current;
      if (!canvas) return;
      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      const ratio = window.devicePixelRatio || 1;
      canvas.width = viewport.width * ratio;
      canvas.height = viewport.height * ratio;
      canvas.style.width = `${viewport.width}px`;
      canvas.style.height = `${viewport.height}px`;

      ctx.scale(ratio, ratio);

      await page.render({ canvasContext: ctx, viewport, canvas }).promise;

      if (!cancelled) {
        setPageSize({ w: viewport.width, h: viewport.height });
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [pdf, pageNum]);

  // update parent on placement changes
  useEffect(() => {
    onChange(placement);
  }, [placement, onChange]);

  // drag / resize
  const dragState = useRef<{
    mode: 'move' | 'resize';
    startX: number;
    startY: number;
    startPlacement: Placement;
  } | null>(null);

  const handleMouseDown = useCallback((e: React.MouseEvent, mode: 'move' | 'resize') => {
    e.preventDefault();
    e.stopPropagation();
    dragState.current = {
      mode,
      startX: e.clientX,
      startY: e.clientY,
      startPlacement: placement,
    };
    document.body.style.userSelect = 'none';
  }, [placement]);

  useEffect(() => {
    if (!pageSize) return;

    function handleMove(e: MouseEvent) {
      const ds = dragState.current;
      if (!ds) return;
      const dx = (e.clientX - ds.startX) / pageSize!.w;
      const dy = (e.clientY - ds.startY) / pageSize!.h;

      if (ds.mode === 'move') {
        const nx = Math.max(0, Math.min(1 - ds.startPlacement.width, ds.startPlacement.x + dx));
        const ny = Math.max(0, Math.min(1 - ds.startPlacement.height, ds.startPlacement.y + dy));
        setPlacement({ ...ds.startPlacement, x: nx, y: ny });
      } else {
        const nw = Math.max(0.10, Math.min(1 - ds.startPlacement.x, ds.startPlacement.width + dx));
        const nh = Math.max(0.05, Math.min(1 - ds.startPlacement.y, ds.startPlacement.height + dy));
        setPlacement({ ...ds.startPlacement, width: nw, height: nh });
      }
    }

    function handleUp() {
      dragState.current = null;
      document.body.style.userSelect = '';
    }

    window.addEventListener('mousemove', handleMove);
    window.addEventListener('mouseup', handleUp);
    return () => {
      window.removeEventListener('mousemove', handleMove);
      window.removeEventListener('mouseup', handleUp);
    };
  }, [pageSize]);

  if (loading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-10 w-40" />
        <Skeleton className="h-[600px] w-full" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-card border border-danger/20 bg-danger-subtle px-6 py-4 text-small text-danger">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Page navigation */}
      <div className="flex items-center gap-3">
        <Button
          variant="secondary"
          size="sm"
          onClick={() => setPageNum((p) => Math.max(1, p - 1))}
          disabled={pageNum <= 1}
        >
          ← Previous
        </Button>
        <span className="text-small text-ink-secondary">
          Page {pageNum} of {pdf?.numPages ?? '?'}
        </span>
        <Button
          variant="secondary"
          size="sm"
          onClick={() => setPageNum((p) => Math.min(pdf?.numPages ?? 1, p + 1))}
          disabled={!pdf || pageNum >= pdf.numPages}
        >
          Next →
        </Button>
        <span className="ml-auto text-caption text-ink-muted">
          Drag the signature to reposition · drag the corner to resize
        </span>
      </div>

      {/* PDF + overlay */}
      <div
        ref={containerRef}
        className="relative mx-auto rounded-card border border-border bg-white shadow-sm overflow-hidden"
        style={{ maxWidth: '100%' }}
      >
        <canvas ref={canvasRef} className="block" />

        {pageSize && pageNum === placement.page && (
          <div
            onMouseDown={(e) => handleMouseDown(e, 'move')}
            style={{
              position: 'absolute',
              left: `${placement.x * pageSize.w}px`,
              top: `${placement.y * pageSize.h}px`,
              width: `${placement.width * pageSize.w}px`,
              height: `${placement.height * pageSize.h}px`,
            }}
            className="border-2 border-dashed border-accent bg-accent/5 cursor-move flex items-center justify-center group"
          >
            <img
              src={signatureImageUrl}
              alt="Your signature"
              draggable={false}
              className="max-h-full max-w-full object-contain pointer-events-none select-none"
            />
            {/* Resize handle */}
            <div
              onMouseDown={(e) => handleMouseDown(e, 'resize')}
              className="absolute bottom-0 right-0 w-3 h-3 bg-accent cursor-se-resize translate-x-1/2 translate-y-1/2 rounded-sm"
            />
          </div>
        )}
      </div>

      {/* Manual coordinates */}
      <details className="text-small text-ink-secondary">
        <summary className="cursor-pointer hover:text-ink">Adjust coordinates manually</summary>
        <div className="mt-3 grid grid-cols-2 md:grid-cols-4 gap-3">
          <Coord label="X" value={placement.x} onChange={(v) => setPlacement({ ...placement, x: v })} />
          <Coord label="Y" value={placement.y} onChange={(v) => setPlacement({ ...placement, y: v })} />
          <Coord label="Width"  value={placement.width}  onChange={(v) => setPlacement({ ...placement, width: v })} />
          <Coord label="Height" value={placement.height} onChange={(v) => setPlacement({ ...placement, height: v })} />
        </div>
      </details>
    </div>
  );
}

function Coord({ label, value, onChange }: { label: string; value: number; onChange: (v: number) => void }) {
  return (
    <label className="block">
      <span className="text-caption uppercase tracking-wider text-ink-muted">{label}</span>
      <input
        type="number"
        step="0.01"
        min="0"
        max="1"
        value={value}
        onChange={(e) => onChange(parseFloat(e.target.value) || 0)}
        className="mt-1 w-full h-9 rounded-input border border-border bg-surface text-small px-2 focus:border-accent focus:outline-none"
      />
    </label>
  );
}
