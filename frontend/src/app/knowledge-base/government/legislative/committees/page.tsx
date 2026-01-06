'use client';

import Link from 'next/link';
import { Users2, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * Committees page.
 *
 * Educational page about Congressional committees - standing, select,
 * and joint committees that handle legislation and oversight.
 */
export default function CommitteesPage() {
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
          <div className="p-3 rounded-lg bg-purple-500/10 text-purple-600 dark:text-purple-400">
            <Users2 className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Congressional Committees</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          Congressional committees are the workhorses of the legislative process. They review
          bills, conduct oversight hearings, and investigate issues within their jurisdictions.
          Most substantive legislative work occurs in committee before bills reach the floor.
        </p>

        {/* Types of Committees */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Types of Committees</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Standing Committees</h3>
              <p>
                Permanent committees with ongoing legislative jurisdiction over specific policy
                areas. They are the most powerful type of committee and are established in the
                rules of each chamber. Examples include Appropriations, Armed Services, Judiciary,
                and Ways and Means (House) / Finance (Senate).
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Select or Special Committees</h3>
              <p>
                Temporary committees created to investigate specific issues or address matters
                that cross committee jurisdictions. They may or may not have legislative authority.
                Examples include the Select Committee on Intelligence and various investigative committees.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Joint Committees</h3>
              <p>
                Committees with membership from both the House and Senate. They typically handle
                administrative matters or conduct studies rather than report legislation.
                Examples include the Joint Committee on Taxation and Joint Economic Committee.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Conference Committees</h3>
              <p>
                Temporary joint committees formed to resolve differences between House and Senate
                versions of a bill. Members are appointed from the relevant standing committees
                in each chamber.
              </p>
            </div>
          </div>
        </div>

        {/* Committee Structure */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Committee Structure</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Leadership</h3>
              <p>
                Each committee is led by a Chair (from the majority party) and a Ranking Member
                (from the minority party). The Chair has significant power over the committee&apos;s
                agenda, including which bills are considered and when hearings are held.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Subcommittees</h3>
              <p>
                Most standing committees are divided into subcommittees that focus on specific
                aspects of the committee&apos;s jurisdiction. Subcommittees often hold initial
                hearings and markups before bills are considered by the full committee.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Staff</h3>
              <p>
                Committees employ professional staff who provide policy expertise, draft legislation,
                organize hearings, and support members in their legislative work. Majority and
                minority staff typically work separately.
              </p>
            </div>
          </div>
        </div>

        {/* Key Senate Committees */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Key Senate Committees</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground text-sm">
            <ul className="list-disc list-inside space-y-1">
              <li>Appropriations</li>
              <li>Armed Services</li>
              <li>Banking, Housing, and Urban Affairs</li>
              <li>Budget</li>
              <li>Commerce, Science, and Transportation</li>
              <li>Energy and Natural Resources</li>
              <li>Environment and Public Works</li>
              <li>Finance</li>
            </ul>
            <ul className="list-disc list-inside space-y-1">
              <li>Foreign Relations</li>
              <li>Health, Education, Labor, and Pensions</li>
              <li>Homeland Security and Governmental Affairs</li>
              <li>Judiciary</li>
              <li>Rules and Administration</li>
              <li>Small Business and Entrepreneurship</li>
              <li>Veterans&apos; Affairs</li>
              <li>Select Committee on Intelligence</li>
            </ul>
          </div>
        </div>

        {/* Key House Committees */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Key House Committees</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground text-sm">
            <ul className="list-disc list-inside space-y-1">
              <li>Appropriations</li>
              <li>Armed Services</li>
              <li>Budget</li>
              <li>Education and the Workforce</li>
              <li>Energy and Commerce</li>
              <li>Financial Services</li>
              <li>Foreign Affairs</li>
              <li>Homeland Security</li>
              <li>Judiciary</li>
              <li>Natural Resources</li>
            </ul>
            <ul className="list-disc list-inside space-y-1">
              <li>Oversight and Accountability</li>
              <li>Rules</li>
              <li>Science, Space, and Technology</li>
              <li>Small Business</li>
              <li>Transportation and Infrastructure</li>
              <li>Veterans&apos; Affairs</li>
              <li>Ways and Means</li>
              <li>Permanent Select Committee on Intelligence</li>
            </ul>
          </div>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.senate.gov/committees/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Senate Committees
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.house.gov/committees"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              House Committees
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.congress.gov/committees"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              All Committees on Congress.gov
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
