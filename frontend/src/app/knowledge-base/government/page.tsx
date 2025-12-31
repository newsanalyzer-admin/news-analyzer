'use client';

import Link from 'next/link';
import { Scale, Landmark, Building, ChevronRight, ArrowLeft } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';

/**
 * Branch card configuration
 */
interface BranchCardProps {
  title: string;
  description: string;
  href: string;
  icon: React.ReactNode;
  color: 'blue' | 'purple' | 'amber';
}

/**
 * Color variants for branch cards
 */
const colorVariants = {
  blue: {
    bg: 'bg-blue-500/10',
    text: 'text-blue-600 dark:text-blue-400',
    border: 'hover:border-blue-500',
  },
  purple: {
    bg: 'bg-purple-500/10',
    text: 'text-purple-600 dark:text-purple-400',
    border: 'hover:border-purple-500',
  },
  amber: {
    bg: 'bg-amber-500/10',
    text: 'text-amber-600 dark:text-amber-400',
    border: 'hover:border-amber-500',
  },
};

/**
 * Branch card component
 */
function BranchCard({ title, description, href, icon, color }: BranchCardProps) {
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
 * Government section landing page.
 * Shows the three branches of the U.S. Federal Government.
 */
export default function GovernmentPage() {
  const branches: BranchCardProps[] = [
    {
      title: 'Executive Branch',
      description: 'The President, Executive Office, Cabinet departments, independent agencies, and government corporations.',
      href: '/knowledge-base/government/executive',
      icon: <Building className="h-6 w-6" />,
      color: 'blue',
    },
    {
      title: 'Legislative Branch',
      description: 'The Senate, House of Representatives, congressional committees, and support agencies like CBO and GAO.',
      href: '/knowledge-base/government/legislative',
      icon: <Landmark className="h-6 w-6" />,
      color: 'purple',
    },
    {
      title: 'Judicial Branch',
      description: 'The Supreme Court, Courts of Appeals, District Courts, and specialized federal courts.',
      href: '/knowledge-base/government/judicial',
      icon: <Scale className="h-6 w-6" />,
      color: 'amber',
    },
  ];

  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-6">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Knowledge Base
          </Link>
        </Button>
      </div>

      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">U.S. Federal Government</h1>
        <p className="text-muted-foreground">
          Explore the structure of the federal government organized by its three
          constitutional branches: Executive, Legislative, and Judicial.
        </p>
      </div>

      {/* Branch Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {branches.map((branch) => (
          <BranchCard key={branch.href} {...branch} />
        ))}
      </div>

      {/* Additional info */}
      <div className="mt-8 p-4 rounded-lg bg-muted/50 border">
        <p className="text-sm text-muted-foreground">
          <strong>Note:</strong> You can also browse all government organizations
          in a flat list view from the{' '}
          <Link href="/knowledge-base/organizations" className="text-primary hover:underline">
            Organizations
          </Link>{' '}
          section.
        </p>
      </div>
    </div>
  );
}
