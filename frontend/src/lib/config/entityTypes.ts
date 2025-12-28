import { Building2, Users, LucideIcon } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { createElement, type ReactNode } from 'react';
import type { GovernmentOrganization, GovernmentBranch } from '@/types/government-org';
import { peopleSubtypes, type SubtypeConfig } from './peopleConfig';

// Re-export SubtypeConfig for convenience
export type { SubtypeConfig } from './peopleConfig';

/**
 * View modes available for entity browsing
 */
export type ViewMode = 'list' | 'hierarchy';

/**
 * Sort direction
 */
export type SortDirection = 'asc' | 'desc';

/**
 * Column configuration for EntityBrowser table view
 */
export interface ColumnConfig<T = unknown> {
  /** Field name/key in the data object */
  id: string;
  /** Display label for column header */
  label: string;
  /** Whether this column is sortable */
  sortable?: boolean;
  /** Optional fixed width (e.g., '150px', '10rem') */
  width?: string;
  /** Custom render function for cell content */
  render?: (value: unknown, row: T) => ReactNode;
  /** Hide this column on mobile devices */
  hideOnMobile?: boolean;
  /** Accessor function to get value from row (if different from id) */
  accessor?: (row: T) => unknown;
}

/**
 * Filter types supported by EntityBrowser
 */
export type FilterType = 'select' | 'multi-select' | 'text';

/**
 * Filter option for select-type filters
 */
export interface FilterOption {
  value: string;
  label: string;
}

/**
 * Filter configuration for EntityBrowser
 */
export interface FilterConfig {
  /** Unique identifier for the filter */
  id: string;
  /** Display label */
  label: string;
  /** Type of filter control */
  type: FilterType;
  /** Options for select/multi-select filters */
  options?: FilterOption[];
  /** Query parameter name for API requests */
  apiParam: string;
  /** Placeholder text for text inputs */
  placeholder?: string;
}

/**
 * Default sort configuration
 */
export interface DefaultSort {
  column: string;
  direction: SortDirection;
}

/**
 * Card configuration for grid view
 */
export interface CardConfig<T = unknown> {
  /** Field to use as card title */
  titleField: string;
  /** Field to use as card subtitle */
  subtitleField?: string;
  /** Custom render function for card content */
  renderContent?: (item: T) => ReactNode;
  /** Custom render function for card badge/status */
  renderBadge?: (item: T) => ReactNode;
}

// =====================================================================
// EntityDetail Configuration Types
// =====================================================================

/**
 * Source citation information
 */
export interface SourceInfo {
  /** Source name (e.g., "Federal Judicial Center") */
  name: string;
  /** Source URL */
  url?: string;
  /** Date when data was retrieved */
  retrievedAt?: string;
  /** Data source identifier (e.g., "FJC", "CONGRESS_GOV") */
  dataSource?: string;
}

/**
 * Field configuration for detail sections
 */
export interface DetailFieldConfig<T = unknown> {
  /** Field path in data (supports dot notation) */
  id: string;
  /** Display label */
  label: string;
  /** Custom render function */
  render?: (value: unknown, entity: T) => ReactNode;
  /** Field containing source info */
  sourceField?: string;
  /** Hide this field if value is empty/null */
  hideIfEmpty?: boolean;
}

/**
 * Layout types for detail sections
 */
export type DetailSectionLayout = 'list' | 'grid' | 'key-value';

/**
 * Section configuration for EntityDetail
 */
export interface DetailSectionConfig<T = unknown> {
  /** Unique identifier */
  id: string;
  /** Section label/title */
  label: string;
  /** Fields in this section */
  fields: DetailFieldConfig<T>[];
  /** Layout type */
  layout: DetailSectionLayout;
  /** Whether section can be collapsed */
  collapsible?: boolean;
  /** Whether section starts collapsed */
  defaultCollapsed?: boolean;
}

/**
 * Related entity configuration
 */
export interface RelatedEntityConfig {
  /** Entity type (e.g., 'organizations', 'people') */
  entityType: string;
  /** Field containing related entity ids or objects */
  field: string;
  /** Section label */
  label: string;
  /** Field to display as link text */
  displayField: string;
  /** Field containing the entity ID (defaults to 'id') */
  idField?: string;
}

/**
 * Header configuration for EntityDetail
 */
export interface DetailHeaderConfig {
  /** Field to use as title */
  titleField: string;
  /** Field to use as subtitle */
  subtitleField?: string;
  /** Field for type badge */
  badgeField?: string;
  /** Custom badge render function */
  renderBadge?: (value: unknown) => ReactNode;
  /** Key metadata fields to show in header */
  metaFields?: string[];
}

/**
 * Complete EntityDetail configuration
 */
export interface EntityDetailConfig<T = unknown> {
  /** Header configuration */
  header: DetailHeaderConfig;
  /** Section configurations */
  sections: DetailSectionConfig<T>[];
  /** Related entities configuration */
  relatedEntities?: RelatedEntityConfig[];
}

// =====================================================================
// HierarchyView Configuration Types
// =====================================================================

/**
 * Node in a hierarchy tree
 * Note: Using Record<string, unknown> to allow for dynamic fields
 */
