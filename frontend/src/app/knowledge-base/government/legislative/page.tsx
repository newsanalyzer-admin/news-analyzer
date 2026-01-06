'use client';

import Link from 'next/link';
import {
  Landmark,
  ChevronRight,
  ArrowLeft,
  Building2,
  Home,
  HeartHandshake,
  Users2,
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
 * Legislative Branch hub page.
 *
 * Provides educational overview of Congress (the Legislative Branch) and navigation
 * cards to its sub-sections: Senate, House of Representatives, Support Services, and Committees.
 */
export default function LegislativeBranchPage() {
  const subSections: SubSectionCardProps[] = [
    {
      title: 'Senate',
      description: 'The upper chamber of Congress with 100 Senators, 2 from each state, serving 6-year terms.',
      href: '/knowledge-base/government/legislative/senate',
      icon: <Building2 className="h-6 w-6" />,
      color: 'blue',
    },
    {
      title: 'House of Representatives',
      description: 'The lower chamber of Congress with 435 members apportioned by state population, led by the Speaker of the House.',
      href: '/knowledge-base/government/legislative/house',
      icon: <Home className="h-6 w-6" />,
      color: 'indigo',
    },
    {
      title: 'Support Services',
      description: 'Congressional support agencies including the Library of Congress, Government Accountability Office, and Congressional Budget Office.',
      href: '/knowledge-base/government/legislative/support-services',
      icon: <HeartHandshake className="h-6 w-6" />,
      color: 'violet',
    },
    {
      title: 'Committees',
      description: 'Standing, select, and joint committees that handle legislation, oversight, and investigations in specialized policy areas.',
      href: '/knowledge-base/government/legislative/committees',
      icon: <Users2 className="h-6 w-6" />,
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
            <Landmark className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Legislative Branch</h1>
        </div>

        {/* Educational Description */}
        <div className="prose prose-sm dark:prose-invert max-w-none">
          <p className="text-muted-foreground text-lg mb-4">
            The United States Congress is the bicameral legislature of the federal government,
            consisting of two chambers: the Senate and the House of Representatives. It is
            the only branch of government that can make new laws.
          </p>

          {/* Article I Reference */}
          <blockquote className="border-l-4 border-blue-500 pl-4 py-2 my-4 bg-muted/30 rounded-r-lg">
            <p className="text-muted-foreground italic mb-2">
              &ldquo;All legislative Powers herein granted shall be vested in a Congress
              of the United States, which shall consist of a Senate and House of Representatives.&rdquo;
            </p>
            <footer className="text-sm">
              &mdash; U.S. Constitution,{' '}
              <a
                href="https://constitution.congress.gov/constitution/article-1/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline inline-flex items-center gap-1"
              >
                Article I, Section 1
                <ExternalLink className="h-3 w-3" />
              </a>
            </footer>
          </blockquote>
        </div>
      </div>

      {/* Sub-section Navigation Cards */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Explore Congress</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {subSections.map((section) => (
            <SubSectionCard key={section.href} {...section} />
          ))}
        </div>
      </div>

      {/* Powers of Congress */}
      <div className="bg-card border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Powers of Congress</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-muted-foreground">
          <div>
            <h3 className="font-medium text-foreground mb-2">Enumerated Powers</h3>
            <ul className="list-disc list-inside space-y-1 text-sm">
              <li>Levy and collect taxes</li>
              <li>Borrow money on U.S. credit</li>
              <li>Regulate commerce</li>
              <li>Establish naturalization rules</li>
              <li>Coin money</li>
              <li>Establish post offices</li>
              <li>Declare war</li>
              <li>Raise and support armies</li>
            </ul>
          </div>
          <div>
            <h3 className="font-medium text-foreground mb-2">Exclusive Senate Powers</h3>
            <ul className="list-disc list-inside space-y-1 text-sm mb-4">
              <li>Confirm presidential appointments</li>
              <li>Ratify treaties (two-thirds vote)</li>
              <li>Try impeachment cases</li>
            </ul>
            <h3 className="font-medium text-foreground mb-2">Exclusive House Powers</h3>
            <ul className="list-disc list-inside space-y-1 text-sm">
              <li>Originate revenue bills</li>
              <li>Impeach federal officials</li>
              <li>Elect President if no Electoral College majority</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Legislative Process */}
      <div className="bg-card border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">The Legislative Process</h2>
        <div className="space-y-3 text-muted-foreground">
          <p>
            For a bill to become law, it must be passed by both chambers in identical form
            and then signed by the President (or passed over a presidential veto by a
            two-thirds vote in each chamber).
          </p>
          <ol className="list-decimal list-inside space-y-2 ml-4 text-sm">
            <li>A bill is introduced in either chamber</li>
            <li>The bill is referred to the appropriate committee(s)</li>
            <li>The committee holds hearings and may amend the bill</li>
            <li>The bill is debated and voted on by the full chamber</li>
            <li>If passed, the bill goes to the other chamber for consideration</li>
            <li>Differences between versions are resolved in conference committee</li>
            <li>Both chambers vote on the final version</li>
            <li>The bill is sent to the President for signature or veto</li>
          </ol>
        </div>
      </div>

      {/* External Links */}
      <div className="bg-muted/50 border rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
        <div className="flex flex-wrap gap-4">
          <a
            href="https://www.congress.gov/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            Congress.gov
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.senate.gov/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            U.S. Senate
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.house.gov/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            U.S. House of Representatives
            <ExternalLink className="h-4 w-4" />
          </a>
        </div>
      </div>
    </div>
  );
}
