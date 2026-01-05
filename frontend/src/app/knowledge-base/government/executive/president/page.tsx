'use client';

import Link from 'next/link';
import { Crown, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * President of the United States page (UI-6.3).
 *
 * Educational page about the office of the President with
 * constitutional references and links to official resources.
 */
export default function PresidentPage() {
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
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          The President of the United States is the head of state and head of government,
          as well as the Commander-in-Chief of the United States Armed Forces. The presidency
          is the highest political office in the United States by influence and recognition.
        </p>

        {/* Constitutional Powers */}
        <div className="bg-card border rounded-lg p-6 mb-6">
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
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Term and Eligibility</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
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
          </div>
        </div>
      </div>
    </div>
  );
}
