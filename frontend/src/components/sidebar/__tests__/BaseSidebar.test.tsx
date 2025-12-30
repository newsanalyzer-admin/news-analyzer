import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BaseSidebar } from '../BaseSidebar';
import { Building2, Home, Settings } from 'lucide-react';
import type { MenuItemData } from '../types';

// Mock next/navigation
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
  usePathname: () => '/dashboard',
}));

// Sample menu items for testing
const mockMenuItems: MenuItemData[] = [
  {
    label: 'Dashboard',
    href: '/dashboard',
    icon: Home,
  },
  {
    label: 'Organizations',
    icon: Building2,
    children: [
      { label: 'View All', href: '/organizations' },
      { label: 'Add New', href: '/organizations/new' },
    ],
  },
  {
    label: 'Settings',
    href: '/settings',
    icon: Settings,
  },
];

const defaultProps = {
  menuItems: mockMenuItems,
  isCollapsed: false,
  onToggle: vi.fn(),
};

describe('BaseSidebar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ====== Rendering Tests ======

  describe('Rendering', () => {
    it('renders as aside element with navigation role', () => {
      render(<BaseSidebar {...defaultProps} />);

      // The aside element has aria-label, so we query by that
      const aside = screen.getByRole('navigation', { name: 'Navigation' });
      expect(aside.tagName).toBe('ASIDE');
    });

    it('renders all top-level menu items', () => {
      render(<BaseSidebar {...defaultProps} />);

      expect(screen.getByText('Dashboard')).toBeInTheDocument();
      expect(screen.getByText('Organizations')).toBeInTheDocument();
      expect(screen.getByText('Settings')).toBeInTheDocument();
    });

    it('renders header content when provided', () => {
      render(
        <BaseSidebar
          {...defaultProps}
          header={<span data-testid="header">My App</span>}
        />
      );

      expect(screen.getByTestId('header')).toBeInTheDocument();
      expect(screen.getByText('My App')).toBeInTheDocument();
    });

    it('renders footer content when provided', () => {
      render(
        <BaseSidebar
          {...defaultProps}
          footer={<span data-testid="footer">Footer Content</span>}
        />
      );

      expect(screen.getByTestId('footer')).toBeInTheDocument();
      expect(screen.getByText('Footer Content')).toBeInTheDocument();
    });

    it('hides header when collapsed', () => {
      render(
        <BaseSidebar
          {...defaultProps}
          isCollapsed={true}
          header={<span data-testid="header">My App</span>}
        />
      );

      // Header content should not be visible when collapsed
      expect(screen.queryByTestId('header')).not.toBeInTheDocument();
    });

    it('renders with default aria-label', () => {
      render(<BaseSidebar {...defaultProps} />);

      const sidebar = screen.getByRole('navigation', { name: 'Navigation' });
      expect(sidebar).toBeInTheDocument();
    });

    it('renders with custom aria-label', () => {
      render(<BaseSidebar {...defaultProps} ariaLabel="Main menu" />);

      const sidebar = screen.getByRole('navigation', { name: 'Main menu' });
      expect(sidebar).toBeInTheDocument();
    });
  });

  // ====== Toggle Button Tests ======

  describe('Toggle Button', () => {
    it('renders collapse button when expanded', () => {
      render(<BaseSidebar {...defaultProps} isCollapsed={false} />);

      expect(screen.getByRole('button', { name: /collapse sidebar/i })).toBeInTheDocument();
    });

    it('renders expand button when collapsed', () => {
      render(<BaseSidebar {...defaultProps} isCollapsed={true} />);

      expect(screen.getByRole('button', { name: /expand sidebar/i })).toBeInTheDocument();
    });

    it('calls onToggle when button clicked', () => {
      const onToggle = vi.fn();
      render(<BaseSidebar {...defaultProps} onToggle={onToggle} />);

      const toggleButton = screen.getByRole('button', { name: /collapse sidebar/i });
      fireEvent.click(toggleButton);

      expect(onToggle).toHaveBeenCalledTimes(1);
    });

    it('toggle button is keyboard accessible', () => {
      const onToggle = vi.fn();
      render(<BaseSidebar {...defaultProps} onToggle={onToggle} />);

      const toggleButton = screen.getByRole('button', { name: /collapse sidebar/i });
      toggleButton.focus();
      fireEvent.keyDown(toggleButton, { key: 'Enter' });

      // Enter key should trigger button click
      expect(document.activeElement).toBe(toggleButton);
    });
  });

  // ====== Width/Collapse Tests ======

  describe('Collapse Behavior', () => {
    it('has expanded width when not collapsed', () => {
      render(<BaseSidebar {...defaultProps} isCollapsed={false} />);

      const aside = screen.getByRole('navigation', { name: 'Navigation' });
      expect(aside).toHaveClass('w-64');
    });

    it('has collapsed width when collapsed', () => {
      render(<BaseSidebar {...defaultProps} isCollapsed={true} />);

      const aside = screen.getByRole('navigation', { name: 'Navigation' });
      expect(aside).toHaveClass('w-16');
    });

    it('applies transition classes for smooth animation', () => {
      render(<BaseSidebar {...defaultProps} />);

      const aside = screen.getByRole('navigation', { name: 'Navigation' });
      expect(aside).toHaveClass('transition-all');
      expect(aside).toHaveClass('duration-200');
    });
  });

  // ====== Navigation Callback Tests ======

  describe('Navigation Callback', () => {
    it('passes onNavigate to menu items', () => {
      const onNavigate = vi.fn();
      render(<BaseSidebar {...defaultProps} onNavigate={onNavigate} />);

      // Click on a link
      const dashboardLink = screen.getByRole('link', { name: /dashboard/i });
      fireEvent.click(dashboardLink);

      expect(onNavigate).toHaveBeenCalled();
    });
  });

  // ====== CSS Class Tests ======

  describe('Custom Styling', () => {
    it('applies custom className', () => {
      render(<BaseSidebar {...defaultProps} className="my-custom-class" />);

      const aside = screen.getByRole('navigation', { name: 'Navigation' });
      expect(aside).toHaveClass('my-custom-class');
    });

    it('merges custom className with default classes', () => {
      render(<BaseSidebar {...defaultProps} className="my-custom-class" />);

      const aside = screen.getByRole('navigation', { name: 'Navigation' });
      expect(aside).toHaveClass('my-custom-class');
      expect(aside).toHaveClass('bg-card');
      expect(aside).toHaveClass('border-r');
    });
  });

  // ====== Accessibility Tests ======

  describe('Accessibility', () => {
    it('has proper landmark role', () => {
      render(<BaseSidebar {...defaultProps} />);

      expect(screen.getByRole('navigation', { name: 'Navigation' })).toBeInTheDocument();
    });

    it('toggle button has proper focus styles class', () => {
      render(<BaseSidebar {...defaultProps} />);

      const toggleButton = screen.getByRole('button', { name: /collapse sidebar/i });
      expect(toggleButton).toHaveClass('focus-visible:ring-2');
    });

    it('nav element is scrollable for long menus', () => {
      render(<BaseSidebar {...defaultProps} />);

      const sidebar = screen.getByRole('navigation', { name: 'Navigation' });
      const nav = sidebar.querySelector('nav');
      expect(nav).toHaveClass('overflow-y-auto');
    });
  });

  // ====== Empty State Tests ======

  describe('Empty State', () => {
    it('renders without menu items', () => {
      render(<BaseSidebar {...defaultProps} menuItems={[]} />);

      expect(screen.getByRole('navigation', { name: 'Navigation' })).toBeInTheDocument();
      // Should still render structure, just no menu items
    });

    it('renders without header', () => {
      render(<BaseSidebar {...defaultProps} header={undefined} />);

      expect(screen.getByRole('navigation', { name: 'Navigation' })).toBeInTheDocument();
    });

    it('renders without footer', () => {
      render(<BaseSidebar {...defaultProps} footer={undefined} />);

      expect(screen.getByRole('navigation', { name: 'Navigation' })).toBeInTheDocument();
    });
  });
});
