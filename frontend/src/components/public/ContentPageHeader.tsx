'use client';

import Link from 'next/link';
import { ChevronRight } from 'lucide-react';
import { cn } from '@/lib/utils';

/**
 * Breadcrumb item for navigation trail.
 */
export interface BreadcrumbItem {
  /** Display label */
  label: string;
  /** Navigation href (optional - last item typically has no href) */
  href?: string;
}

/**
 * Props for ContentPageHeader component.
 */
export interface ContentPageHeaderProps {
  /** Page title displayed as h1 */
  title: string;
  /** Educational description explaining the page content */
  description: string;
  /** Optional breadcrumb navigation trail */
  breadcrumbs?: BreadcrumbItem[];
  /** Additional CSS classes */
  className?: string;
}

/**
 * ContentPageHeader Component
 *
 * Provides consistent header styling for all factbase content pages.
 * Includes title, educational description, and optional breadcrumb navigation.
 */
export function ContentPageHeader({
  title,
  description,
  breadcrumbs,
  className,
}: ContentPageHeaderProps) {
  return (
    <div className={cn('mb-8', className)}>
      {/* Breadcrumb Navigation */}
      {breadcrumbs && breadcrumbs.length > 0 && (
        <nav
          aria-label="Breadcrumb"
          className="mb-4 flex items-center text-sm text-muted-foreground"
        >
          <ol className="flex items-center gap-1">
            {breadcrumbs.map((item, index) => {
              const isLast = index === breadcrumbs.length - 1;

              return (
                <li key={item.label} className="flex items-center gap-1">
                  {index > 0 && (
                    <ChevronRight className="h-4 w-4 text-muted-foreground/50" aria-hidden="true" />
                  )}
                  {item.href && !isLast ? (
                    <Link
                      href={item.href}
                      className="hover:text-foreground transition-colors"
                    >
                      {item.label}
                    </Link>
                  ) : (
                    <span
                      className={cn(isLast && 'text-foreground font-medium')}
                      aria-current={isLast ? 'page' : undefined}
                    >
                      {item.label}
                    </span>
                  )}
                </li>
              );
            })}
          </ol>
        </nav>
      )}

      {/* Title */}
      <h1 className="text-3xl font-bold tracking-tight mb-4">{title}</h1>

      {/* Description */}
      <p className="text-muted-foreground text-lg leading-relaxed max-w-3xl">
        {description}
      </p>
    </div>
  );
}
