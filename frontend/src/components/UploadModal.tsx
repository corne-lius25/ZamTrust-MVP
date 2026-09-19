import { useRef, useState, type DragEvent } from 'react';
import { Modal } from './Modal';
import { Button } from './Button';
import { Input } from './Input';
import { Alert } from './Alert';
import { uploadDocument, formatBytes, type DocumentRow } from '../lib/documents';
import { toMessage } from '../lib/errors';

type Props = {
  open: boolean;
  onClose: () => void;
  onUploaded: (doc: DocumentRow) => void;
};

export function UploadModal({ open, onClose, onUploaded }: Props) {
  const [file, setFile] = useState<File | null>(null);
  const [title, setTitle] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [dragging, setDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  function reset() {
    setFile(null);
    setTitle('');
    setError(null);
    setDragging(false);
  }

  function pick(f: File | null | undefined) {
    if (!f) return;
    if (f.size > 25 * 1024 * 1024) {
      setError('File exceeds 25 MB.');
      return;
    }
    setError(null);
    setFile(f);
    if (!title) setTitle(f.name.replace(/\.[^.]+$/, ''));
  }

  function onDrop(e: DragEvent<HTMLDivElement>) {
    e.preventDefault();
    setDragging(false);
    pick(e.dataTransfer.files?.[0]);
  }

  async function submit() {
    if (!file) {
      setError('Choose a file to upload.');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const doc = await uploadDocument(file, title.trim() || file.name);
      onUploaded(doc);
      reset();
      onClose();
    } catch (err) {
      setError(toMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={() => {
        if (!submitting) {
          reset();
          onClose();
        }
      }}
      title="Upload document"
      description="Up to 25 MB. Any file type. A SHA-256 hash is computed on upload."
    >
      <div className="space-y-5">
        {error && <Alert variant="danger">{error}</Alert>}

        <div
          onDragOver={(e) => {
            e.preventDefault();
            setDragging(true);
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={onDrop}
          onClick={() => inputRef.current?.click()}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') inputRef.current?.click();
          }}
          className={
            'cursor-pointer rounded-card border-2 border-dashed px-6 py-10 text-center transition-colors ' +
            (dragging
              ? 'border-accent bg-accent-subtle'
              : 'border-border hover:border-border-strong bg-surface-subtle')
          }
        >
          <input
            ref={inputRef}
            type="file"
            className="hidden"
            onChange={(e) => pick(e.target.files?.[0])}
          />
          {file ? (
            <div>
              <p className="text-small font-medium text-ink truncate">{file.name}</p>
              <p className="mt-1 text-caption text-ink-muted">
                {formatBytes(file.size)} · click to replace
              </p>
            </div>
          ) : (
            <>
              <p className="text-small text-ink font-medium">
                Drop a file here or click to browse
              </p>
              <p className="mt-1 text-caption text-ink-muted">
                PDF, DOCX, images, or plain text
              </p>
            </>
          )}
        </div>

        <Input
          label="Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Optional"
          disabled={submitting}
        />

        <div className="flex justify-end gap-2 pt-1">
          <Button
            variant="ghost"
            onClick={() => {
              reset();
              onClose();
            }}
            disabled={submitting}
          >
            Cancel
          </Button>
          <Button onClick={submit} loading={submitting} disabled={submitting || !file}>
            {submitting ? 'Uploading…' : 'Upload'}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
