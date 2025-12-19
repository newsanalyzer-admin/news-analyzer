/**
 * Judge Types
 *
 * TypeScript interfaces and Zod schemas for federal judge data.
 */

import { z } from 'zod';

/**
 * Court level enum
 */
export type CourtLevel = 'SUPREME' | 'APPEALS' | 'DISTRICT';

/**
 * Judicial status enum
 */
export type JudicialStatus = 'ACTIVE' | 'SENIOR' | 'DECEASED' | 'RESIGNED' | 'TERMINATED';

/**
 * Circuit enum
 */
export type Circuit =
  | '1'
  | '2'
  | '3'
  | '4'
  | '5'
  | '6'
  | '7'
  | '8'
  | '9'
  | '10'
  | '11'
  | 'DC'
  | 'FEDERAL';

/**
 * Judge entity from /api/judges
 */
export interface Judge {
  id: string;
  fjcNid?: string | null;
  firstName: string;
  middleName?: string | null;
  lastName: string;
  suffix?: string | null;
  fullName: string;
  gender?: string | null;
  birthDate?: string | null;
  deathDate?: string | null;

  // Court Information
  courtName?: string | null;
  courtType?: string | null;
  circuit?: string | null;
  courtOrganizationId?: string | null;

  // Appointment Information
  appointingPresident?: string | null;
  partyOfAppointingPresident?: string | null;
  abaRating?: string | null;
  nominationDate?: string | null;
  confirmationDate?: string | null;
  commissionDate?: string | null;
  ayesCount?: number | null;
  naysCount?: number | null;

  // Service Information
  seniorStatusDate?: string | null;
  terminationDate?: string | null;
  terminationReason?: string | null;
  judicialStatus?: string | null;

  // Professional Background
  professionalCareer?: string | null;

  current: boolean;
}

// ============ Zod Schemas ============

export const JudgeSchema = z.object({
  id: z.string().uuid(),
  fjcNid: z.string().optional(),
  firstName: z.string(),
  middleName: z.string().optional().nullable(),
  lastName: z.string(),
  suffix: z.string().optional().nullable(),
  fullName: z.string(),
  gender: z.string().optional().nullable(),
  birthDate: z.string().optional().nullable(),
  deathDate: z.string().optional().nullable(),

  // Court Information
  courtName: z.string().optional().nullable(),
  courtType: z.string().optional().nullable(),
  circuit: z.string().optional().nullable(),
  courtOrganizationId: z.string().uuid().optional().nullable(),

  // Appointment Information
  appointingPresident: z.string().optional().nullable(),
  partyOfAppointingPresident: z.string().optional().nullable(),
  abaRating: z.string().optional().nullable(),
  nominationDate: z.string().optional().nullable(),
  confirmationDate: z.string().optional().nullable(),
  commissionDate: z.string().optional().nullable(),
  ayesCount: z.number().optional().nullable(),
  naysCount: z.number().optional().nullable(),

  // Service Information
  seniorStatusDate: z.string().optional().nullable(),
  terminationDate: z.string().optional().nullable(),
  terminationReason: z.string().optional().nullable(),
  judicialStatus: z.string().optional().nullable(),

  // Professional Background
  professionalCareer: z.string().optional().nullable(),

  current: z.boolean(),
});

// ============ Validation Helpers ============

/**
 * Validate a Judge object at runtime
 */
export function validateJudge(data: unknown): Judge {
  return JudgeSchema.parse(data);
}

// ============ API Response Types ============

/**
 * Judge statistics response
 */
export interface JudgeStats {
  totalJudges: number;
  activeJudges: number;
  seniorJudges: number;
  byCourtLevel: Record<string, number>;
  byCircuit: Record<string, number>;
  byAppointingParty: Record<string, number>;
}

// ============ Display Helpers ============

/**
 * Get display label for court level
 */
export function getCourtLevelLabel(level?: string | null): string {
  switch (level?.toUpperCase()) {
    case 'SUPREME':
      return 'Supreme Court';
    case 'APPEALS':
      return 'Court of Appeals';
    case 'DISTRICT':
      return 'District Court';
    default:
      return level || 'Unknown';
  }
}

/**
 * Get display label for circuit
 */
export function getCircuitLabel(circuit?: string | null): string {
  if (!circuit) return 'Unknown';
  if (circuit === 'DC') return 'D.C. Circuit';
  if (circuit === 'FEDERAL') return 'Federal Circuit';
  return `${circuit}${getOrdinalSuffix(parseInt(circuit))} Circuit`;
}

/**
 * Get ordinal suffix for numbers
 */
function getOrdinalSuffix(n: number): string {
  const s = ['th', 'st', 'nd', 'rd'];
  const v = n % 100;
  return s[(v - 20) % 10] || s[v] || s[0];
}

/**
 * Get status badge color
 */
export function getStatusColor(status?: string | null): string {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-800';
    case 'SENIOR':
      return 'bg-blue-100 text-blue-800';
    case 'DECEASED':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

/**
 * Get party color for appointing president's party
 */
export function getPartyColor(party?: string | null): string {
  switch (party?.toLowerCase()) {
    case 'democratic':
    case 'democrat':
      return 'bg-blue-100 text-blue-800';
    case 'republican':
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}
