import { ComponentType } from 'react';

/**
 * Menu item data structure for sidebar navigation.
 * Supports nested menus up to 3 levels deep.
 */
export interface MenuItemData {
  /** Display label for the menu item */
  label: string;
  /** Optional navigation href (makes item a link) */
  href?: string;
  /** Optional Lucide icon component */
  icon?: ComponentType<{ className?: string }>;
  /** Nested child menu items (max 3 levels recommended) */
  children?: MenuItemData[];
  /** Whether menu item is disabled (grayed out, not clickable) */
  disabled?: boolean;
}

/**
 * Props for BaseSidebar component.
 */
export interface BaseSidebarProps {
  /** Array of menu items to display */
  menuItems: MenuItemData[];
  /** Whether sidebar is in collapsed state */
  isCollapsed: boolean;
  /** Callback to toggle collapsed state */
  onToggle: () => void;
  /** Optional header content (displayed at top) */
  header?: React.ReactNode;
  /** Optional footer content (displayed at bottom) */
  footer?: React.ReactNode;
  /** Additional CSS classes */
  className?: string;
  /** ARIA label for accessibility */
  ariaLabel?: string;
  /** Callback when navigation occurs (for mobile close) */
  onNavigate?: () => void;
}

/**
 * Props for SidebarMenuItem component.
 */
export interface SidebarMenuItemProps {
  /** Menu item data */
  item: MenuItemData;
  /** Current nesting level (0 = root) */
  level?: number;
  /** Whether sidebar is collapsed */
  isCollapsed?: boolean;
  /** Parent path for tooltip display */
  parentPath?: string;
  /** Callback when navigation occurs */
  onNavigate?: () => void;
}
