'use client';

import { Database } from 'lucide-react';
import { useArticleAnalyzerSidebarStore } from '@/stores/articleAnalyzerSidebarStore';
import { BaseSidebar } from '@/components/sidebar';
import { articleAnalyzerMenuItems } from '@/lib/menu-config';
import { cn } from '@/lib/utils';
import Link from 'next/link';

interface ArticleAnalyzerSidebarProps {
  className?: string;
}

/**
 * ArticleAnalyzerSidebar - Sidebar navigation for the Article Analyzer section.
 * Provides navigation between Analyze, Articles, and Entities views.
 * Includes a footer link to Knowledge Base.
 */
export function ArticleAnalyzerSidebar({ className }: ArticleAnalyzerSidebarProps) {
  const { isCollapsed, toggle, closeMobile } = useArticleAnalyzerSidebarStore();

  const kbFooter = (
    <Link
      href="/knowledge-base"
      className={cn(
        'flex items-center gap-3 w-full rounded-md py-2 px-3 text-sm font-medium',
        'hover:bg-accent hover:text-accent-foreground transition-colors',
        isCollapsed && 'justify-center px-0'
      )}
      title="Knowledge Base"
    >
      <Database className="h-5 w-5 shrink-0" />
      {!isCollapsed && <span>Knowledge Base</span>}
    </Link>
  );

  return (
    <BaseSidebar
      menuItems={articleAnalyzerMenuItems}
      isCollapsed={isCollapsed}
      onToggle={toggle}
      header={
        <Link
          href="/article-analyzer"
          className="font-semibold text-lg hover:text-primary transition-colors"
        >
          Article Analyzer
        </Link>
      }
      footer={kbFooter}
      className={className}
      ariaLabel="Article Analyzer navigation"
      onNavigate={closeMobile}
    />
  );
}
