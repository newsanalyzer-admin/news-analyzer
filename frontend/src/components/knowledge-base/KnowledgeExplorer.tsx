'use client';

import Link from 'next/link';
import { Settings } from 'lucide-react';
import { cn } from '@/lib/utils';
import { EntityTypeSelector } from './EntityTypeSelector';
import { ViewModeSelector } from './ViewModeSelector';

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
            {/* SearchBar placeholder - will be added in UI-2.5 */}
            <div className="hidden md:block md:ml-auto">
              {/* Search bar will go here */}
            </div>
          </div>
        </div>
      </header>

      {/* Content area */}
      <main className="flex-1">
        <div className="container py-6">
          {children}
        </div>
      </main>
    </div>
  );
}
