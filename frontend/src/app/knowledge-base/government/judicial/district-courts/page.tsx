'use client';

import Link from 'next/link';
import { MapPin, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * District Courts page.
 *
 * Educational page about the U.S. District Courts,
 * the federal trial courts of general jurisdiction.
 */
export default function DistrictCourtsPage() {
  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government/judicial">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Judicial Branch
          </Link>
        </Button>
      </div>

      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-violet-500/10 text-violet-600 dark:text-violet-400">
            <MapPin className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">U.S. District Courts</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          The United States District Courts are the general trial courts of the federal
          judiciary. They have jurisdiction to hear nearly all categories of federal cases,
          including both civil and criminal matters.
        </p>

        {/* Structure */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Structure</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">94 Districts</h3>
              <p>
                There are 94 federal judicial districts, including at least one in each
                state, the District of Columbia, and Puerto Rico. Larger states are
                divided into multiple districts (e.g., Northern, Southern, Eastern, Western).
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">District Judges</h3>
              <p>
                Each district has at least two judges. There are currently 673 authorized
                district judgeships. Judges are nominated by the President and confirmed
                by the Senate for lifetime appointments.
              </p>
            </div>
          </div>
        </div>

        {/* Jurisdiction */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Jurisdiction</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Federal Question</h3>
              <p>
                Cases arising under the Constitution, federal laws, or treaties. Examples
                include civil rights violations, federal crimes, antitrust cases, and
                intellectual property disputes.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Diversity Jurisdiction</h3>
              <p>
                Civil cases between citizens of different states (or between a U.S. citizen
                and a foreign citizen) where the amount in controversy exceeds $75,000.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Admiralty and Maritime</h3>
              <p>
                Cases involving navigable waters, shipping, and maritime commerce.
              </p>
            </div>
          </div>
        </div>

        {/* Types of Cases */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Types of Cases</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Criminal Cases</h3>
              <ul className="list-disc list-inside space-y-1 text-sm">
                <li>Drug trafficking</li>
                <li>White-collar crime (fraud, embezzlement)</li>
                <li>Immigration violations</li>
                <li>Weapons offenses</li>
                <li>Crimes on federal property</li>
                <li>Terrorism and national security</li>
              </ul>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Civil Cases</h3>
              <ul className="list-disc list-inside space-y-1 text-sm">
                <li>Civil rights claims</li>
                <li>Employment discrimination</li>
                <li>Patent and trademark disputes</li>
                <li>Environmental enforcement</li>
                <li>Social Security appeals</li>
                <li>Bankruptcy (handled by specialized courts)</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Court Personnel */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Court Personnel</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">District Judges</h3>
              <p className="text-sm">
                Presidentially appointed, lifetime tenure. Handle trials, rule on motions,
                and manage their caseloads.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Magistrate Judges</h3>
              <p className="text-sm">
                Appointed by district judges for 8-year terms. Handle preliminary matters
                in criminal cases, some civil cases with party consent, and help manage
                heavy caseloads.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Clerk of Court</h3>
              <p className="text-sm">
                Manages court administration, maintains records, and oversees case filing
                and scheduling.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">U.S. Attorneys</h3>
              <p className="text-sm">
                Federal prosecutors appointed by the President for each district. They
                represent the United States in criminal prosecutions and civil litigation.
              </p>
            </div>
          </div>
        </div>

        {/* Trial Process */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">The Trial Process</h2>
          <p className="text-muted-foreground mb-3">
            District courts are where federal trials take place. Cases may be decided by
            a judge alone (bench trial) or by a jury. The Seventh Amendment guarantees
            the right to a jury trial in civil cases exceeding $20.
          </p>
          <p className="text-muted-foreground text-sm">
            Most cases never go to trial—approximately 97% of federal criminal cases end
            in plea agreements, and most civil cases settle before trial.
          </p>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.uscourts.gov/about-federal-courts/court-role-and-structure/about-us-district-courts"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              About District Courts
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.uscourts.gov/about-federal-courts/federal-courts-public/court-website-links"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Find Your District
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://pacer.uscourts.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              PACER (Case Records)
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
