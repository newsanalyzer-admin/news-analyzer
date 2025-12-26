import { Building2, Users, LucideIcon } from 'lucide-react';

/**
 * View modes available for entity browsing
 */
export type ViewMode = 'list' | 'hierarchy';

/**
 * Entity type configuration that drives UI behavior
 */
export interface EntityTypeConfig {
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
}

/**
 * Configuration for all available entity types in Knowledge Explorer
 */
export const entityTypes: EntityTypeConfig[] = [
  {
    id: 'organizations',
    label: 'Organizations',
    icon: Building2,
    apiEndpoint: '/api/government-organizations',
    supportedViews: ['list', 'hierarchy'],
    defaultView: 'list',
  },
  {
    id: 'people',
    label: 'People',
    icon: Users,
    apiEndpoint: '/api/people',
    supportedViews: ['list'],
    defaultView: 'list',
    hasSubtypes: true,
  },
];

/**
 * Get entity type configuration by ID
 */
export function getEntityTypeConfig(entityTypeId: string): EntityTypeConfig | undefined {
  return entityTypes.find((et) => et.id === entityTypeId);
}

/**
 * Get the default entity type (first in the list)
 */
export function getDefaultEntityType(): EntityTypeConfig {
  return entityTypes[0];
}

/**
 * Check if a view mode is supported for an entity type
 */
export function isViewModeSupported(entityTypeId: string, viewMode: ViewMode): boolean {
  const config = getEntityTypeConfig(entityTypeId);
  return config?.supportedViews.includes(viewMode) ?? false;
}
