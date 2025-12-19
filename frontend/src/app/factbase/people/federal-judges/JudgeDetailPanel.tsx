'use client';

/**
 * JudgeDetailPanel Component
 *
 * Slide-out panel showing detailed judge information.
 */

import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import type { Judge } from '@/types/judge';
import { getStatusColor, getPartyColor } from '@/types/judge';

interface JudgeDetailPanelProps {
  judge: Judge;
  onClose: () => void;
}

export function JudgeDetailPanel({ judge, onClose }: JudgeDetailPanelProps) {
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
          <h2 className="text-xl font-semibold">Judge Details</h2>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        <div className="p-6 space-y-6">
          {/* Name and Status */}
          <div>
            <h3 className="text-2xl font-bold">
              {judge.fullName || `${judge.firstName} ${judge.lastName}`}
              {judge.suffix && ` ${judge.suffix}`}
            </h3>
            <div className="flex items-center gap-2 mt-2">
              <Badge className={getStatusColor(judge.judicialStatus)} variant="outline">
                {judge.judicialStatus || (judge.current ? 'Active' : 'Inactive')}
              </Badge>
              {judge.fjcNid && (
                <span className="text-sm text-muted-foreground">
                  FJC ID: {judge.fjcNid}
                </span>
              )}
            </div>
          </div>

          <hr className="border-border" />

          {/* Court Information */}
          <div>
            <h4 className="font-semibold mb-3">Court Information</h4>
            <dl className="space-y-2">
              {judge.courtName && (
                <div>
                  <dt className="text-sm text-muted-foreground">Court</dt>
                  <dd className="font-medium">{judge.courtName}</dd>
                </div>
              )}
              {judge.courtType && (
                <div>
                  <dt className="text-sm text-muted-foreground">Court Type</dt>
                  <dd>{judge.courtType}</dd>
                </div>
              )}
              {judge.circuit && (
                <div>
                  <dt className="text-sm text-muted-foreground">Circuit</dt>
                  <dd>{formatCircuit(judge.circuit)}</dd>
                </div>
              )}
            </dl>
          </div>

          <hr className="border-border" />

          {/* Appointment Information */}
          <div>
            <h4 className="font-semibold mb-3">Appointment</h4>
            <dl className="space-y-2">
              {judge.appointingPresident && (
                <div>
                  <dt className="text-sm text-muted-foreground">Appointing President</dt>
                  <dd className="flex items-center gap-2">
                    {judge.appointingPresident}
                    {judge.partyOfAppointingPresident && (
                      <Badge
                        className={getPartyColor(judge.partyOfAppointingPresident)}
                        variant="outline"
                      >
                        {judge.partyOfAppointingPresident}
                      </Badge>
                    )}
                  </dd>
                </div>
              )}
              {judge.nominationDate && (
                <div>
                  <dt className="text-sm text-muted-foreground">Nomination Date</dt>
                  <dd>{formatDate(judge.nominationDate)}</dd>
                </div>
              )}
              {judge.confirmationDate && (
                <div>
                  <dt className="text-sm text-muted-foreground">Confirmation Date</dt>
                  <dd>{formatDate(judge.confirmationDate)}</dd>
                </div>
              )}
              {judge.commissionDate && (
                <div>
                  <dt className="text-sm text-muted-foreground">Commission Date</dt>
                  <dd>{formatDate(judge.commissionDate)}</dd>
                </div>
              )}
              {(judge.ayesCount !== undefined && judge.ayesCount !== null) && (
                <div>
                  <dt className="text-sm text-muted-foreground">Senate Vote</dt>
                  <dd>
                    {judge.ayesCount} - {judge.naysCount || 0}
                    {judge.ayesCount && judge.naysCount !== undefined && (
                      <span className="text-muted-foreground ml-2">
                        ({Math.round((judge.ayesCount / (judge.ayesCount + (judge.naysCount || 0))) * 100)}% approval)
                      </span>
                    )}
                  </dd>
                </div>
              )}
              {judge.abaRating && (
                <div>
                  <dt className="text-sm text-muted-foreground">ABA Rating</dt>
                  <dd>{judge.abaRating}</dd>
                </div>
              )}
            </dl>
          </div>

          <hr className="border-border" />

          {/* Service Information */}
          <div>
            <h4 className="font-semibold mb-3">Service</h4>
            <dl className="space-y-2">
              {judge.seniorStatusDate && (
                <div>
                  <dt className="text-sm text-muted-foreground">Senior Status Date</dt>
                  <dd>{formatDate(judge.seniorStatusDate)}</dd>
                </div>
              )}
              {judge.terminationDate && (
                <div>
                  <dt className="text-sm text-muted-foreground">Termination Date</dt>
                  <dd>{formatDate(judge.terminationDate)}</dd>
                </div>
              )}
              {judge.terminationReason && (
                <div>
                  <dt className="text-sm text-muted-foreground">Termination Reason</dt>
                  <dd>{judge.terminationReason}</dd>
                </div>
              )}
            </dl>
          </div>

          {/* Personal Information */}
          {(judge.birthDate || judge.deathDate || judge.gender) && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">Personal Information</h4>
                <dl className="space-y-2">
                  {judge.gender && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Gender</dt>
                      <dd>{judge.gender}</dd>
                    </div>
                  )}
                  {judge.birthDate && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Birth Date</dt>
                      <dd>{formatDate(judge.birthDate)}</dd>
                    </div>
                  )}
                  {judge.deathDate && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Death Date</dt>
                      <dd>{formatDate(judge.deathDate)}</dd>
                    </div>
                  )}
                </dl>
              </div>
            </>
          )}

          {/* Professional Career */}
          {judge.professionalCareer && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">Professional Career</h4>
                <p className="text-sm whitespace-pre-wrap">{judge.professionalCareer}</p>
              </div>
            </>
          )}
        </div>
      </div>
    </>
  );
}

// Helper functions
function formatCircuit(circuit?: string | null): string {
  if (!circuit) return '-';
  if (circuit === 'DC') return 'D.C. Circuit';
  if (circuit === 'FEDERAL') return 'Federal Circuit';
  return `${circuit}${getOrdinalSuffix(parseInt(circuit))} Circuit`;
}

function getOrdinalSuffix(n: number): string {
  const s = ['th', 'st', 'nd', 'rd'];
  const v = n % 100;
  return s[(v - 20) % 10] || s[v] || s[0];
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
