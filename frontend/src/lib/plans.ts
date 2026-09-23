import { api } from './api';

export type UsageBucket = {
  used: number;
  limit: number;
  remaining: number;
  unlimited?: boolean;
  enabled?: boolean;
};

export type CurrentUsage = {
  periodKey: string;
  plan: string;
  planDisplayName: string;
  signatures: UsageBucket;
  apiCalls: UsageBucket;
};

export async function fetchCurrentUsage(): Promise<CurrentUsage> {
  const { data } = await api.get<CurrentUsage>('/api/usage/current');
  return data;
}

/**
 * Static plan definitions used by the public pricing page and upgrade flows.
 * Kept in sync with the backend Plan enum.
 */
export type PlanInfo = {
  key: 'FREE' | 'PERSONAL' | 'BUSINESS' | 'ENTERPRISE';
  name: string;
  priceLabel: string;
  priceMonthly: number;
  tagline: string;
  features: string[];
  cta: string;
  highlight?: boolean;
};

export const PLANS: PlanInfo[] = [
  {
    key: 'FREE',
    name: 'Free',
    priceLabel: '$0',
    priceMonthly: 0,
    tagline: 'Try ZamTrust with the essentials.',
    features: [
      '3 signatures per month',
      '10 verifications per month',
      'Public verification links',
      'QR code on signed PDFs',
      'Basic audit trail',
    ],
    cta: 'Current plan',
  },
  {
    key: 'PERSONAL',
    name: 'Personal',
    priceLabel: '$9',
    priceMonthly: 900,
    tagline: 'For freelancers and sole traders.',
    features: [
      '50 signatures per month',
      '500 verifications per month',
      'Persistent signing identity',
      'Priority support',
      'Everything in Free',
    ],
    cta: 'Upgrade to Personal',
    highlight: true,
  },
  {
    key: 'BUSINESS',
    name: 'Business',
    priceLabel: '$49',
    priceMonthly: 4900,
    tagline: 'For teams and growing companies.',
    features: [
      '500 signatures per month',
      '5,000 verifications per month',
      '10,000 API calls per month',
      'Persistent signing identity',
      'Everything in Personal',
    ],
    cta: 'Upgrade to Business',
  },
  {
    key: 'ENTERPRISE',
    name: 'Enterprise',
    priceLabel: '$499+',
    priceMonthly: 49900,
    tagline: 'For regulated industries and high-volume use.',
    features: [
      'Unlimited signatures',
      'Unlimited verifications',
      '1M+ API calls per month',
      'HSM-backed signing (roadmap)',
      'Priority support + SLA',
      'Everything in Business',
    ],
    cta: 'Contact sales',
  },
];
