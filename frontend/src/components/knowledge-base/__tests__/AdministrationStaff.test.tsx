import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AdministrationStaff } from '../AdministrationStaff';
import type { OfficeholderDTO, CabinetMemberDTO } from '@/hooks/usePresidencySync';

const mockChiefOfStaff: OfficeholderDTO = {
  holdingId: 'cos-1',
  individualId: 'ind-cos-1',
  fullName: 'Susie Wiles',
  firstName: 'Susie',
  lastName: 'Wiles',
  positionTitle: 'White House Chief of Staff',
  startDate: '2025-01-20',
  endDate: null,
  termLabel: '2025-present',
  imageUrl: null,
};

const mockCabinetMember: CabinetMemberDTO = {
  holdingId: 'cab-1',
  individualId: 'ind-cab-1',
  fullName: 'Marco Rubio',
  positionTitle: 'Secretary of State',
  departmentName: 'Department of State',
  departmentId: 'dept-1',
  startDate: '2025-01-20',
  endDate: null,
};

describe('AdministrationStaff', () => {
  describe('Loading State', () => {
    it('renders loading skeletons when isLoading is true', () => {
      render(
        <AdministrationStaff chiefsOfStaff={[]} cabinetMembers={[]} isLoading={true} />
      );

      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });
  });

  describe('Empty State', () => {
    it('renders empty state when no staff data', () => {
      render(
        <AdministrationStaff chiefsOfStaff={[]} cabinetMembers={[]} isLoading={false} />
      );

      expect(screen.getByText(/no staff data available/i)).toBeInTheDocument();
    });
  });

  describe('Chiefs of Staff', () => {
    it('renders Chief of Staff section with data', () => {
      render(
        <AdministrationStaff
          chiefsOfStaff={[mockChiefOfStaff]}
          cabinetMembers={[]}
          isLoading={false}
        />
      );

      expect(screen.getByText('White House Chief of Staff')).toBeInTheDocument();
      expect(screen.getByText('Susie Wiles')).toBeInTheDocument();
    });
  });

  describe('Cabinet Secretaries', () => {
    it('renders Cabinet section with data', () => {
      render(
        <AdministrationStaff
          chiefsOfStaff={[]}
          cabinetMembers={[mockCabinetMember]}
          isLoading={false}
        />
      );

      expect(screen.getByText('Cabinet Secretaries')).toBeInTheDocument();
      expect(screen.getByText('Marco Rubio')).toBeInTheDocument();
      expect(screen.getByText('Department of State')).toBeInTheDocument();
    });
  });

  describe('Full Staff Display', () => {
    it('renders both sections when both have data', () => {
      render(
        <AdministrationStaff
          chiefsOfStaff={[mockChiefOfStaff]}
          cabinetMembers={[mockCabinetMember]}
          isLoading={false}
        />
      );

      expect(screen.getByText('White House Chief of Staff')).toBeInTheDocument();
      expect(screen.getByText('Cabinet Secretaries')).toBeInTheDocument();
      expect(screen.getByText('Susie Wiles')).toBeInTheDocument();
      expect(screen.getByText('Marco Rubio')).toBeInTheDocument();
    });
  });
});
