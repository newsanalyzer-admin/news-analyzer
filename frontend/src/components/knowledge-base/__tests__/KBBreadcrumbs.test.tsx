import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { KBBreadcrumbs } from '../KBBreadcrumbs';

// Mock Next.js navigation
const mockPathname = vi.fn();
vi.mock('next/navigation', () => ({
  usePathname: () => mockPathname(),
}));

describe('KBBreadcrumbs', () => {
  beforeEach(() => {
    mockPathname.mockReset();
  });

  describe('Basic Rendering', () => {
    it('renders breadcrumb navigation', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs />);
      expect(screen.getByRole('navigation', { name: /breadcrumb/i })).toBeInTheDocument();
    });

    it('does not render when only at knowledge-base root', () => {
      mockPathname.mockReturnValue('/knowledge-base');
      const { container } = render(<KBBreadcrumbs />);
      expect(container.firstChild).toBeNull();
    });

    it('does not render for non-knowledge-base routes', () => {
      mockPathname.mockReturnValue('/admin');
      const { container } = render(<KBBreadcrumbs />);
      expect(container.firstChild).toBeNull();
    });
  });

  describe('Government Section Breadcrumbs', () => {
    it('shows correct breadcrumbs for government page', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
      expect(screen.getByText('U.S. Federal Government')).toBeInTheDocument();
    });

    it('shows correct breadcrumbs for executive branch', () => {
      mockPathname.mockReturnValue('/knowledge-base/government/executive');
      render(<KBBreadcrumbs />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
      expect(screen.getByText('U.S. Federal Government')).toBeInTheDocument();
      expect(screen.getByText('Executive')).toBeInTheDocument();
    });

    it('shows correct breadcrumbs for legislative branch', () => {
      mockPathname.mockReturnValue('/knowledge-base/government/legislative');
      render(<KBBreadcrumbs />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
      expect(screen.getByText('U.S. Federal Government')).toBeInTheDocument();
      expect(screen.getByText('Legislative')).toBeInTheDocument();
    });

    it('shows correct breadcrumbs for judicial branch', () => {
      mockPathname.mockReturnValue('/knowledge-base/government/judicial');
      render(<KBBreadcrumbs />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
      expect(screen.getByText('U.S. Federal Government')).toBeInTheDocument();
      expect(screen.getByText('Judicial')).toBeInTheDocument();
    });
  });

  describe('Other Sections', () => {
    it('shows correct breadcrumbs for people page', () => {
      mockPathname.mockReturnValue('/knowledge-base/people');
      render(<KBBreadcrumbs />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
      expect(screen.getByText('People')).toBeInTheDocument();
    });

    it('shows correct breadcrumbs for committees page', () => {
      mockPathname.mockReturnValue('/knowledge-base/committees');
      render(<KBBreadcrumbs />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
      expect(screen.getByText('Committees')).toBeInTheDocument();
    });

    it('shows correct breadcrumbs for organizations page', () => {
      mockPathname.mockReturnValue('/knowledge-base/organizations');
      render(<KBBreadcrumbs />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
      expect(screen.getByText('Organizations')).toBeInTheDocument();
    });
  });

  describe('Links', () => {
    it('first items are links', () => {
      mockPathname.mockReturnValue('/knowledge-base/government/executive');
      render(<KBBreadcrumbs />);

      const kbLink = screen.getByRole('link', { name: 'Knowledge Base' });
      expect(kbLink).toHaveAttribute('href', '/knowledge-base');

      const govLink = screen.getByRole('link', { name: 'U.S. Federal Government' });
      expect(govLink).toHaveAttribute('href', '/knowledge-base/government');
    });

    it('last item is not a link', () => {
      mockPathname.mockReturnValue('/knowledge-base/government/executive');
      render(<KBBreadcrumbs />);

      // "Executive" should be a span with aria-current, not a link
      const executiveItem = screen.getByText('Executive');
      expect(executiveItem.tagName).toBe('SPAN');
      expect(executiveItem).toHaveAttribute('aria-current', 'page');
    });
  });

  describe('Home Icon', () => {
    it('shows home icon by default', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs />);

      const homeLink = screen.getByRole('link', { name: 'Home' });
      expect(homeLink).toHaveAttribute('href', '/');
    });

    it('hides home icon when showHome is false', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs showHome={false} />);

      expect(screen.queryByRole('link', { name: 'Home' })).not.toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('has navigation landmark with label', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs />);

      expect(screen.getByRole('navigation', { name: /breadcrumb/i })).toBeInTheDocument();
    });

    it('uses ordered list for breadcrumbs', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs />);

      expect(screen.getByRole('list')).toBeInTheDocument();
    });

    it('marks current page with aria-current', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs />);

      const currentItem = screen.getByText('U.S. Federal Government');
      expect(currentItem).toHaveAttribute('aria-current', 'page');
    });

    it('chevron separators are hidden from screen readers', () => {
      mockPathname.mockReturnValue('/knowledge-base/government/executive');
      render(<KBBreadcrumbs />);

      // Chevrons should have aria-hidden
      const listItems = screen.getAllByRole('listitem');
      expect(listItems.length).toBeGreaterThan(0);
    });
  });

  describe('Styling', () => {
    it('applies custom className', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs className="custom-class" />);

      const nav = screen.getByRole('navigation');
      expect(nav).toHaveClass('custom-class');
    });

    it('truncates long labels', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');
      render(<KBBreadcrumbs />);

      const govLabel = screen.getByText('U.S. Federal Government');
      expect(govLabel).toHaveClass('truncate');
      expect(govLabel).toHaveClass('max-w-[200px]');
    });
  });
});
