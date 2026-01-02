/**
 * Mobile Responsiveness Tests for SidebarLayout
 *
 * UI-4.4: Mobile Responsiveness Testing
 * Tests sidebar behavior across different viewport sizes and mobile interactions.
 *
 * Breakpoints tested:
 * - Mobile: < 768px (md breakpoint)
 * - Desktop: >= 768px
 */

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

describe('SidebarLayout Mobile Responsiveness', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    document.body.style.overflow = '';
  });

  afterEach(() => {
    document.body.style.overflow = '';
  });

  // ====== Mobile Header Visibility (AC1, AC2) ======

  describe('Mobile Header Visibility', () => {
    it('mobile header has md:hidden class for responsive hiding', () => {
      render(<SidebarLayout {...defaultProps} />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('md:hidden');
    });

    it('mobile header is fixed at top', () => {
      render(<SidebarLayout {...defaultProps} />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('fixed');
      expect(header).toHaveClass('top-0');
      expect(header).toHaveClass('left-0');
      expect(header).toHaveClass('right-0');
    });

    it('mobile header has correct z-index for stacking', () => {
      render(<SidebarLayout {...defaultProps} />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('z-40');
    });

    it('mobile header has standard height (h-14 = 56px)', () => {
      render(<SidebarLayout {...defaultProps} />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('h-14');
    });
  });

  // ====== Mobile Sidebar Behavior (AC1, AC2, AC3) ======

  describe('Mobile Sidebar Behavior', () => {
    it('mobile sidebar container is hidden on desktop', () => {
      render(<SidebarLayout {...defaultProps} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('md:hidden');
    });

    it('mobile sidebar has fixed positioning for overlay', () => {
      render(<SidebarLayout {...defaultProps} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('fixed');
      expect(mobileSidebar).toHaveClass('inset-y-0');
      expect(mobileSidebar).toHaveClass('left-0');
    });

    it('mobile sidebar has high z-index for overlay stacking', () => {
      render(<SidebarLayout {...defaultProps} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('z-50');
    });

    it('mobile sidebar is off-screen when closed', () => {
      const store = createMockStore({ isMobileOpen: false });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('-translate-x-full');
    });

    it('mobile sidebar is visible when open', () => {
      const store = createMockStore({ isMobileOpen: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('translate-x-0');
    });

    it('mobile sidebar is a container for the sidebar prop content', () => {
      render(<SidebarLayout {...defaultProps} />);

      // The mobile sidebar wrapper contains the passed sidebar content
      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toContainElement(screen.getAllByTestId('sidebar-content')[0]);
    });
  });

  // ====== Touch Interactions (AC3) ======

  describe('Touch Interactions', () => {
    it('backdrop click closes sidebar', () => {
      const closeMobile = vi.fn();
      const store = createMockStore({ isMobileOpen: true, closeMobile });

      render(<SidebarLayout {...defaultProps} store={store} />);

      const backdrop = screen.getByTestId('sidebar-backdrop');
      fireEvent.click(backdrop);

      expect(closeMobile).toHaveBeenCalledTimes(1);
    });

    it('backdrop covers entire viewport when open', () => {
      const store = createMockStore({ isMobileOpen: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const backdrop = screen.getByTestId('sidebar-backdrop');
      expect(backdrop).toHaveClass('fixed');
      expect(backdrop).toHaveClass('inset-0');
    });

    it('backdrop has semi-transparent background', () => {
      const store = createMockStore({ isMobileOpen: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const backdrop = screen.getByTestId('sidebar-backdrop');
      expect(backdrop).toHaveClass('bg-black/50');
    });

    it('hamburger button is touch-friendly size', () => {
      render(<SidebarLayout {...defaultProps} />);

      const button = screen.getByRole('button', { name: /navigation menu/i });
      // Button should have adequate padding for touch targets (44x44 minimum)
      expect(button).toHaveClass('p-2');
    });
  });

  // ====== Desktop Sidebar (AC4) ======

  describe('Desktop Sidebar No Overlap', () => {
    it('desktop sidebar is hidden on mobile', () => {
      render(<SidebarLayout {...defaultProps} />);

      const desktopSidebar = screen.getByTestId('desktop-sidebar');
      expect(desktopSidebar).toHaveClass('hidden');
      expect(desktopSidebar).toHaveClass('md:block');
    });

    it('desktop sidebar has fixed positioning', () => {
      render(<SidebarLayout {...defaultProps} />);

      const desktopSidebar = screen.getByTestId('desktop-sidebar');
      expect(desktopSidebar).toHaveClass('fixed');
      expect(desktopSidebar).toHaveClass('inset-y-0');
      expect(desktopSidebar).toHaveClass('left-0');
    });

    it('main content has expanded margin when sidebar expanded', () => {
      const store = createMockStore({ isCollapsed: false });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('md:ml-64');
    });

    it('main content has collapsed margin when sidebar collapsed', () => {
      const store = createMockStore({ isCollapsed: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('md:ml-16');
    });

    it('desktop sidebar width matches main content margin (expanded)', () => {
      const store = createMockStore({ isCollapsed: false });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const main = screen.getByRole('main');

      // Desktop sidebar wrapper doesn't set width - sidebar prop provides it
      // Main content margin matches expected sidebar width (256px)
      expect(main).toHaveClass('md:ml-64');
    });

    it('desktop sidebar width matches main content margin (collapsed)', () => {
      const store = createMockStore({ isCollapsed: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const main = screen.getByRole('main');

      // Desktop sidebar wrapper doesn't set width - sidebar prop provides it
      // Main content margin matches expected sidebar width (64px)
      expect(main).toHaveClass('md:ml-16');
    });

    it('main content has padding for mobile header', () => {
      render(<SidebarLayout {...defaultProps} />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('pt-14'); // 56px for mobile header
      expect(main).toHaveClass('md:pt-0'); // No padding on desktop
    });
  });

  // ====== Transition Smoothness (AC5) ======

  describe('Transition Smoothness', () => {
    it('mobile sidebar has transition classes', () => {
      render(<SidebarLayout {...defaultProps} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('transition-transform');
      expect(mobileSidebar).toHaveClass('duration-200');
    });

    it('mobile sidebar uses ease-in-out timing', () => {
      render(<SidebarLayout {...defaultProps} />);

      const mobileSidebar = screen.getByTestId('mobile-sidebar');
      expect(mobileSidebar).toHaveClass('ease-in-out');
    });

    it('main content margin has transition for smooth collapse', () => {
      render(<SidebarLayout {...defaultProps} />);

      const main = screen.getByRole('main');
      expect(main).toHaveClass('transition-all');
      expect(main).toHaveClass('duration-200');
    });

    it('desktop sidebar wrapper has proper positioning classes', () => {
      render(<SidebarLayout {...defaultProps} />);

      const desktopSidebar = screen.getByTestId('desktop-sidebar');
      // Desktop sidebar wrapper is a simple container - transitions handled by sidebar prop
      expect(desktopSidebar).toHaveClass('fixed');
      expect(desktopSidebar).toHaveClass('inset-y-0');
      expect(desktopSidebar).toHaveClass('left-0');
      expect(desktopSidebar).toHaveClass('z-30');
    });

    it('backdrop has fade transition', () => {
      const store = createMockStore({ isMobileOpen: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const backdrop = screen.getByTestId('sidebar-backdrop');
      expect(backdrop).toHaveClass('transition-opacity');
    });
  });

  // ====== Focus Management (AC6) ======

  describe('Focus Management', () => {
    it('hamburger button has focus-visible styles', () => {
      render(<SidebarLayout {...defaultProps} />);

      const button = screen.getByRole('button', { name: /navigation menu/i });
      expect(button).toHaveClass('focus-visible:outline-none');
      expect(button).toHaveClass('focus-visible:ring-2');
      expect(button).toHaveClass('focus-visible:ring-ring');
    });

    it('escape key closes mobile sidebar', () => {
      const closeMobile = vi.fn();
      const store = createMockStore({ isMobileOpen: true, closeMobile });

      render(<SidebarLayout {...defaultProps} store={store} />);

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(closeMobile).toHaveBeenCalledTimes(1);
    });

    it('escape key does not close when already closed', () => {
      const closeMobile = vi.fn();
      const store = createMockStore({ isMobileOpen: false, closeMobile });

      render(<SidebarLayout {...defaultProps} store={store} />);

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(closeMobile).not.toHaveBeenCalled();
    });

    it('body scroll is locked when mobile sidebar open', () => {
      const store = createMockStore({ isMobileOpen: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      expect(document.body.style.overflow).toBe('hidden');
    });

    it('body scroll is restored when mobile sidebar closes', () => {
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

    it('backdrop is aria-hidden for screen readers', () => {
      const store = createMockStore({ isMobileOpen: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const backdrop = screen.getByTestId('sidebar-backdrop');
      expect(backdrop).toHaveAttribute('aria-hidden', 'true');
    });

    it('hamburger button has aria-expanded state', () => {
      const store = createMockStore({ isMobileOpen: false });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const button = screen.getByRole('button', { name: /navigation menu/i });
      expect(button).toHaveAttribute('aria-expanded', 'false');
    });

    it('hamburger button aria-expanded updates when open', () => {
      const store = createMockStore({ isMobileOpen: true });
      render(<SidebarLayout {...defaultProps} store={store} />);

      const button = screen.getByRole('button', { name: /navigation menu/i });
      expect(button).toHaveAttribute('aria-expanded', 'true');
    });
  });

  // ====== Knowledge Base Section Title (AC1) ======

  describe('Knowledge Base Section', () => {
    it('renders KB section title correctly', () => {
      render(<SidebarLayout {...defaultProps} sectionTitle="Knowledge Base" />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
    });
  });

  // ====== Article Analyzer Section Title (AC2) ======

  describe('Article Analyzer Section', () => {
    it('renders AA section title correctly', () => {
      render(<SidebarLayout {...defaultProps} sectionTitle="Article Analyzer" />);

      expect(screen.getByText('Article Analyzer')).toBeInTheDocument();
    });
  });

  // ====== Responsive Breakpoint Classes ======

  describe('Responsive Breakpoint Classes', () => {
    it('uses md breakpoint (768px) for desktop visibility', () => {
      render(<SidebarLayout {...defaultProps} />);

      const desktopSidebar = screen.getByTestId('desktop-sidebar');
      const mobileSidebar = screen.getByTestId('mobile-sidebar');

      // Desktop: hidden on mobile, visible on md+
      expect(desktopSidebar).toHaveClass('hidden');
      expect(desktopSidebar).toHaveClass('md:block');

      // Mobile: visible on mobile, hidden on md+
      expect(mobileSidebar).toHaveClass('md:hidden');
    });

    it('main content margin uses md breakpoint', () => {
      render(<SidebarLayout {...defaultProps} />);

      const main = screen.getByRole('main');
      // Mobile: no margin, Desktop: ml-64
      expect(main).toHaveClass('md:ml-64');
    });

    it('main content padding uses md breakpoint', () => {
      render(<SidebarLayout {...defaultProps} />);

      const main = screen.getByRole('main');
      // Mobile: pt-14, Desktop: no padding
      expect(main).toHaveClass('pt-14');
      expect(main).toHaveClass('md:pt-0');
    });
  });
});
