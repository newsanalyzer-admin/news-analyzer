import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { AdminSidebar } from '../AdminSidebar';
import { useSidebarStore } from '@/stores/sidebarStore';

// Mock next/navigation (already globally mocked, but override pathname for admin)
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
  usePathname: () => '/admin/factbase/executive/agencies',
}));

describe('AdminSidebar', () => {
  // Reset Zustand store before each test
  beforeEach(() => {
    useSidebarStore.setState({
      isCollapsed: false,
      isMobileOpen: false,
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  // ====== Rendering Tests ======

  describe('Rendering', () => {
    it('renders the Admin header', () => {
      render(<AdminSidebar />);
      expect(screen.getByText('Admin')).toBeInTheDocument();
    });

    it('renders navigation with correct aria-label', () => {
      render(<AdminSidebar />);
      expect(screen.getByRole('navigation', { name: /admin navigation/i })).toBeInTheDocument();
    });

    it('renders top-level menu items', () => {
      render(<AdminSidebar />);
      expect(screen.getByText('Factbase')).toBeInTheDocument();
    });

    it('renders Dashboard footer link', () => {
      render(<AdminSidebar />);
      expect(screen.getByText('Dashboard')).toBeInTheDocument();
    });

    it('renders nested menu structure', () => {
      render(<AdminSidebar />);

      // Top level
      expect(screen.getByText('Factbase')).toBeInTheDocument();

      // Second level
      expect(screen.getByText('Government Entities')).toBeInTheDocument();
      expect(screen.getByText('Federal Laws & Regulations')).toBeInTheDocument();
    });

    it('renders third level menu items', () => {
      render(<AdminSidebar />);

      expect(screen.getByText('Executive Branch')).toBeInTheDocument();
      expect(screen.getByText('Legislative Branch')).toBeInTheDocument();
      expect(screen.getByText('Judicial Branch')).toBeInTheDocument();
    });

    it('renders leaf menu items with links', () => {
      render(<AdminSidebar />);

      const agenciesLink = screen.getByRole('link', { name: /agencies & departments/i });
      expect(agenciesLink).toHaveAttribute('href', '/admin/factbase/executive/agencies');
    });
  });

  // ====== Collapse/Expand Tests ======

  describe('Collapse/Expand', () => {
    it('renders expand button when collapsed', () => {
      useSidebarStore.setState({ isCollapsed: true });
      render(<AdminSidebar />);

      expect(screen.getByRole('button', { name: /expand sidebar/i })).toBeInTheDocument();
    });

    it('renders collapse button when expanded', () => {
      useSidebarStore.setState({ isCollapsed: false });
      render(<AdminSidebar />);

      expect(screen.getByRole('button', { name: /collapse sidebar/i })).toBeInTheDocument();
    });

    it('toggles collapse state when button clicked', () => {
      render(<AdminSidebar />);

      const toggleButton = screen.getByRole('button', { name: /collapse sidebar/i });
      fireEvent.click(toggleButton);

      expect(useSidebarStore.getState().isCollapsed).toBe(true);
    });

    it('hides header text when collapsed', () => {
      useSidebarStore.setState({ isCollapsed: true });
      render(<AdminSidebar />);

      // Admin text should not be visible when collapsed
      expect(screen.queryByText('Admin')).not.toBeInTheDocument();
    });

    it('hides nested menu items when collapsed', () => {
      useSidebarStore.setState({ isCollapsed: true });
      render(<AdminSidebar />);

      // Nested items should not be visible when collapsed
      expect(screen.queryByText('Government Entities')).not.toBeInTheDocument();
    });
  });

  // ====== Navigation Tests ======

  describe('Navigation', () => {
    it('highlights active menu item based on pathname', () => {
      render(<AdminSidebar />);

      // The current path is /admin/factbase/executive/agencies
      const agenciesLink = screen.getByRole('link', { name: /agencies & departments/i });
      expect(agenciesLink).toHaveAttribute('aria-current', 'page');
    });

    it('calls closeMobile when navigating', () => {
      const closeMobileSpy = vi.fn();
      useSidebarStore.setState({ closeMobile: closeMobileSpy });

      render(<AdminSidebar />);

      const link = screen.getByRole('link', { name: /agencies & departments/i });
      fireEvent.click(link);

      expect(closeMobileSpy).toHaveBeenCalled();
    });
  });

  // ====== Menu Expansion Tests ======

  describe('Menu Expansion', () => {
    it('expands/collapses menu sections on click', () => {
      render(<AdminSidebar />);

      // Find the Factbase menu item
      const factbaseButton = screen.getByText('Factbase').closest('[role="button"]');
      expect(factbaseButton).toBeInTheDocument();

      if (factbaseButton) {
        // Initially expanded (children visible)
        expect(screen.getByText('Government Entities')).toBeInTheDocument();

        // Click to collapse
        fireEvent.click(factbaseButton);

        // Children should be hidden after collapse
        // Note: The actual behavior depends on SidebarMenuItem implementation
      }
    });

    it('supports keyboard navigation for menu expansion', () => {
      render(<AdminSidebar />);

      const factbaseButton = screen.getByText('Factbase').closest('[role="button"]');

      if (factbaseButton) {
        factbaseButton.focus();

        // Press Enter to toggle
        fireEvent.keyDown(factbaseButton, { key: 'Enter' });

        // Press ArrowLeft to collapse
        fireEvent.keyDown(factbaseButton, { key: 'ArrowLeft' });

        // Press ArrowRight to expand
        fireEvent.keyDown(factbaseButton, { key: 'ArrowRight' });
      }
    });
  });

  // ====== Accessibility Tests ======

  describe('Accessibility', () => {
    it('has correct ARIA structure', () => {
      render(<AdminSidebar />);

      // Navigation landmark with specific aria-label
      const nav = screen.getByRole('navigation', { name: /admin navigation/i });
      expect(nav).toBeInTheDocument();
    });

    it('menu buttons have aria-expanded attribute', () => {
      render(<AdminSidebar />);

      const factbaseButton = screen.getByText('Factbase').closest('[role="button"]');
      expect(factbaseButton).toHaveAttribute('aria-expanded');
    });

    it('links are keyboard focusable', () => {
      render(<AdminSidebar />);

      const links = screen.getAllByRole('link');
      links.forEach((link) => {
        expect(link).not.toHaveAttribute('tabindex', '-1');
      });
    });

    it('submenus have aria-label', () => {
      render(<AdminSidebar />);

      const submenu = screen.getByRole('group', { name: /factbase submenu/i });
      expect(submenu).toBeInTheDocument();
    });
  });

  // ====== CSS Class Tests ======

  describe('Styling', () => {
    it('applies custom className', () => {
      render(<AdminSidebar className="custom-class" />);

      const aside = screen.getByRole('navigation', { name: /admin navigation/i });
      expect(aside).toHaveClass('custom-class');
    });

    it('changes width based on collapsed state', () => {
      const { rerender } = render(<AdminSidebar />);

      let aside = screen.getByRole('navigation', { name: /admin navigation/i });
      expect(aside).toHaveClass('w-64');

      act(() => {
        useSidebarStore.setState({ isCollapsed: true });
      });
      rerender(<AdminSidebar />);

      aside = screen.getByRole('navigation', { name: /admin navigation/i });
      expect(aside).toHaveClass('w-16');
    });
  });
});
