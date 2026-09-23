import { Link } from 'react-router-dom';
import { Card } from '../components/Card';
import { ButtonLink } from '../components/Button';
import { PLANS, type PlanInfo } from '../lib/plans';
import { useAuth } from '../lib/store';

export default function Pricing() {
  const { token } = useAuth();
  const loggedIn = !!token;

  return (
    <div className="min-h-screen bg-surface">
      <Header loggedIn={loggedIn} />

      <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12 sm:py-16 lg:py-24">
        <div className="text-center max-w-2xl mx-auto">
          <p className="eyebrow mb-3">Pricing</p>
          <h1 className="text-h2 sm:text-h1 text-ink">
            Simple pricing. Cryptographic trust included.
          </h1>
          <p className="mt-5 text-body-lg text-ink-secondary">
            Every plan includes SHA-256 integrity, RSA-2048 signatures, and public
            verification links. Start free. Upgrade when you need more.
          </p>
        </div>

        <div className="mt-14 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {PLANS.map((plan) => (
            <PlanCard key={plan.key} plan={plan} loggedIn={loggedIn} />
          ))}
        </div>

        <div className="mt-14 sm:mt-16 max-w-3xl mx-auto">
          <h2 className="text-h3 text-ink text-center">Common questions</h2>
          <dl className="mt-8 space-y-6">
            <Faq q="What counts as a signature?">
              Each document you sign with a visible signature counts as one
              signature, regardless of file size.
            </Faq>
            <Faq q="What happens if I hit my monthly limit?">
              You'll be prompted to upgrade. Your existing signed documents
              remain accessible and verifiable.
            </Faq>
            <Faq q="Do my signed documents expire?">
              No. Signed PDFs and their verification links stay valid forever.
              Only your monthly signing quota resets.
            </Faq>
            <Faq q="Can I cancel any time?">
              Yes. Paid plans are month-to-month with no lock-in.
            </Faq>
            <Faq q="Is there an on-premise option?">
              Enterprise customers can deploy ZamTrust in their own
              infrastructure. Contact us to discuss.
            </Faq>
          </dl>
        </div>

        <div className="mt-16 text-center">
          <p className="text-small text-ink-muted">
            All prices in USD. Local currency billing available on request.
          </p>
        </div>
      </main>

      <Footer />
    </div>
  );
}

function PlanCard({ plan, loggedIn }: { plan: PlanInfo; loggedIn: boolean }) {
  return (
    <Card
      className={
        plan.highlight
          ? 'ring-2 ring-accent relative overflow-visible'
          : 'relative'
      }
    >
      {plan.highlight && (
        <span className="absolute -top-2.5 left-4 inline-flex items-center h-5 px-2 rounded-[3px] bg-accent text-white text-caption uppercase tracking-wider font-medium">
          Most popular
        </span>
      )}

      <div className="p-6">
        <h3 className="text-h4 text-ink">{plan.name}</h3>
        <p className="mt-1 text-caption text-ink-muted min-h-[2.2em]">{plan.tagline}</p>

        <div className="mt-5 flex items-baseline gap-1">
          <span className="text-h3 sm:text-h2 text-ink tracking-tight">{plan.priceLabel}</span>
          {plan.priceMonthly > 0 && (
            <span className="text-small text-ink-muted">/month</span>
          )}
        </div>

        <ul className="mt-6 space-y-2">
          {plan.features.map((f) => (
            <li key={f} className="flex items-start gap-2 text-small">
              <span
                aria-hidden="true"
                className="flex-shrink-0 w-4 h-4 rounded-full bg-success-subtle text-success grid place-items-center mt-0.5"
              >
                <svg width="8" height="8" viewBox="0 0 10 10" fill="none">
                  <path
                    d="M1.5 5.5l2 2 5-5"
                    stroke="currentColor"
                    strokeWidth="1.75"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </span>
              <span className="text-ink-secondary">{f}</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="px-6 pb-6">
        {plan.key === 'FREE' ? (
          loggedIn ? (
            <ButtonLink to="/app" variant="secondary" className="w-full">
              Go to dashboard
            </ButtonLink>
          ) : (
            <ButtonLink to="/register" variant="secondary" className="w-full">
              {plan.cta}
            </ButtonLink>
          )
        ) : plan.key === 'ENTERPRISE' ? (
          <a
            href="mailto:sales@zamtrust.dev"
            className="inline-flex items-center justify-center w-full h-10 px-4 rounded-btn border border-border bg-surface text-small font-medium text-ink hover:border-border-strong transition-colors"
          >
            {plan.cta}
          </a>
        ) : loggedIn ? (
          <ButtonLink
            to={`/app/upgrade?plan=${plan.key}`}
            className="w-full"
          >
            {plan.cta}
          </ButtonLink>
        ) : (
          <ButtonLink to="/register" className="w-full">
            {plan.cta}
          </ButtonLink>
        )}
      </div>
    </Card>
  );
}

function Faq({ q, children }: { q: string; children: React.ReactNode }) {
  return (
    <div>
      <dt className="text-body font-medium text-ink">{q}</dt>
      <dd className="mt-1.5 text-body text-ink-secondary">{children}</dd>
    </div>
  );
}

function Header({ loggedIn }: { loggedIn: boolean }) {
  return (
    <header className="sticky top-0 z-40 bg-surface/85 backdrop-blur-md border-b border-border">
      <div className="max-w-wide mx-auto px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 no-underline">
          <span
            aria-hidden="true"
            className="grid place-items-center w-7 h-7 rounded-btn bg-ink text-white font-semibold text-small"
          >
            Z
          </span>
          <span className="text-body font-semibold text-ink tracking-tight">
            ZamTrust
          </span>
        </Link>
        <div className="flex items-center gap-3">
          {loggedIn ? (
            <ButtonLink to="/app" size="sm" variant="secondary">
              Dashboard
            </ButtonLink>
          ) : (
            <>
              <Link to="/login" className="text-small text-ink-secondary hover:text-ink transition-colors">
                Sign in
              </Link>
              <ButtonLink to="/register" size="sm">
                Get started
              </ButtonLink>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

function Footer() {
  return (
    <footer className="border-t border-border mt-20 py-8">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 text-center">
        <p className="text-caption text-ink-muted">
          ZamTrust · Cryptographic document trust
        </p>
      </div>
    </footer>
  );
}
