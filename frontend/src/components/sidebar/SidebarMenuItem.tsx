'use client';

import { useState, useCallback, KeyboardEvent } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { cn } from '@/lib/utils';
import { MenuItemData, SidebarMenuItemProps } from './types';

// Re-export for backwards compatibility
export type { MenuItemData };

const MAX_DEPTH = 3; // Maximum nesting levels

export function SidebarMenuItem({
  item,
  level = 0,
  isCollapsed = false,
  parentPath = '',
  onNavigate,
}: SidebarMenuItemProps) {
  const pathname = usePathname();
  const [isExpanded, setIsExpanded] = useState(true);

  const { label, href, icon: Icon, children } = item;
  const hasChildren = children && children.length > 0;

  // Build tooltip path for collapsed state
  const fullPath = parentPath ? `${parentPath} > ${label}` : label;

  // Recursively check if any descendant is active
  const isDescendantActive = (items: MenuItemData[] | undefined): boolean => {
    if (!items) return false;
    return items.some((child) => {
      if (child.href && pathname === child.href) return true;
      return isDescendantActive(child.children);
    });
  };

  // Check if this item or any child is active
  const isActive = href ? pathname === href : false;
  const isChildActive = isDescendantActive(children);

  const handleToggle = useCallback(() => {
    if (hasChildren) {
      setIsExpanded((prev) => !prev);
    }
  }, [hasChildren]);

  const handleKeyDown = useCallback(
    (e: KeyboardEvent<HTMLDivElement | HTMLAnchorElement>) => {
      switch (e.key) {
        case 'Enter':
        case ' ':
          if (hasChildren && !href) {
            e.preventDefault();
            handleToggle();
          }
          break;
        case 'ArrowRight':
          if (hasChildren && !isExpanded) {
            e.preventDefault();
            setIsExpanded(true);
          }
          break;
        case 'ArrowLeft':
          if (hasChildren && isExpanded) {
            e.preventDefault();
            setIsExpanded(false);
          }
          break;
      }
    },
    [hasChildren, href, isExpanded, handleToggle]
  );

  const handleClick = useCallback(() => {
    if (href && onNavigate) {
      onNavigate();
    }
  }, [href, onNavigate]);

  // Calculate padding based on level (only when not collapsed)
  const paddingLeft = isCollapsed ? 16 : 16 + level * 16;

  // Common button/link styles
  const itemStyles = cn(
    'flex items-center gap-3 w-full rounded-md text-sm font-medium transition-colors duration-150',
    'hover:bg-accent hover:text-accent-foreground',
    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
    isCollapsed ? 'justify-center py-3 px-2' : 'py-2 pr-3',
    isActive && 'bg-accent text-accent-foreground font-semibold',
    (isChildActive && !isActive) && 'text-accent-foreground'
  );

  // Render icon
  const renderIcon = () => {
    if (Icon) {
      return <Icon className={cn('h-5 w-5 shrink-0', isActive && 'text-primary')} />;
    }
    // Indent placeholder for items without icons at deeper levels
    if (level > 0 && !isCollapsed) {
      return <span className="w-5" />;
    }
    return null;
  };

  // Content to render inside the item
  const content = (
    <>
      {renderIcon()}
      {!isCollapsed && (
        <>
          <span className="flex-1 truncate">{label}</span>
          {hasChildren && (
            <span className="shrink-0">
              {isExpanded ? (
                <ChevronDown className="h-4 w-4" />
              ) : (
                <ChevronRight className="h-4 w-4" />
              )}
            </span>
          )}
        </>
      )}
    </>
  );

  // If collapsed and not at top level, don't render
  if (isCollapsed && level > 0) {
    return null;
  }

  // Enforce max depth - render items but not their children beyond MAX_DEPTH
  const shouldRenderChildren = hasChildren && level < MAX_DEPTH;

  // If item has href, render as Link
  if (href && !hasChildren) {
    return (
      <Link
        href={href}
        className={itemStyles}
        style={{ paddingLeft }}
        aria-current={isActive ? 'page' : undefined}
        title={isCollapsed ? fullPath : undefined}
        onClick={handleClick}
        onKeyDown={handleKeyDown}
      >
        {content}
      </Link>
    );
  }

  // If item has children or no href, render as button/div
  return (
    <div>
      <div
        role="button"
        tabIndex={0}
        className={itemStyles}
        style={{ paddingLeft }}
        onClick={hasChildren ? handleToggle : undefined}
        onKeyDown={handleKeyDown}
        aria-expanded={hasChildren ? isExpanded : undefined}
        title={isCollapsed ? fullPath : undefined}
      >
        {content}
      </div>

      {/* Render children */}
      {shouldRenderChildren && isExpanded && !isCollapsed && (
        <div role="group" aria-label={`${label} submenu`}>
          {children.map((child) => (
            <SidebarMenuItem
              key={child.label}
              item={child}
              level={level + 1}
              isCollapsed={isCollapsed}
              parentPath={fullPath}
              onNavigate={onNavigate}
            />
          ))}
        </div>
      )}
    </div>
  );
}
