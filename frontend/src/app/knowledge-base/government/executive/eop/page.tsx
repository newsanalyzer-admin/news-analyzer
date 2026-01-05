'use client';

import Link from 'next/link';
import { Building, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs, HierarchyView } from '@/components/knowledge-base';
import { useGovernmentOrgsHierarchy } from '@/hooks/useGovernmentOrgs';
import type { HierarchyConfig } from '@/lib/config/entityTypes';

/**
 * Hierarchy configuration for EOP organizations
 */
const eopHierarchyConfig: HierarchyConfig = {
  labelField: 'officialName',
  metaFields: ['acronym'],
  childrenField: 'children',
  idField: 'id',
  defaultExpandDepth: 2,
  showChildCount: true,
};

/**
 * Executive Office of the President page (UI-6.3).
 *
 * Shows EOP component agencies and offices that directly
 * support the President in policy development and operations.
 */
export default function EOPPage() {
  // Fetch EOP organizations - filter to EOP parent in the future
  // For now, show all executive branch and let users explore
  const { data, isLoading, error, refetch } = useGovernmentOrgsHierarchy('executive');

  // Filter to show only top-level EOP-related organizations
  // This is a client-side filter until we have proper EOP parent classification
  const eopData = data?.filter((org) => {
    const name = org.officialName?.toLowerCase() || '';
    return (
      name.includes('executive office') ||
      name.includes('office of management and budget') ||
      name.includes('national security council') ||
      name.includes('council of economic') ||
      name.includes('office of the vice president') ||
      name.includes('office of science and technology') ||
      name.includes('office of national drug control') ||
      name.includes('office of the united states trade')
    );
  }) || [];

  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government/executive">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Executive Branch
          </Link>
        </Button>
      </div>

      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-violet-500/10 text-violet-600 dark:text-violet-400">
            <Building className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Executive Office of the President</h1>
        </div>

        <p className="text-lg text-muted-foreground mb-4">
          The Executive Office of the President (EOP) comprises the immediate staff of the
          President and multiple levels of support staff reporting to the President. The EOP
          was created in 1939 by President Franklin D. Roosevelt.
        </p>

        <div className="bg-muted/50 border rounded-lg p-4 mb-6">
          <p className="text-sm text-muted-foreground">
            <strong>Key Components:</strong> The EOP includes the White House Office, Office of
            Management and Budget (OMB), National Security Council (NSC), Council of Economic
            Advisers (CEA), and other specialized offices advising the President.
          </p>
        </div>
      </div>

      {/* Organizations List */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold mb-4">EOP Component Agencies</h2>
        {eopData.length > 0 ? (
          <HierarchyView
            data={eopData}
            config={eopHierarchyConfig}
            entityType="organizations"
            isLoading={isLoading}
            error={error?.message}
            onRetry={() => refetch()}
          />
        ) : isLoading ? (
          <div className="text-muted-foreground">Loading organizations...</div>
        ) : (
          <div className="bg-card border rounded-lg p-6">
            <p className="text-muted-foreground">
              EOP organization data is being compiled. Key offices include:
            </p>
            <ul className="list-disc list-inside mt-4 text-muted-foreground space-y-1">
              <li>White House Office</li>
              <li>Office of Management and Budget (OMB)</li>
              <li>National Security Council (NSC)</li>
              <li>Council of Economic Advisers (CEA)</li>
              <li>Office of Science and Technology Policy (OSTP)</li>
              <li>Office of the United States Trade Representative (USTR)</li>
              <li>Office of National Drug Control Policy (ONDCP)</li>
              <li>Council on Environmental Quality (CEQ)</li>
            </ul>
          </div>
        )}
      </div>

      {/* External Links */}
      <div className="bg-muted/50 border rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
        <div className="flex flex-wrap gap-4">
          <a
            href="https://www.whitehouse.gov/administration/executive-office-of-the-president/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            EOP Official Page
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.whitehouse.gov/omb/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            Office of Management and Budget
            <ExternalLink className="h-4 w-4" />
          </a>
        </div>
      </div>
    </div>
  );
}
