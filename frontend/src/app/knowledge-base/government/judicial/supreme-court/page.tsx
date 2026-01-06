'use client';

import Link from 'next/link';
import { Gavel, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * Supreme Court page.
 *
 * Educational page about the Supreme Court of the United States,
 * the highest court in the federal judiciary.
 */
export default function SupremeCourtPage() {
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
          <div className="p-3 rounded-lg bg-blue-500/10 text-blue-600 dark:text-blue-400">
            <Gavel className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Supreme Court of the United States</h1>
        </div>
      </div>

      {/* Content */}
      <div className="prose prose-sm dark:prose-invert max-w-none">
        <p className="text-lg text-muted-foreground mb-6">
          The Supreme Court is the highest court in the federal judiciary and the final
          arbiter of constitutional questions. Its decisions are binding on all other
          courts and establish precedents that shape American law.
        </p>

        {/* Composition */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Composition</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Nine Justices</h3>
              <p>
                The Court consists of the Chief Justice of the United States and eight
                Associate Justices. Congress sets the number of justices; it has been
                nine since 1869.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Lifetime Tenure</h3>
              <p>
                Justices serve &ldquo;during good Behaviour,&rdquo; effectively for life.
                They can only be removed through impeachment by the House and conviction
                by the Senate.
              </p>
            </div>
          </div>
        </div>

        {/* Appointment Process */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Appointment Process</h2>
          <div className="space-y-3 text-muted-foreground">
            <ol className="list-decimal list-inside space-y-2">
              <li>A vacancy occurs through death, retirement, or resignation</li>
              <li>The President nominates a candidate</li>
              <li>The Senate Judiciary Committee holds confirmation hearings</li>
              <li>The full Senate votes on confirmation (simple majority required)</li>
              <li>The confirmed justice takes the judicial oath</li>
            </ol>
            <p className="text-sm mt-4">
              There are no constitutional requirements for Supreme Court justices—no age,
              citizenship, or legal experience requirements. By tradition, all justices
              have been lawyers.
            </p>
          </div>
        </div>

        {/* Jurisdiction */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Jurisdiction</h2>
          <div className="space-y-4 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Original Jurisdiction</h3>
              <p>
                The Court has original jurisdiction (hears cases first) in disputes between
                states, cases involving ambassadors, and cases where a state is a party.
                These cases are rare.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Appellate Jurisdiction</h3>
              <p>
                The vast majority of cases reach the Court on appeal. The Court has
                discretionary review and chooses which cases to hear through the
                &ldquo;writ of certiorari&rdquo; process.
              </p>
            </div>
          </div>
        </div>

        {/* How Cases Are Decided */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">How Cases Are Decided</h2>
          <div className="space-y-3 text-muted-foreground">
            <div>
              <h3 className="font-medium text-foreground mb-2">Certiorari</h3>
              <p>
                The Court receives approximately 7,000-8,000 petitions per year but only
                hears about 100-150 cases. Four justices must vote to hear a case
                (&ldquo;Rule of Four&rdquo;).
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Oral Arguments</h3>
              <p>
                Each side typically gets 30 minutes to present their case. Justices
                frequently interrupt with questions. Arguments are open to the public.
              </p>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">Conference and Opinions</h3>
              <p>
                Justices meet privately to discuss and vote on cases. The senior justice
                in the majority assigns who writes the opinion. Dissenting justices may
                write separate opinions.
              </p>
            </div>
          </div>
        </div>

        {/* The Term */}
        <div className="bg-card border rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">The Supreme Court Term</h2>
          <p className="text-muted-foreground">
            The Court&apos;s term begins the first Monday in October and typically runs
            through late June or early July. Oral arguments are held October through April,
            and opinions are usually released from late May through the end of June.
          </p>
        </div>

        {/* External Links */}
        <div className="bg-muted/50 border rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
          <div className="flex flex-wrap gap-4">
            <a
              href="https://www.supremecourt.gov/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              SupremeCourt.gov
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.supremecourt.gov/about/biographies.aspx"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Current Justices
              <ExternalLink className="h-4 w-4" />
            </a>
            <a
              href="https://www.oyez.org/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-primary hover:underline"
            >
              Oyez (Oral Arguments)
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
