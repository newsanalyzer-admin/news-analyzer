import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AdministrationListItem } from '../AdministrationListItem';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';

const mockPresidency: PresidencyDTO = {
  id: 'pres-45',
  number: 45,
  ordinalLabel: '45th',
  individualId: 'ind-trump-45',
  presidentFullName: 'Donald J. Trump',
  presidentFirstName: 'Donald',
  presidentLastName: 'Trump',
  imageUrl: null,
  birthDate: '1946-06-14',
  deathDate: null,
  birthPlace: 'Queens, New York',
  party: 'Republican',
  startDate: '2017-01-20',
  endDate: '2021-01-20',
  termLabel: '2017-2021',
  termDays: 1461,
  electionYear: 2016,
  endReason: null,
  executiveOrderCount: null,
  vicePresidents: [],
  predecessorId: null,
  successorId: null,
  current: false,
  living: true,
};

const currentPresidency: PresidencyDTO = {
  ...mockPresidency,
  id: 'pres-47',
  number: 47,
  ordinalLabel: '47th',
  startDate: '2025-01-20',
  endDate: null,
  termLabel: '2025-present',
  current: true,
};

describe('AdministrationListItem', () => {
  it('renders presidency number and name', () => {
    render(
      <AdministrationListItem
        presidency={mockPresidency}
        isSelected={false}
        onSelect={vi.fn()}
      />
    );

    expect(screen.getByText('45')).toBeInTheDocument();
    expect(screen.getByText('Donald J. Trump')).toBeInTheDocument();
  });

  it('renders term label and party', () => {
    render(
      <AdministrationListItem
        presidency={mockPresidency}
        isSelected={false}
        onSelect={vi.fn()}
      />
    );

    expect(screen.getByText('2017-2021')).toBeInTheDocument();
    expect(screen.getByText('Republican')).toBeInTheDocument();
  });

  it('shows Current badge for current administration', () => {
    render(
      <AdministrationListItem
        presidency={currentPresidency}
        isSelected={false}
        onSelect={vi.fn()}
      />
    );

    expect(screen.getByText('Current')).toBeInTheDocument();
  });

  it('does not show Current badge for historical administration', () => {
    render(
      <AdministrationListItem
        presidency={mockPresidency}
        isSelected={false}
        onSelect={vi.fn()}
      />
    );

    expect(screen.queryByText('Current')).not.toBeInTheDocument();
  });

  it('applies selected styling when isSelected is true', () => {
    const { container } = render(
      <AdministrationListItem
        presidency={mockPresidency}
        isSelected={true}
        onSelect={vi.fn()}
      />
    );

    const button = container.querySelector('button');
    expect(button?.className).toContain('border-primary');
  });

  it('calls onSelect with presidency number when clicked', async () => {
    const onSelect = vi.fn();
    render(
      <AdministrationListItem
        presidency={mockPresidency}
        isSelected={false}
        onSelect={onSelect}
      />
    );

    const user = userEvent.setup();
    await user.click(screen.getByRole('button'));

    expect(onSelect).toHaveBeenCalledWith(45);
  });

  it('handles non-consecutive terms (same name, different number)', () => {
    const cleveland22: PresidencyDTO = {
      ...mockPresidency,
      id: 'pres-22',
      number: 22,
      ordinalLabel: '22nd',
      presidentFullName: 'Grover Cleveland',
      party: 'Democratic',
      startDate: '1885-03-04',
      endDate: '1889-03-04',
      termLabel: '1885-1889',
    };

    render(
      <AdministrationListItem
        presidency={cleveland22}
        isSelected={false}
        onSelect={vi.fn()}
      />
    );

    expect(screen.getByText('22')).toBeInTheDocument();
    expect(screen.getByText('Grover Cleveland')).toBeInTheDocument();
    expect(screen.getByText('1885-1889')).toBeInTheDocument();
  });
});
