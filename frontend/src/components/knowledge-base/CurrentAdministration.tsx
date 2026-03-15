'use client';

/**
 * CurrentAdministration Component (KB-2.2, refactored in KB-2.3)
 *
 * Thin wrapper that fetches the current presidency and delegates
 * rendering to the shared AdministrationDetail component.
 */

import { useCurrentPresidency } from '@/hooks/usePresidencySync';
import { AdministrationDetail } from './AdministrationDetail';

export function CurrentAdministration() {
  const {
    data: presidency,
    isLoading,
    error,
  } = useCurrentPresidency();

  return (
    <AdministrationDetail
      presidency={presidency}
      isLoading={isLoading}
      error={error instanceof Error ? error : null}
    />
  );
}
