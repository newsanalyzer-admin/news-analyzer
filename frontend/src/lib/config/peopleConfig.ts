/**
 * People Entity Subtype Configurations
 *
 * Configuration for judges, members, and appointees subtypes
 * within the People entity type.
 */

import { Badge } from '@/components/ui/badge';
import { createElement, type ReactNode } from 'react';
import type {
  ColumnConfig,
  FilterConfig,
  DefaultSort,
  EntityDetailConfig,
} from './entityTypes';
import type { Judge } from '@/types/judge';
import type { Person } from '@/types/member';
import type { Appointee, AppointmentType } from '@/types/appointee';
import { APPOINTMENT_TYPE_LABELS } from '@/types/appointee';
import {
  getStatusColor,
  getPartyColor,
  getCircuitLabel,
  getCourtLevelLabel,
} from '@/types/judge';

// =====================================================================
// Subtype Configuration Interface
// =====================================================================

/**
 * Subtype configuration for people entity types
 */
export interface SubtypeConfig<T = unknown> {
  /** Unique identifier for the subtype */
  id: string;
  /** Display label */
  label: string;
  /** API endpoint for this subtype */
  apiEndpoint: string;
  /** Column configuration */
  columns: ColumnConfig<T>[];
  /** Filter configuration */
  filters: FilterConfig[];
  /** Default sort */
  defaultSort: DefaultSort;
  /** Detail configuration */
  detailConfig?: EntityDetailConfig<T>;
  /** Optional stats component to display above list */
  hasStats?: boolean;
  /** ID field name (for detail navigation) */
  idField?: string;
}

// =====================================================================
// Shared Renderers
// =====================================================================

/**
 * Render party badge (shared between judges and members)
 */
function renderPartyBadge(party: unknown): ReactNode {
  const partyStr = party as string | null;
  if (!partyStr) return '-';
  const colorClass = getPartyColor(partyStr);
  return createElement(
    'span',
    { className: `inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${colorClass}` },
    partyStr
  );
}

// =====================================================================
// Judges Configuration
// =====================================================================

/**
 * Render judge status badge
 */
function renderJudgeStatusBadge(status: unknown): ReactNode {
  const statusStr = status as string | null;
  if (!statusStr) return '-';
  const colorClass = getStatusColor(statusStr);
  return createElement(
    'span',
    { className: `inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${colorClass}` },
    statusStr
  );
}

/**
 * Render circuit with label
 */
function renderCircuit(circuit: unknown): ReactNode {
  const circuitStr = circuit as string | null;
  if (!circuitStr) return '-';
  return getCircuitLabel(circuitStr);
}

/**
 * Judges column configuration
 */
const judgeColumns: ColumnConfig<Judge>[] = [
  {
    id: 'fullName',
    label: 'Name',
    sortable: true,
    width: '25%',
  },
  {
    id: 'courtName',
    label: 'Court',
    sortable: true,
    width: '25%',
    render: (value) => (value as string) || '-',
  },
  {
    id: 'circuit',
    label: 'Circuit',
    sortable: true,
    width: '15%',
    render: renderCircuit,
    hideOnMobile: true,
  },
  {
    id: 'judicialStatus',
    label: 'Status',
    sortable: true,
    width: '10%',
    render: renderJudgeStatusBadge,
  },
  {
    id: 'appointingPresident',
    label: 'Appointed By',
    sortable: true,
    width: '15%',
    hideOnMobile: true,
    render: (value) => (value as string) || '-',
  },
  {
    id: 'partyOfAppointingPresident',
    label: 'Party',
    sortable: false,
    width: '10%',
    render: renderPartyBadge,
    hideOnMobile: true,
  },
];

/**
 * Judges filter configuration
 */
