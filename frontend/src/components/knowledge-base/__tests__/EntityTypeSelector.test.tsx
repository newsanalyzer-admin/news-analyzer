import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { EntityTypeSelector } from '../EntityTypeSelector';

// Mock Next.js navigation
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: vi.fn(),
  }),
  usePathname: () => '/knowledge-base/organizations',
}));

describe('EntityTypeSelector', () => {
  beforeEach(() => {
    mockPush.mockClear();
  });

  it('renders all configured entity types', () => {
    render(<EntityTypeSelector />);

    expect(screen.getByRole('tab', { name: /organizations/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /people/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /committees/i })).toBeInTheDocument();
  });

  it('shows current entity type as active', () => {
    render(<EntityTypeSelector />);

    const orgsTab = screen.getByRole('tab', { name: /organizations/i });
    expect(orgsTab).toHaveAttribute('aria-selected', 'true');
  });

  it('navigates to new entity type on click', () => {
    render(<EntityTypeSelector />);

    const peopleTab = screen.getByRole('tab', { name: /people/i });
    fireEvent.click(peopleTab);

    expect(mockPush).toHaveBeenCalledWith('/knowledge-base/people');
  });

  it('calls onNavigate callback when type selected', () => {
    const onNavigate = vi.fn();
    render(<EntityTypeSelector onNavigate={onNavigate} />);

    const peopleTab = screen.getByRole('tab', { name: /people/i });
    fireEvent.click(peopleTab);

    expect(onNavigate).toHaveBeenCalled();
  });

  it('supports keyboard navigation with arrow keys', () => {
    render(<EntityTypeSelector />);

    const orgsTab = screen.getByRole('tab', { name: /organizations/i });
    orgsTab.focus();

    // Press ArrowRight to move to People
    fireEvent.keyDown(orgsTab, { key: 'ArrowRight' });

    const peopleTab = screen.getByRole('tab', { name: /people/i });
    expect(document.activeElement).toBe(peopleTab);
  });

  it('wraps around on keyboard navigation', () => {
    render(<EntityTypeSelector />);

    const committeesTab = screen.getByRole('tab', { name: /committees/i });
    committeesTab.focus();

    // Press ArrowRight when at end should wrap to beginning
    fireEvent.keyDown(committeesTab, { key: 'ArrowRight' });

    const orgsTab = screen.getByRole('tab', { name: /organizations/i });
    expect(document.activeElement).toBe(orgsTab);
  });

  it('supports Home and End keys', () => {
    render(<EntityTypeSelector />);

    const orgsTab = screen.getByRole('tab', { name: /organizations/i });
    orgsTab.focus();

    // Press End to go to last (committees is now last)
    fireEvent.keyDown(orgsTab, { key: 'End' });
    const committeesTab = screen.getByRole('tab', { name: /committees/i });
    expect(document.activeElement).toBe(committeesTab);

    // Press Home to go to first
    fireEvent.keyDown(committeesTab, { key: 'Home' });
    expect(document.activeElement).toBe(orgsTab);
  });

  it('has correct ARIA attributes', () => {
    render(<EntityTypeSelector />);

    const tablist = screen.getByRole('tablist');
    expect(tablist).toHaveAttribute('aria-label', 'Entity type');

    const tabs = screen.getAllByRole('tab');
    expect(tabs).toHaveLength(3);
    tabs.forEach((tab) => {
      expect(tab).toHaveAttribute('aria-selected');
    });
  });
});
