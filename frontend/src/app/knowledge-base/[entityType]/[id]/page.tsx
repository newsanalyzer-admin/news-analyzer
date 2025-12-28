'use client';

import { use } from 'react';
import { notFound } from 'next/navigation';
import { getEntityTypeConfig } from '@/lib/config/entityTypes';
import { EntityDetail } from '@/components/knowledge-base';
import { useGovernmentOrg } from '@/hooks/useGovernmentOrgs';

interface EntityDetailPageProps {
  params: Promise<{
    entityType: string;
    id: string;
  }>;
}

/**
 * Entity Detail page - displays details for a specific entity.
 * Uses the EntityDetail pattern component for configuration-driven rendering.
 */
export default function EntityDetailPage({ params }: EntityDetailPageProps) {
  const { entityType, id } = use(params);

  // Get entity configuration
  const entityConfig = getEntityTypeConfig(entityType);
  if (!entityConfig) {
    notFound();
  }

  // Parse ID as number for organizations
  const numericId = parseInt(id, 10);
  const isValidId = !isNaN(numericId);

  // Render organization detail
  if (entityType === 'organizations') {
    return (
      <OrganizationDetail
        id={isValidId ? numericId : null}
        entityConfig={entityConfig}
      />
    );
  }

  // Placeholder for other entity types
  const Icon = entityConfig.icon;
  return (
    <div className="p-6">
      <div className="flex items-center gap-3 mb-6">
        <Icon className="h-8 w-8 text-primary" />
        <h1 className="text-2xl font-bold">{entityConfig.label} Detail</h1>
      </div>

      <div className="border rounded-lg p-8 text-center text-muted-foreground">
        <p className="mb-2">
          EntityDetail for <strong>{entityConfig.label}</strong> (ID: {id})
        </p>
        <p className="text-sm">
          Data fetching hooks for this entity type will be added in future stories.
        </p>
      </div>
    </div>
  );
}

/**
 * Organization-specific detail component
 */
function OrganizationDetail({
  id,
  entityConfig,
}: {
  id: number | null;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  entityConfig: ReturnType<typeof getEntityTypeConfig>;
}) {
  const { data, isLoading, error, refetch } = useGovernmentOrg(id);

  // Handle invalid ID
  if (id === null) {
    return (
      <div className="p-6">
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">&#x26a0;&#xfe0f;</div>
          <h3 className="text-lg font-semibold mb-2">Invalid Organization ID</h3>
          <p className="text-muted-foreground">
            The organization ID must be a valid number.
          </p>
        </div>
      </div>
    );
  }

  // Ensure we have detail config
  if (!entityConfig?.detailConfig) {
    return (
      <div className="p-6">
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">&#x2699;&#xfe0f;</div>
          <h3 className="text-lg font-semibold mb-2">Configuration Missing</h3>
          <p className="text-muted-foreground">
            Detail configuration for this entity type is not available.
          </p>
        </div>
      </div>
    );
  }

  return (
    <EntityDetail
      entityConfig={entityConfig}
      detailConfig={entityConfig.detailConfig}
      data={data || null}
      isLoading={isLoading}
      error={error?.message || null}
      backUrl={`/knowledge-base/${entityConfig.id}`}
      onRetry={() => refetch()}
    />
  );
}
