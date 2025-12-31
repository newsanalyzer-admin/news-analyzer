'use client';

import { Suspense } from 'react';
import { ArticleAnalyzerShell } from '@/components/article-analyzer';

/**
 * Loading skeleton for Article Analyzer section
 */
function ArticleAnalyzerSkeleton() {
  return (
    <div className="min-h-screen bg-background animate-pulse">
      <div className="border-b">
        <div className="container py-4">
          <div className="h-8 bg-muted rounded w-48 mb-4" />
          <div className="flex gap-3">
            <div className="h-10 bg-muted rounded w-32" />
            <div className="h-10 bg-muted rounded w-32" />
            <div className="h-10 bg-muted rounded w-32" />
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * Layout for the Article Analyzer section.
 * Wraps all article-analyzer routes with the ArticleAnalyzerShell.
 */
export default function ArticleAnalyzerLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <Suspense fallback={<ArticleAnalyzerSkeleton />}>
      <ArticleAnalyzerShell>{children}</ArticleAnalyzerShell>
    </Suspense>
  );
}
