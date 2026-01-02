import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ArticleAnalyzerSidebar } from '../ArticleAnalyzerSidebar';

// Mock the sidebar store
const mockStore = {
  isCollapsed: false,
  toggle: vi.fn(),
  closeMobile: vi.fn(),
};

vi.mock('@/stores/articleAnalyzerSidebarStore', () => ({
  useArticleAnalyzerSidebarStore: () => mockStore,
}));

// Mock BaseSidebar
vi.mock('@/components/sidebar', () => ({
  BaseSidebar: ({
    menuItems,
    isCollapsed,
    onToggle,
    header,
    footer,
    ariaLabel,
    onNavigate,
    className,
  }: {
    menuItems: Array<{ label: string; href?: string; disabled?: boolean }>;
    isCollapsed: boolean;
    onToggle: () => void;
    header: React.ReactNode;
    footer: React.ReactNode;
    ariaLabel: string;
    onNavigate: () => void;
    className?: string;
  }) => (
    <nav
      data-testid="base-sidebar"
      data-collapsed={isCollapsed}
      data-menu-count={menuItems.length}
      aria-label={ariaLabel}
      className={className}
    >
      <div data-testid="sidebar-header">{header}</div>
      <ul data-testid="menu-items">
        {menuItems.map((item) => (
          <li key={item.label} data-disabled={item.disabled}>
            {item.label}
          </li>
        ))}
      </ul>
      <div data-testid="sidebar-footer">{footer}</div>
      <button data-testid="toggle-btn" onClick={onToggle}>
        Toggle
      </button>
      <button data-testid="navigate-btn" onClick={onNavigate}>
        Navigate
      </button>
    </nav>
  ),
}));

describe('ArticleAnalyzerSidebar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockStore.isCollapsed = false;
  });

  // ====== Rendering Tests ======

  describe('Rendering', () => {
    it('renders BaseSidebar with correct props', () => {
      render(<ArticleAnalyzerSidebar />);

      const sidebar = screen.getByTestId('base-sidebar');
      expect(sidebar).toBeInTheDocument();
      expect(sidebar).toHaveAttribute('aria-label', 'Article Analyzer navigation');
    });

    it('renders with custom className', () => {
      render(<ArticleAnalyzerSidebar className="custom-class" />);

      const sidebar = screen.getByTestId('base-sidebar');
      expect(sidebar).toHaveClass('custom-class');
    });

    it('renders header with Article Analyzer link', () => {
      render(<ArticleAnalyzerSidebar />);

      const header = screen.getByTestId('sidebar-header');
      const link = header.querySelector('a');
      expect(link).toHaveAttribute('href', '/article-analyzer');
      expect(link).toHaveTextContent('Article Analyzer');
    });

    it('renders footer with Knowledge Base link', () => {
      render(<ArticleAnalyzerSidebar />);

      const footer = screen.getByTestId('sidebar-footer');
      const link = footer.querySelector('a');
      expect(link).toHaveAttribute('href', '/knowledge-base');
      expect(link).toHaveAttribute('title', 'Knowledge Base');
    });
  });

  // ====== Menu Items Tests ======

  describe('Menu Items', () => {
    it('renders three menu items', () => {
      render(<ArticleAnalyzerSidebar />);

      const sidebar = screen.getByTestId('base-sidebar');
      expect(sidebar).toHaveAttribute('data-menu-count', '3');
    });

    it('renders Analyze Article menu item (disabled)', () => {
      render(<ArticleAnalyzerSidebar />);

      const menuItems = screen.getByTestId('menu-items');
      const analyzeItem = menuItems.querySelector('li[data-disabled="true"]');
      expect(analyzeItem).toHaveTextContent('Analyze Article');
    });

    it('renders Articles menu item', () => {
      render(<ArticleAnalyzerSidebar />);

      const menuItems = screen.getByTestId('menu-items');
      expect(menuItems).toHaveTextContent('Articles');
    });

    it('renders Entities menu item', () => {
      render(<ArticleAnalyzerSidebar />);

      const menuItems = screen.getByTestId('menu-items');
      expect(menuItems).toHaveTextContent('Entities');
    });
  });

  // ====== Store Integration Tests ======

  describe('Store Integration', () => {
    it('passes isCollapsed state to BaseSidebar', () => {
      mockStore.isCollapsed = true;

      render(<ArticleAnalyzerSidebar />);

      const sidebar = screen.getByTestId('base-sidebar');
      expect(sidebar).toHaveAttribute('data-collapsed', 'true');
    });

    it('passes toggle function to BaseSidebar', () => {
      render(<ArticleAnalyzerSidebar />);

      const toggleBtn = screen.getByTestId('toggle-btn');
      toggleBtn.click();

      expect(mockStore.toggle).toHaveBeenCalledTimes(1);
    });

    it('passes closeMobile function to BaseSidebar for navigation', () => {
      render(<ArticleAnalyzerSidebar />);

      const navigateBtn = screen.getByTestId('navigate-btn');
      navigateBtn.click();

      expect(mockStore.closeMobile).toHaveBeenCalledTimes(1);
    });
  });

  // ====== Collapsed State Tests ======

  describe('Collapsed State', () => {
    it('shows only icon in footer when collapsed', () => {
      mockStore.isCollapsed = true;

      render(<ArticleAnalyzerSidebar />);

      const footer = screen.getByTestId('sidebar-footer');
      const link = footer.querySelector('a');
      expect(link).toHaveClass('justify-center');
    });

    it('shows icon and text in footer when expanded', () => {
      mockStore.isCollapsed = false;

      render(<ArticleAnalyzerSidebar />);

      const footer = screen.getByTestId('sidebar-footer');
      const link = footer.querySelector('a');
      expect(link).not.toHaveClass('justify-center');
      expect(footer).toHaveTextContent('Knowledge Base');
    });
  });
});
