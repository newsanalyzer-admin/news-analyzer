import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { SidebarLayout, SidebarStore } from '../SidebarLayout';

// Mock store for testing
const createMockStore = (overrides: Partial<SidebarStore> = {}): SidebarStore => ({
  isCollapsed: false,
  isMobileOpen: false,
  toggleMobile: vi.fn(),
  closeMobile: vi.fn(),
  ...overrides,
});

const defaultProps = {
  children: <div data-testid="main-content">Main Content</div>,
  sidebar: <div data-testid="sidebar-content">Sidebar Content</div>,
  sectionTitle: 'Test Section',
  store: createMockStore(),
};

describe('SidebarLayout', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset body overflow
    document.body.style.overflow = '';
  });

  afterEach(() => {
    // Clean up body overflow after each test
    document.body.style.overflow = '';
  });

  // ====== Rendering Tests ======

  describe('Rendering', () => {
    it('renders main content', () => {
      render(<SidebarLayout {...defaultProps} />);

      expect(screen.getByTestId('main-content')).toBeInTheDocument();
      expect(screen.getByText('Main Content')).toBeInTheDocument();
    });

    it('renders sidebar content in both mobile and desktop containers', () => {
      render(<SidebarLayout {...defaultProps} />);

      // Sidebar is rendered twice - once for mobile and once for desktop
      const sidebars = screen.getAllByTestId('sidebar-content');
      expect(sidebars).toHaveLength(2);
      expect(screen.getAllByText('Sidebar Content')).toHaveLength(2);
    });

    it('renders section title in mobile header', () => {
      render(<SidebarLayout {...defaultProps} />);

      expect(screen.getByText('Test Section')).toBeInTheDocument();
    });

    it('renders hamburger menu button', () => {
      render(<SidebarLayout {...defaultProps} />);

      const button = screen.getByRole('button', { name: /open navigation menu/i });
      expect(button).toBeInTheDocument();
    });

    it('renders with custom className on main element', () => {
      render(<SidebarLayout {...defaultProps} className="custom-class" />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('custom-class');
    });
  });

  // ====== Mobile Menu Toggle Tests ======

  describe('Mobile Menu Toggle', () => {
    it('calls toggleMobile when hamburger button is clicked', () => {
      const toggleMobile = vi.fn();
      const store = createMockStore({ toggleMobile });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const button = screen.getByRole('button', { name: /open navigation menu/i });
      fireEvent.click(button);

      expect(toggleMobile).toHaveBeenCalledTimes(1);
    });

    it('shows close icon when mobile menu is open', () => {
      const store = createMockStore({ isMobileOpen: true });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const button = screen.getByRole('button', { name: /close navigation menu/i });
      expect(button).toBeInTheDocument();
    });

    it('has correct aria-expanded when closed', () => {
      const store = createMockStore({ isMobileOpen: false });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const button = screen.getByRole('button', { name: /open navigation menu/i });
      expect(button).toHaveAttribute('aria-expanded', 'false');
    });

    it('has correct aria-expanded when open', () => {
      const store = createMockStore({ isMobileOpen: true });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const button = screen.getByRole('button', { name: /close navigation menu/i });
      expect(button).toHaveAttribute('aria-expanded', 'true');
    });
  });

  // ====== Backdrop Tests ======

  describe('Mobile Backdrop', () => {
    it('does not render backdrop when mobile menu is closed', () => {
      const store = createMockStore({ isMobileOpen: false });

      render(<SidebarLayout {...defaultProps} store={store} />);

      expect(screen.queryByTestId('sidebar-backdrop')).not.toBeInTheDocument();
    });

    it('renders backdrop when mobile menu is open', () => {
      const store = createMockStore({ isMobileOpen: true });

      render(<SidebarLayout {...defaultProps} store={store} />);

      expect(screen.getByTestId('sidebar-backdrop')).toBeInTheDocument();
    });

    it('calls closeMobile when backdrop is clicked', () => {
      const closeMobile = vi.fn();
      const store = createMockStore({ isMobileOpen: true, closeMobile });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const backdrop = screen.getByTestId('sidebar-backdrop');
      fireEvent.click(backdrop);

      expect(closeMobile).toHaveBeenCalledTimes(1);
    });

    it('backdrop has aria-hidden attribute', () => {
      const store = createMockStore({ isMobileOpen: true });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const backdrop = screen.getByTestId('sidebar-backdrop');
      expect(backdrop).toHaveAttribute('aria-hidden', 'true');
    });
  });

  // ====== Mobile Sidebar Tests ======

  describe('Mobile Sidebar', () => {
    it('has translate-x-full class when closed', () => {
      const store = createMockStore({ isMobileOpen: false });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('-translate-x-full');
    });

    it('has translate-x-0 class when open', () => {
      const store = createMockStore({ isMobileOpen: true });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('translate-x-0');
    });

    it('has transition classes for animation', () => {
      render(<SidebarLayout {...defaultProps} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('transition-transform');
      expect(mobileSidebar).toHaveClass('duration-200');
    });
  });

  // ====== Desktop Sidebar Tests ======

  describe('Desktop Sidebar', () => {
    it('renders desktop sidebar container', () => {
      render(<SidebarLayout {...defaultProps} />);

      const desktopSidebar = screen.getByTestId('desktop-sidebar');
      expect(desktopSidebar).toBeInTheDocument();
    });

    it('desktop sidebar is fixed position', () => {
      render(<SidebarLayout {...defaultProps} />);

      const desktopSidebar = screen.getByTestId('desktop-sidebar');
      expect(desktopSidebar).toHaveClass('fixed');
    });

    it('desktop sidebar is hidden on mobile', () => {
      render(<SidebarLayout {...defaultProps} />);

      const desktopSidebar = screen.getByTestId('desktop-sidebar');
      expect(desktopSidebar).toHaveClass('hidden');
      expect(desktopSidebar).toHaveClass('md:block');
    });
  });

  // ====== Main Content Margin Tests ======

  describe('Main Content Margin', () => {
    it('has expanded margin when sidebar is not collapsed', () => {
      const store = createMockStore({ isCollapsed: false });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('md:ml-64');
    });

    it('has collapsed margin when sidebar is collapsed', () => {
      const store = createMockStore({ isCollapsed: true });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('md:ml-16');
    });

    it('has padding for mobile header', () => {
      render(<SidebarLayout {...defaultProps} />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('pt-14');
      expect(main).toHaveClass('md:pt-0');
    });

    it('has transition for smooth margin changes', () => {
      render(<SidebarLayout {...defaultProps} />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('transition-all');
      expect(main).toHaveClass('duration-200');
    });
  });

  // ====== Escape Key Tests ======

  describe('Escape Key Handling', () => {
    it('calls closeMobile when Escape is pressed and menu is open', () => {
      const closeMobile = vi.fn();
      const store = createMockStore({ isMobileOpen: true, closeMobile });

      render(<SidebarLayout {...defaultProps} store={store} />);

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(closeMobile).toHaveBeenCalledTimes(1);
    });

    it('does not call closeMobile when Escape is pressed and menu is closed', () => {
      const closeMobile = vi.fn();
      const store = createMockStore({ isMobileOpen: false, closeMobile });

      render(<SidebarLayout {...defaultProps} store={store} />);

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(closeMobile).not.toHaveBeenCalled();
    });

    it('does not call closeMobile for other keys', () => {
      const closeMobile = vi.fn();
      const store = createMockStore({ isMobileOpen: true, closeMobile });

      render(<SidebarLayout {...defaultProps} store={store} />);

      fireEvent.keyDown(document, { key: 'Enter' });
      fireEvent.keyDown(document, { key: 'Tab' });
      fireEvent.keyDown(document, { key: 'a' });

      expect(closeMobile).not.toHaveBeenCalled();
    });
  });

  // ====== Body Scroll Lock Tests ======

  describe('Body Scroll Lock', () => {
    it('sets body overflow to hidden when mobile menu opens', () => {
      const store = createMockStore({ isMobileOpen: true });

      render(<SidebarLayout {...defaultProps} store={store} />);

      expect(document.body.style.overflow).toBe('hidden');
    });

    it('removes body overflow hidden when mobile menu closes', () => {
      const { rerender } = render(
        <SidebarLayout
          {...defaultProps}
          store={createMockStore({ isMobileOpen: true })}
        />
      );

      expect(document.body.style.overflow).toBe('hidden');

      rerender(
        <SidebarLayout
          {...defaultProps}
          store={createMockStore({ isMobileOpen: false })}
        />
      );

      expect(document.body.style.overflow).toBe('');
    });

    it('cleans up body overflow on unmount', () => {
      const store = createMockStore({ isMobileOpen: true });

      const { unmount } = render(<SidebarLayout {...defaultProps} store={store} />);

      expect(document.body.style.overflow).toBe('hidden');

      unmount();

      expect(document.body.style.overflow).toBe('');
    });
  });

  // ====== Mobile Header Tests ======

  describe('Mobile Header', () => {
    it('is fixed at top', () => {
      render(<SidebarLayout {...defaultProps} />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('fixed');
      expect(header).toHaveClass('top-0');
    });

    it('has correct height', () => {
      render(<SidebarLayout {...defaultProps} />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('h-14');
    });

    it('is hidden on desktop', () => {
      render(<SidebarLayout {...defaultProps} />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('md:hidden');
    });
  });

  // ====== Accessibility Tests ======

  describe('Accessibility', () => {
    it('hamburger button has focus-visible styles', () => {
      render(<SidebarLayout {...defaultProps} />);

      const button = screen.getByRole('button', { name: /navigation menu/i });
      expect(button).toHaveClass('focus-visible:outline-none');
      expect(button).toHaveClass('focus-visible:ring-2');
    });

    it('main element is present for landmark navigation', () => {
      render(<SidebarLayout {...defaultProps} />);

      expect(screen.getByRole('main')).toBeInTheDocument();
    });

    it('header element is present for landmark navigation', () => {
      render(<SidebarLayout {...defaultProps} />);

      const header = document.querySelector('header');
      expect(header).toBeInTheDocument();
    });
  });

  // ====== Different Section Title Tests ======

  describe('Different Section Titles', () => {
    it('renders Knowledge Base title', () => {
      render(<SidebarLayout {...defaultProps} sectionTitle="Knowledge Base" />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
    });

    it('renders Article Analyzer title', () => {
      render(<SidebarLayout {...defaultProps} sectionTitle="Article Analyzer" />);

      expect(screen.getByText('Article Analyzer')).toBeInTheDocument();
    });

    it('renders Admin title', () => {
      render(<SidebarLayout {...defaultProps} sectionTitle="Admin" />);

      expect(screen.getByText('Admin')).toBeInTheDocument();
    });
  });
});
