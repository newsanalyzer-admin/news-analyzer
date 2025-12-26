import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ViewModeSelector } from '../ViewModeSelector';

// Mock Next.js navigation
const mockReplace = vi.fn();
let mockPathname = '/knowledge-base/organizations';
let mockSearchParams = new URLSearchParams();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: mockReplace,
  }),
  usePathname: () => mockPathname,
  useSearchParams: () => mockSearchParams,
}));

describe('ViewModeSelector', () => {
  beforeEach(() => {
    mockReplace.mockClear();
    mockPathname = '/knowledge-base/organizations';
    mockSearchParams = new URLSearchParams();
  });

  it('renders view modes for organizations (supports list and hierarchy)', () => {
    render(<ViewModeSelector />);

    expect(screen.getByRole('tab', { name: /list/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /hierarchy/i })).toBeInTheDocument();
  });

  it('shows list as default active view for organizations', () => {
    render(<ViewModeSelector />);

    const listTab = screen.getByRole('tab', { name: /list/i });
    expect(listTab).toHaveAttribute('aria-selected', 'true');
  });

  it('updates URL query param when view mode changed', () => {
    render(<ViewModeSelector />);

    const hierarchyTab = screen.getByRole('tab', { name: /hierarchy/i });
    fireEvent.click(hierarchyTab);

    expect(mockReplace).toHaveBeenCalledWith('/knowledge-base/organizations?view=hierarchy');
  });

  it('removes view param when selecting default view', () => {
    mockSearchParams = new URLSearchParams('view=hierarchy');
    render(<ViewModeSelector />);

    const listTab = screen.getByRole('tab', { name: /list/i });
    fireEvent.click(listTab);

    expect(mockReplace).toHaveBeenCalledWith('/knowledge-base/organizations');
  });

  it('preserves other query params when updating view', () => {
    mockSearchParams = new URLSearchParams('q=test&filter=active');
    render(<ViewModeSelector />);

    const hierarchyTab = screen.getByRole('tab', { name: /hierarchy/i });
    fireEvent.click(hierarchyTab);

    expect(mockReplace).toHaveBeenCalledWith(
      expect.stringContaining('view=hierarchy')
    );
    expect(mockReplace).toHaveBeenCalledWith(
      expect.stringContaining('q=test')
    );
  });

  it('does not render when entity type only supports one view', () => {
    mockPathname = '/knowledge-base/people';
    const { container } = render(<ViewModeSelector />);

    expect(container.firstChild).toBeNull();
  });

  it('supports keyboard navigation', () => {
    render(<ViewModeSelector />);

    const listTab = screen.getByRole('tab', { name: /list/i });
    listTab.focus();

    fireEvent.keyDown(listTab, { key: 'ArrowRight' });

    const hierarchyTab = screen.getByRole('tab', { name: /hierarchy/i });
    expect(document.activeElement).toBe(hierarchyTab);
  });

  it('has correct ARIA attributes', () => {
    render(<ViewModeSelector />);

    const tablist = screen.getByRole('tablist');
    expect(tablist).toHaveAttribute('aria-label', 'View mode');
  });
});
