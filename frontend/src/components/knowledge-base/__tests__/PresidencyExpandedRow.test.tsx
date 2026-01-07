import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { PresidencyExpandedRow } from '../PresidencyExpandedRow';
import type { PresidencyDTO, PresidencyAdministrationDTO } from '@/hooks/usePresidencySync';

// Mock the usePresidencyAdministration hook
const mockAdministrationData: PresidencyAdministrationDTO = {
  presidencyId: '123',
  presidencyNumber: 47,
  presidencyLabel: '47th Presidency',
  vicePresidents: [
    {
      holdingId: 'h1',
      personId: 'vp1',
      fullName: 'JD Vance',
      firstName: 'JD',
      lastName: 'Vance',
      positionTitle: 'Vice President',
      startDate: '2025-01-20',
      endDate: null,
      termLabel: '2025-present',
      imageUrl: null,
    },
  ],
  chiefsOfStaff: [
    {
      holdingId: 'h2',
      personId: 'cos1',
      fullName: 'Susie Wiles',
      firstName: 'Susie',
      lastName: 'Wiles',
      positionTitle: 'Chief of Staff',
      startDate: '2025-01-20',
      endDate: null,
      termLabel: '2025-present',
      imageUrl: null,
    },
  ],
  cabinetMembers: [],
};

vi.mock('@/hooks/usePresidencySync', async () => {
  const actual = await vi.importActual('@/hooks/usePresidencySync');
  return {
    ...actual,
    usePresidencyAdministration: vi.fn(() => ({
      data: mockAdministrationData,
      isLoading: false,
      error: null,
    })),
  };
});

import { usePresidencyAdministration } from '@/hooks/usePresidencySync';
const mockUsePresidencyAdministration = vi.mocked(usePresidencyAdministration);

const mockPresidency: PresidencyDTO = {
  id: '123',
  number: 47,
  ordinalLabel: '47th',
  personId: 'p1',
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
    { personId: 'vp1', fullName: 'JD Vance', firstName: 'JD', lastName: 'Vance', startDate: '2025-01-20', endDate: null, termLabel: '2025-present' },
  ],
  predecessorId: null,
  successorId: null,
  current: true,
  living: true,
};

describe('PresidencyExpandedRow', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUsePresidencyAdministration.mockReturnValue({
      data: mockAdministrationData,
      isLoading: false,
      error: null,
    } as ReturnType<typeof usePresidencyAdministration>);
  });

  describe('Section Headers', () => {
    it('renders Vice Presidents section', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Vice Presidents')).toBeInTheDocument();
    });

    it('renders Chiefs of Staff section', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Chiefs of Staff')).toBeInTheDocument();
    });

    it('renders Term Details section', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Term Details')).toBeInTheDocument();
    });

    it('renders Executive Orders section', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Executive Orders')).toBeInTheDocument();
    });
  });

  describe('Vice President Display', () => {
    it('displays vice president name from administration data', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('JD Vance')).toBeInTheDocument();
    });

    it('displays vice president term label', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getAllByText('2025-present').length).toBeGreaterThan(0);
    });
  });

  describe('Chiefs of Staff Display', () => {
    it('displays chief of staff name', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Susie Wiles')).toBeInTheDocument();
    });
  });

  describe('Term Details', () => {
    it('displays term start date', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Started:')).toBeInTheDocument();
      expect(screen.getByText('Jan 2025')).toBeInTheDocument();
    });

    it('displays term end date as Present for current', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Ended:')).toBeInTheDocument();
      expect(screen.getByText('Present')).toBeInTheDocument();
    });

    it('displays election year', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Elected:')).toBeInTheDocument();
      expect(screen.getByText('2024')).toBeInTheDocument();
    });
  });

  describe('Biographical Info', () => {
    it('displays birth place', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText(/born: queens, new york/i)).toBeInTheDocument();
    });

    it('displays Living badge for living president', () => {
      render(<PresidencyExpandedRow presidency={mockPresidency} />);
      expect(screen.getByText('Living')).toBeInTheDocument();
    });
  });

  describe('Loading State', () => {
    it('shows loading skeletons when administration data is loading', () => {
      mockUsePresidencyAdministration.mockReturnValue({
        data: undefined,
        isLoading: true,
        error: null,
      } as ReturnType<typeof usePresidencyAdministration>);

      render(<PresidencyExpandedRow presidency={mockPresidency} />);

      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });
  });

  describe('Historical Presidency', () => {
    const historicalPresidency: PresidencyDTO = {
      ...mockPresidency,
      id: '456',
      number: 1,
      ordinalLabel: '1st',
      presidentFullName: 'George Washington',
      presidentFirstName: 'George',
      presidentLastName: 'Washington',
      party: 'Independent',
      startDate: '1789-04-30',
      endDate: '1797-03-04',
      termLabel: '1789-1797',
      termDays: 2865,
      electionYear: 1788,
      endReason: 'TERM_END',
      executiveOrderCount: 8,
      birthDate: '1732-02-22',
      deathDate: '1799-12-14',
      birthPlace: 'Westmoreland County, Virginia',
      current: false,
      living: false,
    };

    it('displays term duration for completed terms', () => {
      mockUsePresidencyAdministration.mockReturnValue({
        data: undefined,
        isLoading: false,
        error: null,
      } as ReturnType<typeof usePresidencyAdministration>);

      render(<PresidencyExpandedRow presidency={historicalPresidency} />);

      // 2865 days = 7 years (2555 days) + 310 days
      expect(screen.getByText(/7 years, 310 days/i)).toBeInTheDocument();
    });

    it('displays EO count when available', () => {
      mockUsePresidencyAdministration.mockReturnValue({
        data: undefined,
        isLoading: false,
        error: null,
      } as ReturnType<typeof usePresidencyAdministration>);

      render(<PresidencyExpandedRow presidency={historicalPresidency} />);

      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText(/orders issued/i)).toBeInTheDocument();
    });

    it('displays death date for deceased president', () => {
      mockUsePresidencyAdministration.mockReturnValue({
        data: undefined,
        isLoading: false,
        error: null,
      } as ReturnType<typeof usePresidencyAdministration>);

      render(<PresidencyExpandedRow presidency={historicalPresidency} />);

      expect(screen.getByText(/died:/i)).toBeInTheDocument();
    });

    it('shows note about CoS for pre-1946 presidencies', () => {
      mockUsePresidencyAdministration.mockReturnValue({
        data: { ...mockAdministrationData, chiefsOfStaff: [] },
        isLoading: false,
        error: null,
      } as ReturnType<typeof usePresidencyAdministration>);

      render(<PresidencyExpandedRow presidency={historicalPresidency} />);

      expect(screen.getByText(/position created in 1946/i)).toBeInTheDocument();
    });
  });
});
