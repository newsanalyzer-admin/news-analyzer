import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import ExecutiveBranchPage from '../page';

// Mock next/link
vi.mock('next/link', () => ({
  default: ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  ),
}));

describe('ExecutiveBranchPage (UI-6.2)', () => {
  describe('Page Header', () => {
    it('renders Executive Branch title', () => {
      render(<ExecutiveBranchPage />);
      expect(screen.getByRole('heading', { level: 1, name: /Executive Branch/i })).toBeInTheDocument();
    });

    it('renders back link to U.S. Federal Government', () => {
      render(<ExecutiveBranchPage />);
      const backLink = screen.getByRole('link', { name: /Back to U.S. Federal Government/i });
      expect(backLink).toHaveAttribute('href', '/knowledge-base/government');
    });
  });

  describe('Educational Content', () => {
    it('renders educational description about executive power', () => {
      render(<ExecutiveBranchPage />);
      expect(screen.getByText(/responsible for implementing and enforcing the laws/i)).toBeInTheDocument();
    });

    it('renders Article II quote', () => {
      render(<ExecutiveBranchPage />);
      expect(screen.getByText(/The executive Power shall be vested in a President/i)).toBeInTheDocument();
    });

    it('renders link to Constitution Article II', () => {
      render(<ExecutiveBranchPage />);
      const articleLink = screen.getByRole('link', { name: /Article II, Section 1/i });
      expect(articleLink).toHaveAttribute('href', 'https://constitution.congress.gov/constitution/article-2/');
      expect(articleLink).toHaveAttribute('target', '_blank');
      expect(articleLink).toHaveAttribute('rel', 'noopener noreferrer');
    });
  });

  describe('Navigation Cards', () => {
    it('renders Explore the Executive Branch heading', () => {
      render(<ExecutiveBranchPage />);
      expect(screen.getByRole('heading', { level: 2, name: /Explore the Executive Branch/i })).toBeInTheDocument();
    });

    it('renders 6 sub-section cards', () => {
      render(<ExecutiveBranchPage />);
      const cards = [
        'President of the United States',
        'Vice President of the United States',
        'Executive Office of the President',
        'Cabinet Departments',
        'Independent Agencies',
        'Government Corporations',
      ];

      cards.forEach((title) => {
        expect(screen.getByRole('heading', { level: 3, name: title })).toBeInTheDocument();
      });
    });

    it('renders President card with correct route', () => {
      render(<ExecutiveBranchPage />);
      const presidentLink = screen.getByRole('link', { name: /President of the United States.*Article II/i });
      expect(presidentLink).toHaveAttribute('href', '/knowledge-base/government/executive/president');
    });

    it('renders Vice President card with correct route', () => {
      render(<ExecutiveBranchPage />);
      const vpLink = screen.getByRole('link', { name: /Vice President of the United States.*succession/i });
      expect(vpLink).toHaveAttribute('href', '/knowledge-base/government/executive/vice-president');
    });

    it('renders EOP card with correct route', () => {
      render(<ExecutiveBranchPage />);
      const eopLink = screen.getByRole('link', { name: /Executive Office of the President.*advisory/i });
      expect(eopLink).toHaveAttribute('href', '/knowledge-base/government/executive/eop');
    });

    it('renders Cabinet card with correct route', () => {
      render(<ExecutiveBranchPage />);
      const cabinetLink = screen.getByRole('link', { name: /Cabinet Departments.*15 executive/i });
      expect(cabinetLink).toHaveAttribute('href', '/knowledge-base/government/executive/cabinet');
    });

    it('renders Independent Agencies card with correct route', () => {
      render(<ExecutiveBranchPage />);
      const agenciesLink = screen.getByRole('link', { name: /Independent Agencies.*EPA.*NASA/i });
      expect(agenciesLink).toHaveAttribute('href', '/knowledge-base/government/executive/independent-agencies');
    });

    it('renders Government Corporations card with correct route', () => {
      render(<ExecutiveBranchPage />);
      const corpsLink = screen.getByRole('link', { name: /Government Corporations.*USPS.*Amtrak/i });
      expect(corpsLink).toHaveAttribute('href', '/knowledge-base/government/executive/corporations');
    });
  });

  describe('Additional Information', () => {
    it('renders note with link to Organizations section', () => {
      render(<ExecutiveBranchPage />);
      const orgLink = screen.getByRole('link', { name: /Organizations/i });
      expect(orgLink).toHaveAttribute('href', '/knowledge-base/organizations');
    });
  });

  describe('Accessibility', () => {
    it('has proper heading hierarchy', () => {
      render(<ExecutiveBranchPage />);

      // h1 - main title
      const h1 = screen.getByRole('heading', { level: 1 });
      expect(h1).toHaveTextContent(/Executive Branch/i);

      // h2 - section heading
      const h2 = screen.getByRole('heading', { level: 2 });
      expect(h2).toHaveTextContent(/Explore the Executive Branch/i);

      // h3 - card titles (6 total)
      const h3s = screen.getAllByRole('heading', { level: 3 });
      expect(h3s).toHaveLength(6);
    });

    it('external link has proper attributes', () => {
      render(<ExecutiveBranchPage />);
      const externalLink = screen.getByRole('link', { name: /Article II, Section 1/i });
      expect(externalLink).toHaveAttribute('target', '_blank');
      expect(externalLink).toHaveAttribute('rel', 'noopener noreferrer');
    });
  });
});
