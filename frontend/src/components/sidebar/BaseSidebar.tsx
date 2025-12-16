'use client';

import { ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '@/lib/utils';
import { BaseSidebarProps } from './types';
import { SidebarMenuItem } from './SidebarMenuItem';

/**
 * Base sidebar component that can be used for both admin and public sidebars.
 * Provides collapsible navigation with support for nested menu items.
 */
export function BaseSidebar({
  menuItems,
  isCollapsed,
  onToggle,
  header,
  footer,
  className,
  ariaLabel = 'Navigation',
  onNavigate,
}: BaseSidebarProps) {
  return (
    <aside
      role="navigation"
      aria-label={ariaLabel}
      className={cn(
        'flex flex-col h-full bg-card border-r border-border transition-all duration-200 ease-in-out',
        isCollapsed ? 'w-16' : 'w-64',
        className
      )}
    >
      {/* Header with toggle */}
      <div
        className={cn(
          'flex items-center h-14 border-b border-border px-4 shrink-0',
          isCollapsed ? 'justify-center' : 'justify-between'
        )}
      >
        {!isCollapsed && header}
        <button
          onClick={onToggle}
          className={cn(
            'p-1.5 rounded-md hover:bg-accent hover:text-accent-foreground transition-colors',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'
          )}
          aria-label={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {isCollapsed ? (
            <ChevronRight className="h-5 w-5" />
          ) : (
            <ChevronLeft className="h-5 w-5" />
          )}
        </button>
      </div>

      {/* Navigation Items */}
      <nav className="flex-1 overflow-y-auto py-4 px-2">
        {menuItems.map((item) => (
          <SidebarMenuItem
            key={item.label}
            item={item}
            isCollapsed={isCollapsed}
            onNavigate={onNavigate}
          />
        ))}
      </nav>

      {/* Footer */}
      {footer && (
        <div className="border-t border-border p-2 shrink-0">
          {footer}
        </div>
      )}
    </aside>
  );
}
