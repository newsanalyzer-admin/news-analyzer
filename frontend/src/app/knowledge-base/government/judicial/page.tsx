'use client';

import Link from 'next/link';
import {
  Scale,
  ChevronRight,
  ArrowLeft,
  Gavel,
  Building,
  MapPin,
  Briefcase,
  ExternalLink,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs } from '@/components/knowledge-base';

/**
 * Sub-section card configuration
 */
interface SubSectionCardProps {
  title: string;
  description: string;
  href: string;
  icon: React.ReactNode;
  color: 'blue' | 'indigo' | 'violet' | 'purple';
}

/**
 * Color variants for sub-section cards
 */
const colorVariants = {
  blue: {
    bg: 'bg-blue-500/10',
    text: 'text-blue-600 dark:text-blue-400',
    border: 'hover:border-blue-500',
  },
  indigo: {
    bg: 'bg-indigo-500/10',
    text: 'text-indigo-600 dark:text-indigo-400',
    border: 'hover:border-indigo-500',
  },
  violet: {
    bg: 'bg-violet-500/10',
    text: 'text-violet-600 dark:text-violet-400',
    border: 'hover:border-violet-500',
  },
  purple: {
    bg: 'bg-purple-500/10',
    text: 'text-purple-600 dark:text-purple-400',
    border: 'hover:border-purple-500',
  },
};

/**
 * Sub-section card component
 */
function SubSectionCard({ title, description, href, icon, color }: SubSectionCardProps) {
  const colors = colorVariants[color];

  return (
    <Link
      href={href}
      className={cn(
        'group flex flex-col p-6 rounded-lg border bg-card',
        colors.border,
        'hover:shadow-md transition-all',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'
      )}
    >
      <div className="flex items-start justify-between mb-4">
        <div className={cn('p-3 rounded-lg', colors.bg, colors.text)}>
          {icon}
        </div>
        <ChevronRight className="h-5 w-5 text-muted-foreground group-hover:text-foreground transition-colors" />
      </div>
      <h3 className="text-lg font-semibold mb-2 group-hover:text-foreground transition-colors">
        {title}
      </h3>
      <p className="text-sm text-muted-foreground flex-grow">
        {description}
      </p>
    </Link>
  );
}

/**
 * Judicial Branch hub page.
 *
 * Provides educational overview of the Federal Judiciary and navigation
 * cards to its sub-sections: Supreme Court, Courts of Appeals, District Courts,
 * and Specialized Courts.
 */
export default function JudicialBranchPage() {
  const subSections: SubSectionCardProps[] = [
    {
      title: 'Supreme Court',
      description: 'The highest court in the United States, with ultimate appellate jurisdiction over all federal and state court cases involving federal law.',
      href: '/knowledge-base/government/judicial/supreme-court',
      icon: <Gavel className="h-6 w-6" />,
      color: 'blue',
    },
    {
      title: 'Courts of Appeals',
      description: 'The 13 circuit courts that hear appeals from district courts and review decisions of federal administrative agencies.',
      href: '/knowledge-base/government/judicial/courts-of-appeals',
      icon: <Building className="h-6 w-6" />,
      color: 'indigo',
    },
    {
      title: 'District Courts',
      description: 'The 94 federal trial courts where most federal cases are initially filed and tried before a judge or jury.',
      href: '/knowledge-base/government/judicial/district-courts',
      icon: <MapPin className="h-6 w-6" />,
      color: 'violet',
    },
    {
      title: 'Specialized Courts',
      description: 'Courts with specific subject matter jurisdiction including Bankruptcy Courts, Tax Court, and the Court of Federal Claims.',
      href: '/knowledge-base/government/judicial/specialized-courts',
      icon: <Briefcase className="h-6 w-6" />,
      color: 'purple',
    },
  ];

  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to U.S. Federal Government
          </Link>
        </Button>
      </div>

      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-blue-500/10 text-blue-600 dark:text-blue-400">
            <Scale className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Judicial Branch</h1>
        </div>

        {/* Educational Description */}
        <div className="prose prose-sm dark:prose-invert max-w-none">
          <p className="text-muted-foreground text-lg mb-4">
            The Judicial Branch interprets the meaning of laws, applies laws to individual cases,
            and decides if laws violate the Constitution. It is headed by the Supreme Court and
            includes all federal courts established by Congress.
          </p>

          {/* Article III Reference */}
          <blockquote className="border-l-4 border-blue-500 pl-4 py-2 my-4 bg-muted/30 rounded-r-lg">
            <p className="text-muted-foreground italic mb-2">
              &ldquo;The judicial Power of the United States, shall be vested in one supreme Court,
              and in such inferior Courts as the Congress may from time to time ordain and establish.&rdquo;
            </p>
            <footer className="text-sm">
              &mdash; U.S. Constitution,{' '}
              <a
                href="https://constitution.congress.gov/constitution/article-3/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline inline-flex items-center gap-1"
              >
                Article III, Section 1
                <ExternalLink className="h-3 w-3" />
              </a>
            </footer>
          </blockquote>
        </div>
      </div>

      {/* Sub-section Navigation Cards */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Explore the Federal Courts</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {subSections.map((section) => (
            <SubSectionCard key={section.href} {...section} />
          ))}
        </div>
      </div>

      {/* Judicial Independence */}
      <div className="bg-card border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Judicial Independence</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
          <div>
            <h3 className="font-medium text-foreground mb-2">Lifetime Appointment</h3>
            <p className="text-sm">
              Federal judges are appointed for life and can only be removed through
              impeachment. This protects them from political pressure and allows them
              to make decisions based solely on the law.
            </p>
          </div>
          <div>
            <h3 className="font-medium text-foreground mb-2">Salary Protection</h3>
            <p className="text-sm">
              The Constitution prohibits reducing judicial salaries during a judge&apos;s
              term, further insulating judges from political retaliation for unpopular
              decisions.
            </p>
          </div>
        </div>
      </div>

      {/* Judicial Review */}
      <div className="bg-card border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Judicial Review</h2>
        <div className="space-y-3 text-muted-foreground">
          <p>
            The power of judicial review—the ability to declare laws unconstitutional—was
            established in the landmark case <em>Marbury v. Madison</em> (1803). This power
            makes the federal judiciary a check on both the legislative and executive branches.
          </p>
          <p className="text-sm">
            When the Supreme Court rules that a law or executive action violates the Constitution,
            that decision is final unless overturned by a constitutional amendment or a later
            Supreme Court decision.
          </p>
        </div>
      </div>

      {/* External Links */}
      <div className="bg-muted/50 border rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
        <div className="flex flex-wrap gap-4">
          <a
            href="https://www.uscourts.gov/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            United States Courts
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.supremecourt.gov/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            Supreme Court
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.fjc.gov/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            Federal Judicial Center
            <ExternalLink className="h-4 w-4" />
          </a>
        </div>
      </div>
    </div>
  );
}