const judgeFilters: FilterConfig[] = [
  {
    id: 'courtLevel',
    label: 'Court Level',
    type: 'select',
    apiParam: 'courtLevel',
    options: [
      { value: 'SUPREME', label: 'Supreme Court' },
      { value: 'APPEALS', label: 'Court of Appeals' },
      { value: 'DISTRICT', label: 'District Court' },
    ],
  },
  {
    id: 'circuit',
    label: 'Circuit',
    type: 'select',
    apiParam: 'circuit',
    options: [
      { value: '1', label: '1st Circuit' },
      { value: '2', label: '2nd Circuit' },
      { value: '3', label: '3rd Circuit' },
      { value: '4', label: '4th Circuit' },
      { value: '5', label: '5th Circuit' },
      { value: '6', label: '6th Circuit' },
      { value: '7', label: '7th Circuit' },
      { value: '8', label: '8th Circuit' },
      { value: '9', label: '9th Circuit' },
      { value: '10', label: '10th Circuit' },
      { value: '11', label: '11th Circuit' },
      { value: 'DC', label: 'D.C. Circuit' },
      { value: 'FEDERAL', label: 'Federal Circuit' },
    ],
  },
  {
    id: 'status',
    label: 'Status',
    type: 'select',
    apiParam: 'status',
    options: [
      { value: 'ACTIVE', label: 'Active' },
      { value: 'SENIOR', label: 'Senior Status' },
    ],
  },
];

/**
 * Judges detail configuration
 */
const judgeDetailConfig: EntityDetailConfig<Judge> = {
  header: {
    titleField: 'fullName',
    badgeField: 'judicialStatus',
    renderBadge: (value) => {
      const status = value as string;
      const colorClass = getStatusColor(status);
      return createElement(
        'span',
        { className: `inline-flex items-center px-2.5 py-0.5 rounded text-sm font-medium ${colorClass}` },
        status || 'Unknown'
      );
    },
    metaFields: ['courtName', 'circuit'],
  },
  sections: [
    {
      id: 'court',
      label: 'Court Information',
      layout: 'grid',
      fields: [
        { id: 'courtName', label: 'Court' },
        {
          id: 'courtType',
          label: 'Court Type',
          render: (value) => getCourtLevelLabel(value as string),
        },
        {
          id: 'circuit',
          label: 'Circuit',
          render: (value) => getCircuitLabel(value as string),
        },
      ],
    },
    {
      id: 'appointment',
      label: 'Appointment',
      layout: 'grid',
      fields: [
        { id: 'appointingPresident', label: 'Appointing President' },
        {
          id: 'partyOfAppointingPresident',
          label: 'Party',
          render: renderPartyBadge,
        },
        { id: 'nominationDate', label: 'Nomination Date' },
        { id: 'confirmationDate', label: 'Confirmation Date' },
        { id: 'commissionDate', label: 'Commission Date' },
        {
          id: 'senateVote',
          label: 'Senate Vote',
          render: (_, entity) => {
            const judge = entity as Judge;
            if (judge.ayesCount === null || judge.ayesCount === undefined) return '-';
            const nays = judge.naysCount || 0;
            const total = judge.ayesCount + nays;
            if (total === 0) return '-';
            const approval = Math.round((judge.ayesCount / total) * 100);
            return `${judge.ayesCount} - ${nays} (${approval}% approval)`;
          },
        },
        { id: 'abaRating', label: 'ABA Rating', hideIfEmpty: true },
      ],
    },
    {
      id: 'service',
      label: 'Service',
      layout: 'grid',
      collapsible: true,
      fields: [
        { id: 'seniorStatusDate', label: 'Senior Status Date', hideIfEmpty: true },
        { id: 'terminationDate', label: 'Termination Date', hideIfEmpty: true },
        { id: 'terminationReason', label: 'Termination Reason', hideIfEmpty: true },
      ],
    },
    {
      id: 'personal',
      label: 'Personal Information',
      layout: 'grid',
      collapsible: true,
      defaultCollapsed: true,
      fields: [
        { id: 'gender', label: 'Gender', hideIfEmpty: true },
        { id: 'birthDate', label: 'Birth Date', hideIfEmpty: true },
        { id: 'deathDate', label: 'Death Date', hideIfEmpty: true },
      ],
    },
    {
      id: 'career',
      label: 'Professional Career',
      layout: 'list',
      collapsible: true,
      defaultCollapsed: true,
      fields: [
        {
          id: 'professionalCareer',
          label: 'Career History',
          hideIfEmpty: true,
          render: (value) => {
            if (!value) return null;
            return createElement(
              'pre',
              { className: 'whitespace-pre-wrap text-sm text-muted-foreground' },
              value as string
            );
          },
        },
      ],
    },
  ],
};

