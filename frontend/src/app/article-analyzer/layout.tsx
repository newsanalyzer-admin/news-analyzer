'use client';

import { Suspense } from 'react';
import { SidebarLayout } from '@/components/layout';
import { useArticleAnalyzerSidebarStore } from '@/stores/articleAnalyzerSidebarStore';
import { ArticleAnalyzerSidebar, AAContentHeader, AABreadcrumbs } from '@/components/article-analyzer';

/**
 * Loading skeleton for Article Analyzer section
 */
function ArticleAnalyzerSkeleton() {
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
              <div className="h-10 bg-muted rounded w-32" />
              <div className="h-10 bg-muted rounded w-32" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * Main content wrapper for Article Analyzer that uses SidebarLayout
 */
function ArticleAnalyzerContent({ children }: { children: React.ReactNode }) {
  const store = useArticleAnalyzerSidebarStore();

  return (
    <SidebarLayout
      sidebar={<ArticleAnalyzerSidebar className="h-full" />}
      sectionTitle="Article Analyzer"
      store={store}
    >
      {/* Content header with navigation links */}
      <AAContentHeader onNavigate={store.closeMobile} />

      {/* Breadcrumbs */}
      <AABreadcrumbs className="container pt-4" />

      {/* Page content */}
      <main className="flex-1">
        {children}
      </main>
    </SidebarLayout>
  );
}

/**
 * Layout for the Article Analyzer section.
 * Provides sidebar navigation and content header with navigation links.
 */
export default function ArticleAnalyzerLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <Suspense fallback={<ArticleAnalyzerSkeleton />}>
      <ArticleAnalyzerContent>{children}</ArticleAnalyzerContent>
    </Suspense>
  );
}
