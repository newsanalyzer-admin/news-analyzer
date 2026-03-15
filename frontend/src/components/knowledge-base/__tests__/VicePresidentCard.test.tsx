import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { VicePresidentCard } from '../VicePresidentCard';
import type { OfficeholderDTO } from '@/hooks/usePresidencySync';

const mockVP: OfficeholderDTO = {
  holdingId: 'vp-1',
  individualId: 'ind-1',
  fullName: 'JD Vance',
  firstName: 'JD',
  lastName: 'Vance',
  positionTitle: 'Vice President of the United States',
  startDate: '2025-01-20',
  endDate: null,
  termLabel: '2025-present',
  imageUrl: null,
};

describe('VicePresidentCard', () => {
  describe('Loading State', () => {
    it('renders loading skeletons when isLoading is true', () => {
      render(<VicePresidentCard vicePresident={null} isLoading={true} />);

      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });
  });

  describe('Empty State', () => {
    it('renders empty state when no VP data', () => {
      render(<VicePresidentCard vicePresident={null} isLoading={false} />);

      expect(screen.getByText(/no vice president data available/i)).toBeInTheDocument();
    });
  });

  describe('VP Display', () => {
    it('renders VP name', () => {
      render(<VicePresidentCard vicePresident={mockVP} />);

      expect(screen.getByText('JD Vance')).toBeInTheDocument();
    });

    it('renders Vice President badge', () => {
      render(<VicePresidentCard vicePresident={mockVP} />);

      expect(screen.getByText('Vice President')).toBeInTheDocument();
    });

    it('renders position title', () => {
      render(<VicePresidentCard vicePresident={mockVP} />);

      expect(screen.getByText('Vice President of the United States')).toBeInTheDocument();
    });

    it('renders start date', () => {
      render(<VicePresidentCard vicePresident={mockVP} />);

      expect(screen.getByText(/since.*2025/i)).toBeInTheDocument();
    });

    it('renders term label', () => {
      render(<VicePresidentCard vicePresident={mockVP} />);

      expect(screen.getByText(/term: 2025-present/i)).toBeInTheDocument();
    });
  });

  describe('Portrait', () => {
    it('shows placeholder when no image URL', () => {
      render(<VicePresidentCard vicePresident={mockVP} />);

      expect(document.querySelector('svg.lucide-users')).toBeInTheDocument();
    });

    it('shows image when imageUrl provided', () => {
      const vpWithImage = { ...mockVP, imageUrl: 'https://example.com/vp.jpg' };
      render(<VicePresidentCard vicePresident={vpWithImage} />);

      const img = screen.getByRole('img');
      expect(img).toHaveAttribute('src', 'https://example.com/vp.jpg');
      expect(img).toHaveAttribute('alt', 'Portrait of JD Vance');
    });
  });
});
