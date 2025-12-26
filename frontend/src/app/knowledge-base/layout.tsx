'use client';

import { Menu, X } from 'lucide-react';
import { useState, Suspense } from 'react';
import { cn } from '@/lib/utils';
import { KnowledgeExplorer } from '@/components/knowledge-base';

function KnowledgeExplorerSkeleton() {
  return (
    <div className="min-h-screen bg-background animate-pulse">
      <div className="border-b">
        <div className="container py-4">
          <div className="h-8 bg-muted rounded w-48 mb-4" />
          <div className="flex gap-3">
            <div className="h-10 bg-muted rounded w-64" />
            <div className="h-10 bg-muted rounded w-32" />
          </div>
        </div>
      </div>
    </div>
  );
}

function KnowledgeBaseContent({ children }: { children: React.ReactNode }) {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  return (
    <div className="min-h-screen bg-background">
      {/* Mobile menu button */}
      <button
        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        className={cn(
          'fixed top-4 left-4 z-50 p-2 rounded-md md:hidden',
          'bg-background border border-border shadow-sm',
          'hover:bg-accent transition-colors',
          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'
        )}
        aria-label={isMobileMenuOpen ? 'Close menu' : 'Open menu'}
      >
        {isMobileMenuOpen ? (
          <X className="h-5 w-5" />
        ) : (
          <Menu className="h-5 w-5" />
        )}
      </button>

      {/* Mobile backdrop */}
      {isMobileMenuOpen && (
        <div
          className="fixed inset-0 z-30 bg-black/50 md:hidden"
          onClick={() => setIsMobileMenuOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Main content area */}
      <KnowledgeExplorer
        isMobileMenuOpen={isMobileMenuOpen}
        onCloseMobileMenu={() => setIsMobileMenuOpen(false)}
      >
        {children}
      </KnowledgeExplorer>
    </div>
  );
}

export default function KnowledgeBaseLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <Suspense fallback={<KnowledgeExplorerSkeleton />}>
      <KnowledgeBaseContent>{children}</KnowledgeBaseContent>
    </Suspense>
  );
}
