'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Settings, FileText, List, Database, ChevronRight, Home } from 'lucide-react';
import { cn } from '@/lib/utils';

interface NavItem {
  label: string;
  href: string;
  icon: React.ReactNode;
  disabled?: boolean;
  description?: string;
}

const navItems: NavItem[] = [
  {
    label: 'Analyze Article',
    href: '/article-analyzer/analyze',
    icon: <FileText className="h-4 w-4" />,
    disabled: true,
    description: 'Coming soon - Phase 4',
  },
  {
    label: 'Articles',
    href: '/article-analyzer/articles',
    icon: <List className="h-4 w-4" />,
    description: 'Browse analyzed articles',
  },
  {
    label: 'Entities',
    href: '/article-analyzer/entities',
    icon: <Database className="h-4 w-4" />,
    description: 'Browse extracted entities',
  },
];

interface ArticleAnalyzerShellProps {
  children: React.ReactNode;
  className?: string;
}

/**
 * ArticleAnalyzerShell - Main layout shell for the Article Analyzer section.
 * Provides consistent header navigation and sidebar for analysis workflows.
 */
export function ArticleAnalyzerShell({
  children,
  className,
}: ArticleAnalyzerShellProps) {
  const pathname = usePathname();

  return (
    <div className={cn('flex flex-col min-h-screen', className)}>
      {/* Header */}
      <header className="sticky top-0 z-20 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container py-4">
          {/* Title row */}
          <div className="flex items-center justify-between mb-4">
            <Link
              href="/article-analyzer"
              className="text-xl font-semibold hover:text-primary transition-colors"
            >
              Article Analyzer
            </Link>
            <div className="flex items-center gap-2">
              <Link
                href="/knowledge-base"
                className={cn(
                  'flex items-center gap-2 rounded-md py-2 px-3 text-sm font-medium',
                  'hover:bg-accent hover:text-accent-foreground transition-colors'
                )}
                title="Knowledge Base"
              >
                <Database className="h-4 w-4" />
                <span className="hidden sm:inline">Knowledge Base</span>
              </Link>
              <Link
                href="/admin"
                className={cn(
                  'flex items-center gap-2 rounded-md py-2 px-3 text-sm font-medium',
                  'hover:bg-accent hover:text-accent-foreground transition-colors'
                )}
                title="Admin"
              >
                <Settings className="h-4 w-4" />
                <span className="hidden sm:inline">Admin</span>
              </Link>
            </div>
          </div>

          {/* Navigation row */}
          <nav aria-label="Article Analyzer navigation">
            <ul className="flex flex-wrap gap-2">
              {navItems.map((item) => {
                const isActive = pathname === item.href ||
                  (item.href !== '/article-analyzer' && pathname.startsWith(item.href));

                return (
                  <li key={item.href}>
                    {item.disabled ? (
                      <span
                        className={cn(
                          'inline-flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium',
                          'text-muted-foreground cursor-not-allowed opacity-60'
                        )}
                        title={item.description}
                      >
                        {item.icon}
                        {item.label}
                        <span className="text-xs bg-muted px-1.5 py-0.5 rounded">Soon</span>
                      </span>
                    ) : (
                      <Link
                        href={item.href}
                        className={cn(
                          'inline-flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
                          isActive
                            ? 'bg-primary text-primary-foreground'
                            : 'hover:bg-accent hover:text-accent-foreground'
                        )}
                        aria-current={isActive ? 'page' : undefined}
                      >
                        {item.icon}
                        {item.label}
                      </Link>
                    )}
                  </li>
                );
              })}
            </ul>
          </nav>
        </div>
      </header>

      {/* Breadcrumbs */}
      <AABreadcrumbs className="container pt-4" />

      {/* Content area */}
      <main className="flex-1">
        {children}
      </main>
    </div>
  );
}

/**
 * Breadcrumb navigation for Article Analyzer section
 */
interface AABreadcrumbsProps {
  className?: string;
}

const segmentLabels: Record<string, string> = {
  'article-analyzer': 'Article Analyzer',
  'analyze': 'Analyze Article',
  'articles': 'Articles',
  'entities': 'Entities',
};

function AABreadcrumbs({ className }: AABreadcrumbsProps) {
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
