/**
 * Committees Entity Type Configuration
 *
 * Configuration for the Committees entity type in the Knowledge Explorer.
 * Committees data comes from the `committees` table (Knowledge Base layer).
 */

import { Badge } from '@/components/ui/badge';
import { createElement, type ReactNode } from 'react';
import type {
  Committee,
  CommitteeChamber,
  CommitteeType,
} from '@/types/committee';
import type {
  ColumnConfig,
  FilterConfig,
  DefaultSort,
  EntityDetailConfig,
  HierarchyConfig,
} from './entityTypes';

// =====================================================================
// Helper Functions
// =====================================================================

/**
 * Get badge variant based on chamber
 */
function getChamberVariant(chamber: CommitteeChamber): 'default' | 'secondary' | 'outline' {
  switch (chamber) {
    case 'SENATE':
      return 'default';
    case 'HOUSE':
      return 'secondary';
    case 'JOINT':
      return 'outline';
    default:
      return 'secondary';
  }
}

/**
 * Format committee type for display
 */
function formatCommitteeType(type: CommitteeType): string {
  const labels: Record<CommitteeType, string> = {
    STANDING: 'Standing',
    SELECT: 'Select',
    SPECIAL: 'Special',
    JOINT: 'Joint',
    SUBCOMMITTEE: 'Subcommittee',
    OTHER: 'Other',
  };
  return labels[type] || type;
}

/**
 * Render chamber badge
 */
function renderChamberBadge(chamber: CommitteeChamber): ReactNode {
  const labels: Record<CommitteeChamber, string> = {
    SENATE: 'Senate',
    HOUSE: 'House',
    JOINT: 'Joint',
  };
  return createElement(
    Badge,
    { variant: getChamberVariant(chamber) },
    labels[chamber] || chamber
  );
}

// =====================================================================
// Column Configuration
// =====================================================================

/**
 * Column configuration for committees list view
 */
export const committeeColumns: ColumnConfig<Committee>[] = [
  {
    id: 'name',
    label: 'Committee Name',
    sortable: true,
    width: '40%',
  },
  {
    id: 'chamber',
    label: 'Chamber',
    sortable: true,
    width: '15%',
    render: (value) => renderChamberBadge(value as CommitteeChamber),
  },
  {
    id: 'committeeType',
    label: 'Type',
    sortable: true,
    width: '15%',
    hideOnMobile: true,
    render: (value) => formatCommitteeType(value as CommitteeType),
  },
  {
    id: 'committeeCode',
    label: 'Code',
    sortable: true,
    width: '15%',
    hideOnMobile: true,
    render: (value) => {
      const code = value as string;
      return createElement(
        'code',
        { className: 'text-xs bg-muted px-1.5 py-0.5 rounded' },
        code
      );
    },
  },
  {
    id: 'url',
    label: 'Link',
    sortable: false,
    width: '15%',
    hideOnMobile: true,
    render: (value) => {
      const url = value as string | null;
      if (!url) return '-';
      return createElement(
        'a',
        {
          href: url,
          target: '_blank',
          rel: 'noopener noreferrer',
          className: 'text-primary hover:underline text-xs',
          onClick: (e: React.MouseEvent) => e.stopPropagation(),
        },
        'Congress.gov'
      );
    },
  },
];

// =====================================================================
// Filter Configuration
// =====================================================================

/**
 * Filter configuration for committees
 */
export const committeeFilters: FilterConfig[] = [
  {
    id: 'chamber',
    label: 'Chamber',
    type: 'select',
    apiParam: 'chamber',
    options: [
      { value: 'SENATE', label: 'Senate' },
      { value: 'HOUSE', label: 'House' },
      { value: 'JOINT', label: 'Joint' },
    ],
  },
  {
    id: 'type',
    label: 'Type',
    type: 'select',
    apiParam: 'type',
    options: [
      { value: 'STANDING', label: 'Standing' },
      { value: 'SELECT', label: 'Select' },
      { value: 'SPECIAL', label: 'Special' },
      { value: 'JOINT', label: 'Joint' },
      { value: 'SUBCOMMITTEE', label: 'Subcommittee' },
    ],
  },
];

