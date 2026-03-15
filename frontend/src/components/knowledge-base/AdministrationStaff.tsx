'use client';

/**
 * AdministrationStaff Component (KB-2.2)
 *
 * Displays White House Chief(s) of Staff and Cabinet Secretaries
 * for the current administration.
 */

import { Briefcase, Users } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import type { OfficeholderDTO, CabinetMemberDTO } from '@/hooks/usePresidencySync';

export interface AdministrationStaffProps {
  chiefsOfStaff: OfficeholderDTO[];
  cabinetMembers: CabinetMemberDTO[];
  isLoading?: boolean;
}

export function AdministrationStaff({
  chiefsOfStaff,
  cabinetMembers,
  isLoading,
}: AdministrationStaffProps) {
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
        </CardHeader>
        <CardContent className="space-y-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="flex items-center gap-3">
              <Skeleton className="h-10 w-10 rounded-full" />
              <div className="space-y-1">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-3 w-24" />
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    );
  }

  const hasChiefs = chiefsOfStaff.length > 0;
  const hasCabinet = cabinetMembers.length > 0;

  if (!hasChiefs && !hasCabinet) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="h-5 w-5" />
            President&apos;s Staff
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">No staff data available yet.</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Users className="h-5 w-5" />
          President&apos;s Staff
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Chiefs of Staff */}
        {hasChiefs && (
          <div>
            <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-3">
              White House Chief of Staff
            </h3>
            <div className="space-y-3">
              {chiefsOfStaff.map((cos) => (
                <StaffMemberRow
                  key={cos.holdingId}
                  name={cos.fullName}
                  title={cos.positionTitle}
                  termLabel={cos.termLabel}
                  imageUrl={cos.imageUrl}
                />
              ))}
            </div>
          </div>
        )}

        {/* Cabinet Secretaries */}
        {hasCabinet && (
          <div>
            <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-3">
              Cabinet Secretaries
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {cabinetMembers.map((member) => (
                <CabinetMemberRow
                  key={member.holdingId}
                  name={member.fullName}
                  title={member.positionTitle}
                  department={member.departmentName}
                />
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function StaffMemberRow({
  name,
  title,
  termLabel,
  imageUrl,
}: {
  name: string;
  title: string;
  termLabel: string;
  imageUrl: string | null;
}) {
  return (
    <div className="flex items-center gap-3">
      <div className="h-10 w-10 rounded-full bg-muted flex items-center justify-center flex-shrink-0 overflow-hidden">
        {imageUrl ? (
          <img src={imageUrl} alt={name} className="h-full w-full object-cover" />
        ) : (
          <Users className="h-5 w-5 text-muted-foreground" />
        )}
      </div>
      <div>
        <p className="font-medium text-sm">{name}</p>
        <p className="text-xs text-muted-foreground">{title} &middot; {termLabel}</p>
      </div>
    </div>
  );
}

function CabinetMemberRow({
  name,
  title,
  department,
}: {
  name: string;
  title: string;
  department: string;
}) {
  return (
    <div className="flex items-center gap-3 p-2 rounded-md hover:bg-muted/50 transition-colors">
      <div className="h-8 w-8 rounded-full bg-muted flex items-center justify-center flex-shrink-0">
        <Briefcase className="h-4 w-4 text-muted-foreground" />
      </div>
      <div className="min-w-0">
        <p className="font-medium text-sm truncate">{name}</p>
        <p className="text-xs text-muted-foreground truncate">{department}</p>
      </div>
    </div>
  );
}
