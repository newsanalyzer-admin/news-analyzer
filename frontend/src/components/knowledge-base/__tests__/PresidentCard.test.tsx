import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { PresidentCard } from '../PresidentCard';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';

const mockPresidency: PresidencyDTO = {
  id: '123',
  number: 47,
  ordinalLabel: '47th',
  personId: '456',
  presidentFullName: 'Donald J. Trump',
  presidentFirstName: 'Donald',
  presidentLastName: 'Trump',
  imageUrl: null,
  birthDate: '1946-06-14',
  deathDate: null,
  birthPlace: 'Queens, New York',
  party: 'Republican',
  startDate: '2025-01-20',
  endDate: null,
  termLabel: '2025-present',
  termDays: null,
  electionYear: 2024,
  endReason: null,
  executiveOrderCount: null,
  vicePresidents: [
    {
      personId: '789',
      fullName: 'JD Vance',
      firstName: 'JD',
      lastName: 'Vance',
      startDate: '2025-01-20',
      endDate: null,
      termLabel: '2025-present',
    },
  ],
  predecessorId: null,
  successorId: null,
  current: true,
  living: true,
};

describe('PresidentCard', () => {
  describe('Loading State', () => {
    it('renders loading skeletons when isLoading is true', () => {
      render(<PresidentCard presidency={null} isLoading={true} />);

      // Card should render with skeletons
      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });
  });

  describe('Empty State', () => {
    it('renders empty state when no presidency data', () => {
      render(<PresidentCard presidency={null} isLoading={false} />);

      expect(screen.getByText(/no presidential data available/i)).toBeInTheDocument();
    });

    it('renders empty state when presidency is undefined', () => {
      render(<PresidentCard presidency={undefined} isLoading={false} />);

      expect(screen.getByText(/no presidential data available/i)).toBeInTheDocument();
    });
  });

  describe('Presidency Display', () => {
    it('renders president name', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      expect(screen.getByText('Donald J. Trump')).toBeInTheDocument();
    });

    it('renders presidency ordinal', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      expect(screen.getByText(/47th President of the United States/i)).toBeInTheDocument();
    });

    it('renders party badge', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      expect(screen.getByText('Republican')).toBeInTheDocument();
    });

    it('renders Current President badge', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      expect(screen.getByText('Current President')).toBeInTheDocument();
    });

    it('renders inauguration date', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      // Date display may vary by timezone, just verify the format
      expect(screen.getByText(/inaugurated/i)).toBeInTheDocument();
      expect(screen.getByText(/2025/i)).toBeInTheDocument();
    });

    it('renders vice president', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      expect(screen.getByText(/vice president: jd vance/i)).toBeInTheDocument();
    });

    it('renders birth place', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      expect(screen.getByText(/born in queens, new york/i)).toBeInTheDocument();
    });
  });

  describe('Party Styling', () => {
    it('applies Republican styling', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      const partyBadge = screen.getByText('Republican');
      expect(partyBadge).toHaveClass('bg-red-100');
    });

    it('applies Democratic styling', () => {
      const democratPresidency = {
        ...mockPresidency,
        party: 'Democratic',
      };
      render(<PresidentCard presidency={democratPresidency} />);

      const partyBadge = screen.getByText('Democratic');
      expect(partyBadge).toHaveClass('bg-blue-100');
    });
  });

  describe('Portrait', () => {
    it('shows placeholder when no image URL', () => {
      render(<PresidentCard presidency={mockPresidency} />);

      // Should show the Crown icon placeholder
      expect(document.querySelector('svg.lucide-crown')).toBeInTheDocument();
    });

    it('shows image when imageUrl provided', () => {
      const presidencyWithImage = {
        ...mockPresidency,
        imageUrl: 'https://example.com/portrait.jpg',
      };
      render(<PresidentCard presidency={presidencyWithImage} />);

      const img = screen.getByRole('img');
      expect(img).toHaveAttribute('src', 'https://example.com/portrait.jpg');
      expect(img).toHaveAttribute('alt', 'Portrait of Donald J. Trump');
    });
  });

  describe('Multiple Vice Presidents', () => {
    it('renders multiple VPs comma-separated', () => {
      const presidencyWithMultipleVPs = {
        ...mockPresidency,
        vicePresidents: [
          { personId: '1', fullName: 'VP One', firstName: 'VP', lastName: 'One', startDate: '2021-01-20', endDate: '2022-01-20', termLabel: '2021-2022' },
          { personId: '2', fullName: 'VP Two', firstName: 'VP', lastName: 'Two', startDate: '2022-01-20', endDate: null, termLabel: '2022-present' },
        ],
      };
      render(<PresidentCard presidency={presidencyWithMultipleVPs} />);

      expect(screen.getByText(/vice president: vp one, vp two/i)).toBeInTheDocument();
    });
  });
});
