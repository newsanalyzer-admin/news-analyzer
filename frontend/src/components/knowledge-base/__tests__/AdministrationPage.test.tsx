import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AdministrationPage } from '../AdministrationPage';

// Mock next/navigation for KBBreadcrumbs
vi.mock('next/navigation', () => ({
  usePathname: () => '/knowledge-base/government/executive/administrations',
}));

// Mock next/link to render a plain anchor
vi.mock('next/link', () => ({
  default: ({ children, href, ...props }: { children: React.ReactNode; href: string }) => (
    <a href={href} {...props}>{children}</a>
  ),
}));

// Mock the hooks used by CurrentAdministration and its children
vi.mock('@/hooks/usePresidencySync', () => ({
  useCurrentPresidency: () => ({
    data: null,
    isLoading: false,
    error: null,
  }),
  usePresidencyAdministration: () => ({
    data: undefined,
    isLoading: false,
  }),
  usePresidencyExecutiveOrders: () => ({
    data: { content: [], totalElements: 0, totalPages: 0, size: 10, number: 0 },
    isLoading: false,
    error: null,
  }),
}));

function renderWithQueryClient(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
  );
}

describe('AdministrationPage', () => {
  describe('Default Rendering', () => {
    it('renders the page title', () => {
      renderWithQueryClient(<AdministrationPage />);

      expect(
        screen.getByRole('heading', { level: 1, name: 'Presidential Administrations' })
      ).toBeInTheDocument();
    });

    it('renders breadcrumbs with correct path', () => {
      renderWithQueryClient(<AdministrationPage />);

      expect(screen.getByText('Knowledge Base')).toBeInTheDocument();
      expect(screen.getByText('Executive Branch')).toBeInTheDocument();
    });

    it('renders back link to Executive Branch', () => {
      renderWithQueryClient(<AdministrationPage />);

      const backLink = screen.getByText('Back to Executive Branch');
      expect(backLink.closest('a')).toHaveAttribute(
        'href',
        '/knowledge-base/government/executive'
      );
    });

    it('renders Current Administration section heading', () => {
      renderWithQueryClient(<AdministrationPage />);

      expect(
        screen.getByRole('heading', { level: 2, name: 'Current Administration' })
      ).toBeInTheDocument();
    });

    it('renders Historical Administrations placeholder section', () => {
      renderWithQueryClient(<AdministrationPage />);

      expect(screen.getByText('Historical Administrations')).toBeInTheDocument();
      expect(screen.getByText(/coming in kb-2\.3/i)).toBeInTheDocument();
    });
  });

  describe('Loading State', () => {
    it('renders loading skeletons when isLoading is true', () => {
      renderWithQueryClient(<AdministrationPage isLoading={true} />);

      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);

      expect(screen.queryByText('Current Administration')).not.toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('renders error message when error prop is provided', () => {
      renderWithQueryClient(<AdministrationPage error="Network error" />);

      expect(screen.getByText('Failed to load administration data')).toBeInTheDocument();
      expect(screen.getByText('Network error')).toBeInTheDocument();
    });

    it('does not render placeholder sections when in error state', () => {
      renderWithQueryClient(<AdministrationPage error="Something went wrong" />);

      expect(screen.queryByText('Current Administration')).not.toBeInTheDocument();
      expect(screen.queryByText('Historical Administrations')).not.toBeInTheDocument();
    });
  });
});
