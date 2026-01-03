import { describe, it, expect } from 'vitest';
import {
  publicMenuConfig,
  publicMenuItemsFlat,
  articleAnalyzerMenuItems,
} from '../menu-config';

describe('menu-config', () => {
  // ====== Public Menu Config Tests ======

  describe('publicMenuConfig', () => {
    it('has Knowledge Base as root item', () => {
      expect(publicMenuConfig).toHaveLength(1);
      expect(publicMenuConfig[0].label).toBe('Knowledge Base');
    });

    it('has four main categories', () => {
      const children = publicMenuConfig[0].children;
      expect(children).toHaveLength(4);
      expect(children?.map((c) => c.label)).toEqual([
        'U.S. Federal Government',
        'People',
        'Committees',
        'Organizations',
      ]);
    });
  });

  describe('publicMenuItemsFlat', () => {
    it('contains all main categories', () => {
      expect(publicMenuItemsFlat).toHaveLength(4);
    });

    it('U.S. Federal Government category has correct structure', () => {
      const government = publicMenuItemsFlat.find(
        (item) => item.label === 'U.S. Federal Government'
      );
      expect(government).toBeDefined();
      expect(government?.href).toBe('/knowledge-base/government');
      // Now has 2 children: Branches (grouping) and U.S. Code
      expect(government?.children).toHaveLength(2);
    });

    it('Branches grouping has no href (non-clickable)', () => {
      const government = publicMenuItemsFlat.find(
        (item) => item.label === 'U.S. Federal Government'
      );
      const branches = government?.children?.find((item) => item.label === 'Branches');

      expect(branches).toBeDefined();
      expect(branches?.href).toBeUndefined();
      expect(branches?.children).toHaveLength(3);
    });

    it('Government branch routes are nested under Branches', () => {
      const government = publicMenuItemsFlat.find(
        (item) => item.label === 'U.S. Federal Government'
      );
      const branches = government?.children?.find((item) => item.label === 'Branches');
      const branchChildren = branches?.children;

      expect(branchChildren?.[0].label).toBe('Executive Branch');
      expect(branchChildren?.[0].href).toBe('/knowledge-base/government/executive');

      expect(branchChildren?.[1].label).toBe('Legislative Branch');
      expect(branchChildren?.[1].href).toBe('/knowledge-base/government/legislative');

      expect(branchChildren?.[2].label).toBe('Judicial Branch');
      expect(branchChildren?.[2].href).toBe('/knowledge-base/government/judicial');
    });

    it('U.S. Code (Federal Laws) has correct route', () => {
      const government = publicMenuItemsFlat.find(
        (item) => item.label === 'U.S. Federal Government'
      );
      const usCode = government?.children?.find(
        (item) => item.label === 'U.S. Code (Federal Laws)'
      );

      expect(usCode).toBeDefined();
      expect(usCode?.href).toBe('/knowledge-base/government/us-code');
    });

    it('People category has correct structure', () => {
      const people = publicMenuItemsFlat.find((item) => item.label === 'People');
      expect(people).toBeDefined();
      expect(people?.href).toBe('/knowledge-base/people');
      expect(people?.children).toHaveLength(3);
    });

    it('People subtype routes use query params', () => {
      const people = publicMenuItemsFlat.find((item) => item.label === 'People');
      const subtypes = people?.children;

      expect(subtypes?.[0].label).toBe('Federal Judges');
      expect(subtypes?.[0].href).toBe('/knowledge-base/people?type=judges');

      expect(subtypes?.[1].label).toBe('Congressional Members');
      expect(subtypes?.[1].href).toBe('/knowledge-base/people?type=members');

      expect(subtypes?.[2].label).toBe('Executive Appointees');
      expect(subtypes?.[2].href).toBe('/knowledge-base/people?type=appointees');
    });

    it('Committees category has direct href', () => {
      const committees = publicMenuItemsFlat.find((item) => item.label === 'Committees');
      expect(committees).toBeDefined();
      expect(committees?.href).toBe('/knowledge-base/committees');
      expect(committees?.children).toBeUndefined();
    });

    it('Organizations category has direct href', () => {
      const organizations = publicMenuItemsFlat.find((item) => item.label === 'Organizations');
      expect(organizations).toBeDefined();
      expect(organizations?.href).toBe('/knowledge-base/organizations');
      expect(organizations?.children).toBeUndefined();
    });

    it('all menu items have icons', () => {
      publicMenuItemsFlat.forEach((item) => {
        expect(item.icon).toBeDefined();
      });
    });
  });

  // ====== Article Analyzer Menu Config Tests ======

  describe('articleAnalyzerMenuItems', () => {
    it('has three menu items', () => {
      expect(articleAnalyzerMenuItems).toHaveLength(3);
    });

    it('Analyze Article is disabled', () => {
      const analyze = articleAnalyzerMenuItems.find((item) => item.label === 'Analyze Article');
      expect(analyze).toBeDefined();
      expect(analyze?.disabled).toBe(true);
      expect(analyze?.href).toBe('/article-analyzer/analyze');
    });

    it('Articles route is correct', () => {
      const articles = articleAnalyzerMenuItems.find((item) => item.label === 'Articles');
      expect(articles).toBeDefined();
      expect(articles?.href).toBe('/article-analyzer/articles');
      expect(articles?.disabled).toBeFalsy();
    });

    it('Entities route is correct', () => {
      const entities = articleAnalyzerMenuItems.find((item) => item.label === 'Entities');
      expect(entities).toBeDefined();
      expect(entities?.href).toBe('/article-analyzer/entities');
      expect(entities?.disabled).toBeFalsy();
    });

    it('all menu items have icons', () => {
      articleAnalyzerMenuItems.forEach((item) => {
        expect(item.icon).toBeDefined();
      });
    });
  });

  // ====== Route Validity Tests ======

  describe('Route Validity', () => {
    it('all KB routes start with /knowledge-base', () => {
      const checkRoutes = (items: typeof publicMenuItemsFlat) => {
        items.forEach((item) => {
          if (item.href) {
            expect(item.href.startsWith('/knowledge-base')).toBe(true);
          }
          if (item.children) {
            checkRoutes(item.children);
          }
        });
      };
      checkRoutes(publicMenuItemsFlat);
    });

    it('all AA routes start with /article-analyzer', () => {
      articleAnalyzerMenuItems.forEach((item) => {
        if (item.href) {
          expect(item.href.startsWith('/article-analyzer')).toBe(true);
        }
      });
    });

    it('no routes contain undefined or empty strings', () => {
      const checkRoutes = (items: typeof publicMenuItemsFlat) => {
        items.forEach((item) => {
          if (item.href !== undefined) {
            expect(item.href).not.toBe('');
            expect(item.href).not.toBe('undefined');
          }
          if (item.children) {
            checkRoutes(item.children);
          }
        });
      };
      checkRoutes(publicMenuItemsFlat);
      checkRoutes(articleAnalyzerMenuItems);
    });
  });
});
