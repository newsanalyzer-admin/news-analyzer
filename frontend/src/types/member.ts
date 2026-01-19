/**
 * Member Types
 *
 * TypeScript interfaces and Zod schemas for Member (CongressionalMember + Individual)
 * and PositionHolding entities.
 *
 * Part of ARCH-1.8: Updated to match dual-entity model (Individual + CongressionalMember).
 */

import { z } from 'zod';

/**
 * Chamber enum
 */
export type Chamber = 'SENATE' | 'HOUSE';

/**
 * Data source for records
 */
export type DataSource = 'CONGRESS_GOV' | 'GOVINFO' | 'LEGISLATORS_REPO' | 'MANUAL' | 'FJC' | 'PLUM';

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
 * Member entity from /api/members
 *
 * This is a flattened DTO combining Individual (biographical) and
 * CongressionalMember (Congress-specific) data.
 */
export interface Member {
  id: string;

  // From Individual (biographical data)
  firstName: string;
  lastName: string;
  middleName?: string | null;
  suffix?: string | null;
  fullName?: string | null;
  birthDate?: string | null;
  deathDate?: string | null;
  birthPlace?: string | null;
  gender?: string | null;
  imageUrl?: string | null;
  isLiving?: boolean | null;

  // From CongressionalMember (Congress-specific data)
  bioguideId: string;
  chamber?: Chamber | null;
  state?: string | null;
  party?: string | null;
  congressLastSync?: string | null;
  enrichmentSource?: string | null;
  enrichmentVersion?: string | null;
  dataSource?: string | null;

  createdAt: string;
  updatedAt: string;
}

/**
 * @deprecated Use Member instead. Alias for backward compatibility.
 */
export type Person = Member;

/**
 * PositionHolding entity from /api/members/{id}/terms
 *
 * Part of ARCH-1.8: Updated to use individualId instead of personId.
 */
export interface PositionHolding {
  id: string;
  individualId: string;
  positionId: string;
  startDate: string;
  endDate?: string | null;
  congress?: number | null;
  dataSource: DataSource;
  sourceReference?: string | null;
  createdAt: string;
  updatedAt: string;
  termLabel?: string | null;

  // Legacy alias for backward compatibility
  /** @deprecated Use individualId instead */
  personId?: string;
}

// ============ Zod Schemas ============

export const SocialMediaSchema = z.object({
  twitter: z.string().optional(),
  facebook: z.string().optional(),
  youtube: z.string().optional(),
  youtube_id: z.string().optional(),
});

export const MemberSchema = z.object({
  id: z.string().uuid(),

  // From Individual (biographical data)
  firstName: z.string(),
  lastName: z.string(),
  middleName: z.string().optional().nullable(),
  suffix: z.string().optional().nullable(),
  fullName: z.string().optional().nullable(),
  birthDate: z.string().optional().nullable(),
  deathDate: z.string().optional().nullable(),
  birthPlace: z.string().optional().nullable(),
  gender: z.string().optional().nullable(),
  imageUrl: z.string().url().optional().nullable(),
  isLiving: z.boolean().optional().nullable(),

  // From CongressionalMember (Congress-specific data)
  bioguideId: z.string(),
  chamber: z.enum(['SENATE', 'HOUSE']).optional().nullable(),
  state: z.string().length(2).optional().nullable(),
  party: z.string().optional().nullable(),
  congressLastSync: z.string().optional().nullable(),
  enrichmentSource: z.string().optional().nullable(),
  enrichmentVersion: z.string().optional().nullable(),
  dataSource: z.string().optional().nullable(),

  createdAt: z.string(),
  updatedAt: z.string(),
});

/**
 * @deprecated Use MemberSchema instead. Alias for backward compatibility.
 */
export const PersonSchema = MemberSchema;

export const DataSourceSchema = z.enum([
  'CONGRESS_GOV',
  'GOVINFO',
  'LEGISLATORS_REPO',
  'MANUAL',
  'FJC',
  'PLUM',
]);

export const PositionHoldingSchema = z.object({
  id: z.string().uuid(),
  individualId: z.string().uuid(),
  positionId: z.string().uuid(),
  startDate: z.string(),
  endDate: z.string().optional().nullable(),
  congress: z.number().optional().nullable(),
  dataSource: DataSourceSchema,
  sourceReference: z.string().optional().nullable(),
  createdAt: z.string(),
  updatedAt: z.string(),
  termLabel: z.string().optional().nullable(),
  // Legacy alias
  personId: z.string().uuid().optional(),
});

// ============ Validation Helpers ============

/**
 * Validate a Member object at runtime
 */
export function validateMember(data: unknown): Member {
  return MemberSchema.parse(data);
}

/**
 * @deprecated Use validateMember instead
 */
export function validatePerson(data: unknown): Member {
  return validateMember(data);
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
