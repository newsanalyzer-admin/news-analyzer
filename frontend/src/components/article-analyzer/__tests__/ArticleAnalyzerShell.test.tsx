import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ArticleAnalyzerShell } from '../ArticleAnalyzerShell';

// Mock usePathname
const mockPathname = vi.fn();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
  usePathname: () => mockPathname(),
}));

describe('ArticleAnalyzerShell', () => {
  beforeEach(() => {
    mockPathname.mockReturnValue('/article-analyzer');
  });

  it('renders children', () => {
    render(
      <ArticleAnalyzerShell>
        <div data-testid="child-content">Test content</div>
      </ArticleAnalyzerShell>
    );
    expect(screen.getByTestId('child-content')).toBeInTheDocument();
  });

  describe('Header', () => {
    it('renders the Article Analyzer title link', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const titleLink = screen.getByRole('link', { name: /article analyzer/i });
      expect(titleLink).toHaveAttribute('href', '/article-analyzer');
    });

    it('renders Knowledge Base link in header', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const kbLinks = screen.getAllByRole('link').filter(
        (link) => link.getAttribute('href') === '/knowledge-base'
      );
      expect(kbLinks.length).toBeGreaterThan(0);
    });

    it('renders Admin link in header', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const adminLink = screen.getByRole('link', { name: /admin/i });
      expect(adminLink).toHaveAttribute('href', '/admin');
    });
  });

  describe('Navigation', () => {
    it('renders the navigation with aria-label', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const nav = screen.getByRole('navigation', { name: /article analyzer navigation/i });
      expect(nav).toBeInTheDocument();
    });

    it('renders Analyze Article nav item as disabled', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      expect(screen.getByText('Analyze Article')).toBeInTheDocument();
      expect(screen.getByText('Soon')).toBeInTheDocument();
    });

    it('renders Articles nav item as link', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const articlesLink = screen.getByRole('link', { name: /articles/i });
      expect(articlesLink).toHaveAttribute('href', '/article-analyzer/articles');
    });

    it('renders Entities nav item as link', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const entitiesLink = screen.getByRole('link', { name: /entities/i });
      expect(entitiesLink).toHaveAttribute('href', '/article-analyzer/entities');
    });
  });

  describe('Active Navigation State', () => {
    it('marks Articles link as active when on articles page', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const articlesLink = screen.getByRole('link', { name: /articles/i });
      expect(articlesLink).toHaveAttribute('aria-current', 'page');
      expect(articlesLink).toHaveClass('bg-primary');
    });

    it('marks Entities link as active when on entities page', () => {
      mockPathname.mockReturnValue('/article-analyzer/entities');
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const entitiesLink = screen.getByRole('link', { name: /entities/i });
      expect(entitiesLink).toHaveAttribute('aria-current', 'page');
      expect(entitiesLink).toHaveClass('bg-primary');
    });

    it('does not mark links as active when on landing page', () => {
      mockPathname.mockReturnValue('/article-analyzer');
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const articlesLink = screen.getByRole('link', { name: /articles/i });
      const entitiesLink = screen.getByRole('link', { name: /entities/i });
      expect(articlesLink).not.toHaveAttribute('aria-current');
      expect(entitiesLink).not.toHaveAttribute('aria-current');
    });
  });

  describe('Breadcrumbs', () => {
    it('does not show breadcrumbs on landing page', () => {
      mockPathname.mockReturnValue('/article-analyzer');
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const breadcrumbNav = screen.queryByRole('navigation', { name: /breadcrumb/i });
      expect(breadcrumbNav).not.toBeInTheDocument();
    });

    it('shows breadcrumbs on sub-pages', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const breadcrumbNav = screen.getByRole('navigation', { name: /breadcrumb/i });
      expect(breadcrumbNav).toBeInTheDocument();
    });

    it('shows correct breadcrumb trail for articles page', () => {
      mockPathname.mockReturnValue('/article-analyzer/articles');
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const breadcrumbNav = screen.getByRole('navigation', { name: /breadcrumb/i });
      // Check breadcrumb contains the expected text
      expect(breadcrumbNav).toHaveTextContent('Article Analyzer');
      expect(breadcrumbNav).toHaveTextContent('Articles');
    });

    it('shows correct breadcrumb trail for entities page', () => {
      mockPathname.mockReturnValue('/article-analyzer/entities');
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const breadcrumbNav = screen.getByRole('navigation', { name: /breadcrumb/i });
      // Check breadcrumb contains the expected text
      expect(breadcrumbNav).toHaveTextContent('Article Analyzer');
      expect(breadcrumbNav).toHaveTextContent('Entities');
    });
  });

  describe('Accessibility', () => {
    it('navigation links have focus-visible styles', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const articlesLink = screen.getByRole('link', { name: /articles/i });
      expect(articlesLink).toHaveClass('focus-visible:ring-2');
    });

    it('header has sticky positioning', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const header = document.querySelector('header');
      expect(header).toHaveClass('sticky');
      expect(header).toHaveClass('top-0');
    });

    it('main content area exists', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const main = document.querySelector('main');
      expect(main).toBeInTheDocument();
      expect(main).toHaveClass('flex-1');
    });
  });

  describe('Keyboard Navigation', () => {
    it('all navigation links are tabbable', () => {
      render(
        <ArticleAnalyzerShell>
          <div>Content</div>
        </ArticleAnalyzerShell>
      );
      const links = screen.getAllByRole('link');
      links.forEach((link) => {
        // Links should not have negative tabindex
        expect(link).not.toHaveAttribute('tabindex', '-1');
      });
    });
  });
});