export type HierarchyNode = {
  /** Unique identifier */
  id: string;
  /** Child nodes */
  children?: HierarchyNode[];
} & Record<string, unknown>;

/**
 * Configuration for hierarchy view rendering
 */
export interface HierarchyConfig {
  /** Field to display as node label */
  labelField: string;
  /** Optional metadata fields to display */
  metaFields?: string[];
  /** Field containing child nodes */
  childrenField: string;
  /** Field containing unique identifier (defaults to 'id') */
  idField?: string;
  /** How many levels to expand by default (1 = only root) */
  defaultExpandDepth: number;
  /** Whether to show child count next to expandable nodes */
  showChildCount?: boolean;
  /** Custom render function for node content */
  renderNode?: (node: HierarchyNode) => ReactNode;
  /** Custom render function for node badge */
  renderBadge?: (node: HierarchyNode) => ReactNode;
}

/**
 * Entity type configuration that drives UI behavior
 */
export interface EntityTypeConfig<T = unknown> {
  /** Unique identifier used in URLs */
  id: string;
  /** Display label */
  label: string;
  /** Icon component from lucide-react */
  icon: LucideIcon;
  /** API endpoint for fetching entities */
  apiEndpoint: string;
  /** Supported view modes for this entity type */
  supportedViews: ViewMode[];
  /** Default view mode */
  defaultView: ViewMode;
  /** Whether this entity type supports subtypes (e.g., people -> judges, members) */
  hasSubtypes?: boolean;
  /** Subtype configurations (if hasSubtypes is true) */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  subtypes?: SubtypeConfig<any>[];
  /** Default subtype ID (if hasSubtypes is true) */
  defaultSubtype?: string;
  /** Column definitions for list/table view */
  columns?: ColumnConfig<T>[];
  /** Filter definitions */
  filters?: FilterConfig[];
  /** Default sort configuration */
  defaultSort?: DefaultSort;
  /** Card configuration for grid view */
  cardConfig?: CardConfig<T>;
  /** Field to use as unique identifier (defaults to 'id') */
  idField?: string;
  /** Detail view configuration */
  detailConfig?: EntityDetailConfig<T>;
  /** Hierarchy view configuration */
  hierarchyConfig?: HierarchyConfig;
}

/**
 * Helper to create a badge variant based on branch
 */
function getBranchVariant(branch: GovernmentBranch): 'default' | 'secondary' | 'outline' {
  switch (branch) {
    case 'executive':
      return 'default';
    case 'legislative':
      return 'secondary';
    case 'judicial':
      return 'outline';
    default:
      return 'secondary';
  }
}

/**
 * Organization-specific column configuration
 */
const organizationColumns: ColumnConfig<GovernmentOrganization>[] = [
  {
    id: 'officialName',
    label: 'Name',
    sortable: true,
    width: '30%',
  },
  {
    id: 'acronym',
    label: 'Acronym',
    sortable: true,
    width: '100px',
    render: (value) => (value as string) || '-',
  },
  {
    id: 'branch',
    label: 'Branch',
    sortable: true,
    width: '120px',
    render: (value) => {
      const branch = value as GovernmentBranch;
      return createElement(
        Badge,
        { variant: getBranchVariant(branch) },
        branch.charAt(0).toUpperCase() + branch.slice(1)
      );
    },
  },
  {
    id: 'orgType',
    label: 'Type',
    sortable: true,
    width: '150px',
    hideOnMobile: true,
    render: (value) => {
      const type = value as string;
      return type.charAt(0).toUpperCase() + type.slice(1).replace(/_/g, ' ');
    },
  },
  {
    id: 'active',
    label: 'Status',
    sortable: false,
    width: '100px',
    hideOnMobile: true,
    render: (value) => {
      const active = value as boolean;
      return createElement(
        Badge,
        { variant: active ? 'default' : 'secondary' },
        active ? 'Active' : 'Inactive'
      );
    },
  },
];

/**
 * Organization-specific detail configuration
 */
