'use client';

import { useEffect } from 'react';
import { Menu, X } from 'lucide-react';
import { cn } from '@/lib/utils';

/**
 * Interface for sidebar store state and actions.
 * Compatible with both publicSidebarStore and articleAnalyzerSidebarStore.
 */
export interface SidebarStore {
  isCollapsed: boolean;
  isMobileOpen: boolean;
  toggleMobile: () => void;
  closeMobile: () => void;
}

/**
 * Props for the SidebarLayout component.
 */
export interface SidebarLayoutProps {
  /** Main content to render in the layout */
  children: React.ReactNode;
  /** Sidebar component to render (e.g., PublicSidebar, ArticleAnalyzerSidebar) */
  sidebar: React.ReactNode;
  /** Title displayed in the mobile header */
  sectionTitle: string;
  /** Sidebar store for state management */
  store: SidebarStore;
  /** Optional additional className for the main content area */
  className?: string;
}

/**
 * SidebarLayout - Shared responsive layout component for sidebar navigation.
 *
 * Features:
 * - Mobile: Fixed header with hamburger menu, slide-in sidebar overlay
 * - Desktop: Fixed collapsible sidebar (64px collapsed, 256px expanded)
 * - Escape key closes mobile sidebar
 * - Body scroll lock when mobile sidebar is open
 * - Backdrop click closes mobile sidebar
 *
 * @example
 * ```tsx
 * import { usePublicSidebarStore } from '@/stores/publicSidebarStore';
 * import { PublicSidebar } from '@/components/public/PublicSidebar';
 *
 * function KnowledgeBaseLayout({ children }) {
 *   const store = usePublicSidebarStore();
 *   return (
 *     <SidebarLayout
 *       sidebar={<PublicSidebar />}
 *       sectionTitle="Knowledge Base"
 *       store={store}
 *     >
 *       {children}
 *     </SidebarLayout>
 *   );
 * }
 * ```
 */
export function SidebarLayout({
  children,
  sidebar,
  sectionTitle,
  store,
  className,
}: SidebarLayoutProps) {
  const { isCollapsed, isMobileOpen, toggleMobile, closeMobile } = store;

  // Close mobile sidebar on escape key
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isMobileOpen) {
        closeMobile();
      }
    };
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isMobileOpen, closeMobile]);

  // Prevent body scroll when mobile sidebar is open
  useEffect(() => {
    if (isMobileOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isMobileOpen]);

  return (
    <div className="min-h-screen bg-background">
      {/* Mobile Header */}
      <header className="md:hidden fixed top-0 left-0 right-0 z-40 h-14 bg-card border-b border-border flex items-center px-4">
        <button
          onClick={toggleMobile}
          className={cn(
            'p-2 rounded-md hover:bg-accent hover:text-accent-foreground transition-colors',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'
          )}
          aria-label={isMobileOpen ? 'Close navigation menu' : 'Open navigation menu'}
          aria-expanded={isMobileOpen}
        >
          {isMobileOpen ? (
            <X className="h-6 w-6" />
          ) : (
            <Menu className="h-6 w-6" />
          )}
        </button>
        <span className="ml-4 font-semibold text-lg">{sectionTitle}</span>
      </header>

      {/* Mobile Backdrop */}
      {isMobileOpen && (
        <div
          className="md:hidden fixed inset-0 z-40 bg-black/50 transition-opacity"
          onClick={closeMobile}
          aria-hidden="true"
          data-testid="sidebar-backdrop"
        />
      )}

      {/* Mobile Sidebar Overlay */}
      <div
        className={cn(
          'md:hidden fixed inset-y-0 left-0 z-50 transform transition-transform duration-200 ease-in-out',
          isMobileOpen ? 'translate-x-0' : '-translate-x-full'
        )}
        data-testid="mobile-sidebar"
      >
        {sidebar}
      </div>

      {/* Desktop Sidebar */}
      <div
        className="hidden md:block fixed inset-y-0 left-0 z-30"
        data-testid="desktop-sidebar"
      >
        {sidebar}
      </div>

      {/* Main Content */}
      <main
        className={cn(
          'transition-all duration-200 ease-in-out',
          'pt-14 md:pt-0', // Account for mobile header
          isCollapsed ? 'md:ml-16' : 'md:ml-64',
          className
        )}
      >
        {children}
      </main>
    </div>
  );
}
