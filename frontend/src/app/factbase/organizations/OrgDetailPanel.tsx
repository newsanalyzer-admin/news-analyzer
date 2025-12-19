'use client';

/**
 * OrgDetailPanel Component
 *
 * Slide-out panel showing detailed organization information.
 */

import { X, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import type { GovernmentOrganization } from '@/types/government-org';

interface OrgDetailPanelProps {
  org: GovernmentOrganization;
  onClose: () => void;
}

const orgTypeColors: Record<string, string> = {
  DEPARTMENT: 'bg-blue-100 text-blue-800 border-blue-200',
  AGENCY: 'bg-green-100 text-green-800 border-green-200',
  BUREAU: 'bg-amber-100 text-amber-800 border-amber-200',
  OFFICE: 'bg-purple-100 text-purple-800 border-purple-200',
  COMMISSION: 'bg-red-100 text-red-800 border-red-200',
  COURT: 'bg-indigo-100 text-indigo-800 border-indigo-200',
  COMMITTEE: 'bg-orange-100 text-orange-800 border-orange-200',
};

export function OrgDetailPanel({ org, onClose }: OrgDetailPanelProps) {
  const typeColor = orgTypeColors[org.orgType] || 'bg-gray-100 text-gray-800 border-gray-200';
  const formattedType = org.orgType.replace(/_/g, ' ');

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
          <h2 className="text-xl font-semibold">Organization Details</h2>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        <div className="p-6 space-y-6">
          {/* Header */}
          <div>
            <h3 className="text-2xl font-bold">{org.officialName}</h3>
            {org.acronym && (
              <p className="text-lg text-muted-foreground mt-1">
                ({org.acronym})
              </p>
            )}
          </div>

          {/* Badges */}
          <div className="flex flex-wrap gap-2">
            <Badge variant="outline" className={typeColor}>
              {formattedType}
            </Badge>
            <Badge variant="outline" className="bg-gray-100 text-gray-800 border-gray-200">
              Level {org.orgLevel}
            </Badge>
            {org.active ? (
              <Badge variant="outline" className="bg-green-100 text-green-800 border-green-200">
                Active
              </Badge>
            ) : (
              <Badge variant="outline" className="bg-red-100 text-red-800 border-red-200">
                Dissolved
              </Badge>
            )}
          </div>

          <hr className="border-border" />

          {/* Basic Information */}
          <div>
            <h4 className="font-semibold mb-3">Basic Information</h4>
            <dl className="space-y-2">
              <div>
                <dt className="text-sm text-muted-foreground">Branch</dt>
                <dd className="font-medium capitalize">{org.branch}</dd>
              </div>
              <div>
                <dt className="text-sm text-muted-foreground">Organization Type</dt>
                <dd className="font-medium">{formattedType}</dd>
              </div>
              <div>
                <dt className="text-sm text-muted-foreground">Hierarchy Level</dt>
                <dd className="font-medium">Level {org.orgLevel}</dd>
              </div>
            </dl>
          </div>

          {/* Dates */}
          {(org.establishedDate || org.dissolvedDate) && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">History</h4>
                <dl className="space-y-2">
                  {org.establishedDate && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Established</dt>
                      <dd className="font-medium">{formatDate(org.establishedDate)}</dd>
                    </div>
                  )}
                  {org.dissolvedDate && (
                    <div>
                      <dt className="text-sm text-muted-foreground">Dissolved</dt>
                      <dd className="font-medium">{formatDate(org.dissolvedDate)}</dd>
                    </div>
                  )}
                </dl>
              </div>
            </>
          )}

          {/* Jurisdiction Areas */}
          {org.jurisdictionAreas && org.jurisdictionAreas.length > 0 && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">Jurisdiction Areas</h4>
                <div className="flex flex-wrap gap-2">
                  {org.jurisdictionAreas.map((area, index) => (
                    <Badge key={index} variant="secondary">
                      {area}
                    </Badge>
                  ))}
                </div>
              </div>
            </>
          )}

          {/* Description/Mission */}
          {(org.description || org.mission) && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">About</h4>
                {org.mission && (
                  <div className="mb-3">
                    <dt className="text-sm text-muted-foreground mb-1">Mission</dt>
                    <dd className="text-sm">{org.mission}</dd>
                  </div>
                )}
                {org.description && (
                  <div>
                    <dt className="text-sm text-muted-foreground mb-1">Description</dt>
                    <dd className="text-sm">{org.description}</dd>
                  </div>
                )}
              </div>
            </>
          )}

          {/* Website */}
          {org.websiteUrl && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">Links</h4>
                <a
                  href={org.websiteUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 text-primary hover:underline"
                >
                  <ExternalLink className="h-4 w-4" />
                  Official Website
                </a>
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
