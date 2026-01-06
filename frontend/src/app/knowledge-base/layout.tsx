'use client';

import { Suspense } from 'react';
import { SidebarLayout } from '@/components/layout';
import { usePublicSidebarStore } from '@/stores/publicSidebarStore';
import { PublicSidebar } from '@/components/public/PublicSidebar';

/**
 * Loading skeleton for Knowledge Base section
 */
function KnowledgeBaseSkeleton() {
  return (
    <div className="min-h-screen bg-background animate-pulse">
      {/* Mobile header skeleton */}
      <div className="md:hidden fixed top-0 left-0 right-0 z-40 h-14 bg-card border-b border-border" />
      {/* Content skeleton */}
      <div className="pt-14 md:pt-0 md:ml-64">
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
    </div>
  );
}

/**
 * Main content wrapper for Knowledge Base that uses SidebarLayout
 */
function KnowledgeBaseContent({ children }: { children: React.ReactNode }) {
  const store = usePublicSidebarStore();

  return (
    <SidebarLayout
      sidebar={<PublicSidebar className="h-full" />}
      sectionTitle="Knowledge Base"
      store={store}
    >
      {/* Page content */}
      <main className="flex-1">
        {children}
      </main>
    </SidebarLayout>
  );
}

/**
 * Layout for the Knowledge Base section.
 * Provides sidebar navigation and content header with selectors.
 */
export default function KnowledgeBaseLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <Suspense fallback={<KnowledgeBaseSkeleton />}>
      <KnowledgeBaseContent>{children}</KnowledgeBaseContent>
    </Suspense>
  );
}
