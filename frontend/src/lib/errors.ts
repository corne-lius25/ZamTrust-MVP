import axios from 'axios';

export function toMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data;
    if (typeof data === 'string' && data.length > 0) return data;
    if (data && typeof data === 'object') {
      if ('message' in data && typeof (data as any).message === 'string') {
        return (data as any).message;
      }
      if ('error' in data && typeof (data as any).error === 'string') {
        return (data as any).error;
      }
    }
    if (err.code === 'ERR_NETWORK') {
      return 'Cannot reach the server. Check your connection.';
    }
    if (err.response?.status === 401) return 'Incorrect username or password.';
    if (err.response?.status === 409) return 'That username or email is already taken.';
    if (err.response?.status === 400) return 'Please check the information you provided.';
  }
  return 'Something went wrong. Please try again.';
}
