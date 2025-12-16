/**
 * Committee Types
 *
 * TypeScript interfaces and Zod schemas for Committee and CommitteeMembership entities.
 */

import { z } from 'zod';
import { PersonSchema, type Person } from './member';

/**
 * Committee chamber type (includes JOINT)
 */
export type CommitteeChamber = 'SENATE' | 'HOUSE' | 'JOINT';

/**
 * Committee type classification
 */
export type CommitteeType =
  | 'STANDING'
  | 'SELECT'
  | 'SPECIAL'
  | 'JOINT'
  | 'SUBCOMMITTEE'
  | 'OTHER';

/**
 * Membership role in a committee
 */
export type MembershipRole =
  | 'CHAIR'
  | 'VICE_CHAIR'
  | 'RANKING_MEMBER'
  | 'MEMBER'
  | 'EX_OFFICIO';

/**
 * Committee entity from /api/committees
 */
export interface Committee {
  committeeCode: string;
  name: string;
  chamber: CommitteeChamber;
  committeeType: CommitteeType;
  parentCommitteeCode?: string | null;
  thomasId?: string | null;
  url?: string | null;
  congressLastSync?: string | null;
  dataSource?: string | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Committee membership entity from /api/members/{id}/committees
 */
export interface CommitteeMembership {
  id: string;
  person: Person;
  committee: Committee;
  role: MembershipRole;
  congress: number;
  startDate?: string | null;
  endDate?: string | null;
  congressLastSync?: string | null;
  dataSource?: string | null;
  createdAt: string;
  updatedAt: string;
}

// ============ Zod Schemas ============

export const CommitteeChamberSchema = z.enum(['SENATE', 'HOUSE', 'JOINT']);

export const CommitteeTypeSchema = z.enum([
  'STANDING',
  'SELECT',
  'SPECIAL',
  'JOINT',
  'SUBCOMMITTEE',
  'OTHER',
]);

export const MembershipRoleSchema = z.enum([
  'CHAIR',
  'VICE_CHAIR',
  'RANKING_MEMBER',
  'MEMBER',
  'EX_OFFICIO',
]);

export const CommitteeSchema = z.object({
  committeeCode: z.string(),
  name: z.string(),
  chamber: CommitteeChamberSchema,
  committeeType: CommitteeTypeSchema,
  parentCommitteeCode: z.string().optional().nullable(),
  thomasId: z.string().optional().nullable(),
  url: z.string().url().optional().nullable(),
  congressLastSync: z.string().optional().nullable(),
  dataSource: z.string().optional().nullable(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const CommitteeMembershipSchema = z.object({
  id: z.string().uuid(),
  person: PersonSchema,
  committee: CommitteeSchema,
  role: MembershipRoleSchema,
  congress: z.number(),
  startDate: z.string().optional().nullable(),
  endDate: z.string().optional().nullable(),
  congressLastSync: z.string().optional().nullable(),
  dataSource: z.string().optional().nullable(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

// ============ Validation Helpers ============

/**
 * Validate a Committee object at runtime
 */
export function validateCommittee(data: unknown): Committee {
  return CommitteeSchema.parse(data);
}

/**
 * Validate a CommitteeMembership object at runtime
 */
export function validateCommitteeMembership(data: unknown): CommitteeMembership {
  return CommitteeMembershipSchema.parse(data);
}
