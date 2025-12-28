'use client';

import Link from 'next/link';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { RelatedEntityConfig } from '@/lib/config/entityTypes';

/**
 * Props for the RelatedEntities component
 */
export interface RelatedEntitiesProps<T> {
  /** Related entity configuration */
  config: RelatedEntityConfig;
  /** Entity data containing the related entities */
  data: T;
  /** Optional className */
  className?: string;
}

/**
 * RelatedEntities - Displays a section of related entities as clickable links.
 * Navigates to the detail page of each related entity.
 */
export function RelatedEntities<T extends Record<string, unknown>>({
  config,
  data,
  className,
}: RelatedEntitiesProps<T>) {
  // Get the related entities from the data
  const relatedData = getNestedValue(data, config.field);

  // Handle different data formats
  const entities = normalizeEntities(relatedData, config);

  // Don't render if no related entities
  if (!entities || entities.length === 0) {
    return null;
  }

  return (
    <section className={cn('', className)}>
      <h3 className="font-semibold text-lg mb-4">{config.label}</h3>
      <div className="flex flex-wrap gap-2">
        {entities.map((entity) => (
          <Link
            key={entity.id}
            href={`/knowledge-base/${config.entityType}/${entity.id}`}
            className="no-underline"
          >
            <Badge
              variant="secondary"
              className="cursor-pointer hover:bg-secondary/80 transition-colors"
            >
              {entity.displayText}
            </Badge>
          </Link>
        ))}
      </div>
    </section>
  );
}

/**
 * Normalized entity for display
 */
interface NormalizedEntity {
  id: string;
  displayText: string;
}

/**
 * Normalize related entities to a consistent format
 */
function normalizeEntities(
  data: unknown,
  config: RelatedEntityConfig
): NormalizedEntity[] {
  if (!data) return [];

  // Handle array of objects
  if (Array.isArray(data)) {
    return data
      .filter((item) => item !== null && item !== undefined)
      .map((item) => {
        if (typeof item === 'object') {
          const idField = config.idField || 'id';
          const id = (item as Record<string, unknown>)[idField];
          const displayText = getNestedValue(
            item as Record<string, unknown>,
            config.displayField
          );
          return {
            id: String(id),
            displayText: formatValue(displayText),
          };
        }
        // Handle array of IDs (strings)
        return {
          id: String(item),
          displayText: String(item),
        };
      });
  }

  // Handle single object
  if (typeof data === 'object') {
    const idField = config.idField || 'id';
    const id = (data as Record<string, unknown>)[idField];
    const displayText = getNestedValue(
      data as Record<string, unknown>,
      config.displayField
    );
    return [
      {
        id: String(id),
        displayText: formatValue(displayText),
      },
    ];
  }

  return [];
}

/**
 * Get a nested value from an object using dot notation
 */
function getNestedValue(obj: Record<string, unknown>, path: string): unknown {
  return path.split('.').reduce((current, key) => {
    if (current === null || current === undefined) return undefined;
    return (current as Record<string, unknown>)[key];
  }, obj as unknown);
}

/**
 * Format a value for display
 */
function formatValue(value: unknown): string {
  if (value === null || value === undefined) {
    return '-';
  }
  return String(value);
}
