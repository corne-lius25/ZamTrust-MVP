import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Alert } from '../components/Alert';
import { useAuth } from '../lib/store';
import { toMessage } from '../lib/errors';

export default function Register() {
  const navigate = useNavigate();
  const register = useAuth((s) => s.register);
  const login = useAuth((s) => s.login);

  const [values, setValues] = useState({
    username: '',
    email: '',
    fullName: '',
    organization: '',
    password: '',
  });

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function set<K extends keyof typeof values>(k: K, v: string) {
    setValues((prev) => ({ ...prev, [k]: v }));
  }

  function validate() {
    const errs: Record<string, string> = {};

    if (!values.username.trim()) errs.username = 'Choose a username.';
    else if (values.username.length < 3) errs.username = 'At least 3 characters.';
    else if (!/^[a-zA-Z0-9._-]+$/.test(values.username))
      errs.username = 'Letters, numbers, dots, dashes, underscores only.';

    if (!values.email.trim()) errs.email = 'Enter your work email.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email))
      errs.email = 'Enter a valid email address.';

    if (!values.fullName.trim()) errs.fullName = 'Enter your full name.';
    if (!values.organization.trim()) errs.organization = 'Enter your organisation.';

    if (!values.password) errs.password = 'Choose a password.';
    else if (values.password.length < 8) errs.password = 'At least 8 characters.';

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setServerError(null);
    if (!validate()) return;

    setSubmitting(true);
    try {
      await register({
        username: values.username.trim(),
        email: values.email.trim(),
        fullName: values.fullName.trim(),
        organization: values.organization.trim(),
        password: values.password,
      });
      // Auto-login after register
      await login(values.username.trim(), values.password);
      navigate('/app', { replace: true });
    } catch (err) {
      setServerError(toMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Sign and verify your first document in under two minutes."
      footer={
        <p className="text-small text-ink-muted">
          Already have an account?{' '}
          <Link to="/login" className="text-accent font-medium hover:text-accent-hover transition-colors">
            Sign in
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
          value={values.username}
          onChange={(e) => set('username', e.target.value)}
          error={fieldErrors.username}
          hint="Used to sign in. Cannot be changed."
          disabled={submitting}
        />

        <Input
          label="Full name"
          name="fullName"
          autoComplete="name"
          value={values.fullName}
          onChange={(e) => set('fullName', e.target.value)}
          error={fieldErrors.fullName}
          disabled={submitting}
        />

        <Input
          label="Organisation"
          name="organization"
          autoComplete="organization"
          value={values.organization}
          onChange={(e) => set('organization', e.target.value)}
          error={fieldErrors.organization}
          hint="Shown as the signer on signed documents."
          disabled={submitting}
        />

        <Input
          label="Work email"
          type="email"
          name="email"
          autoComplete="email"
          value={values.email}
          onChange={(e) => set('email', e.target.value)}
          error={fieldErrors.email}
          disabled={submitting}
        />

        <Input
          label="Password"
          type="password"
          name="password"
          autoComplete="new-password"
          value={values.password}
          onChange={(e) => set('password', e.target.value)}
          error={fieldErrors.password}
          hint="Minimum 8 characters."
          disabled={submitting}
        />

        <Button
          type="submit"
          size="lg"
          className="w-full"
          loading={submitting}
          disabled={submitting}
        >
          {submitting ? 'Creating account…' : 'Create account'}
        </Button>

        <p className="text-caption text-ink-muted text-center">
          By creating an account you agree to use ZamTrust for lawful purposes only.
        </p>
      </form>
    </AuthLayout>
  );
}
