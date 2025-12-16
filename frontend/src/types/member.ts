/**
 * Member Types
 *
 * TypeScript interfaces and Zod schemas for Person and PositionHolding entities.
 */

import { z } from 'zod';

/**
 * Chamber enum
 */
export type Chamber = 'SENATE' | 'HOUSE';

/**
 * Data source for records
 */
export type DataSource = 'congress_gov' | 'govinfo' | 'legislators_repo' | 'manual';

/**
 * Social media links
 */
export interface SocialMedia {
  twitter?: string;
  facebook?: string;
  youtube?: string;
  youtube_id?: string;
}

/**
 * Person entity from /api/members
 */
export interface Person {
  id: string;
  bioguideId: string;
  firstName: string;
  lastName: string;
  middleName?: string;
  suffix?: string;
  party?: string;
  state?: string;
  chamber?: Chamber;
  birthDate?: string;
  gender?: string;
  imageUrl?: string | null;
  externalIds?: Record<string, unknown>;
  socialMedia?: SocialMedia | null;
  enrichmentSource?: string | null;
  enrichmentVersion?: string | null;
  congressLastSync?: string | null;
  dataSource?: string | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * PositionHolding entity from /api/members/{id}/terms
 */
export interface PositionHolding {
  id: string;
  personId: string;
  positionId: string;
  startDate: string;
  endDate?: string | null;
  congress?: number | null;
  dataSource: DataSource;
  sourceReference?: string | null;
  createdAt: string;
  updatedAt: string;
  termLabel?: string | null;
}

// ============ Zod Schemas ============

export const SocialMediaSchema = z.object({
  twitter: z.string().optional(),
  facebook: z.string().optional(),
  youtube: z.string().optional(),
  youtube_id: z.string().optional(),
});

export const PersonSchema = z.object({
  id: z.string().uuid(),
  bioguideId: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  middleName: z.string().optional(),
  suffix: z.string().optional(),
  party: z.string().optional(),
  state: z.string().length(2).optional(),
  chamber: z.enum(['SENATE', 'HOUSE']).optional(),
  birthDate: z.string().optional(),
  gender: z.string().optional(),
  imageUrl: z.string().url().optional().nullable(),
  externalIds: z.record(z.unknown()).optional(),
  socialMedia: SocialMediaSchema.optional().nullable(),
  enrichmentSource: z.string().optional().nullable(),
  enrichmentVersion: z.string().optional().nullable(),
  congressLastSync: z.string().optional().nullable(),
  dataSource: z.string().optional().nullable(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const DataSourceSchema = z.enum([
  'congress_gov',
  'govinfo',
  'legislators_repo',
  'manual',
]);

export const PositionHoldingSchema = z.object({
  id: z.string().uuid(),
  personId: z.string().uuid(),
  positionId: z.string().uuid(),
  startDate: z.string(),
  endDate: z.string().optional().nullable(),
  congress: z.number().optional().nullable(),
  dataSource: DataSourceSchema,
  sourceReference: z.string().optional().nullable(),
  createdAt: z.string(),
  updatedAt: z.string(),
  termLabel: z.string().optional().nullable(),
});

// ============ Validation Helpers ============

/**
 * Validate a Person object at runtime
 */
export function validatePerson(data: unknown): Person {
  return PersonSchema.parse(data);
}

/**
 * Validate a PositionHolding object at runtime
 */
export function validatePositionHolding(data: unknown): PositionHolding {
  return PositionHoldingSchema.parse(data);
}

// ============ API Response Types ============

/**
 * Party statistics response
 */
export interface PartyStats {
  party: string;
  count: number;
}

/**
 * State statistics response
 */
export interface StateStats {
  state: string;
  count: number;
}

/**
 * Enrichment status response
 */
export interface EnrichmentStatus {
  totalMembers: number;
  enrichedMembers: number;
  pendingMembers: number;
  lastSyncTime?: string;
}
