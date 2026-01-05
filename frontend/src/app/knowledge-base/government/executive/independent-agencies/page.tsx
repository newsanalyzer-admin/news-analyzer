'use client';

import Link from 'next/link';
import { Building2, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs, HierarchyView } from '@/components/knowledge-base';
import { useGovernmentOrgsHierarchy } from '@/hooks/useGovernmentOrgs';
import type { HierarchyConfig } from '@/lib/config/entityTypes';

/**
 * Hierarchy configuration for independent agencies
 */
const agencyHierarchyConfig: HierarchyConfig = {
  labelField: 'officialName',
  metaFields: ['acronym'],
  childrenField: 'children',
  idField: 'id',
  defaultExpandDepth: 1,
  showChildCount: true,
};

/**
 * Notable independent agencies for fallback display
 */
const notableAgencies = [
  { name: 'Environmental Protection Agency (EPA)', description: 'Environmental protection' },
  { name: 'National Aeronautics and Space Administration (NASA)', description: 'Space exploration' },
  { name: 'Central Intelligence Agency (CIA)', description: 'Foreign intelligence' },
  { name: 'Federal Communications Commission (FCC)', description: 'Communications regulation' },
  { name: 'Federal Trade Commission (FTC)', description: 'Consumer protection' },
  { name: 'Securities and Exchange Commission (SEC)', description: 'Securities regulation' },
  { name: 'Social Security Administration (SSA)', description: 'Social security programs' },
  { name: 'National Labor Relations Board (NLRB)', description: 'Labor relations' },
  { name: 'Federal Election Commission (FEC)', description: 'Election oversight' },
  { name: 'Consumer Financial Protection Bureau (CFPB)', description: 'Financial consumer protection' },
  { name: 'Federal Reserve System', description: 'Monetary policy' },
  { name: 'National Science Foundation (NSF)', description: 'Science funding' },
];

/**
 * Independent Agencies page (UI-6.3).
 *
 * Shows federal agencies that operate outside of Cabinet departments,
 * including regulatory commissions and other specialized agencies.
 */
export default function IndependentAgenciesPage() {
  // Fetch executive branch organizations
  const { data, isLoading, error, refetch } = useGovernmentOrgsHierarchy('executive');

  // Filter to show independent agencies (not departments, not corporations)
  const agencyData = data?.filter((org) => {
    const name = org.officialName?.toLowerCase() || '';
    const orgType = (org as { orgType?: string }).orgType;

    // Exclude departments and corporations
    if (name.startsWith('department of')) return false;
    if (orgType === 'government_corporation') return false;
    if (name.includes('corporation') && !name.includes('commission')) return false;

    // Include agencies, commissions, boards, etc.
    return (
      name.includes('agency') ||
      name.includes('administration') ||
      name.includes('commission') ||
      name.includes('board') ||
      orgType === 'independent_agency'
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
          <div className="p-3 rounded-lg bg-fuchsia-500/10 text-fuchsia-600 dark:text-fuchsia-400">
            <Building2 className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Independent Agencies</h1>
        </div>

        <p className="text-lg text-muted-foreground mb-4">
          Independent agencies are federal agencies that exist outside of the Cabinet departments.
          They are established by Congress to address specific issues and often have regulatory
          or quasi-judicial functions. Many are headed by boards or commissions rather than
          a single administrator.
        </p>

        <div className="bg-muted/50 border rounded-lg p-4 mb-6">
          <p className="text-sm text-muted-foreground">
            <strong>Independence:</strong> While part of the executive branch, independent agencies
            often have structural features designed to insulate them from direct presidential control,
            such as fixed terms for commissioners and bipartisan membership requirements.
          </p>
        </div>
      </div>

      {/* Organizations List */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Federal Independent Agencies</h2>

        {agencyData.length > 0 ? (
          <HierarchyView
            data={agencyData}
            config={agencyHierarchyConfig}
            entityType="organizations"
            isLoading={isLoading}
            error={error?.message}
            onRetry={() => refetch()}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {notableAgencies.map((agency) => (
              <div
                key={agency.name}
                className="bg-card border rounded-lg p-4 hover:border-fuchsia-500 transition-colors"
              >
                <h3 className="font-medium mb-1">{agency.name}</h3>
                <p className="text-sm text-muted-foreground">{agency.description}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Types of Agencies */}
      <div className="bg-card border rounded-lg p-6 mb-8">
        <h2 className="text-xl font-semibold mb-4">Types of Independent Agencies</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-muted-foreground">
          <div>
            <h3 className="font-medium text-foreground mb-2">Independent Executive Agencies</h3>
            <p>
              Headed by single administrators appointed by the President (e.g., EPA, NASA, CIA).
              The President can generally remove the head at will.
            </p>
          </div>
          <div>
            <h3 className="font-medium text-foreground mb-2">Independent Regulatory Commissions</h3>
            <p>
              Headed by multi-member boards with fixed, staggered terms (e.g., FCC, SEC, FTC).
              Members can typically only be removed for cause.
            </p>
          </div>
        </div>
      </div>

      {/* External Links */}
      <div className="bg-muted/50 border rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
        <div className="flex flex-wrap gap-4">
          <a
            href="https://www.usa.gov/federal-agencies"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            A-Z Index of Federal Agencies
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.federalregister.gov/agencies"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            Federal Register Agency List
            <ExternalLink className="h-4 w-4" />
          </a>
        </div>
      </div>
    </div>
  );
}
