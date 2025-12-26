'use client';

import { notFound } from 'next/navigation';
import { getEntityTypeConfig } from '@/lib/config/entityTypes';

interface EntityBrowserPageProps {
  params: {
    entityType: string;
  };
}

/**
 * Entity Browser page - displays entities for a given entity type.
 * This is a placeholder that will be enhanced with the EntityBrowser pattern component in UI-2.2.
 */
export default function EntityBrowserPage({ params }: EntityBrowserPageProps) {
  const entityConfig = getEntityTypeConfig(params.entityType);

  if (!entityConfig) {
    notFound();
  }

  const Icon = entityConfig.icon;

  return (
    <div className="p-6">
      <div className="flex items-center gap-3 mb-6">
        <Icon className="h-8 w-8 text-primary" />
        <h1 className="text-2xl font-bold">{entityConfig.label}</h1>
      </div>

      <div className="border rounded-lg p-8 text-center text-muted-foreground">
        <p className="mb-2">EntityBrowser placeholder for <strong>{entityConfig.label}</strong></p>
        <p className="text-sm">
          This will be replaced with the full EntityBrowser pattern component in UI-2.2
        </p>
        <p className="text-sm mt-2">
          Supported views: {entityConfig.supportedViews.join(', ')}
        </p>
      </div>
    </div>
  );
}
