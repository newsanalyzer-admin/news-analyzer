/**
 * Appointee Types
 *
 * TypeScript interfaces for Executive Branch appointee data.
 */

/**
 * Appointment type codes
 */
export type AppointmentType = 'PAS' | 'PA' | 'NA' | 'CA' | 'XS';

/**
 * Appointment type descriptions
 */
export const APPOINTMENT_TYPE_LABELS: Record<AppointmentType, string> = {
  PAS: 'Presidential Appointment with Senate Confirmation',
  PA: 'Presidential Appointment',
  NA: 'Noncareer Appointment',
  CA: 'Career Appointment',
  XS: 'Schedule C',
};

/**
 * Executive Branch Appointee
 *
 * Represents a person holding an executive branch position.
 */
export interface Appointee {
  id: string;
  firstName: string | null;
  lastName: string | null;
  fullName: string | null;

  positionTitle: string | null;
  positionId: string | null;
  appointmentType: AppointmentType | null;
  appointmentTypeDescription: string | null;
  payPlan: string | null;
  payGrade: string | null;
  location: string | null;

  agencyName: string | null;
  organizationName: string | null;
  organizationId: string | null;

  startDate: string | null;
  endDate: string | null;
  expirationDate: string | null;

  tenure: number | null;
  current: boolean;
  status: 'Filled' | 'Vacant';
}
