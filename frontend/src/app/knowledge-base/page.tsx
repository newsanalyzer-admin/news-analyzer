'use client';

import Link from 'next/link';
import { Building2, Users, Users2, ChevronRight } from 'lucide-react';
import { cn } from '@/lib/utils';

/**
 * Category card configuration
 */
interface CategoryCardProps {
  title: string;
  description: string;
  href: string;
  icon: React.ReactNode;
  count?: number;
}

/**
 * Category card component for KB landing page
 */
function CategoryCard({ title, description, href, icon, count }: CategoryCardProps) {
  return (
    <Link
      href={href}
      className={cn(
        'group flex flex-col p-6 rounded-lg border bg-card',
        'hover:border-primary hover:shadow-md transition-all',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'
      )}
    >
      <div className="flex items-start justify-between mb-4">
        <div className="p-3 rounded-lg bg-primary/10 text-primary">
          {icon}
        </div>
        <ChevronRight className="h-5 w-5 text-muted-foreground group-hover:text-primary transition-colors" />
      </div>
      <h3 className="text-lg font-semibold mb-2 group-hover:text-primary transition-colors">
        {title}
      </h3>
      <p className="text-sm text-muted-foreground mb-4 flex-grow">
        {description}
      </p>
      {count !== undefined && (
        <p className="text-xs text-muted-foreground">
          {count.toLocaleString()} records
        </p>
      )}
    </Link>
  );
}

/**
 * Knowledge Base landing page.
 * Shows top-level categories for exploring authoritative reference data.
 */
export default function KnowledgeBasePage() {
  const categories: CategoryCardProps[] = [
    {
      title: 'U.S. Federal Government',
      description: 'Explore the structure of the federal government including executive departments, agencies, congressional committees, and judicial courts.',
      href: '/knowledge-base/government',
      icon: <Building2 className="h-6 w-6" />,
    },
    {
      title: 'People',
      description: 'Browse federal judges, congressional members, and executive appointees with their positions and affiliations.',
      href: '/knowledge-base/people',
      icon: <Users className="h-6 w-6" />,
    },
    {
      title: 'Committees',
      description: 'View congressional committees and subcommittees from the Senate, House, and joint committees.',
      href: '/knowledge-base/committees',
      icon: <Users2 className="h-6 w-6" />,
    },
  ];

  return (
    <div className="container py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Knowledge Base</h1>
        <p className="text-muted-foreground">
          Explore authoritative reference data about the U.S. federal government,
          key personnel, and congressional committees.
        </p>
      </div>

      {/* Category Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {categories.map((category) => (
          <CategoryCard key={category.href} {...category} />
        ))}
      </div>
    </div>
  );
}
