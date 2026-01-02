'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ChevronRight, Home } from 'lucide-react';
import { cn } from '@/lib/utils';

interface AABreadcrumbsProps {
  className?: string;
}

const segmentLabels: Record<string, string> = {
  'article-analyzer': 'Article Analyzer',
  'analyze': 'Analyze Article',
  'articles': 'Articles',
  'entities': 'Entities',
};

/**
 * Breadcrumb navigation for Article Analyzer section.
 * Shows hierarchical path from home to current location.
 */
export function AABreadcrumbs({ className }: AABreadcrumbsProps) {
  const pathname = usePathname();

  if (!pathname.startsWith('/article-analyzer')) {
    return null;
  }

  const segments = pathname.split('/').filter(Boolean);

  // Don't show breadcrumbs if at root
  if (segments.length <= 1) {
    return null;
  }

  const breadcrumbs: { label: string; href: string }[] = [];
  let currentPath = '';

  for (const segment of segments) {
    currentPath += `/${segment}`;
    const label = segmentLabels[segment];
    if (label) {
      breadcrumbs.push({ label, href: currentPath });
    }
  }

  if (breadcrumbs.length <= 1) {
    return null;
  }

  return (
    <nav aria-label="Breadcrumb" className={cn('flex items-center text-sm', className)}>
      <ol className="flex items-center flex-wrap gap-1">
        <li>
          <Link
            href="/"
            className="text-muted-foreground hover:text-foreground transition-colors p-1"
            aria-label="Home"
          >
            <Home className="h-4 w-4" />
          </Link>
        </li>
        <li aria-hidden="true">
          <ChevronRight className="h-4 w-4 text-muted-foreground" />
        </li>

        {breadcrumbs.map((crumb, index) => {
          const isLast = index === breadcrumbs.length - 1;

          return (
            <li key={crumb.href} className="flex items-center gap-1">
              {index > 0 && (
                <ChevronRight
                  className="h-4 w-4 text-muted-foreground flex-shrink-0"
                  aria-hidden="true"
                />
              )}
              {isLast ? (
                <span
                  className="font-medium text-foreground truncate max-w-[200px]"
                  aria-current="page"
                >
                  {crumb.label}
                </span>
              ) : (
                <Link
                  href={crumb.href}
                  className="text-muted-foreground hover:text-foreground transition-colors truncate max-w-[200px]"
                >
                  {crumb.label}
                </Link>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
