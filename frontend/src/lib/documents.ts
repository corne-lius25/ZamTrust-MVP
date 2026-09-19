import { api } from './api';

export type DocumentStatus = 'UPLOADED' | 'PENDING_SIGNATURE' | 'SIGNED' | 'REVOKED';

export type DocumentRow = {
  id: number;
  verificationId: string;
  fileName: string;
  title: string | null;
  contentType: string | null;
  sizeBytes: number;
  originalHash: string;
  status: DocumentStatus;
  ownerUsername: string | null;
  createdAt: string;
  signedAt: string | null;
};

export type SignatureRow = {
  id: number;
  documentId: number;
  signerUsername: string;
  algorithm: string;
  documentHash: string;
  signedAt: string;
};

export async function listDocuments(): Promise<DocumentRow[]> {
  const { data } = await api.get<DocumentRow[]>('/api/documents');
  return data;
}

export async function getDocument(id: number | string): Promise<DocumentRow> {
  const { data } = await api.get<DocumentRow>(`/api/documents/${id}`);
  return data;
}

export async function uploadDocument(file: File, title: string): Promise<DocumentRow> {
  const form = new FormData();
  form.append('file', file);
  if (title) form.append('title', title);
  const { data } = await api.post<DocumentRow>('/api/documents', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function signDocument(id: number): Promise<SignatureRow> {
  const { data } = await api.post<SignatureRow>(`/api/documents/${id}/sign`);
  return data;
}

export type VerificationData = {
  verificationId: string;
  fileName: string;
  integrityValid: boolean;
  signatureValid: boolean;
  signer: string | null;
  signedAt: string | null;
  algorithm: string | null;
  message: string;
};

export async function verifyDocument(verificationId: string): Promise<VerificationData> {
  const { data } = await api.get<VerificationData>(`/api/verifications/${verificationId}`);
  return data;
}

export function formatBytes(n: number): string {
  if (n < 1024) return `${n} B`;
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
  return `${(n / (1024 * 1024)).toFixed(1)} MB`;
}

export function formatDate(iso: string | null): string {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return iso;
  }
}
