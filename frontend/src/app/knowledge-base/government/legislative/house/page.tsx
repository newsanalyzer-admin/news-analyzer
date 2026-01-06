'use client';

import Link from 'next/link';
import { Home, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * House of Representatives page.
 *
 * Educational page about the U.S. House of Representatives,
 * the lower chamber of Congress.
 */
export default function HouseOfRepresentativesPage() {
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
          <div className="p-3 rounded-lg bg-indigo-500/10 text-indigo-600 dark:text-indigo-400">
            <Home className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">House of Representatives</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          The United States House of Representatives is the lower chamber of Congress.
          It is often referred to as &ldquo;the People&apos;s House&rdquo; because its members
          are directly elected by the citizens and apportioned by population.
        </p>

        {/* Composition */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Composition</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Voting Members</h3>
              <p>
                435 Representatives apportioned among the 50 states based on population
                as determined by the decennial census. Each state is guaranteed at least
                one Representative.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Non-Voting Delegates</h3>
              <p>
                6 non-voting members represent the District of Columbia, Puerto Rico,
                Guam, American Samoa, the U.S. Virgin Islands, and the Northern Mariana Islands.
              </p>
            </div>
          </div>
        </div>

        {/* Leadership */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Leadership</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Speaker of the House</h3>
              <p>
                The presiding officer of the House, elected by the members. The Speaker is
                second in the presidential line of succession (after the Vice President) and
                is the most powerful member of the House.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Majority Leader</h3>
              <p>
                The floor leader of the majority party, responsible for scheduling legislation
                and maintaining party unity on votes.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Minority Leader</h3>
              <p>
                The floor leader of the minority party, responsible for organizing opposition
                and developing alternative legislative strategies.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Party Whips</h3>
              <p>
                The Majority and Minority Whips are responsible for counting votes, ensuring
                party members attend important votes, and communicating the party&apos;s position.
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
                <li>At least 25 years old</li>
                <li>U.S. citizen for at least 7 years</li>
                <li>Resident of the state represented</li>
              </ul>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Term of Office</h3>
              <p>
                2 years, with no limit on the number of terms. All 435 seats are up for
                election every two years in November of even-numbered years.
              </p>
            </div>
          </div>
        </div>

        {/* Exclusive Powers */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Exclusive Powers</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              The Constitution grants certain powers exclusively to the House of Representatives:
            </p>
            <ul className="list-disc list-inside space-y-2">
              <li>
                <strong>Revenue Bills:</strong> All bills for raising revenue must originate
                in the House (though the Senate may propose amendments).
              </li>
              <li>
                <strong>Impeachment:</strong> The House has the sole power to impeach federal
                officials, including the President. A simple majority vote is required.
              </li>
              <li>
                <strong>Contingent Election:</strong> If no presidential candidate receives a
                majority of electoral votes, the House elects the President with each state
                delegation receiving one vote.
              </li>
            </ul>
          </div>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.house.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              House.gov
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://clerk.house.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Office of the Clerk
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.house.gov/representatives"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Find Your Representative
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
