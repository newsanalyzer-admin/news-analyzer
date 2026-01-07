'use client';

import Link from 'next/link';
import { Crown, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs, PresidentCard, PresidencyTable } from '@/components/knowledge-base';
import { useCurrentPresidency, useAllPresidencies } from '@/hooks/usePresidencySync';

/**
 * President of the United States page (KB-1.5).
 *
 * Educational page about the office of the President with
 * current president card, historical table, and constitutional references.
 */
export default function PresidentPage() {
  const { data: currentPresidency, isLoading: currentLoading } = useCurrentPresidency();
  const { data: allPresidencies, isLoading: allLoading } = useAllPresidencies();

  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government/executive">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Executive Branch
          </Link>
        </Button>
      </div>

      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-blue-500/10 text-blue-600 dark:text-blue-400">
            <Crown className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">President of the United States</h1>
        </div>
        <p className="text-lg text-muted-foreground max-w-3xl">
          The President of the United States is the head of state and head of government,
          as well as the Commander-in-Chief of the United States Armed Forces.
        </p>
      </div>

      {/* Current President Card */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-4">Current President</h2>
        <PresidentCard presidency={currentPresidency} isLoading={currentLoading} />
      </section>

      {/* Historical Presidencies Table */}
      <section className="mb-10">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold">All Presidencies</h2>
          {allPresidencies && (
            <p className="text-sm text-muted-foreground">
              {allPresidencies.length} presidencies
            </p>
          )}
        </div>
        <p className="text-muted-foreground mb-4">
          Click on any row to view details including Vice Presidents, Chiefs of Staff, and term information.
        </p>
        <PresidencyTable
          presidencies={allPresidencies || []}
          isLoading={allLoading}
        />
      </section>

      {/* Educational Content */}
      <section className="space-y-6">
        {/* Constitutional Powers */}
        <div className="bg-card border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Constitutional Powers</h2>
          <p className="text-muted-foreground mb-4">
            Article II of the Constitution vests the executive power in the President. Key powers include:
          </p>
          <ul className="list-disc list-inside text-muted-foreground space-y-2">
            <li>Serve as Commander-in-Chief of the Armed Forces</li>
            <li>Grant reprieves and pardons for federal offenses</li>
            <li>Make treaties with the advice and consent of the Senate</li>
            <li>Nominate federal judges, ambassadors, and Cabinet members</li>
            <li>Deliver the State of the Union address to Congress</li>
            <li>Sign or veto legislation passed by Congress</li>
            <li>Issue executive orders directing executive branch operations</li>
          </ul>
        </div>

        {/* Term and Eligibility */}
        <div className="bg-card border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Term and Eligibility</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Term Length</h3>
              <p>Four years, with a maximum of two terms (22nd Amendment)</p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Requirements</h3>
              <ul className="list-disc list-inside space-y-1">
                <li>Natural-born citizen of the United States</li>
                <li>At least 35 years of age</li>
                <li>Resident of the U.S. for at least 14 years</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Non-consecutive Terms Note */}
        <div className="bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800 rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4 text-amber-800 dark:text-amber-200">
            Non-Consecutive Terms
          </h2>
          <p className="text-amber-700 dark:text-amber-300 mb-3">
            Two presidents have served non-consecutive terms:
          </p>
          <ul className="list-disc list-inside text-amber-700 dark:text-amber-300 space-y-2">
            <li>
              <strong>Grover Cleveland</strong> served as both the 22nd (1885-1889) and
              24th (1893-1897) President
            </li>
            <li>
              <strong>Donald Trump</strong> served as both the 45th (2017-2021) and
              47th (2025-present) President
            </li>
          </ul>
          <p className="text-amber-600 dark:text-amber-400 text-sm mt-3">
            Note: Each term is counted as a separate presidency in our data model.
          </p>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.whitehouse.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              The White House
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://constitution.congress.gov/constitution/article-2/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              U.S. Constitution Article II
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.archives.gov/federal-register/executive-orders"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Executive Orders Archive
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </section>
    </div>
  );
}
