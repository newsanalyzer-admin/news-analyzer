'use client';

import { Menu, X } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePublicSidebarStore } from '@/stores/publicSidebarStore';
import { PublicSidebar } from '@/components/public/PublicSidebar';

export default function FactbaseLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isMobileOpen, toggleMobile, closeMobile, isCollapsed } = usePublicSidebarStore();

  return (
    <div className="flex h-screen bg-background">
      {/* Mobile menu button */}
      <button
        onClick={toggleMobile}
        className={cn(
          'fixed top-4 left-4 z-50 p-2 rounded-md md:hidden',
          'bg-background border border-border shadow-sm',
          'hover:bg-accent transition-colors',
          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'
        )}
        aria-label={isMobileOpen ? 'Close menu' : 'Open menu'}
      >
        {isMobileOpen ? (
          <X className="h-5 w-5" />
        ) : (
          <Menu className="h-5 w-5" />
        )}
      </button>

      {/* Mobile backdrop */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 z-30 bg-black/50 md:hidden"
          onClick={closeMobile}
          aria-hidden="true"
        />
      )}

      {/* Sidebar - Desktop: always visible, Mobile: overlay */}
      <div
        className={cn(
          // Mobile styles
          'fixed inset-y-0 left-0 z-40 md:relative md:z-auto',
          'transform transition-transform duration-200 ease-in-out',
          // Mobile visibility
          isMobileOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0',
        )}
      >
        <PublicSidebar />
      </div>

      {/* Main content */}
      <main
        className={cn(
          'flex-1 overflow-auto',
          // Add padding on mobile for the menu button
          'pt-16 md:pt-0'
        )}
      >
        {children}
      </main>
    </div>
  );
}
