/**
 * Congress.gov Search Types
 *
 * Type definitions for Congress.gov member search and import.
 */

import type { SearchResponse, ImportResult } from './search-import';

/**
 * Congress member search result from backend
 */
export interface CongressMemberSearchResult {
  bioguideId: string;
  name: string;
  firstName: string;
  lastName: string;
  state: string;
  party: string;
  chamber: string;
  district?: string;
  currentMember: boolean;
  imageUrl?: string;
  url?: string;
}

/**
 * Congress member detailed information for preview
 */
export interface CongressMemberDetail extends CongressMemberSearchResult {
  middleName?: string;
  suffix?: string;
  birthDate?: string;
  officialWebsiteUrl?: string;
  addressLine1?: string;
  addressLine2?: string;
  phone?: string;
  terms?: CongressMemberTerm[];
  govtrackId?: number;
  opensecretsId?: string;
  votesmartId?: number;
  icpsrId?: string;
  lisId?: string;
}

/**
 * Congress member term information
 */
export interface CongressMemberTerm {
  chamber: string;
  congress: number;
  startYear: string;
  endYear?: string;
  stateCode: string;
  district?: string;
}

/**
 * Filter values for Congress.gov search
 */
export interface CongressMemberFilters {
  name?: string;
  state?: string;
  party?: string;
  chamber?: string;
  congress?: number;
}

/**
 * Search parameters for Congress.gov API
 */
export interface CongressSearchParams {
  name?: string;
  state?: string;
  party?: string;
  chamber?: string;
  congress?: number;
  page?: number;
  pageSize?: number;
}

/**
 * Response from Congress search endpoint with rate limit info
 */
export interface CongressSearchResponse extends SearchResponse<CongressMemberSearchResult> {
  rateLimitRemaining?: number;
  rateLimitResetSeconds?: number;
}

/**
 * Import request for Congress member
 */
export interface CongressMemberImportRequest {
  bioguideId: string;
  forceOverwrite?: boolean;
  fieldsToUpdate?: string[];
}

/**
 * Import result specific to Congress members
 */
export interface CongressImportResult extends ImportResult {
  bioguideId?: string;
  name?: string;
}

/**
 * Party abbreviation options
 */
export const PARTY_OPTIONS = [
  { value: 'D', label: 'Democrat' },
  { value: 'R', label: 'Republican' },
  { value: 'I', label: 'Independent' },
  { value: 'L', label: 'Libertarian' },
] as const;

/**
 * Chamber options
 */
export const CHAMBER_OPTIONS = [
  { value: 'house', label: 'House' },
  { value: 'senate', label: 'Senate' },
] as const;

/**
 * Recent congress numbers (last 5)
 */
export const CONGRESS_OPTIONS = [
  { value: '118', label: '118th (2023-2025)' },
  { value: '117', label: '117th (2021-2023)' },
  { value: '116', label: '116th (2019-2021)' },
  { value: '115', label: '115th (2017-2019)' },
  { value: '114', label: '114th (2015-2017)' },
] as const;
