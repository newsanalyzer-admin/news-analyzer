'use client';

/**
 * AppointeeDetailPanel Component
 *
 * Slide-out panel showing detailed appointee information.
 */

import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import type { Appointee } from '@/types/appointee';
import { APPOINTMENT_TYPE_LABELS } from '@/types/appointee';

interface AppointeeDetailPanelProps {
  appointee: Appointee;
  onClose: () => void;
}

const appointmentTypeColors: Record<string, string> = {
  PAS: 'bg-purple-100 text-purple-800 border-purple-200',
  PA: 'bg-blue-100 text-blue-800 border-blue-200',
  NA: 'bg-amber-100 text-amber-800 border-amber-200',
  CA: 'bg-green-100 text-green-800 border-green-200',
  XS: 'bg-orange-100 text-orange-800 border-orange-200',
};

export function AppointeeDetailPanel({ appointee, onClose }: AppointeeDetailPanelProps) {
  const displayName = appointee.fullName ||
    [appointee.firstName, appointee.lastName].filter(Boolean).join(' ') ||
    'Unknown';

  const typeColor = appointee.appointmentType
    ? appointmentTypeColors[appointee.appointmentType] || 'bg-gray-100 text-gray-800 border-gray-200'
    : 'bg-gray-100 text-gray-800 border-gray-200';

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/50 z-40"
        onClick={onClose}
      />

      {/* Panel */}
      <div className="fixed right-0 top-0 h-full w-full max-w-lg bg-background shadow-xl z-50 overflow-y-auto">
        <div className="sticky top-0 bg-background border-b p-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold">Appointee Details</h2>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        <div className="p-6 space-y-6">
          {/* Profile Header */}
          <div>
            <h3 className="text-2xl font-bold">{displayName}</h3>
            {appointee.positionTitle && (
              <p className="text-lg text-muted-foreground mt-1">
                {appointee.positionTitle}
              </p>
            )}
            {appointee.agencyName && (
              <p className="text-muted-foreground">
                {appointee.agencyName}
              </p>
            )}
          </div>

          {/* Badges */}
          <div className="flex flex-wrap gap-2">
            {appointee.appointmentType && (
              <Badge variant="outline" className={typeColor}>
                {appointee.appointmentType}
              </Badge>
            )}
            {appointee.status === 'Filled' && (
              <Badge variant="outline" className="bg-green-100 text-green-800 border-green-200">
                Currently Serving
              </Badge>
            )}
            {appointee.status === 'Vacant' && (
              <Badge variant="outline" className="bg-red-100 text-red-800 border-red-200">
                Vacant
              </Badge>
            )}
          </div>

          <hr className="border-border" />

          {/* Position Information */}
          <div>
            <h4 className="font-semibold mb-3">Position Information</h4>
            <dl className="space-y-2">
              {appointee.positionTitle && (
                <div>
                  <dt className="text-sm text-muted-foreground">Position Title</dt>
                  <dd className="font-medium">{appointee.positionTitle}</dd>
                </div>
              )}
              {appointee.appointmentType && (
                <div>
                  <dt className="text-sm text-muted-foreground">Appointment Type</dt>
                  <dd className="font-medium">
                    {appointee.appointmentType} - {APPOINTMENT_TYPE_LABELS[appointee.appointmentType]}
                  </dd>
                </div>
              )}
              {appointee.agencyName && (
                <div>
                  <dt className="text-sm text-muted-foreground">Agency</dt>
                  <dd className="font-medium">{appointee.agencyName}</dd>
                </div>
              )}
              {appointee.location && (
                <div>
                  <dt className="text-sm text-muted-foreground">Location</dt>
                  <dd className="font-medium">{appointee.location}</dd>
                </div>
              )}
            </dl>
          </div>

          {/* Pay Information */}
          {(appointee.payPlan || appointee.payGrade) && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">Pay Information</h4>
                <dl className="space-y-2">
                  {appointee.payPlan && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Pay Plan</dt>
                      <dd className="font-medium">{appointee.payPlan}</dd>
                    </div>
                  )}
                  {appointee.payGrade && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Pay Grade</dt>
                      <dd className="font-medium">{appointee.payGrade}</dd>
                    </div>
                  )}
                </dl>
              </div>
            </>
          )}

          {/* Tenure Information */}
          {(appointee.startDate || appointee.tenure !== null) && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">Tenure</h4>
                <dl className="space-y-2">
                  {appointee.startDate && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Start Date</dt>
                      <dd className="font-medium">{formatDate(appointee.startDate)}</dd>
                    </div>
                  )}
                  {appointee.endDate && (
                    <div>
                      <dt className="text-sm text-muted-foreground">End Date</dt>
                      <dd className="font-medium">{formatDate(appointee.endDate)}</dd>
                    </div>
                  )}
                  {appointee.tenure !== null && appointee.tenure > 0 && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Years in Position</dt>
                      <dd className="font-medium">{appointee.tenure} year{appointee.tenure !== 1 ? 's' : ''}</dd>
                    </div>
                  )}
                  {appointee.expirationDate && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Position Expiration</dt>
                      <dd className="font-medium">{formatDate(appointee.expirationDate)}</dd>
                    </div>
                  )}
                </dl>
              </div>
            </>
          )}
        </div>
      </div>
    </>
  );
}

function formatDate(dateStr?: string | null): string {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  } catch {
    return dateStr;
  }
}
