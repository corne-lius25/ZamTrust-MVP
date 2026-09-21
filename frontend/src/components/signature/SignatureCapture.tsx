import { useRef, useState, useEffect } from 'react';
import SignatureCanvas from 'react-signature-canvas';
import { Button } from '../Button';

export type SignatureMode = 'draw' | 'type';

export type SignaturePayload = {
  /** PNG data URL (data:image/png;base64,...) */
  imageBase64: string;
  widthPx: number;
  heightPx: number;
};

type Props = {
  value?: string;                    // full name for typed mode
  onChange: (name: string) => void;  // typed-name change handler
  onCapture: (payload: SignaturePayload | null) => void;
  font?: 'sig-caveat' | 'sig-dancing';
};

export function SignatureCapture({ value = '', onChange, onCapture, font = 'sig-dancing' }: Props) {
  const [mode, setMode] = useState<SignatureMode>('draw');
  const [typedFont, setTypedFont] = useState<'sig-caveat' | 'sig-dancing'>(font);

  const canvasRef = useRef<SignatureCanvas | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const typedTextRef = useRef<HTMLDivElement>(null);

  /** Resize the drawing canvas to match its container */
  useEffect(() => {
    if (mode !== 'draw') return;
    const canvas = canvasRef.current?.getCanvas();
    const container = containerRef.current;
    if (!canvas || !container) return;

    const ratio = Math.max(window.devicePixelRatio || 1, 1);
    canvas.width = container.clientWidth * ratio;
    canvas.height = 200 * ratio;
    canvas.getContext('2d')?.scale(ratio, ratio);
    canvasRef.current?.clear();
    onCapture(null);
  }, [mode, onCapture]);

  function clearDrawing() {
    canvasRef.current?.clear();
    onCapture(null);
  }

  function captureDrawing() {
    const canvas = canvasRef.current;
    if (!canvas || canvas.isEmpty()) {
      onCapture(null);
      return;
    }
    const dataUrl = canvas.getCanvas().toDataURL('image/png');
    onCapture({ imageBase64: dataUrl, widthPx: 400, heightPx: 120 });
  }

  /** Typed signature → render to canvas and export PNG */
  async function captureTyped() {
    const node = typedTextRef.current;
    if (!node || !(value ?? '').trim()) {
      onCapture(null);
      return;
    }

    const width = 400;
    const height = 120;
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, width, height);
    const fontSize = 56;
    const family = typedFont === 'sig-caveat' ? 'Caveat' : '"Dancing Script"';
    ctx.font = `600 ${fontSize}px ${family}, cursive`;
    ctx.fillStyle = '#0a0a0a';
    ctx.textBaseline = 'middle';
    ctx.textAlign = 'center';
    ctx.fillText(value ?? '', width / 2, height / 2);

    const dataUrl = canvas.toDataURL('image/png');
    onCapture({ imageBase64: dataUrl, widthPx: width, heightPx: height });
  }

  /** Re-capture typed signature whenever text changes */
  useEffect(() => {
    if (mode === 'type') {
      captureTyped();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value, typedFont, mode]);

  return (
    <div className="space-y-4">
      {/* Tabs */}
      <div className="inline-flex rounded-btn border border-border p-0.5 bg-surface-muted">
        <TabButton active={mode === 'draw'} onClick={() => setMode('draw')}>Draw</TabButton>
        <TabButton active={mode === 'type'} onClick={() => setMode('type')}>Type</TabButton>
      </div>

      {/* Draw mode */}
      {mode === 'draw' && (
        <div>
          <div
            ref={containerRef}
            className="rounded-card border-2 border-dashed border-border bg-surface-subtle overflow-hidden"
            style={{ height: 200 }}
          >
            <SignatureCanvas
              ref={(r) => { canvasRef.current = r; }}
              onEnd={captureDrawing}
              canvasProps={{
                className: 'w-full h-full cursor-crosshair',
                style: { width: '100%', height: '200px' },
              }}
              penColor="#0a0a0a"
              minWidth={1.2}
              maxWidth={2.8}
            />
          </div>
          <div className="mt-3 flex items-center justify-between">
            <p className="text-caption text-ink-muted">
              Use your mouse or finger. This is only for placement — it does not affect the cryptographic signature.
            </p>
            <Button variant="ghost" size="sm" onClick={clearDrawing}>Clear</Button>
          </div>
        </div>
      )}

      {/* Type mode */}
      {mode === 'type' && (
        <div className="space-y-3">
          <input
            type="text"
            value={value ?? ''}
            onChange={(e) => onChange(e.target.value)}
            placeholder="Type your name"
            className="w-full h-10 rounded-input border border-border bg-surface text-body px-3 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/15"
          />
          <div className="rounded-card border-2 border-dashed border-border bg-surface-subtle flex items-center justify-center px-4" style={{ height: 200 }}>
            {(value ?? '').trim() ? (
              <div
                ref={typedTextRef}
                className={`text-ink ${typedFont === 'sig-caveat' ? 'font-sig-caveat' : 'font-sig-dancing'}`}
                style={{ fontSize: 56, lineHeight: 1, fontWeight: 600 }}
              >
                {value}
              </div>
            ) : (
              <p className="text-small text-ink-muted">Start typing to preview your signature</p>
            )}
          </div>
          <div className="flex items-center gap-2">
            <span className="text-caption text-ink-muted">Style:</span>
            <button
              type="button"
              onClick={() => setTypedFont('sig-dancing')}
              className={`text-caption px-3 py-1 rounded-input border transition-colors ${
                typedFont === 'sig-dancing'
                  ? 'bg-accent-subtle text-accent border-accent/30'
                  : 'bg-surface border-border text-ink-muted hover:text-ink'
              }`}
            >
              <span className="font-sig-dancing" style={{ fontSize: 16 }}>Elegant</span>
            </button>
            <button
              type="button"
              onClick={() => setTypedFont('sig-caveat')}
              className={`text-caption px-3 py-1 rounded-input border transition-colors ${
                typedFont === 'sig-caveat'
                  ? 'bg-accent-subtle text-accent border-accent/30'
                  : 'bg-surface border-border text-ink-muted hover:text-ink'
              }`}
            >
              <span className="font-sig-caveat" style={{ fontSize: 18 }}>Casual</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function TabButton({
  active,
  onClick,
  children,
}: {
  active: boolean;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={
        'px-4 h-8 rounded-[4px] text-small font-medium transition-colors ' +
        (active ? 'bg-surface text-ink shadow-xs' : 'text-ink-muted hover:text-ink')
      }
    >
      {children}
    </button>
  );
}