/**
 * Complete judges subtype configuration
 */
export const judgesSubtypeConfig: SubtypeConfig<Judge> = {
  id: 'judges',
  label: 'Federal Judges',
  apiEndpoint: '/api/judges',
  columns: judgeColumns,
  filters: judgeFilters,
  defaultSort: { column: 'lastName', direction: 'asc' },
  detailConfig: judgeDetailConfig,
  hasStats: true,
  idField: 'id',
};

// =====================================================================
// Members Configuration
// =====================================================================

/**
 * Render chamber badge
 */
function renderChamberBadge(chamber: unknown): ReactNode {
  const chamberStr = chamber as string | null;
  if (!chamberStr) return '-';
  const isHouse = chamberStr === 'HOUSE';
  return createElement(
    Badge,
    { variant: isHouse ? 'secondary' : 'default' },
    isHouse ? 'House' : 'Senate'
  );
}

/**
 * Members column configuration
 */
const memberColumns: ColumnConfig<Person>[] = [
  {
    id: 'fullName',
    label: 'Name',
    sortable: true,
    width: '25%',
    accessor: (row) => `${row.firstName} ${row.lastName}`,
  },
  {
    id: 'chamber',
    label: 'Chamber',
    sortable: true,
    width: '15%',
    render: renderChamberBadge,
  },
  {
    id: 'state',
    label: 'State',
    sortable: true,
    width: '15%',
    render: (value) => (value as string) || '-',
  },
  {
    id: 'party',
    label: 'Party',
    sortable: true,
    width: '15%',
    render: renderPartyBadge,
  },
];

/**
 * US States for filter options
 */
const US_STATES_OPTIONS = [
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
  { value: 'AS', label: 'American Samoa' },
  { value: 'GU', label: 'Guam' },
  { value: 'MP', label: 'Northern Mariana Islands' },
  { value: 'PR', label: 'Puerto Rico' },
  { value: 'VI', label: 'U.S. Virgin Islands' },
];

/**
 * Members filter configuration
 */
const memberFilters: FilterConfig[] = [
  {
    id: 'chamber',
    label: 'Chamber',
    type: 'select',
    apiParam: 'chamber',
    options: [
      { value: 'SENATE', label: 'Senate' },
      { value: 'HOUSE', label: 'House of Representatives' },
    ],
  },
  {
    id: 'party',
    label: 'Party',
    type: 'select',
    apiParam: 'party',
    options: [
      { value: 'Democratic', label: 'Democratic' },
      { value: 'Republican', label: 'Republican' },
      { value: 'Independent', label: 'Independent' },
    ],
  },
  {
    id: 'state',
    label: 'State',
    type: 'select',
    apiParam: 'state',
    options: US_STATES_OPTIONS,
  },
];

/**
 * Members detail configuration
 */
