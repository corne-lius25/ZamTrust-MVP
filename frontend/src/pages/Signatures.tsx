import { useEffect, useState } from 'react';
import { SignatureCapture, type SignaturePayload } from '../components/signature/SignatureCapture';
import { SignatureThumb } from '../components/signature/SignatureThumb';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { Modal } from '../components/Modal';
import { Input } from '../components/Input';
import { Alert } from '../components/Alert';
import { EmptyState } from '../components/EmptyState';
import { Skeleton } from '../components/Skeleton';
import {
  listSignatures,
  saveSignature,
  setDefaultSignature,
  deleteSignature,
  type SignatureDto,
} from '../lib/signatures';
import { toMessage } from '../lib/errors';

export default function Signatures() {
  const [sigs, setSigs] = useState<SignatureDto[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);

  async function load() {
    setError(null);
    try {
      setSigs(await listSignatures());
    } catch (e) {
      setError(toMessage(e));
    }
  }

  useEffect(() => { load(); }, []);

  return (
    <>
      <div className="flex items-start justify-between gap-6 mb-8">
        <div>
          <h1 className="text-h3 text-ink">Signatures</h1>
          <p className="mt-1 text-small text-ink-secondary max-w-xl">
            Your saved signatures. When you sign a document, you'll place one of these on the page.
            The cryptographic signature is generated separately and cannot be forged by editing the image.
          </p>
        </div>
        <Button onClick={() => setCreating(true)}>New signature</Button>
      </div>

      {error && <Alert variant="danger" className="mb-6">{error}</Alert>}

      <Card>
        {sigs === null && !error && (
          <div className="p-6 grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-32 w-full" />)}
          </div>
        )}

        {sigs !== null && sigs.length === 0 && (
          <EmptyState
            title="No signatures yet"
            description="Create your first signature — draw it with your mouse or type your name. You can save up to 5."
            action={<Button onClick={() => setCreating(true)}>Create signature</Button>}
          />
        )}

        {sigs !== null && sigs.length > 0 && (
          <div className="p-6 grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {sigs.map((s) => <SignatureTile key={s.id} sig={s} onChanged={load} />)}
          </div>
        )}
      </Card>

      <CreateSignatureModal open={creating} onClose={() => setCreating(false)} onCreated={load} />
    </>
  );
}

/* -------------------------------------------------------------------------- */

function SignatureTile({ sig, onChanged }: { sig: SignatureDto; onChanged: () => void }) {
  const [busy, setBusy] = useState(false);

  async function handleDefault() {
    setBusy(true);
    try { await setDefaultSignature(sig.id); onChanged(); }
    finally { setBusy(false); }
  }

  async function handleDelete() {
    if (!confirm(`Delete signature "${sig.label}"?`)) return;
    setBusy(true);
    try { await deleteSignature(sig.id); onChanged(); }
    finally { setBusy(false); }
  }

  return (
    <div className="rounded-card border border-border bg-surface overflow-hidden">
      <div className="aspect-[4/1.5] bg-surface-subtle flex items-center justify-center p-3 border-b border-border">
        <SignatureThumb id={sig.id} alt={sig.label} className="h-full w-auto max-w-full object-contain" />
      </div>
      <div className="px-4 py-3">
        <div className="flex items-center justify-between mb-2">
          <p className="text-small font-medium text-ink truncate">{sig.label}</p>
          {sig.isDefault && (
            <span className="text-caption uppercase tracking-wider text-accent">Default</span>
          )}
        </div>
        <div className="flex items-center gap-2">
          {!sig.isDefault && (
            <Button size="sm" variant="secondary" onClick={handleDefault} disabled={busy}>
              Set default
            </Button>
          )}
          <Button size="sm" variant="ghost" onClick={handleDelete} disabled={busy}>
            Delete
          </Button>
        </div>
      </div>
    </div>
  );
}

/* -------------------------------------------------------------------------- */

function CreateSignatureModal({
  open,
  onClose,
  onCreated,
}: {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}) {
  const [label, setLabel] = useState('Signature');
  const [typedName, setTypedName] = useState('');
  const [payload, setPayload] = useState<SignaturePayload | null>(null);
  const [makeDefault, setMakeDefault] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function reset() {
    setLabel('Signature');
    setTypedName('');
    setPayload(null);
    setMakeDefault(true);
    setError(null);
  }

  async function save() {
    if (!payload) {
      setError('Draw or type a signature first.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await saveSignature({
        label: label.trim() || 'Signature',
        imageBase64: payload.imageBase64,
        widthPx: payload.widthPx,
        heightPx: payload.heightPx,
        makeDefault,
      });
      reset();
      onCreated();
      onClose();
    } catch (e) {
      setError(toMessage(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={() => { if (!saving) { reset(); onClose(); } }}
      title="Create signature"
      description="Draw it, or type your name and choose a style."
    >
      <div className="space-y-5">
        {error && <Alert variant="danger">{error}</Alert>}

        <SignatureCapture
          value={typedName}
          onChange={setTypedName}
          onCapture={setPayload}
        />

        <Input label="Label" value={label} onChange={(e) => setLabel(e.target.value)} placeholder="Full signature, Initials, …" />

        <label className="flex items-center gap-2 text-small text-ink-secondary cursor-pointer">
          <input type="checkbox" checked={makeDefault} onChange={(e) => setMakeDefault(e.target.checked)} />
          Make this my default signature
        </label>

        <div className="flex justify-end gap-2 pt-1">
          <Button variant="ghost" onClick={() => { reset(); onClose(); }} disabled={saving}>Cancel</Button>
          <Button onClick={save} loading={saving} disabled={saving || !payload}>
            {saving ? 'Saving…' : 'Save signature'}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