// =====================================================================
// Default Sort
// =====================================================================

/**
 * Default sort configuration
 */
export const committeeDefaultSort: DefaultSort = {
  column: 'name',
  direction: 'asc',
};

// =====================================================================
// Detail Configuration
// =====================================================================

/**
 * Detail view configuration for committees
 */
export const committeeDetailConfig: EntityDetailConfig<Committee> = {
  header: {
    titleField: 'name',
    subtitleField: 'committeeCode',
    badgeField: 'chamber',
    renderBadge: (value) => renderChamberBadge(value as CommitteeChamber),
    metaFields: ['committeeType'],
  },
  sections: [
    {
      id: 'overview',
      label: 'Overview',
      layout: 'grid',
      fields: [
        {
          id: 'chamber',
          label: 'Chamber',
          render: (value) => renderChamberBadge(value as CommitteeChamber),
        },
        {
          id: 'committeeType',
          label: 'Committee Type',
          render: (value) => formatCommitteeType(value as CommitteeType),
        },
        {
          id: 'committeeCode',
          label: 'Committee Code',
          render: (value) =>
            createElement(
              'code',
              { className: 'text-sm bg-muted px-2 py-1 rounded' },
              value as string
            ),
        },
        {
          id: 'parentCommitteeCode',
          label: 'Parent Committee',
          hideIfEmpty: true,
          render: (value) => {
            if (!value) return null;
            return createElement(
              'code',
              { className: 'text-sm bg-muted px-2 py-1 rounded' },
              value as string
            );
          },
        },
      ],
    },
    {
      id: 'links',
      label: 'External Links',
      layout: 'list',
      collapsible: true,
      fields: [
        {
          id: 'url',
          label: 'Congress.gov',
          hideIfEmpty: true,
          render: (value) => {
            if (!value) return null;
            return createElement(
              'a',
              {
                href: value as string,
                target: '_blank',
                rel: 'noopener noreferrer',
                className: 'text-primary hover:underline',
              },
              value as string
            );
          },
        },
        {
          id: 'thomasId',
          label: 'THOMAS ID',
          hideIfEmpty: true,
        },
      ],
    },
    {
      id: 'metadata',
      label: 'Metadata',
      layout: 'grid',
      collapsible: true,
      defaultCollapsed: true,
      fields: [
        {
          id: 'dataSource',
          label: 'Data Source',
          hideIfEmpty: true,
        },
        {
          id: 'congressLastSync',
          label: 'Last Synced',
          hideIfEmpty: true,
        },
        {
          id: 'createdAt',
          label: 'Created',
        },
        {
          id: 'updatedAt',
          label: 'Updated',
        },
      ],
    },
  ],
};

// =====================================================================
// Hierarchy Configuration
// =====================================================================

/**
 * Hierarchy view configuration for committees (subcommittee relationships)
 */
export const committeeHierarchyConfig: HierarchyConfig = {
  labelField: 'name',
  metaFields: ['committeeCode'],
  childrenField: 'subcommittees',
  idField: 'committeeCode',
  defaultExpandDepth: 1,
  showChildCount: true,
  renderBadge: (node) => {
    const chamber = node.chamber as CommitteeChamber;
    if (!chamber) return null;
    return createElement(
      Badge,
      { variant: getChamberVariant(chamber), className: 'text-xs' },
      chamber
    );
  },
};

// =====================================================================
// Card Configuration
// =====================================================================

/**
 * Card configuration for grid view
 */
export const committeeCardConfig = {
  titleField: 'name',
  subtitleField: 'committeeCode',
  renderBadge: (item: Committee) => renderChamberBadge(item.chamber),
};
