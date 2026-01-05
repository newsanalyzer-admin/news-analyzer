'use client';

import Link from 'next/link';
import { UserCircle, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * Vice President of the United States page (UI-6.3).
 *
 * Educational page about the office of the Vice President with
 * constitutional references and information about succession.
 */
export default function VicePresidentPage() {
  return (
    <div className="container py-8">
      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Back link */}
      <div className="mb-6">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government/executive">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Executive Branch
          </Link>
        </Button>
      </div>

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-indigo-500/10 text-indigo-600 dark:text-indigo-400">
            <UserCircle className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Vice President of the United States</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          The Vice President of the United States is the second-highest officer in the
          executive branch, serving as first in the presidential line of succession and
          as the President of the Senate.
        </p>

        {/* Constitutional Roles */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Constitutional Roles</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">President of the Senate</h3>
              <p>
                The Vice President serves as the presiding officer of the Senate but may only
                vote to break a tie. This role is defined in Article I, Section 3 of the Constitution.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Presidential Succession</h3>
              <p>
                First in line to assume the presidency if the President dies, resigns, or is
                removed from office. The 25th Amendment clarifies the succession process.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Advisory Role</h3>
              <p>
                Modern Vice Presidents serve as key advisors to the President and often lead
                important policy initiatives and diplomatic missions.
              </p>
            </div>
          </div>
        </div>

        {/* Term and Eligibility */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Term and Eligibility</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Term Length</h3>
              <p>Four years, elected on the same ticket as the President</p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Requirements</h3>
              <p>Same as the President: natural-born citizen, at least 35 years old,
                resident for 14 years (12th Amendment)</p>
            </div>
          </div>
        </div>

        {/* Historical Note */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Historical Note</h2>
          <p className="text-muted-foreground">
            Nine Vice Presidents have assumed the presidency due to the death or resignation
            of the President. The 25th Amendment (1967) established procedures for filling
            a vice presidential vacancy and for the Vice President to serve as Acting President.
          </p>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.whitehouse.gov/administration/vice-president/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Office of the Vice President
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.senate.gov/about/officers-staff/vice-president.htm"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Vice President of the Senate
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
