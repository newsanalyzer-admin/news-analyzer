import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';

// Mock next/navigation
vi.mock('next/navigation', () => ({
  usePathname: () => '/knowledge-base/government/executive/president',
}));

// Mock next/link
vi.mock('next/link', () => ({
  default: ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  ),
}));

// Mock the government orgs hook
vi.mock('@/hooks/useGovernmentOrgs', () => ({
  useGovernmentOrgsHierarchy: () => ({
    data: [],
    isLoading: false,
    error: null,
    refetch: vi.fn(),
  }),
}));

// Import pages
import PresidentPage from '../president/page';
import VicePresidentPage from '../vice-president/page';
import EOPPage from '../eop/page';
import CabinetPage from '../cabinet/page';
import IndependentAgenciesPage from '../independent-agencies/page';
import CorporationsPage from '../corporations/page';

describe('Executive Branch Sub-Section Pages (UI-6.3)', () => {
  // ========== President Page ==========
  describe('PresidentPage', () => {
    it('renders the page title', () => {
      render(<PresidentPage />);
      expect(
        screen.getByRole('heading', { level: 1, name: /President of the United States/i })
      ).toBeInTheDocument();
    });

    it('renders back link to Executive Branch', () => {
      render(<PresidentPage />);
      const backLink = screen.getByRole('link', { name: /Back to Executive Branch/i });
      expect(backLink).toHaveAttribute('href', '/knowledge-base/government/executive');
    });

    it('renders constitutional powers section', () => {
      render(<PresidentPage />);
      expect(screen.getByRole('heading', { name: /Constitutional Powers/i })).toBeInTheDocument();
    });

    it('renders term and eligibility information', () => {
      render(<PresidentPage />);
      expect(screen.getByRole('heading', { name: /Term and Eligibility/i })).toBeInTheDocument();
      expect(screen.getByText(/Natural-born citizen/i)).toBeInTheDocument();
    });

    it('renders external links to official resources', () => {
      render(<PresidentPage />);
      const whiteHouseLink = screen.getByRole('link', { name: /The White House/i });
      expect(whiteHouseLink).toHaveAttribute('href', 'https://www.whitehouse.gov/');
      expect(whiteHouseLink).toHaveAttribute('target', '_blank');
    });
  });

  // ========== Vice President Page ==========
  describe('VicePresidentPage', () => {
    it('renders the page title', () => {
      render(<VicePresidentPage />);
      expect(
        screen.getByRole('heading', { level: 1, name: /Vice President of the United States/i })
      ).toBeInTheDocument();
    });

    it('renders constitutional roles section', () => {
      render(<VicePresidentPage />);
      expect(screen.getByRole('heading', { name: /Constitutional Roles/i })).toBeInTheDocument();
    });

    it('renders term and eligibility section', () => {
      render(<VicePresidentPage />);
      expect(screen.getByRole('heading', { name: /Term and Eligibility/i })).toBeInTheDocument();
    });

    it('renders historical note', () => {
      render(<VicePresidentPage />);
      expect(screen.getByRole('heading', { name: /Historical Note/i })).toBeInTheDocument();
    });

    it('has official resources section', () => {
      render(<VicePresidentPage />);
      expect(screen.getByRole('heading', { name: /Official Resources/i })).toBeInTheDocument();
    });
  });

  // ========== EOP Page ==========
  describe('EOPPage', () => {
    it('renders the page title', () => {
      render(<EOPPage />);
      expect(
        screen.getByRole('heading', { level: 1, name: /Executive Office of the President/i })
      ).toBeInTheDocument();
    });

    it('renders component agencies section', () => {
      render(<EOPPage />);
      expect(screen.getByRole('heading', { name: /EOP Component Agencies/i })).toBeInTheDocument();
    });

    it('has official resources section', () => {
      render(<EOPPage />);
      expect(screen.getByRole('heading', { name: /Official Resources/i })).toBeInTheDocument();
    });
  });

  // ========== Cabinet Page ==========
  describe('CabinetPage', () => {
    it('renders the page title', () => {
      render(<CabinetPage />);
      expect(
        screen.getByRole('heading', { level: 1, name: /Cabinet Departments/i })
      ).toBeInTheDocument();
    });

    it('renders the 15 departments section', () => {
      render(<CabinetPage />);
      expect(screen.getByRole('heading', { name: /The 15 Executive Departments/i })).toBeInTheDocument();
    });

    it('shows fallback department list when no data', () => {
      render(<CabinetPage />);
      // Check that the fallback grid is rendered (15 departments)
      const cards = screen.getAllByText(/^Department of/i);
      expect(cards.length).toBeGreaterThan(0);
    });

    it('has official resources section', () => {
      render(<CabinetPage />);
      expect(screen.getByRole('heading', { name: /Official Resources/i })).toBeInTheDocument();
    });
  });

  // ========== Independent Agencies Page ==========
  describe('IndependentAgenciesPage', () => {
    it('renders the page title', () => {
      render(<IndependentAgenciesPage />);
      expect(
        screen.getByRole('heading', { level: 1, name: /Independent Agencies/i })
      ).toBeInTheDocument();
    });

    it('renders types of agencies section', () => {
      render(<IndependentAgenciesPage />);
      expect(screen.getByRole('heading', { name: /Types of Independent Agencies/i })).toBeInTheDocument();
    });

    it('renders federal independent agencies section', () => {
      render(<IndependentAgenciesPage />);
      expect(screen.getByRole('heading', { name: /Federal Independent Agencies/i })).toBeInTheDocument();
    });

    it('has official resources section', () => {
      render(<IndependentAgenciesPage />);
      expect(screen.getByRole('heading', { name: /Official Resources/i })).toBeInTheDocument();
    });
  });

  // ========== Government Corporations Page ==========
  describe('CorporationsPage', () => {
    it('renders the page title', () => {
      render(<CorporationsPage />);
      expect(
        screen.getByRole('heading', { level: 1, name: /Government Corporations/i })
      ).toBeInTheDocument();
    });

    it('renders types of corporations section', () => {
      render(<CorporationsPage />);
      expect(screen.getByRole('heading', { name: /Types of Government Corporations/i })).toBeInTheDocument();
    });

    it('renders federal government corporations section', () => {
      render(<CorporationsPage />);
      expect(screen.getByRole('heading', { name: /Federal Government Corporations/i })).toBeInTheDocument();
    });

    it('has official resources section', () => {
      render(<CorporationsPage />);
      expect(screen.getByRole('heading', { name: /Official Resources/i })).toBeInTheDocument();
    });

    it('renders external links to corporation websites', () => {
      render(<CorporationsPage />);
      const links = screen.getAllByRole('link');
      const uspsLink = links.find(link => link.getAttribute('href') === 'https://www.usps.com/');
      expect(uspsLink).toBeDefined();
    });
  });

  // ========== Common Elements Tests ==========
  describe('Common Elements', () => {
    const pages = [
      { Page: PresidentPage, name: 'President' },
      { Page: VicePresidentPage, name: 'Vice President' },
      { Page: EOPPage, name: 'EOP' },
      { Page: CabinetPage, name: 'Cabinet' },
      { Page: IndependentAgenciesPage, name: 'Independent Agencies' },
      { Page: CorporationsPage, name: 'Corporations' },
    ];

    pages.forEach(({ Page, name }) => {
      it(`${name} page has back link to Executive Branch`, () => {
        render(<Page />);
        const backLink = screen.getByRole('link', { name: /Back to Executive Branch/i });
        expect(backLink).toHaveAttribute('href', '/knowledge-base/government/executive');
      });

      it(`${name} page has Official Resources section`, () => {
        render(<Page />);
        expect(screen.getByRole('heading', { name: /Official Resources/i })).toBeInTheDocument();
      });
    });
  });
});
