'use client';

import Link from 'next/link';
import { Factory, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs, HierarchyView } from '@/components/knowledge-base';
import { useGovernmentOrgsHierarchy } from '@/hooks/useGovernmentOrgs';
import type { HierarchyConfig } from '@/lib/config/entityTypes';

/**
 * Hierarchy configuration for government corporations
 */
const corpHierarchyConfig: HierarchyConfig = {
  labelField: 'officialName',
  metaFields: ['acronym'],
  childrenField: 'children',
  idField: 'id',
  defaultExpandDepth: 1,
  showChildCount: true,
};

/**
 * Notable government corporations for fallback display
 */
const notableCorporations = [
  {
    name: 'United States Postal Service (USPS)',
    description: 'National mail delivery service',
    established: 1971,
  },
  {
    name: 'Amtrak (National Railroad Passenger Corporation)',
    description: 'Intercity passenger rail service',
    established: 1971,
  },
  {
    name: 'Tennessee Valley Authority (TVA)',
    description: 'Electric power generation and regional development',
    established: 1933,
  },
  {
    name: 'Federal Deposit Insurance Corporation (FDIC)',
    description: 'Bank deposit insurance',
    established: 1933,
  },
  {
    name: 'Pension Benefit Guaranty Corporation (PBGC)',
    description: 'Private pension insurance',
    established: 1974,
  },
  {
    name: 'Corporation for Public Broadcasting (CPB)',
    description: 'Public media funding',
    established: 1967,
  },
  {
    name: 'Export-Import Bank of the United States',
    description: 'Export credit financing',
    established: 1934,
  },
  {
    name: 'Federal Prison Industries (UNICOR)',
    description: 'Prison labor program',
    established: 1934,
  },
];

/**
 * Government Corporations page (UI-6.3).
 *
 * Shows federally chartered corporations that provide public services
 * while operating with business-like flexibility.
 */
export default function CorporationsPage() {
  // Fetch executive branch organizations
  const { data, isLoading, error, refetch } = useGovernmentOrgsHierarchy('executive');

  // Filter to show government corporations
  const corpData = data?.filter((org) => {
    const name = org.officialName?.toLowerCase() || '';
    const orgType = (org as { orgType?: string }).orgType;

    // Check for government_corporation type or corporation in name
    if (orgType === 'government_corporation') return true;

    return (
      name.includes('corporation') ||
      name.includes('usps') ||
      name.includes('postal service') ||
      name.includes('amtrak') ||
      name.includes('tennessee valley authority') ||
      name.includes('fdic') ||
      name.includes('federal deposit insurance')
    );
  }) || [];

  return (
    <div className="container py-8">
      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Back link */}
      <div className="mb-6">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government/executive">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Executive Branch
          </Link>
        </Button>
      </div>

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-pink-500/10 text-pink-600 dark:text-pink-400">
            <Factory className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Government Corporations</h1>
        </div>

        <p className="text-lg text-muted-foreground mb-4">
          Government corporations are federally chartered entities that combine public purposes
          with the flexibility of private business operations. They provide market-oriented
          public services, generate their own revenue, and operate with more autonomy than
          typical federal agencies.
        </p>

        <div className="bg-muted/50 border rounded-lg p-4 mb-6">
          <p className="text-sm text-muted-foreground">
            <strong>Key Characteristics:</strong> Government corporations are created by Congress,
            have their own legal identity, can sue and be sued, and often have the authority
            to raise capital, acquire property, and enter into contracts independently.
          </p>
        </div>
      </div>

      {/* Organizations List */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Federal Government Corporations</h2>

        {corpData.length > 0 ? (
          <HierarchyView
            data={corpData}
            config={corpHierarchyConfig}
            entityType="organizations"
            isLoading={isLoading}
            error={error?.message}
            onRetry={() => refetch()}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {notableCorporations.map((corp) => (
              <div
                key={corp.name}
                className="bg-card border rounded-lg p-4 hover:border-pink-500 transition-colors"
              >
                <h3 className="font-medium mb-1">{corp.name}</h3>
                <p className="text-sm text-muted-foreground mb-2">{corp.description}</p>
                <p className="text-xs text-muted-foreground">Established {corp.established}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Types Section */}
      <div className="bg-card border rounded-lg p-6 mb-8">
        <h2 className="text-xl font-semibold mb-4">Types of Government Corporations</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-muted-foreground">
          <div>
            <h3 className="font-medium text-foreground mb-2">Wholly-Owned Corporations</h3>
            <p>
              100% government-owned entities like USPS, TVA, and FDIC. They receive appropriations
              or generate revenue through services and are subject to federal oversight.
            </p>
          </div>
          <div>
            <h3 className="font-medium text-foreground mb-2">Government-Sponsored Enterprises</h3>
            <p>
              Privately owned but federally chartered entities like Fannie Mae and Freddie Mac.
              They have public policy missions but operate as private companies.
            </p>
          </div>
        </div>
      </div>

      {/* External Links */}
      <div className="bg-muted/50 border rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
        <div className="flex flex-wrap gap-4">
          <a
            href="https://www.usps.com/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            U.S. Postal Service
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.amtrak.com/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            Amtrak
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.tva.com/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            Tennessee Valley Authority
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.fdic.gov/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            FDIC
            <ExternalLink className="h-4 w-4" />
          </a>
        </div>
      </div>
    </div>
  );
}
