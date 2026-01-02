import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { AAContentHeader } from '../AAContentHeader';

describe('AAContentHeader', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ====== Rendering Tests ======

  describe('Rendering', () => {
    it('renders the header element', () => {
      render(<AAContentHeader />);

      const header = document.querySelector('header');
      expect(header).toBeInTheDocument();
    });

    it('renders Knowledge Base link', () => {
      render(<AAContentHeader />);

      const link = screen.getByRole('link', { name: /knowledge base/i });
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute('href', '/knowledge-base');
    });

    it('renders Admin link', () => {
      render(<AAContentHeader />);

      const link = screen.getByRole('link', { name: /admin/i });
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute('href', '/admin');
    });

    it('applies custom className', () => {
      render(<AAContentHeader className="custom-class" />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('custom-class');
    });
  });

  // ====== Navigation Link Tests ======

  describe('Navigation Links', () => {
    it('Knowledge Base link has correct title', () => {
      render(<AAContentHeader />);

      const link = screen.getByRole('link', { name: /knowledge base/i });
      expect(link).toHaveAttribute('title', 'Knowledge Base');
    });

    it('Admin link has correct title', () => {
      render(<AAContentHeader />);

      const link = screen.getByRole('link', { name: /admin/i });
      expect(link).toHaveAttribute('title', 'Admin');
    });

    it('Knowledge Base link has Database icon', () => {
      render(<AAContentHeader />);

      const link = screen.getByRole('link', { name: /knowledge base/i });
      const svg = link.querySelector('svg');
      expect(svg).toBeInTheDocument();
    });

    it('Admin link has Settings icon', () => {
      render(<AAContentHeader />);

      const link = screen.getByRole('link', { name: /admin/i });
      const svg = link.querySelector('svg');
      expect(svg).toBeInTheDocument();
    });

    it('link text is hidden on small screens', () => {
      render(<AAContentHeader />);

      const kbLink = screen.getByRole('link', { name: /knowledge base/i });
      const kbSpan = kbLink.querySelector('span');
      expect(kbSpan).toHaveClass('hidden');
      expect(kbSpan).toHaveClass('sm:inline');

      const adminLink = screen.getByRole('link', { name: /admin/i });
      const adminSpan = adminLink.querySelector('span');
      expect(adminSpan).toHaveClass('hidden');
      expect(adminSpan).toHaveClass('sm:inline');
    });
  });

  // ====== onNavigate Callback Tests ======

  describe('onNavigate Callback', () => {
    it('calls onNavigate when Knowledge Base link is clicked', () => {
      const onNavigate = vi.fn();
      render(<AAContentHeader onNavigate={onNavigate} />);

      const link = screen.getByRole('link', { name: /knowledge base/i });
      fireEvent.click(link);

      expect(onNavigate).toHaveBeenCalledTimes(1);
    });

    it('calls onNavigate when Admin link is clicked', () => {
      const onNavigate = vi.fn();
      render(<AAContentHeader onNavigate={onNavigate} />);

      const link = screen.getByRole('link', { name: /admin/i });
      fireEvent.click(link);

      expect(onNavigate).toHaveBeenCalledTimes(1);
    });

    it('works without onNavigate prop', () => {
      render(<AAContentHeader />);

      const link = screen.getByRole('link', { name: /knowledge base/i });
      expect(() => fireEvent.click(link)).not.toThrow();
    });
  });

  // ====== Styling Tests ======

  describe('Styling', () => {
    it('has sticky positioning classes', () => {
      render(<AAContentHeader />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('sticky');
      expect(header).toHaveClass('top-14');
    });

    it('has backdrop blur classes', () => {
      render(<AAContentHeader />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('backdrop-blur');
    });

    it('has border and background classes', () => {
      render(<AAContentHeader />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('border-b');
      expect(header).toHaveClass('bg-background/95');
    });

    it('has z-index class', () => {
      render(<AAContentHeader />);

      const header = document.querySelector('header');
      expect(header).toHaveClass('z-10');
    });

    it('links have hover styling classes', () => {
      render(<AAContentHeader />);

      const kbLink = screen.getByRole('link', { name: /knowledge base/i });
      expect(kbLink).toHaveClass('hover:bg-accent');
      expect(kbLink).toHaveClass('hover:text-accent-foreground');

      const adminLink = screen.getByRole('link', { name: /admin/i });
      expect(adminLink).toHaveClass('hover:bg-accent');
      expect(adminLink).toHaveClass('hover:text-accent-foreground');
    });
  });

  // ====== Layout Tests ======

  describe('Layout', () => {
    it('contains container div for content', () => {
      render(<AAContentHeader />);

      const container = document.querySelector('.container');
      expect(container).toBeInTheDocument();
    });

    it('has navigation row with justify-end', () => {
      render(<AAContentHeader />);

      const kbLink = screen.getByRole('link', { name: /knowledge base/i });
      const navRow = kbLink.parentElement;
      expect(navRow).toHaveClass('justify-end');
    });

    it('has gap between navigation links', () => {
      render(<AAContentHeader />);

      const kbLink = screen.getByRole('link', { name: /knowledge base/i });
      const navRow = kbLink.parentElement;
      expect(navRow).toHaveClass('gap-2');
    });
  });
});
