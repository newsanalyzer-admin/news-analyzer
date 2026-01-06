'use client';

import Link from 'next/link';
import { HeartHandshake, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * Support Services page.
 *
 * Educational page about the support agencies that serve Congress,
 * including the Library of Congress, GAO, CBO, and others.
 */
export default function SupportServicesPage() {
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
          <div className="p-3 rounded-lg bg-violet-500/10 text-violet-600 dark:text-violet-400">
            <HeartHandshake className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Congressional Support Services</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          Several agencies within the Legislative Branch provide essential support services
          to Congress. These non-partisan agencies offer research, analysis, and operational
          support to help lawmakers make informed decisions.
        </p>

        {/* Library of Congress */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Library of Congress</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              The largest library in the world, serving as the research arm of Congress and
              the de facto national library of the United States. Founded in 1800.
            </p>
            <ul className="list-disc list-inside space-y-1">
              <li>Over 170 million items in its collections</li>
              <li>Houses the Congressional Research Service (CRS)</li>
              <li>Administers the U.S. Copyright Office</li>
              <li>Provides accessible formats for the blind and print-disabled</li>
            </ul>
            <a
              href="https://www.loc.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Visit loc.gov
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>

        {/* Congressional Research Service */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Congressional Research Service (CRS)</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              A division within the Library of Congress that provides non-partisan policy
              analysis exclusively to members of Congress and their staff.
            </p>
            <ul className="list-disc list-inside space-y-1">
              <li>Produces confidential research reports on policy issues</li>
              <li>Provides expert testimony and consultation</li>
              <li>Analyzes pending legislation</li>
              <li>Employs over 600 policy analysts, attorneys, and information specialists</li>
            </ul>
          </div>
        </div>

        {/* Government Accountability Office */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Government Accountability Office (GAO)</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              Often called the &ldquo;congressional watchdog,&rdquo; the GAO audits and evaluates
              federal programs and expenditures on behalf of Congress.
            </p>
            <ul className="list-disc list-inside space-y-1">
              <li>Investigates how taxpayer dollars are spent</li>
              <li>Provides legal opinions on appropriations</li>
              <li>Reports on government fraud, waste, and abuse</li>
              <li>Led by the Comptroller General, appointed to a 15-year term</li>
            </ul>
            <a
              href="https://www.gao.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Visit gao.gov
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>

        {/* Congressional Budget Office */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Congressional Budget Office (CBO)</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              Provides non-partisan budgetary and economic analysis to Congress. Created in 1974
              to give Congress independent expertise on fiscal matters.
            </p>
            <ul className="list-disc list-inside space-y-1">
              <li>&ldquo;Scores&rdquo; legislation for its budgetary impact</li>
              <li>Produces economic forecasts</li>
              <li>Analyzes the President&apos;s budget proposals</li>
              <li>Reports on long-term budget outlooks and debt projections</li>
            </ul>
            <a
              href="https://www.cbo.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Visit cbo.gov
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>

        {/* Architect of the Capitol */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Architect of the Capitol</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              Responsible for the maintenance, operation, and preservation of the United States
              Capitol Complex, including the Capitol Building and surrounding grounds.
            </p>
            <ul className="list-disc list-inside space-y-1">
              <li>Maintains the Capitol Building, House and Senate office buildings</li>
              <li>Manages the Capitol Grounds and Botanic Garden</li>
              <li>Preserves historic art and artifacts</li>
              <li>Oversees the Capitol Visitor Center</li>
            </ul>
            <a
              href="https://www.aoc.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Visit aoc.gov
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>

        {/* Government Publishing Office */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Government Publishing Office (GPO)</h2>
          <div className="space-y-3 text-muted-foreground">
            <p>
              The official printer for the federal government, responsible for producing and
              distributing government publications.
            </p>
            <ul className="list-disc list-inside space-y-1">
              <li>Publishes the Congressional Record</li>
              <li>Prints bills, laws, and federal regulations</li>
              <li>Operates GovInfo.gov for public access to documents</li>
              <li>Maintains the Federal Depository Library Program</li>
            </ul>
            <a
              href="https://www.gpo.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Visit gpo.gov
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Additional Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.govinfo.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              GovInfo.gov
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.usbg.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              U.S. Botanic Garden
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