const organizationDetailConfig: EntityDetailConfig<GovernmentOrganization> = {
  header: {
    titleField: 'officialName',
    subtitleField: 'acronym',
    badgeField: 'branch',
    renderBadge: (value) => {
      const branch = value as GovernmentBranch;
      return createElement(
        Badge,
        { variant: getBranchVariant(branch) },
        branch.charAt(0).toUpperCase() + branch.slice(1)
      );
    },
    metaFields: ['orgType', 'orgLevel'],
  },
  sections: [
    {
      id: 'overview',
      label: 'Overview',
      layout: 'key-value',
      fields: [
        {
          id: 'description',
          label: 'Description',
          hideIfEmpty: true,
        },
        {
          id: 'mission',
          label: 'Mission',
          hideIfEmpty: true,
        },
        {
          id: 'websiteUrl',
          label: 'Website',
          hideIfEmpty: true,
          render: (value) => {
            if (!value) return '-';
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
      ],
    },
    {
      id: 'details',
      label: 'Organization Details',
      layout: 'grid',
      fields: [
        {
          id: 'orgType',
          label: 'Organization Type',
          render: (value) => {
            if (!value) return '-';
            const type = value as string;
            return type.charAt(0).toUpperCase() + type.slice(1).replace(/_/g, ' ');
          },
        },
        {
          id: 'orgLevel',
          label: 'Organization Level',
        },
        {
          id: 'active',
          label: 'Status',
          render: (value) => {
            const active = value as boolean;
            return createElement(
              Badge,
              { variant: active ? 'default' : 'secondary' },
              active ? 'Active' : 'Inactive'
            );
          },
        },
        {
          id: 'branch',
          label: 'Branch',
          render: (value) => {
            const branch = value as GovernmentBranch;
            return createElement(
              Badge,
              { variant: getBranchVariant(branch) },
              branch.charAt(0).toUpperCase() + branch.slice(1)
            );
          },
        },
      ],
    },
    {
      id: 'dates',
      label: 'Timeline',
      layout: 'grid',
      collapsible: true,
      fields: [
        {
          id: 'establishedDate',
          label: 'Established',
          hideIfEmpty: true,
        },
        {
          id: 'dissolvedDate',
          label: 'Dissolved',
          hideIfEmpty: true,
        },
      ],
    },
    {
      id: 'jurisdiction',
      label: 'Jurisdiction',
      layout: 'list',
      collapsible: true,
      defaultCollapsed: true,
      fields: [
        {
          id: 'jurisdictionAreas',
          label: 'Jurisdiction Areas',
          hideIfEmpty: true,
          render: (value) => {
            if (!value || !Array.isArray(value) || value.length === 0) return '-';
            return value.join(', ');
          },
        },
      ],
    },
  ],
  relatedEntities: [
    // Future: Add related people/positions when available
  ],
};

/**
 * Organization-specific hierarchy configuration
 */
const organizationHierarchyConfig: HierarchyConfig = {
  labelField: 'officialName',
  metaFields: ['acronym'],
  childrenField: 'children',
  idField: 'id',
  defaultExpandDepth: 1,
  showChildCount: true,
  renderBadge: (node) => {
    const branch = node.branch as GovernmentBranch;
    if (!branch) return null;
    return createElement(
      Badge,
      { variant: getBranchVariant(branch), className: 'text-xs' },
      branch.charAt(0).toUpperCase() + branch.slice(1)
    );
  },
};

/**
 * Organization-specific filter configuration
 */
const organizationFilters: FilterConfig[] = [
  {
    id: 'branch',
    label: 'Branch',
    type: 'select',
    apiParam: 'branch',
    options: [
      { value: 'executive', label: 'Executive' },
      { value: 'legislative', label: 'Legislative' },
      { value: 'judicial', label: 'Judicial' },
    ],
  },
  {
    id: 'orgType',
    label: 'Type',
    type: 'select',
    apiParam: 'type',
    options: [
      { value: 'department', label: 'Department' },
      { value: 'agency', label: 'Agency' },
      { value: 'bureau', label: 'Bureau' },
      { value: 'commission', label: 'Commission' },
      { value: 'office', label: 'Office' },
      { value: 'council', label: 'Council' },
    ],
  },
  {
    id: 'active',
    label: 'Status',
    type: 'select',
    apiParam: 'active',
    options: [
      { value: 'true', label: 'Active' },
      { value: 'false', label: 'Inactive' },
    ],
  },
];

/**
 * Configuration for all available entity types in Knowledge Explorer
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const entityTypes: EntityTypeConfig<any>[] = [
  {
    id: 'organizations',
    label: 'Organizations',
    icon: Building2,
    apiEndpoint: '/api/government-organizations',
    supportedViews: ['list', 'hierarchy'],
    defaultView: 'list',
    columns: organizationColumns,
    filters: organizationFilters,
    defaultSort: {
      column: 'officialName',
      direction: 'asc',
    },
    cardConfig: {
      titleField: 'officialName',
      subtitleField: 'acronym',
      renderBadge: (item) =>
        createElement(
          Badge,
          { variant: getBranchVariant(item.branch) },
          item.branch.charAt(0).toUpperCase() + item.branch.slice(1)
        ),
    },
    detailConfig: organizationDetailConfig,
    hierarchyConfig: organizationHierarchyConfig,
  },
  {
    id: 'people',
    label: 'People',
    icon: Users,
    apiEndpoint: '/api/judges', // Default to judges, varies by subtype
    supportedViews: ['list'],
    defaultView: 'list',
    hasSubtypes: true,
    subtypes: peopleSubtypes,
    defaultSubtype: 'judges',
  },
];

/**
 * Get entity type configuration by ID
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getEntityTypeConfig(entityTypeId: string): EntityTypeConfig<any> | undefined {
  return entityTypes.find((et) => et.id === entityTypeId);
}

/**
 * Get the default entity type (first in the list)
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getDefaultEntityType(): EntityTypeConfig<any> {
  return entityTypes[0];
}

/**
 * Check if a view mode is supported for an entity type
 */
export function isViewModeSupported(entityTypeId: string, viewMode: ViewMode): boolean {
  const config = getEntityTypeConfig(entityTypeId);
  return config?.supportedViews.includes(viewMode) ?? false;
}
