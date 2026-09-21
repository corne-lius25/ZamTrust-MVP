import { api } from './api';

export type SignatureDto = {
  id: number;
  label: string;
  widthPx: number;
  heightPx: number;
  isDefault: boolean;
  sha256: string;
  createdAt: string;
};

export async function listSignatures(): Promise<SignatureDto[]> {
  const { data } = await api.get<SignatureDto[]>('/api/signatures');
  return data;
}

export async function saveSignature(payload: {
  label: string;
  imageBase64: string;
  widthPx: number;
  heightPx: number;
  makeDefault?: boolean;
}): Promise<SignatureDto> {
  const { data } = await api.post<SignatureDto>('/api/signatures/base64', payload);
  return data;
}

export async function setDefaultSignature(id: number): Promise<void> {
  await api.put(`/api/signatures/${id}/default`);
}

export async function deleteSignature(id: number): Promise<void> {
  await api.delete(`/api/signatures/${id}`);
}

export function signatureImageUrl(id: number): string {
  const base = import.meta.env.VITE_API_BASE || '';
  return `${base}/api/signatures/${id}/image`;
}

/**
 * Fetches a signature image via the authenticated axios client and
 * returns a temporary blob URL for rendering in <img src="...">.
 * Browsers do not send the Authorization header on plain <img> requests,
 * so we must load the image through axios.
 */
export async function fetchSignatureImageBlobUrl(id: number): Promise<string> {
  const response = await api.get(`/api/signatures/${id}/image`, {
    responseType: 'blob',
  });
  return URL.createObjectURL(response.data);
}
