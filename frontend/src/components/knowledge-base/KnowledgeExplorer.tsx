'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Settings, FileText } from 'lucide-react';
import { cn } from '@/lib/utils';
import { getEntityTypeConfig } from '@/lib/config/entityTypes';
import { EntityTypeSelector } from './EntityTypeSelector';
import { ViewModeSelector } from './ViewModeSelector';
import { SearchBar } from './SearchBar';
import { KBBreadcrumbs } from './KBBreadcrumbs';

interface KnowledgeExplorerProps {
  children: React.ReactNode;
  isMobileMenuOpen?: boolean;
  onCloseMobileMenu?: () => void;
  className?: string;
}

/**
 * Main layout shell for the Knowledge Explorer.
 * Contains the header with EntityTypeSelector and ViewModeSelector,
 * and a content area that renders child pages.
 */
export function KnowledgeExplorer({
  children,
  isMobileMenuOpen = false,
  onCloseMobileMenu,
  className,
}: KnowledgeExplorerProps) {
  const pathname = usePathname();

  // Extract entity type from pathname (e.g., /knowledge-base/organizations -> organizations)
  const pathParts = pathname.split('/');
  const entityTypeFromPath = pathParts.length >= 3 ? pathParts[2] : null;
  const entityConfig = entityTypeFromPath ? getEntityTypeConfig(entityTypeFromPath) : null;

  return (
    <div className={cn('flex flex-col min-h-screen', className)}>
      {/* Header */}
      <header className="sticky top-0 z-20 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container py-4">
          {/* Title row */}
          <div className="flex items-center justify-between mb-4">
            <Link
              href="/knowledge-base"
              className="text-xl font-semibold hover:text-primary transition-colors"
            >
              Knowledge Base
            </Link>
            <Link
              href="/article-analyzer"
              className={cn(
                'flex items-center gap-2 rounded-md py-2 px-3 text-sm font-medium',
                'hover:bg-accent hover:text-accent-foreground transition-colors'
              )}
              title="Article Analyzer"
            >
              <FileText className="h-4 w-4" />
              <span className="hidden sm:inline">Article Analyzer</span>
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

          {/* Selectors row */}
          <div className="flex flex-col sm:flex-row sm:items-center gap-3">
            <EntityTypeSelector onNavigate={onCloseMobileMenu} />
            <ViewModeSelector />
            {/* SearchBar - only shown when an entity type is selected */}
            {entityConfig && (
              <SearchBar
                entityType={entityConfig.id}
                entityLabel={entityConfig.label}
                className="w-full sm:w-auto sm:ml-auto sm:min-w-[280px] md:min-w-[320px]"
              />
            )}
          </div>
        </div>
      </header>

      {/* Breadcrumbs */}
      <KBBreadcrumbs className="container pt-4" />

      {/* Content area */}
      <main className="flex-1">
        {children}
      </main>
    </div>
  );
}
