'use client';

import Link from 'next/link';
import {
  Building,
  ChevronRight,
  ArrowLeft,
  Crown,
  UserCircle,
  Briefcase,
  Building2,
  Factory,
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
  color: 'blue' | 'indigo' | 'violet' | 'purple' | 'fuchsia' | 'pink';
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
  fuchsia: {
    bg: 'bg-fuchsia-500/10',
    text: 'text-fuchsia-600 dark:text-fuchsia-400',
    border: 'hover:border-fuchsia-500',
  },
  pink: {
    bg: 'bg-pink-500/10',
    text: 'text-pink-600 dark:text-pink-400',
    border: 'hover:border-pink-500',
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
 * Executive Branch hub page (UI-6.2).
 *
 * Provides educational overview of the Executive Branch and navigation
 * cards to its 6 sub-sections as defined by constitutional structure.
 */
export default function ExecutiveBranchPage() {
  const subSections: SubSectionCardProps[] = [
    {
      title: 'President of the United States',
      description: 'The head of state and government, Commander in Chief of the Armed Forces, with executive power vested by Article II.',
      href: '/knowledge-base/government/executive/president',
      icon: <Crown className="h-6 w-6" />,
      color: 'blue',
    },
    {
      title: 'Vice President of the United States',
      description: 'First in presidential succession, President of the Senate, and advisor to the President.',
      href: '/knowledge-base/government/executive/vice-president',
      icon: <UserCircle className="h-6 w-6" />,
      color: 'indigo',
    },
    {
      title: 'Executive Office of the President',
      description: 'Key advisory offices including the Office of Management and Budget, National Security Council, and Council of Economic Advisers.',
      href: '/knowledge-base/government/executive/eop',
      icon: <Building className="h-6 w-6" />,
      color: 'violet',
    },
    {
      title: 'Cabinet Departments',
      description: 'The 15 executive departments led by Secretaries, from State and Treasury to Homeland Security.',
      href: '/knowledge-base/government/executive/cabinet',
      icon: <Briefcase className="h-6 w-6" />,
      color: 'purple',
    },
    {
      title: 'Independent Agencies',
      description: 'Federal agencies operating outside Cabinet departments, including EPA, NASA, CIA, and regulatory commissions.',
      href: '/knowledge-base/government/executive/independent-agencies',
      icon: <Building2 className="h-6 w-6" />,
      color: 'fuchsia',
    },
    {
      title: 'Government Corporations',
      description: 'Federally chartered corporations providing public services, including USPS, Amtrak, TVA, and FDIC.',
      href: '/knowledge-base/government/executive/corporations',
      icon: <Factory className="h-6 w-6" />,
      color: 'pink',
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
            <Building className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Executive Branch</h1>
        </div>

        {/* Educational Description */}
        <div className="prose prose-sm dark:prose-invert max-w-none">
          <p className="text-muted-foreground text-lg mb-4">
            The Executive Branch is responsible for implementing and enforcing the laws
            written by Congress. It is headed by the President, who serves as both the
            head of state and head of government of the United States.
          </p>

          {/* Article II Reference */}
          <blockquote className="border-l-4 border-blue-500 pl-4 py-2 my-4 bg-muted/30 rounded-r-lg">
            <p className="text-muted-foreground italic mb-2">
              &ldquo;The executive Power shall be vested in a President of the United States
              of America.&rdquo;
            </p>
            <footer className="text-sm">
              &mdash; U.S. Constitution,{' '}
              <a
                href="https://constitution.congress.gov/constitution/article-2/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline inline-flex items-center gap-1"
              >
                Article II, Section 1
                <ExternalLink className="h-3 w-3" />
              </a>
            </footer>
          </blockquote>

          <p className="text-muted-foreground">
            The Executive Branch includes the President, Vice President, Executive Office
            of the President, 15 Cabinet-level departments, numerous independent agencies,
            and several government corporations. Together, these entities employ millions
            of federal workers and manage the day-to-day operations of the federal government.
          </p>
        </div>
      </div>

      {/* Sub-section Navigation Cards */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Explore the Executive Branch</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {subSections.map((section) => (
            <SubSectionCard key={section.href} {...section} />
          ))}
        </div>
      </div>

      {/* Additional context */}
      <div className="p-4 rounded-lg bg-muted/50 border">
        <p className="text-sm text-muted-foreground">
          <strong>Note:</strong> The structure shown here reflects the constitutional
          and organizational hierarchy of the Executive Branch. For a complete list
          of all executive organizations, visit the{' '}
          <Link href="/knowledge-base/organizations" className="text-primary hover:underline">
            Organizations
          </Link>{' '}
          section.
        </p>
      </div>
    </div>
  );
}
