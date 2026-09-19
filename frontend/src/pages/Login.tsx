import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Alert } from '../components/Alert';
import { useAuth } from '../lib/store';
import { toMessage } from '../lib/errors';

export default function Login() {
  const navigate = useNavigate();
  const login = useAuth((s) => s.login);

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function validate() {
    const errs: Record<string, string> = {};
    if (!username.trim()) errs.username = 'Enter your username.';
    if (!password) errs.password = 'Enter your password.';
    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setServerError(null);
    if (!validate()) return;

    setSubmitting(true);
    try {
      await login(username.trim(), password);
      navigate('/app', { replace: true });
    } catch (err) {
      setServerError(toMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout
      title="Sign in"
      subtitle="Access your documents, signatures, and audit trail."
      footer={
        <p className="text-small text-ink-muted">
          Don't have an account?{' '}
          <Link to="/register" className="text-accent font-medium hover:text-accent-hover transition-colors">
            Create one
          </Link>
        </p>
      }
    >
      <form onSubmit={onSubmit} noValidate className="space-y-5">
        {serverError && <Alert variant="danger">{serverError}</Alert>}

        <Input
          label="Username"
          name="username"
          autoComplete="username"
          autoFocus
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          error={fieldErrors.username}
          disabled={submitting}
        />

        <Input
          label="Password"
          type="password"
          name="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={fieldErrors.password}
          disabled={submitting}
        />

        <Button
          type="submit"
          size="lg"
          className="w-full"
          loading={submitting}
          disabled={submitting}
        >
          {submitting ? 'Signing in…' : 'Sign in'}
        </Button>
      </form>
    </AuthLayout>
  );
}
