/**
 * Legislators Repo Search Types
 *
 * Type definitions for searching the unitedstates/congress-legislators
 * GitHub repository and enriching Person records.
 */

import type { SearchResponse } from './search-import';

/**
 * Legislator search result from backend
 */
export interface LegislatorSearchResult {
  bioguideId: string;
  name: string;
  state: string;
  party: string;
  chamber: string;
  currentMember: boolean;
  socialMedia: Record<string, string>;
  externalIds: Record<string, unknown>;
  externalIdCount: number;
  socialMediaCount: number;
}

/**
 * Legislator detailed information for preview/enrichment
 */
export interface LegislatorDetail {
  bioguideId: string;
  name: LegislatorNameInfo;
  bio: LegislatorBioInfo;
  terms: LegislatorTermInfo[];
  socialMedia: LegislatorSocialMedia;
  externalIds: LegislatorExternalIds;
  currentMember: boolean;
}

/**
 * Legislator name breakdown
 */
export interface LegislatorNameInfo {
  first: string;
  last: string;
  middle?: string;
  suffix?: string;
  nickname?: string;
  officialFull?: string;
}

/**
 * Legislator biographical info
 */
export interface LegislatorBioInfo {
  birthday?: string;
  gender?: string;
  religion?: string;
}

/**
 * Legislator term info
 */
export interface LegislatorTermInfo {
  type: string;
  start: string;
  end?: string;
  state: string;
  party: string;
  district?: string;
  stateRank?: string;
  url?: string;
  address?: string;
  phone?: string;
  contactForm?: string;
}

/**
 * Legislator social media info
 */
export interface LegislatorSocialMedia {
  twitter?: string;
  facebook?: string;
  youtube?: string;
  instagram?: string;
}

/**
 * Legislator external IDs
 */
export interface LegislatorExternalIds {
  govtrack?: number;
  opensecrets?: string;
  votesmart?: number;
  fec?: string[];
  thomas?: string;
  wikipedia?: string;
  ballotpedia?: string;
  icpsr?: number;
  lis?: string;
  cspan?: number;
  houseHistory?: number;
}

/**
 * Filter values for legislators search
 */
export interface LegislatorFilters {
  name?: string;
  bioguideId?: string;
  state?: string;
}

/**
 * Search parameters for legislators API
 */
export interface LegislatorsSearchParams {
  name?: string;
  bioguideId?: string;
  state?: string;
  page?: number;
  pageSize?: number;
}

/**
 * Response from legislators search endpoint
 */
export interface LegislatorsSearchResponse extends SearchResponse<LegislatorSearchResult> {
  cached: boolean;
}

/**
 * Enrichment request for a Person
 */
export interface LegislatorEnrichmentRequest {
  bioguideId: string;
}

/**
 * Enrichment result
 */
export interface LegislatorEnrichmentResult {
  matched: boolean;
  personId?: string;
  personName?: string;
  bioguideId: string;
  fieldsAdded: string[];
  fieldsUpdated: string[];
  totalChanges: number;
  error?: string;
}

/**
 * Enrichment preview showing current vs new data
 */
export interface EnrichmentPreview {
  bioguideId: string;
  localMatch: boolean;
  currentPerson?: EnrichmentPersonSnapshot;
  newData?: EnrichmentData;
  fieldsToAdd: string[];
  fieldsToUpdate: string[];
  totalChanges: number;
}

/**
 * Snapshot of current Person data
 */
export interface EnrichmentPersonSnapshot {
  id: string;
  name: string;
  externalIds: Record<string, unknown>;
  socialMedia: Record<string, unknown>;
  enrichmentSource?: string;
  enrichmentVersion?: string;
}

/**
 * New data from Legislators Repo
 */
export interface EnrichmentData {
  externalIds: Record<string, unknown>;
  socialMedia: Record<string, string>;
}

/**
 * Legislator exists check response
 */
export interface LegislatorExistsResponse {
  existsInRepo: boolean;
  localMatch: boolean;
  localPersonId?: string;
  localPersonName?: string;
}

/**
 * US State options for filter
 */
export const US_STATE_OPTIONS = [
  { value: 'AL', label: 'Alabama' },
  { value: 'AK', label: 'Alaska' },
  { value: 'AZ', label: 'Arizona' },
  { value: 'AR', label: 'Arkansas' },
  { value: 'CA', label: 'California' },
  { value: 'CO', label: 'Colorado' },
  { value: 'CT', label: 'Connecticut' },
  { value: 'DE', label: 'Delaware' },
  { value: 'FL', label: 'Florida' },
  { value: 'GA', label: 'Georgia' },
  { value: 'HI', label: 'Hawaii' },
  { value: 'ID', label: 'Idaho' },
  { value: 'IL', label: 'Illinois' },
  { value: 'IN', label: 'Indiana' },
  { value: 'IA', label: 'Iowa' },
  { value: 'KS', label: 'Kansas' },
  { value: 'KY', label: 'Kentucky' },
  { value: 'LA', label: 'Louisiana' },
  { value: 'ME', label: 'Maine' },
  { value: 'MD', label: 'Maryland' },
  { value: 'MA', label: 'Massachusetts' },
  { value: 'MI', label: 'Michigan' },
  { value: 'MN', label: 'Minnesota' },
  { value: 'MS', label: 'Mississippi' },
  { value: 'MO', label: 'Missouri' },
  { value: 'MT', label: 'Montana' },
  { value: 'NE', label: 'Nebraska' },
  { value: 'NV', label: 'Nevada' },
  { value: 'NH', label: 'New Hampshire' },
  { value: 'NJ', label: 'New Jersey' },
  { value: 'NM', label: 'New Mexico' },
  { value: 'NY', label: 'New York' },
  { value: 'NC', label: 'North Carolina' },
  { value: 'ND', label: 'North Dakota' },
  { value: 'OH', label: 'Ohio' },
  { value: 'OK', label: 'Oklahoma' },
  { value: 'OR', label: 'Oregon' },
  { value: 'PA', label: 'Pennsylvania' },
  { value: 'RI', label: 'Rhode Island' },
  { value: 'SC', label: 'South Carolina' },
  { value: 'SD', label: 'South Dakota' },
  { value: 'TN', label: 'Tennessee' },
  { value: 'TX', label: 'Texas' },
  { value: 'UT', label: 'Utah' },
  { value: 'VT', label: 'Vermont' },
  { value: 'VA', label: 'Virginia' },
  { value: 'WA', label: 'Washington' },
  { value: 'WV', label: 'West Virginia' },
  { value: 'WI', label: 'Wisconsin' },
  { value: 'WY', label: 'Wyoming' },
  { value: 'DC', label: 'District of Columbia' },
  { value: 'PR', label: 'Puerto Rico' },
  { value: 'GU', label: 'Guam' },
  { value: 'VI', label: 'Virgin Islands' },
  { value: 'AS', label: 'American Samoa' },
  { value: 'MP', label: 'Northern Mariana Islands' },
] as const;

/**
 * Format social media count as badge text
 */
export function formatSocialMediaBadge(count: number): string {
  return count > 0 ? `${count} social` : 'No social';
}

/**
 * Format external IDs count as badge text
 */
export function formatExternalIdsBadge(count: number): string {
  return count > 0 ? `${count} IDs` : 'No IDs';
}
