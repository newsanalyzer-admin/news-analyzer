import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { KnowledgeExplorer } from '../KnowledgeExplorer';

// Mock Next.js navigation
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
  usePathname: () => '/knowledge-base/organizations',
  useSearchParams: () => new URLSearchParams(),
}));

describe('KnowledgeExplorer', () => {
  it('renders header with Knowledge Base title', () => {
    render(
      <KnowledgeExplorer>
        <div>Content</div>
      </KnowledgeExplorer>
    );

    // Header link comes first, breadcrumb link second
    const kbLinks = screen.getAllByRole('link', { name: 'Knowledge Base' });
    expect(kbLinks[0]).toHaveAttribute('href', '/knowledge-base');
  });

  it('renders EntityTypeSelector in header', () => {
    render(
      <KnowledgeExplorer>
        <div>Content</div>
      </KnowledgeExplorer>
    );

    expect(screen.getByRole('tablist', { name: 'Entity type' })).toBeInTheDocument();
  });

  it('renders ViewModeSelector in header when entity supports multiple views', () => {
    render(
      <KnowledgeExplorer>
        <div>Content</div>
      </KnowledgeExplorer>
    );

    expect(screen.getByRole('tablist', { name: 'View mode' })).toBeInTheDocument();
  });

  it('renders children in content area', () => {
    render(
      <KnowledgeExplorer>
        <div data-testid="test-content">Test Content</div>
      </KnowledgeExplorer>
    );

    expect(screen.getByTestId('test-content')).toBeInTheDocument();
  });

  it('renders Admin link', () => {
    render(
      <KnowledgeExplorer>
        <div>Content</div>
      </KnowledgeExplorer>
    );

    const adminLink = screen.getByTitle('Admin');
    expect(adminLink).toBeInTheDocument();
    expect(adminLink).toHaveAttribute('href', '/admin');
  });

  it('applies custom className', () => {
    render(
      <KnowledgeExplorer className="custom-class">
        <div>Content</div>
      </KnowledgeExplorer>
    );

    // Header link comes first, use it to find the container with custom class
    const kbLinks = screen.getAllByRole('link', { name: 'Knowledge Base' });
    const container = kbLinks[0].closest('.custom-class');
    expect(container).toBeInTheDocument();
  });
});
