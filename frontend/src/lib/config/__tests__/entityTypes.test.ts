import { describe, it, expect } from 'vitest';
import {
  entityTypes,
  getEntityTypeConfig,
  getDefaultEntityType,
  isViewModeSupported,
  type DataLayer,
  type EntityTypeConfig,
} from '../entityTypes';

describe('entityTypes', () => {
  describe('entityTypes array', () => {
    it('includes organizations, people, and committees', () => {
      const entityIds = entityTypes.map((et) => et.id);
      expect(entityIds).toContain('organizations');
      expect(entityIds).toContain('people');
      expect(entityIds).toContain('committees');
    });

    it('has at least 3 entity types', () => {
      expect(entityTypes.length).toBeGreaterThanOrEqual(3);
    });
  });

  describe('dataLayer field', () => {
    it('all entity types have dataLayer field', () => {
      entityTypes.forEach((entityType) => {
        expect(entityType.dataLayer).toBeDefined();
        expect(['kb', 'analysis']).toContain(entityType.dataLayer);
      });
    });

    it('KB entity types have dataLayer: kb', () => {
      const kbEntityIds = ['organizations', 'people', 'committees'];
      kbEntityIds.forEach((id) => {
        const config = getEntityTypeConfig(id);
        expect(config?.dataLayer).toBe('kb' as DataLayer);
      });
    });

    it('organizations has dataLayer: kb', () => {
      const config = getEntityTypeConfig('organizations');
      expect(config?.dataLayer).toBe('kb');
    });

    it('people has dataLayer: kb', () => {
      const config = getEntityTypeConfig('people');
      expect(config?.dataLayer).toBe('kb');
    });

    it('committees has dataLayer: kb', () => {
      const config = getEntityTypeConfig('committees');
      expect(config?.dataLayer).toBe('kb');
    });
  });

  describe('getEntityTypeConfig', () => {
    it('returns config for organizations', () => {
      const config = getEntityTypeConfig('organizations');
      expect(config).toBeDefined();
      expect(config?.id).toBe('organizations');
      expect(config?.label).toBe('Organizations');
      expect(config?.apiEndpoint).toBe('/api/government-organizations');
    });

    it('returns config for people', () => {
      const config = getEntityTypeConfig('people');
      expect(config).toBeDefined();
      expect(config?.id).toBe('people');
      expect(config?.label).toBe('People');
      expect(config?.hasSubtypes).toBe(true);
    });

    it('returns config for committees', () => {
      const config = getEntityTypeConfig('committees');
      expect(config).toBeDefined();
      expect(config?.id).toBe('committees');
      expect(config?.label).toBe('Committees');
      expect(config?.apiEndpoint).toBe('/api/committees');
      expect(config?.idField).toBe('committeeCode');
    });

    it('returns undefined for unknown entity type', () => {
      const config = getEntityTypeConfig('unknown-type');
      expect(config).toBeUndefined();
    });
  });

  describe('committees entity type', () => {
    let config: EntityTypeConfig | undefined;

    beforeAll(() => {
      config = getEntityTypeConfig('committees');
    });

    it('has required configuration fields', () => {
      expect(config).toBeDefined();
      expect(config?.id).toBe('committees');
      expect(config?.label).toBe('Committees');
      expect(config?.icon).toBeDefined();
      expect(config?.apiEndpoint).toBe('/api/committees');
      expect(config?.dataLayer).toBe('kb');
    });

    it('supports list and hierarchy views', () => {
      expect(config?.supportedViews).toContain('list');
      expect(config?.supportedViews).toContain('hierarchy');
    });

    it('has default view set to list', () => {
      expect(config?.defaultView).toBe('list');
    });

    it('uses committeeCode as idField', () => {
      expect(config?.idField).toBe('committeeCode');
    });

    it('has column configurations', () => {
      expect(config?.columns).toBeDefined();
      expect(Array.isArray(config?.columns)).toBe(true);
      expect(config?.columns?.length).toBeGreaterThan(0);
    });

    it('has filter configurations', () => {
      expect(config?.filters).toBeDefined();
      expect(Array.isArray(config?.filters)).toBe(true);
    });

    it('has detail configuration', () => {
      expect(config?.detailConfig).toBeDefined();
      expect(config?.detailConfig?.header).toBeDefined();
      expect(config?.detailConfig?.sections).toBeDefined();
    });

    it('has hierarchy configuration', () => {
      expect(config?.hierarchyConfig).toBeDefined();
      expect(config?.hierarchyConfig?.labelField).toBe('name');
      expect(config?.hierarchyConfig?.childrenField).toBe('subcommittees');
      expect(config?.hierarchyConfig?.idField).toBe('committeeCode');
    });

    it('has card configuration', () => {
      expect(config?.cardConfig).toBeDefined();
      expect(config?.cardConfig?.titleField).toBe('name');
    });
  });

  describe('getDefaultEntityType', () => {
    it('returns the first entity type', () => {
      const defaultType = getDefaultEntityType();
      expect(defaultType).toBeDefined();
      expect(defaultType.id).toBe(entityTypes[0].id);
    });

    it('returns organizations as default', () => {
      const defaultType = getDefaultEntityType();
      expect(defaultType.id).toBe('organizations');
    });
  });

  describe('isViewModeSupported', () => {
    it('returns true for supported view modes', () => {
      expect(isViewModeSupported('organizations', 'list')).toBe(true);
      expect(isViewModeSupported('organizations', 'hierarchy')).toBe(true);
      expect(isViewModeSupported('committees', 'list')).toBe(true);
      expect(isViewModeSupported('committees', 'hierarchy')).toBe(true);
    });

    it('returns true for people list view', () => {
      expect(isViewModeSupported('people', 'list')).toBe(true);
    });

    it('returns false for people hierarchy view', () => {
      // People doesn't support hierarchy
      expect(isViewModeSupported('people', 'hierarchy')).toBe(false);
    });

    it('returns false for unknown entity type', () => {
      expect(isViewModeSupported('unknown-type', 'list')).toBe(false);
    });
  });
});
