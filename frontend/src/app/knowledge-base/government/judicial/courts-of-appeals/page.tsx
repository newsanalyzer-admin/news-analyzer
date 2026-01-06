'use client';

import Link from 'next/link';
import { Building, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * Courts of Appeals page.
 *
 * Educational page about the U.S. Courts of Appeals (Circuit Courts),
 * the intermediate appellate courts in the federal system.
 */
export default function CourtsOfAppealsPage() {
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
          <div className="p-3 rounded-lg bg-indigo-500/10 text-indigo-600 dark:text-indigo-400">
            <Building className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">U.S. Courts of Appeals</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          The United States Courts of Appeals are the intermediate appellate courts
          of the federal judiciary. They review decisions from district courts and
          federal administrative agencies, serving as the final word on most federal cases.
        </p>

        {/* Structure */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Structure</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">13 Circuits</h3>
              <p>
                There are 13 Courts of Appeals: 12 regional circuits (numbered 1-11 plus
                the D.C. Circuit) and the Federal Circuit, which has nationwide jurisdiction
                over specific subject matters.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Panel Review</h3>
              <p>
                Cases are typically heard by panels of three judges. In rare cases,
                all judges in a circuit may hear a case together (&ldquo;en banc&rdquo;).
              </p>
            </div>
          </div>
        </div>

        {/* The Regional Circuits */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">The Regional Circuits</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground text-sm">
            <ul className="space-y-2">
              <li><strong>1st Circuit:</strong> ME, MA, NH, RI, PR</li>
              <li><strong>2nd Circuit:</strong> CT, NY, VT</li>
              <li><strong>3rd Circuit:</strong> DE, NJ, PA, VI</li>
              <li><strong>4th Circuit:</strong> MD, NC, SC, VA, WV</li>
              <li><strong>5th Circuit:</strong> LA, MS, TX</li>
              <li><strong>6th Circuit:</strong> KY, MI, OH, TN</li>
            </ul>
            <ul className="space-y-2">
              <li><strong>7th Circuit:</strong> IL, IN, WI</li>
              <li><strong>8th Circuit:</strong> AR, IA, MN, MO, NE, ND, SD</li>
              <li><strong>9th Circuit:</strong> AK, AZ, CA, HI, ID, MT, NV, OR, WA, Guam, N. Mariana Islands</li>
              <li><strong>10th Circuit:</strong> CO, KS, NM, OK, UT, WY</li>
              <li><strong>11th Circuit:</strong> AL, FL, GA</li>
              <li><strong>D.C. Circuit:</strong> Washington, D.C.</li>
            </ul>
          </div>
        </div>

        {/* Special Circuits */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Special Circuits</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">D.C. Circuit</h3>
              <p>
                Often considered the second most important court after the Supreme Court.
                It has special jurisdiction over many federal agency decisions and cases
                involving separation of powers.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Federal Circuit</h3>
              <p>
                Has nationwide jurisdiction over specific subject areas including patents,
                international trade, government contracts, and veterans&apos; benefits.
                Based in Washington, D.C.
              </p>
            </div>
          </div>
        </div>

        {/* Appeals Process */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">The Appeals Process</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              Appeals courts do not retry cases or hear new evidence. They review whether
              the lower court correctly applied the law and followed proper procedures.
            </p>
            <ol className="list-decimal list-inside space-y-2 text-sm">
              <li>Notice of appeal filed within strict deadlines</li>
              <li>Record from lower court transmitted</li>
              <li>Written briefs submitted by both parties</li>
              <li>Oral argument (if granted)</li>
              <li>Three-judge panel issues decision</li>
              <li>Losing party may petition for en banc review or Supreme Court certiorari</li>
            </ol>
          </div>
        </div>

        {/* Judges */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Circuit Judges</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Appointment</h3>
              <p className="text-sm">
                Circuit judges are nominated by the President and confirmed by the Senate.
                Like Supreme Court justices, they serve lifetime appointments during
                &ldquo;good behaviour.&rdquo;
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Numbers</h3>
              <p className="text-sm">
                There are currently 179 authorized circuit judgeships. The number varies
                by circuit based on caseload. The 9th Circuit is the largest with 29 judges.
              </p>
            </div>
          </div>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.uscourts.gov/about-federal-courts/court-role-and-structure/about-us-courts-appeals"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              About Courts of Appeals
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.uscourts.gov/about-federal-courts/federal-courts-public/court-website-links"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Find Your Circuit
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
