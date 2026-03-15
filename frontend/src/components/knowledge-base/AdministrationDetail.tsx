'use client';

/**
 * AdministrationDetail Component (KB-2.3)
 *
 * Shared detail view for any administration — displays President card,
 * Vice President, Staff, and Executive Orders. Used by both the
 * CurrentAdministration section and HistoricalAdministrations selection.
 */

import { AlertCircle } from 'lucide-react';
import { usePresidencyAdministration } from '@/hooks/usePresidencySync';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';
import { PresidentCard } from './PresidentCard';
import { VicePresidentCard } from './VicePresidentCard';
import { AdministrationStaff } from './AdministrationStaff';
import { AdministrationExecutiveOrders } from './AdministrationExecutiveOrders';

export interface AdministrationDetailProps {
  /** The presidency to display details for */
  presidency: PresidencyDTO | null | undefined;
  /** Whether the presidency data is still loading */
  isLoading?: boolean;
  /** Error from fetching presidency data */
  error?: Error | null;
}

export function AdministrationDetail({
  presidency,
  isLoading = false,
  error = null,
}: AdministrationDetailProps) {
  const {
    data: administration,
    isLoading: adminLoading,
  } = usePresidencyAdministration(presidency?.id ?? null);

  if (error) {
    return (
      <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-6 text-center">
        <AlertCircle className="h-8 w-8 mx-auto text-destructive mb-2" />
        <p className="text-destructive font-medium">Failed to load administration</p>
        <p className="text-sm text-muted-foreground mt-1">
          {error.message || 'An unexpected error occurred'}
        </p>
      </div>
    );
  }

  const currentVP = administration?.vicePresidents?.[0] ?? null;

  return (
    <div className="space-y-6">
      {/* President + VP side-by-side */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <PresidentCard
          presidency={presidency}
          isLoading={isLoading}
        />
        <VicePresidentCard
          vicePresident={currentVP}
          isLoading={isLoading || adminLoading}
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
