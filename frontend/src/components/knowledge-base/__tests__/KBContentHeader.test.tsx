import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { KBContentHeader } from '../KBContentHeader';

// Mock next/navigation
const mockPathname = vi.fn();
vi.mock('next/navigation', () => ({
  usePathname: () => mockPathname(),
}));

// Mock child components
vi.mock('../EntityTypeSelector', () => ({
  EntityTypeSelector: ({ onNavigate }: { onNavigate?: () => void }) => (
    <div data-testid="entity-type-selector" onClick={onNavigate}>
      Entity Type Selector
    </div>
  ),
}));

vi.mock('../ViewModeSelector', () => ({
  ViewModeSelector: () => <div data-testid="view-mode-selector">View Mode Selector</div>,
}));

vi.mock('../SearchBar', () => ({
  SearchBar: ({
    entityType,
    entityLabel,
    className,
  }: {
    entityType: string;
    entityLabel: string;
    className?: string;
  }) => (
    <div data-testid="search-bar" data-entity-type={entityType} data-entity-label={entityLabel} className={className}>
      Search Bar
    </div>
  ),
}));

// Mock entityTypes config
vi.mock('@/lib/config/entityTypes', () => ({
  getEntityTypeConfig: (type: string) => {
    const configs: Record<string, { id: string; label: string } | null> = {
      organizations: { id: 'organizations', label: 'Organizations' },
      persons: { id: 'persons', label: 'Persons' },
      committees: { id: 'committees', label: 'Committees' },
      government: null,
      people: null,
    };
    return configs[type] ?? null;
  },
}));

