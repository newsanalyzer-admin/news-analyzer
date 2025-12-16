/**
 * EntityCard Component
 *
 * Displays an extracted entity with Schema.org data visualization
 */

'use client';

import { useState } from 'react';
import type { ExtractedEntity } from '@/types/entity';
import { ENTITY_TYPE_METADATA } from '@/types/entity';
import { cn } from '@/lib/utils';

interface EntityCardProps {
  entity: ExtractedEntity;
  showJsonLd?: boolean;
}

export function EntityCard({ entity, showJsonLd = false }: EntityCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const metadata = ENTITY_TYPE_METADATA[entity.entity_type];

  return (
    <div className="border rounded-lg p-4 hover:shadow-md transition-shadow">
      {/* Header */}
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-2">
          <span className="text-2xl">{metadata.icon}</span>
          <div>
            <h3 className="font-semibold text-lg">{entity.text}</h3>
            <div className="flex items-center gap-2 mt-1">
              <span
                className={cn(
                  'text-xs px-2 py-1 rounded-full font-medium',
                  metadata.color,
                  metadata.bgColor
                )}
              >
                {metadata.label}
              </span>
              <span className="text-xs text-gray-500">
                {entity.schema_org_type}
              </span>
            </div>
          </div>
        </div>
        <div className="text-right">
          <div className="text-sm font-medium text-gray-700">
            {Math.round(entity.confidence * 100)}%
          </div>
          <div className="text-xs text-gray-500">confidence</div>
        </div>
      </div>

      {/* Schema.org Properties */}
      {entity.schema_org_data && Object.keys(entity.schema_org_data).length > 3 && (
        <div className="mb-3 pl-9">
          <div className="text-sm space-y-1">
            {Object.entries(entity.schema_org_data).map(([key, value]) => {
              // Skip JSON-LD metadata fields
              if (key.startsWith('@') || key === 'name') return null;

              // Handle nested objects
              if (typeof value === 'object' && value !== null) {
                if ('@type' in value && 'name' in value) {
                  return (
                    <div key={key} className="flex gap-2">
                      <span className="text-gray-500 font-medium">{key}:</span>
                      <span className="text-gray-700">{value.name}</span>
                    </div>
                  );
                }
                return null;
              }

              return (
                <div key={key} className="flex gap-2">
                  <span className="text-gray-500 font-medium">{key}:</span>
                  <span className="text-gray-700">{String(value)}</span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* JSON-LD Toggle */}
      {showJsonLd && (
        <div className="mt-3 pt-3 border-t">
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="text-sm text-blue-600 hover:text-blue-700 font-medium"
          >
            {isExpanded ? '▼' : '▶'} Schema.org JSON-LD
          </button>

          {isExpanded && (
            <pre className="mt-2 p-3 bg-gray-50 rounded text-xs overflow-x-auto">
              {JSON.stringify(entity.schema_org_data, null, 2)}
            </pre>
          )}
        </div>
      )}

      {/* Position Info */}
      <div className="mt-3 pt-3 border-t text-xs text-gray-400">
        Position: {entity.start}—{entity.end}
      </div>
    </div>
  );
}
