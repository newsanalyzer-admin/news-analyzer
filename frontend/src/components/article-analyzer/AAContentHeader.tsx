'use client';

import Link from 'next/link';
import { Database, Settings } from 'lucide-react';
import { cn } from '@/lib/utils';

interface AAContentHeaderProps {
  /** Callback when navigation occurs (for mobile sidebar close) */
  onNavigate?: () => void;
  /** Optional additional className */
  className?: string;
}

/**
 * AAContentHeader - Content header for Article Analyzer pages.
 * Contains navigation links to Knowledge Base and Admin.
 * Positioned within the main content area (not in sidebar).
 */
export function AAContentHeader({ onNavigate, className }: AAContentHeaderProps) {
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
        <div className="flex items-center justify-end gap-2">
          <Link
            href="/knowledge-base"
            onClick={onNavigate}
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
            onClick={onNavigate}
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
    </header>
  );
}