const memberDetailConfig: EntityDetailConfig<Person> = {
  header: {
    titleField: 'fullName',
    subtitleField: 'bioguideId',
    badgeField: 'party',
    renderBadge: (value) => {
      const party = value as string;
      const colorClass = getPartyColor(party);
      return createElement(
        'span',
        { className: `inline-flex items-center px-2.5 py-0.5 rounded text-sm font-medium ${colorClass}` },
        party || 'Unknown'
      );
    },
    metaFields: ['chamber', 'state'],
  },
  sections: [
    {
      id: 'position',
      label: 'Position',
      layout: 'grid',
      fields: [
        {
          id: 'chamber',
          label: 'Chamber',
          render: renderChamberBadge,
        },
        { id: 'state', label: 'State' },
        { id: 'party', label: 'Party' },
      ],
    },
    {
      id: 'personal',
      label: 'Personal Information',
      layout: 'grid',
      collapsible: true,
      fields: [
        { id: 'gender', label: 'Gender', hideIfEmpty: true },
        { id: 'birthDate', label: 'Birth Date', hideIfEmpty: true },
      ],
    },
    {
      id: 'contact',
      label: 'Social Media',
      layout: 'grid',
      collapsible: true,
      defaultCollapsed: true,
      fields: [
        {
          id: 'socialMedia.twitter',
          label: 'Twitter',
          hideIfEmpty: true,
          render: (value) => {
            if (!value) return null;
            const handle = value as string;
            return createElement(
              'a',
              {
                href: `https://twitter.com/${handle}`,
                target: '_blank',
                rel: 'noopener noreferrer',
                className: 'text-primary hover:underline',
              },
              `@${handle}`
            );
          },
        },
        {
          id: 'socialMedia.facebook',
          label: 'Facebook',
          hideIfEmpty: true,
          render: (value) => {
            if (!value) return null;
            return createElement(
              'a',
              {
                href: `https://facebook.com/${value}`,
                target: '_blank',
                rel: 'noopener noreferrer',
                className: 'text-primary hover:underline',
              },
              value as string
            );
          },
        },
        {
          id: 'socialMedia.youtube',
          label: 'YouTube',
          hideIfEmpty: true,
          render: (value) => {
            if (!value) return null;
            return createElement(
              'a',
              {
                href: `https://youtube.com/${value}`,
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
  ],
};

/**
 * Complete members subtype configuration
 */
export const membersSubtypeConfig: SubtypeConfig<Person> = {
  id: 'members',
  label: 'Congressional Members',
  apiEndpoint: '/api/members',
  columns: memberColumns,
  filters: memberFilters,
  defaultSort: { column: 'lastName', direction: 'asc' },
  detailConfig: memberDetailConfig,
  hasStats: false,
  idField: 'bioguideId',
};

// =====================================================================
// Appointees Configuration
// =====================================================================

/**
 * Render appointment type badge
 */
function renderAppointmentTypeBadge(type: unknown): ReactNode {
  const typeStr = type as AppointmentType | null;
  if (!typeStr) return '-';

  const colorMap: Record<AppointmentType, string> = {
    PAS: 'bg-purple-100 text-purple-800',
    PA: 'bg-blue-100 text-blue-800',
    NA: 'bg-orange-100 text-orange-800',
    CA: 'bg-green-100 text-green-800',
    XS: 'bg-gray-100 text-gray-800',
  };

  return createElement(
    'span',
    {
      className: `inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${colorMap[typeStr] || 'bg-gray-100 text-gray-800'}`,
      title: (APPOINTMENT_TYPE_LABELS as Record<string, string>)[typeStr] || typeStr,
    },
    typeStr
  );
}

/**
 * Appointees column configuration
 */
const appointeeColumns: ColumnConfig<Appointee>[] = [
  {
    id: 'fullName',
    label: 'Name',
    sortable: true,
    width: '25%',
    render: (value) => (value as string) || '-',
  },
  {
    id: 'positionTitle',
    label: 'Position',
    sortable: true,
    width: '30%',
    render: (value) => (value as string) || '-',
  },
  {
    id: 'agencyName',
    label: 'Agency',
    sortable: true,
    width: '25%',
    hideOnMobile: true,
    render: (value) => (value as string) || '-',
  },
  {
    id: 'appointmentType',
    label: 'Type',
    sortable: true,
    width: '10%',
    render: renderAppointmentTypeBadge,
  },
  {
    id: 'status',
    label: 'Status',
    sortable: false,
    width: '10%',
    hideOnMobile: true,
    render: (value) => {
      const status = value as string;
      const isFilled = status === 'Filled';
      return createElement(
        Badge,
        { variant: isFilled ? 'default' : 'secondary' },
        status || '-'
      );
    },
  },
];

/**
 * Appointees filter configuration
 */
const appointeeFilters: FilterConfig[] = [
  {
    id: 'type',
    label: 'Appointment Type',
    type: 'select',
    apiParam: 'type',
    options: [
      { value: 'PAS', label: 'PAS - Presidential with Senate' },
      { value: 'PA', label: 'PA - Presidential' },
      { value: 'NA', label: 'NA - Non-Career SES' },
      { value: 'CA', label: 'CA - Career SES' },
      { value: 'XS', label: 'XS - Schedule C' },
    ],
  },
];

/**
 * Appointees detail configuration
 */
const appointeeDetailConfig: EntityDetailConfig<Appointee> = {
  header: {
    titleField: 'fullName',
    subtitleField: 'positionTitle',
    badgeField: 'appointmentType',
    renderBadge: renderAppointmentTypeBadge,
    metaFields: ['agencyName', 'status'],
  },
  sections: [
    {
      id: 'position',
      label: 'Position Information',
      layout: 'grid',
      fields: [
        { id: 'positionTitle', label: 'Position' },
        { id: 'agencyName', label: 'Agency' },
        { id: 'organizationName', label: 'Organization', hideIfEmpty: true },
        {
          id: 'appointmentType',
          label: 'Appointment Type',
          render: (value) => {
            const type = value as AppointmentType;
            if (!type) return '-';
            const labels = APPOINTMENT_TYPE_LABELS as Record<string, string>;
            return labels[type] || type;
          },
        },
      ],
    },
    {
      id: 'employment',
      label: 'Employment Details',
      layout: 'grid',
      collapsible: true,
      fields: [
        { id: 'payPlan', label: 'Pay Plan', hideIfEmpty: true },
        { id: 'payGrade', label: 'Pay Grade', hideIfEmpty: true },
        { id: 'location', label: 'Location', hideIfEmpty: true },
        { id: 'tenure', label: 'Tenure (years)', hideIfEmpty: true },
      ],
    },
    {
      id: 'dates',
      label: 'Timeline',
      layout: 'grid',
      collapsible: true,
      fields: [
        { id: 'startDate', label: 'Start Date', hideIfEmpty: true },
        { id: 'endDate', label: 'End Date', hideIfEmpty: true },
        { id: 'expirationDate', label: 'Expiration Date', hideIfEmpty: true },
      ],
    },
    {
      id: 'status',
      label: 'Status',
      layout: 'grid',
      fields: [
        {
          id: 'status',
          label: 'Position Status',
          render: (value) => {
            const status = value as string;
            const isFilled = status === 'Filled';
            return createElement(
              Badge,
              { variant: isFilled ? 'default' : 'secondary' },
              status || '-'
            );
          },
        },
        {
          id: 'current',
          label: 'Current',
          render: (value) => {
            const current = value as boolean;
            return createElement(
              Badge,
              { variant: current ? 'default' : 'secondary' },
              current ? 'Yes' : 'No'
            );
          },
        },
      ],
    },
  ],
};

/**
 * Complete appointees subtype configuration
 */
export const appointeesSubtypeConfig: SubtypeConfig<Appointee> = {
  id: 'appointees',
  label: 'Executive Appointees',
  apiEndpoint: '/api/appointees',
  columns: appointeeColumns,
  filters: appointeeFilters,
  defaultSort: { column: 'fullName', direction: 'asc' },
  detailConfig: appointeeDetailConfig,
  hasStats: false,
  idField: 'id',
};

// =====================================================================
// People Subtypes Registry
// =====================================================================

/**
 * All available people subtypes
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const peopleSubtypes: SubtypeConfig<any>[] = [
  judgesSubtypeConfig,
  membersSubtypeConfig,
  appointeesSubtypeConfig,
];

/**
 * Get subtype configuration by ID
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getPeopleSubtypeConfig(subtypeId: string): SubtypeConfig<any> | undefined {
  return peopleSubtypes.find((st) => st.id === subtypeId);
}

/**
 * Get the default people subtype (first in list)
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getDefaultPeopleSubtype(): SubtypeConfig<any> {
  return peopleSubtypes[0];
}
