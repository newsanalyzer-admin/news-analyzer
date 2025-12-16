/**
 * Entity Types for NewsAnalyzer v2
 *
 * Type definitions for entities extracted from news articles with Schema.org support.
 */

/**
 * Internal entity type classification
 */
export type EntityType =
  | 'person'
  | 'government_org'
  | 'organization'
  | 'location'
  | 'event'
  | 'concept'
  | 'legislation'
  | 'political_party'
  | 'news_media';

/**
 * Schema.org JSON-LD representation
 */
export interface SchemaOrgData {
  '@context': string;
  '@type': string;
  '@id'?: string;
  name: string;
  [key: string]: any;
}

/**
 * Extracted entity from Python reasoning service
 */
export interface ExtractedEntity {
  text: string;
  entity_type: EntityType;
  start: number;
  end: number;
  confidence: number;
  schema_org_type: string;
  schema_org_data: SchemaOrgData;
  properties: Record<string, any>;
}

/**
 * Entity extraction request
 */
export interface EntityExtractionRequest {
  text: string;
  confidence_threshold?: number;
}

/**
 * Entity extraction response
 */
export interface EntityExtractionResponse {
  entities: ExtractedEntity[];
  total_count: number;
}

/**
 * Entity stored in backend (Java)
 */
export interface Entity {
  id: string;
  entity_type: EntityType;
  name: string;
  properties: Record<string, any>;
  schema_org_type: string;
  schema_org_data: SchemaOrgData;
  source?: string;
  confidence_score: number;
  verified: boolean;
  created_at: string;
  updated_at: string;
}

/**
 * Entity creation request for backend
 */
export interface CreateEntityRequest {
  entity_type: EntityType;
  name: string;
  properties?: Record<string, any>;
  schema_org_type?: string;
  schema_org_data?: SchemaOrgData;
  source?: string;
  confidence_score?: number;
  verified?: boolean;
}

/**
 * Entity type metadata for UI display
 */
export interface EntityTypeMetadata {
  type: EntityType;
  label: string;
  icon: string;
  color: string;
  bgColor: string;
  schemaOrgType: string;
}

/**
 * Entity type to metadata mapping
 */
export const ENTITY_TYPE_METADATA: Record<EntityType, EntityTypeMetadata> = {
  person: {
    type: 'person',
    label: 'Person',
    icon: '👤',
    color: 'text-blue-700',
    bgColor: 'bg-blue-100',
    schemaOrgType: 'Person',
  },
  government_org: {
    type: 'government_org',
    label: 'Government',
    icon: '🏛️',
    color: 'text-purple-700',
    bgColor: 'bg-purple-100',
    schemaOrgType: 'GovernmentOrganization',
  },
  organization: {
    type: 'organization',
    label: 'Organization',
    icon: '🏢',
    color: 'text-green-700',
    bgColor: 'bg-green-100',
    schemaOrgType: 'Organization',
  },
  location: {
    type: 'location',
    label: 'Location',
    icon: '📍',
    color: 'text-red-700',
    bgColor: 'bg-red-100',
    schemaOrgType: 'Place',
  },
  event: {
    type: 'event',
    label: 'Event',
    icon: '📅',
    color: 'text-orange-700',
    bgColor: 'bg-orange-100',
    schemaOrgType: 'Event',
  },
  concept: {
    type: 'concept',
    label: 'Concept',
    icon: '💡',
    color: 'text-yellow-700',
    bgColor: 'bg-yellow-100',
    schemaOrgType: 'Thing',
  },
  legislation: {
    type: 'legislation',
    label: 'Legislation',
    icon: '📜',
    color: 'text-indigo-700',
    bgColor: 'bg-indigo-100',
    schemaOrgType: 'Legislation',
  },
  political_party: {
    type: 'political_party',
    label: 'Political Party',
    icon: '🎭',
    color: 'text-pink-700',
    bgColor: 'bg-pink-100',
    schemaOrgType: 'PoliticalParty',
  },
  news_media: {
    type: 'news_media',
    label: 'News Media',
    icon: '📰',
    color: 'text-gray-700',
    bgColor: 'bg-gray-100',
    schemaOrgType: 'NewsMediaOrganization',
  },
};
