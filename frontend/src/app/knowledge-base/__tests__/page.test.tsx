import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import KnowledgeBasePage from '../page';

// Mock Next.js navigation
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
  usePathname: () => '/knowledge-base',
}));

describe('KnowledgeBasePage', () => {
  it('renders the page title', () => {
    render(<KnowledgeBasePage />);
    expect(screen.getByRole('heading', { name: /knowledge base/i })).toBeInTheDocument();
  });

  it('renders description text', () => {
    render(<KnowledgeBasePage />);
    expect(screen.getByText(/explore authoritative reference data/i)).toBeInTheDocument();
  });

  describe('Category Cards', () => {
    it('renders U.S. Federal Government card', () => {
      render(<KnowledgeBasePage />);
      const link = screen.getByRole('link', { name: /u\.s\. federal government/i });
      expect(link).toHaveAttribute('href', '/knowledge-base/government');
    });

    it('card title is visible', () => {
      render(<KnowledgeBasePage />);
      expect(screen.getByText('U.S. Federal Government')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('category cards are focusable', () => {
      render(<KnowledgeBasePage />);
      const links = screen.getAllByRole('link');
      links.forEach((link) => {
        expect(link).toHaveClass('focus-visible:ring-2');
      });
    });
  });

  describe('Responsive Layout', () => {
    it('uses responsive grid classes', () => {
      render(<KnowledgeBasePage />);
      const grid = screen.getByRole('heading', { name: /knowledge base/i })
        .closest('div')
        ?.parentElement?.querySelector('.grid');
      expect(grid).toHaveClass('grid-cols-1');
      expect(grid).toHaveClass('md:grid-cols-2');
      expect(grid).toHaveClass('lg:grid-cols-3');
    });
  });
});
