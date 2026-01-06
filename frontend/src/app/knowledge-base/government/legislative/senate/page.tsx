'use client';

import Link from 'next/link';
import { Building2, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * Senate page.
 *
 * Educational page about the U.S. Senate, the upper chamber of Congress.
 */
export default function SenatePage() {
  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government/legislative">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Legislative Branch
          </Link>
        </Button>
      </div>

      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-blue-500/10 text-blue-600 dark:text-blue-400">
            <Building2 className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">United States Senate</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          The United States Senate is the upper chamber of Congress. Often called
          &ldquo;the world&apos;s greatest deliberative body,&rdquo; the Senate is designed
          to give each state equal representation regardless of population.
        </p>

        {/* Composition */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Composition</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Equal Representation</h3>
              <p>
                Each state has exactly 2 Senators, regardless of population. This means
                Wyoming (population ~580,000) has the same Senate representation as
                California (population ~39 million).
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Total Membership</h3>
              <p>
                100 Senators total: 2 from each of the 50 states. This number has remained
                constant since Hawaii became the 50th state in 1959.
              </p>
            </div>
          </div>
        </div>

        {/* Leadership */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Leadership</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">President of the Senate</h3>
              <p>
                The Vice President of the United States serves as President of the Senate
                but may only vote to break a tie. In practice, the Vice President rarely
                presides over daily sessions.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">President Pro Tempore</h3>
              <p>
                The Senator who presides in the Vice President&apos;s absence. By tradition,
                this position goes to the most senior member of the majority party. The
                President Pro Tempore is third in the presidential line of succession.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Majority and Minority Leaders</h3>
              <p>
                The Majority Leader is the most powerful Senator, controlling the floor
                schedule and legislative agenda. The Minority Leader represents the opposition
                party. Both are elected by their respective party caucuses.
              </p>
            </div>
          </div>
        </div>

        {/* Requirements and Terms */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Requirements and Terms</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Qualifications</h3>
              <ul className="list-disc list-inside space-y-1">
                <li>At least 30 years old</li>
                <li>U.S. citizen for at least 9 years</li>
                <li>Resident of the state represented</li>
              </ul>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Term of Office</h3>
              <p>
                6 years, with no limit on the number of terms. Elections are staggered
                so that approximately one-third of Senators are up for election every
                two years (Class I, II, or III).
              </p>
            </div>
          </div>
        </div>

        {/* Exclusive Powers */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Exclusive Powers</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              The Constitution grants certain powers exclusively to the Senate:
            </p>
            <ul className="list-disc list-inside space-y-2">
              <li>
                <strong>Advice and Consent:</strong> The Senate must confirm presidential
                appointments to the Cabinet, federal judiciary (including Supreme Court
                justices), ambassadors, and other key positions.
              </li>
              <li>
                <strong>Treaty Ratification:</strong> Treaties negotiated by the President
                require approval by a two-thirds vote of the Senate.
              </li>
              <li>
                <strong>Impeachment Trials:</strong> When the House impeaches a federal
                official, the Senate conducts the trial. A two-thirds vote is required
                for conviction and removal from office.
              </li>
            </ul>
          </div>
        </div>

        {/* The Filibuster */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">The Filibuster</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              Unlike the House, the Senate allows unlimited debate unless 60 Senators
              vote for &ldquo;cloture&rdquo; to end discussion. This means a determined minority
              of 41 Senators can block most legislation through a filibuster.
            </p>
            <p>
              The 60-vote threshold effectively requires bipartisan cooperation for
              major legislation, making the Senate a more deliberative and slower-moving
              body than the House.
            </p>
          </div>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.senate.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Senate.gov
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.senate.gov/senators/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Find Your Senators
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.senate.gov/about/origins-foundations.htm"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              History of the Senate
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
