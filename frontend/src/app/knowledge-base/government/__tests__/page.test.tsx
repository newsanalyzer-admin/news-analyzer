import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import GovernmentPage from '../page';

// Mock Next.js navigation
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
  usePathname: () => '/knowledge-base/government',
}));

describe('GovernmentPage', () => {
  it('renders the page title', () => {
    render(<GovernmentPage />);
    expect(screen.getByRole('heading', { name: /u\.s\. federal government/i })).toBeInTheDocument();
  });

  it('renders description text', () => {
    render(<GovernmentPage />);
    expect(screen.getByText(/explore the structure of the federal government/i)).toBeInTheDocument();
  });

  it('renders back link to Knowledge Base', () => {
    render(<GovernmentPage />);
    const backLink = screen.getByRole('link', { name: /back to knowledge base/i });
    expect(backLink).toHaveAttribute('href', '/knowledge-base');
  });

  describe('Branch Cards', () => {
    it('renders Executive Branch card', () => {
      render(<GovernmentPage />);
      expect(screen.getByRole('link', { name: /executive branch/i })).toBeInTheDocument();
    });

    it('renders Legislative Branch card', () => {
      render(<GovernmentPage />);
      expect(screen.getByRole('link', { name: /legislative branch/i })).toBeInTheDocument();
    });

    it('renders Judicial Branch card', () => {
      render(<GovernmentPage />);
      expect(screen.getByRole('link', { name: /judicial branch/i })).toBeInTheDocument();
    });

    it('Executive card links to correct route', () => {
      render(<GovernmentPage />);
      const link = screen.getByRole('link', { name: /executive branch/i });
      expect(link).toHaveAttribute('href', '/knowledge-base/government/executive');
    });

    it('Legislative card links to correct route', () => {
      render(<GovernmentPage />);
      const link = screen.getByRole('link', { name: /legislative branch/i });
      expect(link).toHaveAttribute('href', '/knowledge-base/government/legislative');
    });

    it('Judicial card links to correct route', () => {
      render(<GovernmentPage />);
      const link = screen.getByRole('link', { name: /judicial branch/i });
      expect(link).toHaveAttribute('href', '/knowledge-base/government/judicial');
    });
  });

  describe('Branch Descriptions', () => {
    it('shows Executive branch description', () => {
      render(<GovernmentPage />);
      expect(screen.getByText(/president.*cabinet departments/i)).toBeInTheDocument();
    });

    it('shows Legislative branch description', () => {
      render(<GovernmentPage />);
      expect(screen.getByText(/senate.*house of representatives/i)).toBeInTheDocument();
    });

    it('shows Judicial branch description', () => {
      render(<GovernmentPage />);
      expect(screen.getByText(/supreme court.*courts of appeals/i)).toBeInTheDocument();
    });
  });

  describe('Organizations Link', () => {
    it('shows link to Organizations flat list', () => {
      render(<GovernmentPage />);
      const orgLink = screen.getByRole('link', { name: /organizations/i });
      expect(orgLink).toHaveAttribute('href', '/knowledge-base/organizations');
    });
  });

  describe('Accessibility', () => {
    it('branch cards are focusable', () => {
      render(<GovernmentPage />);
      const branchCards = [
        screen.getByRole('link', { name: /executive branch/i }),
        screen.getByRole('link', { name: /legislative branch/i }),
        screen.getByRole('link', { name: /judicial branch/i }),
      ];
      branchCards.forEach((card) => {
        expect(card).toHaveClass('focus-visible:ring-2');
      });
    });
  });
});
