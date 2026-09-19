import { useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/store';

export default function AppLayout() {
  const user = useAuth((s) => s.user);
  const logout = useAuth((s) => s.logout);
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="min-h-screen bg-surface-subtle">
      <header className="sticky top-0 z-30 bg-surface border-b border-border">
        <div className="max-w-wide mx-auto px-6 lg:px-8 h-14 flex items-center justify-between">
          <div className="flex items-center gap-8">
            <Link to="/app" className="flex items-center gap-2 no-underline">
              <Mark />
              <span className="text-body font-semibold text-ink tracking-tight">
                ZamTrust
              </span>
            </Link>

            <nav className="hidden md:flex items-center gap-1">
              <NavItem to="/app" end>
                Documents
              </NavItem>
            </nav>
          </div>

          <div className="relative">
            <button
              type="button"
              onClick={() => setMenuOpen((v) => !v)}
              className="flex items-center gap-2 h-8 pl-1 pr-2 rounded-btn hover:bg-surface-muted transition-colors"
              aria-haspopup="menu"
              aria-expanded={menuOpen}
            >
              <span className="grid place-items-center w-6 h-6 rounded-full bg-surface-muted text-caption font-medium text-ink-secondary uppercase">
                {user?.username?.slice(0, 1) ?? '?'}
              </span>
              <span className="text-small text-ink">{user?.username}</span>
              <Chevron open={menuOpen} />
            </button>

            {menuOpen && (
              <>
                <div
                  className="fixed inset-0 z-10"
                  onClick={() => setMenuOpen(false)}
                  aria-hidden="true"
                />
                <div
                  role="menu"
                  className="absolute right-0 top-10 z-20 w-56 rounded-card border border-border bg-surface shadow-sm py-1"
                >
                  <div className="px-3 py-2 border-b border-border">
                    <p className="text-small font-medium text-ink truncate">
                      {user?.username}
                    </p>
                    <p className="text-caption text-ink-muted truncate">
                      {user?.roles?.join(', ') ?? ''}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={handleLogout}
                    role="menuitem"
                    className="w-full text-left px-3 py-2 text-small text-ink-secondary hover:bg-surface-muted hover:text-ink transition-colors"
                  >
                    Sign out
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="max-w-wide mx-auto px-6 lg:px-8 py-10">
        <Outlet />
      </main>
    </div>
  );
}

function NavItem({
  to,
  end,
  children,
}: {
  to: string;
  end?: boolean;
  children: React.ReactNode;
}) {
  return (
    <NavLink
      to={to}
      end={end}
      className={({ isActive }) =>
        'px-3 h-8 inline-flex items-center rounded-btn text-small transition-colors ' +
        (isActive
          ? 'bg-surface-muted text-ink font-medium'
          : 'text-ink-secondary hover:text-ink hover:bg-surface-muted')
      }
    >
      {children}
    </NavLink>
  );
}

function Mark() {
  return (
    <span
      aria-hidden="true"
      className="grid place-items-center w-6 h-6 rounded-btn bg-ink text-white font-semibold text-caption"
    >
      Z
    </span>
  );
}

function Chevron({ open }: { open: boolean }) {
  return (
    <svg
      width="10"
      height="10"
      viewBox="0 0 10 10"
      className={
        'text-ink-muted transition-transform duration-150 ' +
        (open ? 'rotate-180' : '')
      }
      aria-hidden="true"
    >
      <path
        d="M2 4l3 3 3-3"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}
