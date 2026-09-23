import axios from 'axios';
import type { QuotaInfo } from '../components/UpgradePrompt';

/**
 * If the error was a 402 Payment Required with a quota body,
 * returns the parsed QuotaInfo. Otherwise returns null.
 */
export function extractQuotaError(err: unknown): QuotaInfo | null {
  if (!axios.isAxiosError(err)) return null;
  const res = err.response;
  if (!res || res.status !== 402) return null;

  const d = res.data as any;
  if (!d || typeof d !== 'object') return null;

  if (typeof d.action !== 'string' || typeof d.limit !== 'number') return null;

  return {
    action: d.action,
    used: typeof d.used === 'number' ? d.used : 0,
    limit: d.limit,
    plan: typeof d.plan === 'string' ? d.plan : 'FREE',
    message: typeof d.message === 'string' ? d.message : 'Quota exceeded',
    upgradeUrl: typeof d.upgradeUrl === 'string' ? d.upgradeUrl : '/pricing',
  };
}
