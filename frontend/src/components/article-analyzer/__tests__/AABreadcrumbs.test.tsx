import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AABreadcrumbs } from '../AABreadcrumbs';

// Mock next/navigation
const mockPathname = vi.fn();
vi.mock('next/navigation', () => ({
  usePathname: () => mockPathname(),
}));

describe('AABreadcrumbs', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockPathname.mockReturnValue('/article-analyzer');
  });

  // ====== Visibility Tests ======

  describe('Visibility', () => {
    it('returns null on article-analyzer landing page', () => {
      mockPathname.mockReturnValue('/article-analyzer');

      const { container } = render(<AABreadcrumbs />);

      expect(container.firstChild).toBeNull();
    });

    it('returns null for non-article-analyzer paths', () => {
      mockPathname.mockReturnValue('/knowledge-base');

      const { container } = render(<AABreadcrumbs />);

      expect(container.firstChild).toBeNull();
    });

    it('renders breadcrumbs on articles page', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      expect(screen.getByRole('navigation', { name: /breadcrumb/i })).toBeInTheDocument();
    });

    it('renders breadcrumbs on entities page', () => {
      mockPathname.mockReturnValue('/article-analyzer/entities');

      render(<AABreadcrumbs />);

      expect(screen.getByRole('navigation', { name: /breadcrumb/i })).toBeInTheDocument();
    });
  });

  // ====== Home Link Tests ======

  describe('Home Link', () => {
    it('includes home link', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const homeLink = screen.getByRole('link', { name: /home/i });
      expect(homeLink).toBeInTheDocument();
      expect(homeLink).toHaveAttribute('href', '/');
    });

    it('home link has Home icon', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const homeLink = screen.getByRole('link', { name: /home/i });
      const svg = homeLink.querySelector('svg');
      expect(svg).toBeInTheDocument();
    });
  });

  // ====== Articles Path Tests ======

  describe('Articles Path', () => {
    it('shows Article Analyzer and Articles breadcrumbs', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      expect(screen.getByText('Article Analyzer')).toBeInTheDocument();
      expect(screen.getByText('Articles')).toBeInTheDocument();
    });

    it('Article Analyzer is a link to /article-analyzer', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const aaLink = screen.getByRole('link', { name: /article analyzer/i });
      expect(aaLink).toHaveAttribute('href', '/article-analyzer');
    });

    it('Articles is the current page (not a link)', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const articlesSpan = screen.getByText('Articles');
      expect(articlesSpan.tagName).toBe('SPAN');
      expect(articlesSpan).toHaveAttribute('aria-current', 'page');
    });
  });

  // ====== Entities Path Tests ======

  describe('Entities Path', () => {
    it('shows Article Analyzer and Entities breadcrumbs', () => {
      mockPathname.mockReturnValue('/article-analyzer/entities');

      render(<AABreadcrumbs />);

      expect(screen.getByText('Article Analyzer')).toBeInTheDocument();
      expect(screen.getByText('Entities')).toBeInTheDocument();
    });

    it('Entities is the current page (not a link)', () => {
      mockPathname.mockReturnValue('/article-analyzer/entities');

      render(<AABreadcrumbs />);

      const entitiesSpan = screen.getByText('Entities');
      expect(entitiesSpan.tagName).toBe('SPAN');
      expect(entitiesSpan).toHaveAttribute('aria-current', 'page');
    });
  });

  // ====== Analyze Path Tests ======

  describe('Analyze Path', () => {
    it('shows Article Analyzer and Analyze Article breadcrumbs', () => {
      mockPathname.mockReturnValue('/article-analyzer/analyze');

      render(<AABreadcrumbs />);

      expect(screen.getByText('Article Analyzer')).toBeInTheDocument();
      expect(screen.getByText('Analyze Article')).toBeInTheDocument();
    });
  });

  // ====== Separator Tests ======

  describe('Separators', () => {
    it('renders chevron separators', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const nav = screen.getByRole('navigation', { name: /breadcrumb/i });
      const separators = nav.querySelectorAll('[aria-hidden="true"]');
      // At least 2 separators: Home > Article Analyzer > Articles
      expect(separators.length).toBeGreaterThanOrEqual(2);
    });
  });

  // ====== Styling Tests ======

  describe('Styling', () => {
    it('applies custom className', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs className="custom-class" />);

      const nav = screen.getByRole('navigation', { name: /breadcrumb/i });
      expect(nav).toHaveClass('custom-class');
    });

    it('has text-sm class', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const nav = screen.getByRole('navigation', { name: /breadcrumb/i });
      expect(nav).toHaveClass('text-sm');
    });

    it('has flex layout', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const nav = screen.getByRole('navigation', { name: /breadcrumb/i });
      expect(nav).toHaveClass('flex');
      expect(nav).toHaveClass('items-center');
    });

    it('current page has font-medium', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const currentPage = screen.getByText('Articles');
      expect(currentPage).toHaveClass('font-medium');
    });
  });

  // ====== Accessibility Tests ======

  describe('Accessibility', () => {
    it('has aria-label on nav element', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const nav = screen.getByRole('navigation', { name: /breadcrumb/i });
      expect(nav).toHaveAttribute('aria-label', 'Breadcrumb');
    });

    it('current page has aria-current attribute', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const currentPage = screen.getByText('Articles');
      expect(currentPage).toHaveAttribute('aria-current', 'page');
    });

    it('uses ordered list for semantics', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');

      render(<AABreadcrumbs />);

      const nav = screen.getByRole('navigation', { name: /breadcrumb/i });
      const ol = nav.querySelector('ol');
      expect(ol).toBeInTheDocument();
    });
  });
});
