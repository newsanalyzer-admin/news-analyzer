'use client';

import { useMemo } from 'react';
import { notFound, useSearchParams } from 'next/navigation';
import { getEntityTypeConfig } from '@/lib/config/entityTypes';
import { getPeopleSubtypeConfig } from '@/lib/config/peopleConfig';
import { EntityDetail } from '@/components/knowledge-base';
import { useGovernmentOrg } from '@/hooks/useGovernmentOrgs';
import { useJudge } from '@/hooks/useJudges';
import { useMember } from '@/hooks/useMembers';
import { useAppointee } from '@/hooks/useAppointees';

interface EntityDetailPageProps {
  params: {
    entityType: string;
    id: string;
  };
}

/**
 * Entity Detail page - displays details for a specific entity.
 * Uses the EntityDetail pattern component for configuration-driven rendering.
 */
export default function EntityDetailPage({ params }: EntityDetailPageProps) {
  const { entityType, id } = params;
  const searchParams = useSearchParams();

  // Get entity configuration
  const entityConfig = getEntityTypeConfig(entityType);
  if (!entityConfig) {
    notFound();
  }

  // Parse ID as number for organizations
  const numericId = parseInt(id, 10);
  const isValidNumericId = !isNaN(numericId);

  // Render organization detail
  if (entityType === 'organizations') {
    return (
      <OrganizationDetail
        id={isValidNumericId ? numericId : null}
        entityConfig={entityConfig}
      />
    );
  }

  // Render people detail (judges, members, appointees)
  if (entityType === 'people') {
    // Get the subtype from URL params or try to detect it
    const subtypeParam = searchParams.get('type');
    return (
      <PeopleDetail
        id={id}
        subtypeHint={subtypeParam}
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

/**
 * People-specific detail component
 * Handles judges, members, and appointees based on subtype hint or data detection
 */
function PeopleDetail({
  id,
  subtypeHint,
  entityConfig,
}: {
  id: string;
  subtypeHint: string | null;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  entityConfig: ReturnType<typeof getEntityTypeConfig>;
}) {
  // Try all three hooks - only one should return data based on the ID format
  // The hooks are designed to handle non-matching IDs gracefully
  const judgeQuery = useJudge(id);
  const memberQuery = useMember(id);
  const appointeeQuery = useAppointee(id);

  // Determine which subtype we're viewing based on data or hint
  const { subtype, data, isLoading, error, refetch, backUrl } = useMemo(() => {
    // If we have a hint, prioritize that subtype's data
    if (subtypeHint === 'judges' && (judgeQuery.data || judgeQuery.isLoading)) {
      return {
        subtype: 'judges',
        data: judgeQuery.data,
        isLoading: judgeQuery.isLoading,
        error: judgeQuery.error?.message || null,
        refetch: judgeQuery.refetch,
        backUrl: '/knowledge-base/people?type=judges',
      };
    }
    if (subtypeHint === 'members' && (memberQuery.data || memberQuery.isLoading)) {
      return {
        subtype: 'members',
        data: memberQuery.data,
        isLoading: memberQuery.isLoading,
        error: memberQuery.error?.message || null,
        refetch: memberQuery.refetch,
        backUrl: '/knowledge-base/people?type=members',
      };
    }
    if (subtypeHint === 'appointees' && (appointeeQuery.data || appointeeQuery.isLoading)) {
      return {
        subtype: 'appointees',
        data: appointeeQuery.data,
        isLoading: appointeeQuery.isLoading,
        error: appointeeQuery.error?.message || null,
        refetch: appointeeQuery.refetch,
        backUrl: '/knowledge-base/people?type=appointees',
      };
    }

    // No hint or hint didn't match - check which query returned data
    if (judgeQuery.data) {
      return {
        subtype: 'judges',
        data: judgeQuery.data,
        isLoading: false,
        error: null,
        refetch: judgeQuery.refetch,
        backUrl: '/knowledge-base/people?type=judges',
      };
    }
    if (memberQuery.data) {
      return {
        subtype: 'members',
        data: memberQuery.data,
        isLoading: false,
        error: null,
        refetch: memberQuery.refetch,
        backUrl: '/knowledge-base/people?type=members',
      };
    }
    if (appointeeQuery.data) {
      return {
        subtype: 'appointees',
        data: appointeeQuery.data,
        isLoading: false,
        error: null,
        refetch: appointeeQuery.refetch,
        backUrl: '/knowledge-base/people?type=appointees',
      };
    }

    // Still loading or no data found
    const anyLoading = judgeQuery.isLoading || memberQuery.isLoading || appointeeQuery.isLoading;
    const firstError =
      judgeQuery.error?.message || memberQuery.error?.message || appointeeQuery.error?.message;

    // Default to judges if we have a hint, otherwise use the first one
    const defaultSubtype = subtypeHint || 'judges';
    return {
      subtype: defaultSubtype,
      data: null,
      isLoading: anyLoading,
      error: anyLoading ? null : firstError || 'Person not found',
      refetch: () => {
        judgeQuery.refetch();
        memberQuery.refetch();
        appointeeQuery.refetch();
      },
      backUrl: `/knowledge-base/people?type=${defaultSubtype}`,
    };
  }, [subtypeHint, judgeQuery, memberQuery, appointeeQuery]);

  // Get the subtype configuration
  const subtypeConfig = getPeopleSubtypeConfig(subtype);

  // Build the entity config with the correct detail config
  // We need to ensure entityConfig has all required properties
  const peopleEntityConfig = useMemo(() => {
    if (!entityConfig) return null;
    return {
      ...entityConfig,
      detailConfig: subtypeConfig?.detailConfig,
    };
  }, [entityConfig, subtypeConfig]);

  // Ensure we have detail config
  if (!subtypeConfig?.detailConfig || !peopleEntityConfig) {
    return (
      <div className="p-6">
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">&#x2699;&#xfe0f;</div>
          <h3 className="text-lg font-semibold mb-2">Configuration Missing</h3>
          <p className="text-muted-foreground">
            Detail configuration for {subtype} is not available.
          </p>
        </div>
      </div>
    );
  }

  return (
    <EntityDetail
      entityConfig={peopleEntityConfig as NonNullable<typeof entityConfig>}
      detailConfig={subtypeConfig.detailConfig}
      data={data || null}
      isLoading={isLoading}
      error={error}
      backUrl={backUrl}
      onRetry={() => refetch()}
    />
  );
}
