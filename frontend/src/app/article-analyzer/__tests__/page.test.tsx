import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import ArticleAnalyzerPage from '../page';

// Mock Next.js navigation
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
  usePathname: () => '/article-analyzer',
}));

describe('ArticleAnalyzerPage', () => {
  it('renders the page title', () => {
    render(<ArticleAnalyzerPage />);
    expect(screen.getByRole('heading', { name: /article analyzer/i })).toBeInTheDocument();
  });

  it('renders description text', () => {
    render(<ArticleAnalyzerPage />);
    expect(screen.getByText(/analyze news articles for factual accuracy/i)).toBeInTheDocument();
  });

  it('renders the info banner about analysis layer', () => {
    render(<ArticleAnalyzerPage />);
    expect(screen.getByText(/analysis layer/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /knowledge base/i })).toBeInTheDocument();
  });

  describe('Feature Cards', () => {
    it('renders all three feature cards', () => {
      render(<ArticleAnalyzerPage />);
      expect(screen.getByText('Analyze Article')).toBeInTheDocument();
      expect(screen.getByText('Articles')).toBeInTheDocument();
      expect(screen.getByText('Extracted Entities')).toBeInTheDocument();
    });

    it('Analyze Article card is disabled with coming soon badge', () => {
      render(<ArticleAnalyzerPage />);
      expect(screen.getByText('Coming Soon')).toBeInTheDocument();
    });

    it('Articles card links to /article-analyzer/articles', () => {
      render(<ArticleAnalyzerPage />);
      const links = screen.getAllByRole('link');
      const articlesLink = links.find(
        (link) => link.getAttribute('href') === '/article-analyzer/articles'
      );
      expect(articlesLink).toBeInTheDocument();
    });

    it('Entities card links to /article-analyzer/entities', () => {
      render(<ArticleAnalyzerPage />);
      const links = screen.getAllByRole('link');
      const entitiesLink = links.find(
        (link) => link.getAttribute('href') === '/article-analyzer/entities'
      );
      expect(entitiesLink).toBeInTheDocument();
    });

    it('feature card descriptions are visible', () => {
      render(<ArticleAnalyzerPage />);
      expect(screen.getByText(/submit a new article url/i)).toBeInTheDocument();
      expect(screen.getByText(/browse previously analyzed articles/i)).toBeInTheDocument();
      expect(screen.getByText(/browse all entities extracted/i)).toBeInTheDocument();
    });
  });

  describe('How it Works Section', () => {
    it('renders the how it works section', () => {
      render(<ArticleAnalyzerPage />);
      expect(screen.getByRole('heading', { name: /how article analysis works/i })).toBeInTheDocument();
    });

    it('renders all three steps', () => {
      render(<ArticleAnalyzerPage />);
      expect(screen.getByText('Submit Article')).toBeInTheDocument();
      expect(screen.getByText('Extract & Analyze')).toBeInTheDocument();
      expect(screen.getByText('Review Results')).toBeInTheDocument();
    });

    it('step numbers are visible', () => {
      render(<ArticleAnalyzerPage />);
      expect(screen.getByText('1')).toBeInTheDocument();
      expect(screen.getByText('2')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('feature cards have proper focus styles', () => {
      render(<ArticleAnalyzerPage />);
      const links = screen.getAllByRole('link');
      // Only check the feature card links (those that start with /article-analyzer/)
      const featureLinks = links.filter(
        (link) => link.getAttribute('href')?.startsWith('/article-analyzer/')
      );
      featureLinks.forEach((link) => {
        expect(link).toHaveClass('focus-visible:ring-2');
      });
      expect(featureLinks.length).toBe(2); // Articles and Entities
    });
  });

  describe('Responsive Layout', () => {
    it('uses responsive grid classes for feature cards', () => {
      render(<ArticleAnalyzerPage />);
      const grid = document.querySelector('.grid.grid-cols-1.md\\:grid-cols-2.lg\\:grid-cols-3');
      expect(grid).toBeInTheDocument();
    });

    it('uses responsive grid classes for steps', () => {
      render(<ArticleAnalyzerPage />);
      const grids = document.querySelectorAll('.grid.grid-cols-1.md\\:grid-cols-3');
      expect(grids.length).toBeGreaterThan(0);
    });
  });
});
