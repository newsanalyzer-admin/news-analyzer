import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { SidebarMenuItem } from '../SidebarMenuItem';
import { Home, Building2, Users, Settings } from 'lucide-react';
import type { MenuItemData } from '../types';

// Mock next/navigation
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: vi.fn(),
  }),
  usePathname: () => '/dashboard',
}));

describe('SidebarMenuItem', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ====== Basic Rendering Tests ======

  describe('Basic Rendering', () => {
    it('renders menu item with label', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} />);

      expect(screen.getByText('Dashboard')).toBeInTheDocument();
    });

    it('renders menu item with icon', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} />);

      // Icon should be rendered (SVG element)
      const link = screen.getByRole('link');
      expect(link.querySelector('svg')).toBeInTheDocument();
    });

    it('renders as link when href is provided', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} />);

      const link = screen.getByRole('link', { name: /dashboard/i });
      expect(link).toHaveAttribute('href', '/dashboard');
    });

    it('renders as button when has children and no href', () => {
      const item: MenuItemData = {
        label: 'Organizations',
        icon: Building2,
        children: [
          { label: 'View All', href: '/organizations' },
        ],
      };

      render(<SidebarMenuItem item={item} />);

      expect(screen.getByRole('button', { name: /organizations/i })).toBeInTheDocument();
    });

    it('hides label when collapsed', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} isCollapsed={true} />);

      expect(screen.queryByText('Dashboard')).not.toBeInTheDocument();
    });

    it('shows tooltip with full path when collapsed', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} isCollapsed={true} />);

      const link = screen.getByRole('link');
      expect(link).toHaveAttribute('title', 'Dashboard');
    });
  });

  // ====== Active State Tests ======

  describe('Active State', () => {
    it('marks current page as active', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} />);

      const link = screen.getByRole('link', { name: /dashboard/i });
      expect(link).toHaveAttribute('aria-current', 'page');
    });

    it('does not mark non-current page as active', () => {
      const item: MenuItemData = {
        label: 'Settings',
        href: '/settings',
        icon: Settings,
      };

      render(<SidebarMenuItem item={item} />);

      const link = screen.getByRole('link', { name: /settings/i });
      expect(link).not.toHaveAttribute('aria-current');
    });

    it('applies active styling class', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} />);

      const link = screen.getByRole('link', { name: /dashboard/i });
      expect(link).toHaveClass('bg-accent');
    });
  });

  // ====== Children/Submenu Tests ======

  describe('Children/Submenu', () => {
    const itemWithChildren: MenuItemData = {
      label: 'Organizations',
      icon: Building2,
      children: [
        { label: 'View All', href: '/organizations' },
        { label: 'Add New', href: '/organizations/new' },
      ],
    };

    it('renders children by default (expanded)', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      expect(screen.getByText('View All')).toBeInTheDocument();
      expect(screen.getByText('Add New')).toBeInTheDocument();
    });

    it('shows chevron icon for expandable items', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      // Should have a chevron icon (ChevronDown when expanded)
      const button = screen.getByRole('button', { name: /organizations/i });
      expect(button.querySelector('svg')).toBeInTheDocument();
    });

    it('has aria-expanded attribute', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      const button = screen.getByRole('button', { name: /organizations/i });
      expect(button).toHaveAttribute('aria-expanded', 'true');
    });

    it('collapses children on click', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      const button = screen.getByRole('button', { name: /organizations/i });
      fireEvent.click(button);

      expect(button).toHaveAttribute('aria-expanded', 'false');
      expect(screen.queryByText('View All')).not.toBeInTheDocument();
    });

    it('expands children on second click', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      const button = screen.getByRole('button', { name: /organizations/i });

      // Collapse
      fireEvent.click(button);
      expect(screen.queryByText('View All')).not.toBeInTheDocument();

      // Expand
      fireEvent.click(button);
      expect(screen.getByText('View All')).toBeInTheDocument();
    });

    it('renders submenu with aria-label', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      const submenu = screen.getByRole('group', { name: /organizations submenu/i });
      expect(submenu).toBeInTheDocument();
    });

    it('hides children when sidebar is collapsed', () => {
      render(<SidebarMenuItem item={itemWithChildren} isCollapsed={true} />);

      // Children should not be rendered when sidebar is collapsed
      expect(screen.queryByText('View All')).not.toBeInTheDocument();
    });
  });

  // ====== Nested Menu Tests ======

  describe('Nested Menus', () => {
    const deeplyNestedItem: MenuItemData = {
      label: 'Level 1',
      icon: Building2,
      children: [
        {
          label: 'Level 2',
          children: [
            {
              label: 'Level 3',
              children: [
                { label: 'Level 4', href: '/deep' },
              ],
            },
          ],
        },
      ],
    };

    it('renders nested menu items', () => {
      render(<SidebarMenuItem item={deeplyNestedItem} />);

      expect(screen.getByText('Level 1')).toBeInTheDocument();
      expect(screen.getByText('Level 2')).toBeInTheDocument();
      expect(screen.getByText('Level 3')).toBeInTheDocument();
    });

    it('respects max depth limit', () => {
      render(<SidebarMenuItem item={deeplyNestedItem} />);

      // Level 4 should still be visible as leaf item
      // but children beyond MAX_DEPTH won't have nested children rendered
      expect(screen.getByText('Level 4')).toBeInTheDocument();
    });

    it('applies increasing indentation for nested levels', () => {
      const item: MenuItemData = {
        label: 'Parent',
        icon: Building2,
        children: [
          { label: 'Child', href: '/child' },
        ],
      };

      render(<SidebarMenuItem item={item} />);

      const parent = screen.getByRole('button', { name: /parent/i });
      const child = screen.getByRole('link', { name: /child/i });

      // Child should have more padding than parent
      const parentPadding = parseInt(parent.style.paddingLeft || '0');
      const childPadding = parseInt(child.style.paddingLeft || '0');

      expect(childPadding).toBeGreaterThan(parentPadding);
    });
  });

  // ====== Keyboard Navigation Tests ======

  describe('Keyboard Navigation', () => {
    const itemWithChildren: MenuItemData = {
      label: 'Organizations',
      icon: Building2,
      children: [
        { label: 'View All', href: '/organizations' },
      ],
    };

    it('expands on Enter key', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      const button = screen.getByRole('button', { name: /organizations/i });

      // First collapse
      fireEvent.click(button);
      expect(button).toHaveAttribute('aria-expanded', 'false');

      // Then expand with Enter
      fireEvent.keyDown(button, { key: 'Enter' });
      expect(button).toHaveAttribute('aria-expanded', 'true');
    });

    it('expands on Space key', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      const button = screen.getByRole('button', { name: /organizations/i });

      // First collapse
      fireEvent.click(button);

      // Then expand with Space
      fireEvent.keyDown(button, { key: ' ' });
      expect(button).toHaveAttribute('aria-expanded', 'true');
    });

    it('expands on ArrowRight key', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      const button = screen.getByRole('button', { name: /organizations/i });

      // First collapse
      fireEvent.click(button);
      expect(button).toHaveAttribute('aria-expanded', 'false');

      // Expand with ArrowRight
      fireEvent.keyDown(button, { key: 'ArrowRight' });
      expect(button).toHaveAttribute('aria-expanded', 'true');
    });

    it('collapses on ArrowLeft key', () => {
      render(<SidebarMenuItem item={itemWithChildren} />);

      const button = screen.getByRole('button', { name: /organizations/i });

      // Initially expanded
      expect(button).toHaveAttribute('aria-expanded', 'true');

      // Collapse with ArrowLeft
      fireEvent.keyDown(button, { key: 'ArrowLeft' });
      expect(button).toHaveAttribute('aria-expanded', 'false');
    });
  });

  // ====== Navigation Callback Tests ======

  describe('Navigation Callback', () => {
    it('calls onNavigate when link is clicked', () => {
      const onNavigate = vi.fn();
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} onNavigate={onNavigate} />);

      const link = screen.getByRole('link', { name: /dashboard/i });
      fireEvent.click(link);

      expect(onNavigate).toHaveBeenCalledTimes(1);
    });

    it('does not call onNavigate for items without href', () => {
      const onNavigate = vi.fn();
      const item: MenuItemData = {
        label: 'Organizations',
        icon: Building2,
        children: [
          { label: 'View All', href: '/organizations' },
        ],
      };

      render(<SidebarMenuItem item={item} onNavigate={onNavigate} />);

      const button = screen.getByRole('button', { name: /organizations/i });
      fireEvent.click(button);

      expect(onNavigate).not.toHaveBeenCalled();
    });

    it('passes onNavigate to child items', () => {
      const onNavigate = vi.fn();
      const item: MenuItemData = {
        label: 'Organizations',
        icon: Building2,
        children: [
          { label: 'View All', href: '/organizations' },
        ],
      };

      render(<SidebarMenuItem item={item} onNavigate={onNavigate} />);

      const childLink = screen.getByRole('link', { name: /view all/i });
      fireEvent.click(childLink);

      expect(onNavigate).toHaveBeenCalledTimes(1);
    });
  });

  // ====== Accessibility Tests ======

  describe('Accessibility', () => {
    it('link items are focusable', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} />);

      const link = screen.getByRole('link', { name: /dashboard/i });
      link.focus();
      expect(document.activeElement).toBe(link);
    });

    it('button items are focusable', () => {
      const item: MenuItemData = {
        label: 'Organizations',
        icon: Building2,
        children: [
          { label: 'View All', href: '/organizations' },
        ],
      };

      render(<SidebarMenuItem item={item} />);

      const button = screen.getByRole('button', { name: /organizations/i });
      expect(button).toHaveAttribute('tabindex', '0');
    });

    it('has focus-visible styles', () => {
      const item: MenuItemData = {
        label: 'Dashboard',
        href: '/dashboard',
        icon: Home,
      };

      render(<SidebarMenuItem item={item} />);

      const link = screen.getByRole('link', { name: /dashboard/i });
      expect(link).toHaveClass('focus-visible:ring-2');
    });
  });

  // ====== Parent Path Tooltip Tests ======

  describe('Parent Path', () => {
    it('shows full path in tooltip when collapsed', () => {
      const item: MenuItemData = {
        label: 'Agencies',
        href: '/agencies',
      };

      render(
        <SidebarMenuItem
          item={item}
          isCollapsed={true}
          parentPath="Admin > Factbase"
        />
      );

      const link = screen.getByRole('link');
      expect(link).toHaveAttribute('title', 'Admin > Factbase > Agencies');
    });
  });
});
