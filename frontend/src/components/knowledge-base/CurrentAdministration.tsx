'use client';

/**
 * CurrentAdministration Component (KB-2.2)
 *
 * Container component that orchestrates data fetching and renders
 * the current president, vice president, staff, and executive orders.
 */

import { AlertCircle } from 'lucide-react';
import { useCurrentPresidency, usePresidencyAdministration } from '@/hooks/usePresidencySync';
import { PresidentCard } from './PresidentCard';
import { VicePresidentCard } from './VicePresidentCard';
import { AdministrationStaff } from './AdministrationStaff';
import { AdministrationExecutiveOrders } from './AdministrationExecutiveOrders';

export function CurrentAdministration() {
  const {
    data: presidency,
    isLoading: presidencyLoading,
    error: presidencyError,
  } = useCurrentPresidency();

  const {
    data: administration,
    isLoading: adminLoading,
  } = usePresidencyAdministration(presidency?.id ?? null);

  // Error state
  if (presidencyError) {
    return (
      <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-6 text-center">
        <AlertCircle className="h-8 w-8 mx-auto text-destructive mb-2" />
        <p className="text-destructive font-medium">Failed to load current administration</p>
        <p className="text-sm text-muted-foreground mt-1">
          {presidencyError instanceof Error ? presidencyError.message : 'An unexpected error occurred'}
        </p>
      </div>
    );
  }

  // Get the current VP from the administration data (first active VP)
  const currentVP = administration?.vicePresidents?.[0] ?? null;

  return (
    <div className="space-y-6">
      {/* President + VP side-by-side */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <PresidentCard
          presidency={presidency}
          isLoading={presidencyLoading}
        />
        <VicePresidentCard
          vicePresident={currentVP}
          isLoading={presidencyLoading || adminLoading}
        />
      </div>

      {/* Staff section */}
      <AdministrationStaff
        chiefsOfStaff={administration?.chiefsOfStaff ?? []}
        cabinetMembers={administration?.cabinetMembers ?? []}
        isLoading={adminLoading}
      />

      {/* Executive Orders section */}
      <AdministrationExecutiveOrders
        presidencyId={presidency?.id ?? null}
      />
    </div>
  );
}
