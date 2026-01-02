'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { FileText } from 'lucide-react';
import { cn } from '@/lib/utils';
import { getEntityTypeConfig } from '@/lib/config/entityTypes';
import { EntityTypeSelector } from './EntityTypeSelector';
import { ViewModeSelector } from './ViewModeSelector';
import { SearchBar } from './SearchBar';

interface KBContentHeaderProps {
  /** Callback when navigation occurs (for mobile sidebar close) */
  onNavigate?: () => void;
  /** Optional additional className */
  className?: string;
}

/**
 * KBContentHeader - Content header for Knowledge Base pages.
 * Contains navigation link, entity type/view mode selectors, and search bar.
 * Positioned within the main content area (not in sidebar).
 */
export function KBContentHeader({ onNavigate, className }: KBContentHeaderProps) {
  const pathname = usePathname();

  // Extract entity type from pathname (e.g., /knowledge-base/organizations -> organizations)
  const pathParts = pathname.split('/');
  const entityTypeFromPath = pathParts.length >= 3 ? pathParts[2] : null;
  const entityConfig = entityTypeFromPath ? getEntityTypeConfig(entityTypeFromPath) : null;

  return (
    <header
      className={cn(
        'sticky top-0 md:top-0 z-10 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60',
        // On mobile, account for the SidebarLayout mobile header (h-14 = 56px, so top-14)
        'top-14',
        className
      )}
    >
      <div className="container py-4">
        {/* Navigation row */}
        <div className="flex items-center justify-end mb-4">
          <Link
            href="/article-analyzer"
            onClick={onNavigate}
            className={cn(
              'flex items-center gap-2 rounded-md py-2 px-3 text-sm font-medium',
              'hover:bg-accent hover:text-accent-foreground transition-colors'
            )}
            title="Article Analyzer"
          >
            <FileText className="h-4 w-4" />
            <span className="hidden sm:inline">Article Analyzer</span>
          </Link>
        </div>

        {/* Selectors row */}
        <div className="flex flex-col sm:flex-row sm:items-center gap-3">
          <EntityTypeSelector onNavigate={onNavigate} />
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
  );
}
