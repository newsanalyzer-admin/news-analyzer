'use client';

import { use } from 'react';
import Link from 'next/link';
import { notFound } from 'next/navigation';
import { ArrowLeft, Scale, Landmark, Building } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { HierarchyView } from '@/components/knowledge-base';
import { useGovernmentOrgsHierarchy } from '@/hooks/useGovernmentOrgs';
import type { GovernmentBranch } from '@/types/government-org';
import type { HierarchyConfig } from '@/lib/config/entityTypes';

/**
 * Valid branch slugs and their configurations
 */
const branchConfig: Record<string, {
  label: string;
  branch: GovernmentBranch;
  description: string;
  icon: React.ReactNode;
}> = {
  executive: {
    label: 'Executive Branch',
    branch: 'executive',
    description: 'Executive departments, agencies, and government corporations under the President.',
    icon: <Building className="h-5 w-5" />,
  },
  legislative: {
    label: 'Legislative Branch',
    branch: 'legislative',
    description: 'The Senate, House of Representatives, and congressional support agencies.',
    icon: <Landmark className="h-5 w-5" />,
  },
  judicial: {
    label: 'Judicial Branch',
    branch: 'judicial',
    description: 'Federal courts including the Supreme Court, Courts of Appeals, and District Courts.',
    icon: <Scale className="h-5 w-5" />,
  },
};

/**
 * Hierarchy configuration for government organizations
 */
const govOrgHierarchyConfig: HierarchyConfig = {
  labelField: 'officialName',
  metaFields: ['acronym'],
  childrenField: 'children',
  idField: 'id',
  defaultExpandDepth: 1,
  showChildCount: true,
};

interface BranchPageProps {
  params: Promise<{ branch: string }>;
}

/**
 * Branch-specific government organization hierarchy page.
 * Shows a tree view of organizations within the selected branch.
 */
export default function BranchPage({ params }: BranchPageProps) {
  const { branch } = use(params);

  // Validate branch parameter
  const config = branchConfig[branch];
  if (!config) {
    notFound();
  }

  // Fetch hierarchy data for this branch
  const { data, isLoading, error, refetch } = useGovernmentOrgsHierarchy(config.branch);

  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-6">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Government
          </Link>
        </Button>
      </div>

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-2">
          <div className="p-2 rounded-lg bg-primary/10 text-primary">
            {config.icon}
          </div>
          <h1 className="text-3xl font-bold">{config.label}</h1>
        </div>
        <p className="text-muted-foreground">
          {config.description}
        </p>
      </div>

      {/* Hierarchy View */}
      <HierarchyView
        data={data || []}
        config={govOrgHierarchyConfig}
        entityType="organizations"
        isLoading={isLoading}
        error={error?.message}
        onRetry={() => refetch()}
      />
    </div>
  );
}
