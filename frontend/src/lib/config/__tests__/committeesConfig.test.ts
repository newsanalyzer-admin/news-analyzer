import { describe, it, expect } from 'vitest';
import {
  committeeColumns,
  committeeFilters,
  committeeDefaultSort,
  committeeDetailConfig,
  committeeHierarchyConfig,
  committeeCardConfig,
} from '../committeesConfig';
import type { Committee } from '@/types/committee';

describe('committeesConfig', () => {
  describe('committeeColumns', () => {
    it('has required columns', () => {
      expect(committeeColumns).toHaveLength(5);

      const columnIds = committeeColumns.map((c) => c.id);
      expect(columnIds).toContain('name');
      expect(columnIds).toContain('chamber');
      expect(columnIds).toContain('committeeType');
      expect(columnIds).toContain('committeeCode');
      expect(columnIds).toContain('url');
    });

    it('name column is sortable', () => {
      const nameColumn = committeeColumns.find((c) => c.id === 'name');
      expect(nameColumn?.sortable).toBe(true);
    });

    it('chamber column renders badge correctly', () => {
      const chamberColumn = committeeColumns.find((c) => c.id === 'chamber');
      expect(chamberColumn?.render).toBeDefined();

      // Test render function returns a ReactNode
      const result = chamberColumn?.render?.('SENATE', {} as Committee);
      expect(result).toBeDefined();
    });

    it('committeeType column formats types correctly', () => {
      const typeColumn = committeeColumns.find((c) => c.id === 'committeeType');
      expect(typeColumn?.render).toBeDefined();

      // Test formatting
      const result = typeColumn?.render?.('STANDING', {} as Committee);
      expect(result).toBe('Standing');
    });
  });

  describe('committeeFilters', () => {
    it('has chamber and type filters', () => {
      expect(committeeFilters).toHaveLength(2);

      const filterIds = committeeFilters.map((f) => f.id);
      expect(filterIds).toContain('chamber');
      expect(filterIds).toContain('type');
    });

    it('chamber filter has correct options', () => {
      const chamberFilter = committeeFilters.find((f) => f.id === 'chamber');
      expect(chamberFilter?.options).toHaveLength(3);

      const values = chamberFilter?.options?.map((o) => o.value);
      expect(values).toContain('SENATE');
      expect(values).toContain('HOUSE');
      expect(values).toContain('JOINT');
    });

    it('type filter has correct options', () => {
      const typeFilter = committeeFilters.find((f) => f.id === 'type');
      expect(typeFilter?.options).toHaveLength(5);

      const values = typeFilter?.options?.map((o) => o.value);
      expect(values).toContain('STANDING');
      expect(values).toContain('SELECT');
      expect(values).toContain('SUBCOMMITTEE');
    });
  });

  describe('committeeDefaultSort', () => {
    it('sorts by name ascending', () => {
      expect(committeeDefaultSort.column).toBe('name');
      expect(committeeDefaultSort.direction).toBe('asc');
    });
  });

  describe('committeeDetailConfig', () => {
    it('has header configuration', () => {
      expect(committeeDetailConfig.header.titleField).toBe('name');
      expect(committeeDetailConfig.header.subtitleField).toBe('committeeCode');
      expect(committeeDetailConfig.header.badgeField).toBe('chamber');
    });

    it('has required sections', () => {
      const sectionIds = committeeDetailConfig.sections.map((s) => s.id);
      expect(sectionIds).toContain('overview');
      expect(sectionIds).toContain('links');
      expect(sectionIds).toContain('metadata');
    });

    it('overview section has chamber and type fields', () => {
      const overview = committeeDetailConfig.sections.find((s) => s.id === 'overview');
      const fieldIds = overview?.fields.map((f) => f.id);
      expect(fieldIds).toContain('chamber');
      expect(fieldIds).toContain('committeeType');
    });
  });

  describe('committeeHierarchyConfig', () => {
    it('uses committeeCode as idField', () => {
      expect(committeeHierarchyConfig.idField).toBe('committeeCode');
    });

    it('uses subcommittees as childrenField', () => {
      expect(committeeHierarchyConfig.childrenField).toBe('subcommittees');
    });

    it('uses name as labelField', () => {
      expect(committeeHierarchyConfig.labelField).toBe('name');
    });
  });

  describe('committeeCardConfig', () => {
    it('uses name as title', () => {
      expect(committeeCardConfig.titleField).toBe('name');
    });

    it('uses committeeCode as subtitle', () => {
      expect(committeeCardConfig.subtitleField).toBe('committeeCode');
    });

    it('has renderBadge function', () => {
      expect(committeeCardConfig.renderBadge).toBeDefined();
    });
  });
});
