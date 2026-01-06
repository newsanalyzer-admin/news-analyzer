'use client';

import Link from 'next/link';
import { Briefcase, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * Specialized Courts page.
 *
 * Educational page about specialized federal courts with specific
 * subject matter jurisdiction.
 */
export default function SpecializedCourtsPage() {
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
          <div className="p-3 rounded-lg bg-purple-500/10 text-purple-600 dark:text-purple-400">
            <Briefcase className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Specialized Federal Courts</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          In addition to district courts and courts of appeals, Congress has created
          several specialized courts with jurisdiction over specific types of cases.
          These courts handle matters requiring technical expertise or special procedures.
        </p>

        {/* Bankruptcy Courts */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Bankruptcy Courts</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              Each of the 94 federal judicial districts has a bankruptcy court as a unit
              of the district court. Bankruptcy judges are appointed by the courts of
              appeals for 14-year terms.
            </p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <h3 className="font-medium text-foreground mb-2">Types of Bankruptcy</h3>
                <ul className="list-disc list-inside space-y-1">
                  <li>Chapter 7: Liquidation</li>
                  <li>Chapter 11: Business reorganization</li>
                  <li>Chapter 13: Individual debt adjustment</li>
                  <li>Chapter 12: Family farmer/fisherman</li>
                </ul>
              </div>
              <div>
                <h3 className="font-medium text-foreground mb-2">Caseload</h3>
                <p>
                  Bankruptcy courts handle hundreds of thousands of cases annually,
                  making them among the busiest federal courts.
                </p>
              </div>
            </div>
            <a
              href="https://www.uscourts.gov/services-forms/bankruptcy"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline text-sm"
            >
              Learn more about Bankruptcy Courts
              <ExternalLink className="h-3 w-3" />
            </a>
          </div>
        </div>

        {/* U.S. Tax Court */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">U.S. Tax Court</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              An independent court where taxpayers can dispute tax deficiencies determined
              by the IRS before paying the disputed amount. Unlike other courts, you can
              challenge the IRS here without paying first.
            </p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <h3 className="font-medium text-foreground mb-2">Composition</h3>
                <p>
                  19 presidentially appointed judges serving 15-year terms. Senior judges
                  and special trial judges assist with the caseload.
                </p>
              </div>
              <div>
                <h3 className="font-medium text-foreground mb-2">Location</h3>
                <p>
                  Based in Washington, D.C., but judges travel to cities nationwide to
                  conduct trials closer to taxpayers.
                </p>
              </div>
            </div>
            <a
              href="https://www.ustaxcourt.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline text-sm"
            >
              U.S. Tax Court website
              <ExternalLink className="h-3 w-3" />
            </a>
          </div>
        </div>

        {/* Court of Federal Claims */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">U.S. Court of Federal Claims</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              Hears monetary claims against the United States government. If you believe
              the federal government owes you money, this is often where you file suit.
            </p>
            <div className="text-sm">
              <h3 className="font-medium text-foreground mb-2">Types of Cases</h3>
              <ul className="list-disc list-inside space-y-1">
                <li>Government contract disputes</li>
                <li>Tax refund suits</li>
                <li>Constitutional takings claims</li>
                <li>Civilian and military pay disputes</li>
                <li>Patent and copyright claims against the U.S.</li>
                <li>Vaccine injury compensation</li>
              </ul>
            </div>
            <a
              href="https://www.uscfc.uscourts.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline text-sm"
            >
              Court of Federal Claims website
              <ExternalLink className="h-3 w-3" />
            </a>
          </div>
        </div>

        {/* Court of International Trade */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">U.S. Court of International Trade</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              Has exclusive jurisdiction over civil actions arising from customs and
              international trade laws. Based in New York City but can hear cases anywhere.
            </p>
            <div className="text-sm">
              <h3 className="font-medium text-foreground mb-2">Jurisdiction Includes</h3>
              <ul className="list-disc list-inside space-y-1">
                <li>Customs duties and import restrictions</li>
                <li>Antidumping and countervailing duty determinations</li>
                <li>Trade adjustment assistance eligibility</li>
                <li>Customs broker licensing</li>
              </ul>
            </div>
            <a
              href="https://www.cit.uscourts.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline text-sm"
            >
              Court of International Trade website
              <ExternalLink className="h-3 w-3" />
            </a>
          </div>
        </div>

        {/* Military Courts */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Military Courts</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              The military justice system operates separately under the Uniform Code of
              Military Justice (UCMJ). Appeals from courts-martial can eventually reach
              the civilian federal courts.
            </p>
            <div className="text-sm">
              <h3 className="font-medium text-foreground mb-2">Court of Appeals for the Armed Forces</h3>
              <p>
                The highest military appellate court, composed of five civilian judges
                appointed by the President for 15-year terms. Reviews decisions from
                the service branch courts of criminal appeals.
              </p>
            </div>
            <a
              href="https://www.armfor.uscourts.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline text-sm"
            >
              Court of Appeals for the Armed Forces
              <ExternalLink className="h-3 w-3" />
            </a>
          </div>
        </div>

        {/* Other Specialized Courts */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Other Specialized Courts</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground text-sm">
            <div>
              <h3 className="font-medium text-foreground mb-2">Court of Appeals for Veterans Claims</h3>
              <p>
                Reviews decisions of the Board of Veterans&apos; Appeals regarding
                veterans&apos; benefits.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Foreign Intelligence Surveillance Court</h3>
              <p>
                A secret court that reviews applications for surveillance warrants under
                FISA. Operates with classified proceedings.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Alien Terrorist Removal Court</h3>
              <p>
                Reviews applications to deport suspected alien terrorists using
                classified evidence.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Judicial Panel on Multidistrict Litigation</h3>
              <p>
                Transfers and consolidates civil cases pending in different districts
                for coordinated pretrial proceedings.
              </p>
            </div>
          </div>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.uscourts.gov/about-federal-courts/court-role-and-structure"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Federal Court Structure
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.fjc.gov/history/courts"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              History of the Federal Courts
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