describe('KBContentHeader', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockPathname.mockReturnValue('/knowledge-base');
  });

  // ====== Rendering Tests ======

  describe('Rendering', () => {
    it('renders the header element', () => {
      render(<KBContentHeader />);

      const header = document.querySelector('header');
      expect(header).toBeInTheDocument();
    });

    it('renders EntityTypeSelector', () => {
      render(<KBContentHeader />);

      expect(screen.getByTestId('entity-type-selector')).toBeInTheDocument();
    });

    it('renders ViewModeSelector', () => {
      render(<KBContentHeader />);

      expect(screen.getByTestId('view-mode-selector')).toBeInTheDocument();
    });

    it('renders Article Analyzer link', () => {
      render(<KBContentHeader />);

      const link = screen.getByRole('link', { name: /article analyzer/i });
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute('href', '/article-analyzer');
    });

    it('applies custom className', () => {
      render(<KBContentHeader className="custom-class" />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('custom-class');
    });
  });

  // ====== SearchBar Visibility Tests ======

  describe('SearchBar Visibility', () => {
    it('does not render SearchBar on landing page', () => {
      mockPathname.mockReturnValue('/knowledge-base');

      render(<KBContentHeader />);

      expect(screen.queryByTestId('search-bar')).not.toBeInTheDocument();
    });

    it('does not render SearchBar on government page', () => {
      mockPathname.mockReturnValue('/knowledge-base/government');

      render(<KBContentHeader />);

      expect(screen.queryByTestId('search-bar')).not.toBeInTheDocument();
    });

    it('does not render SearchBar on people page', () => {
      mockPathname.mockReturnValue('/knowledge-base/people');

      render(<KBContentHeader />);

      expect(screen.queryByTestId('search-bar')).not.toBeInTheDocument();
    });

    it('renders SearchBar on organizations page', () => {
      mockPathname.mockReturnValue('/knowledge-base/organizations');

      render(<KBContentHeader />);

      const searchBar = screen.getByTestId('search-bar');
      expect(searchBar).toBeInTheDocument();
      expect(searchBar).toHaveAttribute('data-entity-type', 'organizations');
      expect(searchBar).toHaveAttribute('data-entity-label', 'Organizations');
    });

    it('renders SearchBar on persons page', () => {
      mockPathname.mockReturnValue('/knowledge-base/persons');

      render(<KBContentHeader />);

      const searchBar = screen.getByTestId('search-bar');
      expect(searchBar).toBeInTheDocument();
      expect(searchBar).toHaveAttribute('data-entity-type', 'persons');
      expect(searchBar).toHaveAttribute('data-entity-label', 'Persons');
    });

    it('renders SearchBar on committees page', () => {
      mockPathname.mockReturnValue('/knowledge-base/committees');

      render(<KBContentHeader />);

      const searchBar = screen.getByTestId('search-bar');
      expect(searchBar).toBeInTheDocument();
      expect(searchBar).toHaveAttribute('data-entity-type', 'committees');
      expect(searchBar).toHaveAttribute('data-entity-label', 'Committees');
    });

    it('handles deep paths correctly', () => {
      mockPathname.mockReturnValue('/knowledge-base/organizations/123');

      render(<KBContentHeader />);

      const searchBar = screen.getByTestId('search-bar');
      expect(searchBar).toBeInTheDocument();
      expect(searchBar).toHaveAttribute('data-entity-type', 'organizations');
    });
  });

  // ====== onNavigate Callback Tests ======

  describe('onNavigate Callback', () => {
    it('passes onNavigate to EntityTypeSelector', () => {
      const onNavigate = vi.fn();
      render(<KBContentHeader onNavigate={onNavigate} />);

      const selector = screen.getByTestId('entity-type-selector');
      selector.click();

      expect(onNavigate).toHaveBeenCalledTimes(1);
    });

    it('passes onNavigate to Article Analyzer link', () => {
      const onNavigate = vi.fn();
      render(<KBContentHeader onNavigate={onNavigate} />);

      const link = screen.getByRole('link', { name: /article analyzer/i });
      link.click();

      expect(onNavigate).toHaveBeenCalledTimes(1);
    });
  });

  // ====== Styling Tests ======

  describe('Styling', () => {
    it('has sticky positioning classes', () => {
      render(<KBContentHeader />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('sticky');
      expect(header).toHaveClass('top-14');
    });

    it('has backdrop blur classes', () => {
      render(<KBContentHeader />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('backdrop-blur');
    });

    it('has border and background classes', () => {
      render(<KBContentHeader />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('border-b');
      expect(header).toHaveClass('bg-background/95');
    });

    it('has z-index class', () => {
      render(<KBContentHeader />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('z-10');
    });
  });

  // ====== Article Analyzer Link Tests ======

  describe('Article Analyzer Link', () => {
    it('has FileText icon', () => {
      render(<KBContentHeader />);

      // The link contains an SVG icon
      const link = screen.getByRole('link', { name: /article analyzer/i });
      const svg = link.querySelector('svg');
      expect(svg).toBeInTheDocument();
    });

    it('has correct hover styling classes', () => {
      render(<KBContentHeader />);

      const link = screen.getByRole('link', { name: /article analyzer/i });
      expect(link).toHaveClass('hover:bg-accent');
      expect(link).toHaveClass('hover:text-accent-foreground');
    });

    it('has title attribute', () => {
      render(<KBContentHeader />);

      const link = screen.getByRole('link', { name: /article analyzer/i });
      expect(link).toHaveAttribute('title', 'Article Analyzer');
    });

    it('shows text on larger screens', () => {
      render(<KBContentHeader />);

      const link = screen.getByRole('link', { name: /article analyzer/i });
      const textSpan = link.querySelector('span');
      expect(textSpan).toHaveClass('hidden');
      expect(textSpan).toHaveClass('sm:inline');
    });
  });

  // ====== Layout Tests ======

  describe('Layout', () => {
    it('contains container div for content', () => {
      render(<KBContentHeader />);

      const container = document.querySelector('.container');
      expect(container).toBeInTheDocument();
    });

    it('has navigation row with justify-end', () => {
      render(<KBContentHeader />);

      const navRow = screen.getByRole('link', { name: /article analyzer/i }).parentElement;
      expect(navRow).toHaveClass('justify-end');
    });

    it('has flex layout for selectors row', () => {
      render(<KBContentHeader />);

      const selectorsRow = screen.getByTestId('entity-type-selector').parentElement;
      expect(selectorsRow).toHaveClass('flex');
      expect(selectorsRow).toHaveClass('gap-3');
    });
  });
});
