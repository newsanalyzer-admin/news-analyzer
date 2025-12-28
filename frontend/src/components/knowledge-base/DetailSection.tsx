'use client';

import { useState, useCallback } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { SourceCitation } from './SourceCitation';
import type {
  DetailSectionConfig,
  DetailFieldConfig,
  SourceInfo,
} from '@/lib/config/entityTypes';

/**
 * Props for the DetailSection component
 */
export interface DetailSectionProps<T> {
  /** Section configuration */
  config: DetailSectionConfig<T>;
  /** Entity data */
  data: T;
  /** Optional className */
  className?: string;
}

/**
 * DetailSection - Renders a configurable section of entity details.
 * Supports list, grid, and key-value layouts with optional collapsibility.
 */
export function DetailSection<T extends Record<string, unknown>>({
  config,
  data,
  className,
}: DetailSectionProps<T>) {
  const [isCollapsed, setIsCollapsed] = useState(config.defaultCollapsed ?? false);

  const toggleCollapse = useCallback(() => {
    setIsCollapsed((prev) => !prev);
  }, []);

  // Filter out empty fields if hideIfEmpty is set
  const visibleFields = config.fields.filter((field) => {
    if (!field.hideIfEmpty) return true;
    const value = getNestedValue(data, field.id);
    return value !== null && value !== undefined && value !== '';
  });

  // Don't render section if no visible fields
  if (visibleFields.length === 0) {
    return null;
  }

  return (
    <section className={cn('', className)}>
      {/* Section Header */}
      {config.collapsible ? (
        <Button
          variant="ghost"
          className="w-full justify-start p-0 h-auto font-semibold text-lg hover:bg-transparent"
          onClick={toggleCollapse}
          aria-expanded={!isCollapsed}
        >
          {isCollapsed ? (
            <ChevronRight className="h-5 w-5 mr-2" />
          ) : (
            <ChevronDown className="h-5 w-5 mr-2" />
          )}
          {config.label}
        </Button>
      ) : (
        <h3 className="font-semibold text-lg mb-4">{config.label}</h3>
      )}

      {/* Section Content */}
      {!isCollapsed && (
        <div className={cn('mt-4', getLayoutClass(config.layout))}>
          {visibleFields.map((field) => (
            <FieldRenderer
              key={field.id}
              field={field}
              data={data}
              layout={config.layout}
            />
          ))}
        </div>
      )}
    </section>
  );
}

/**
 * Get CSS class for section layout
 */
function getLayoutClass(layout: DetailSectionConfig['layout']): string {
  switch (layout) {
    case 'grid':
      return 'grid grid-cols-1 md:grid-cols-2 gap-4';
    case 'list':
      return 'space-y-3';
    case 'key-value':
    default:
      return 'space-y-3';
  }
}

/**
 * Props for the FieldRenderer component
 */
interface FieldRendererProps<T> {
  field: DetailFieldConfig<T>;
  data: T;
  layout: DetailSectionConfig['layout'];
}

/**
 * FieldRenderer - Renders a single field with optional source citation
 */
function FieldRenderer<T extends Record<string, unknown>>({
  field,
  data,
  layout,
}: FieldRendererProps<T>) {
  const value = getNestedValue(data, field.id);
  const sourceInfo = field.sourceField
    ? (getNestedValue(data, field.sourceField) as SourceInfo | undefined)
    : undefined;

  // Render the value
  const renderedValue = field.render
    ? field.render(value, data)
    : formatValue(value);

  if (layout === 'key-value') {
    return (
      <div className="flex flex-col">
        <dt className="text-sm text-muted-foreground flex items-center gap-1">
          {field.label}
          {sourceInfo && <SourceCitation source={sourceInfo} />}
        </dt>
        <dd className="font-medium">{renderedValue}</dd>
      </div>
    );
  }

  if (layout === 'grid') {
    return (
      <div className="flex flex-col">
        <dt className="text-sm text-muted-foreground flex items-center gap-1">
          {field.label}
          {sourceInfo && <SourceCitation source={sourceInfo} />}
        </dt>
        <dd className="font-medium">{renderedValue}</dd>
      </div>
    );
  }

  // List layout
  return (
    <div>
      <span className="text-sm text-muted-foreground">{field.label}: </span>
      <span className="font-medium">{renderedValue}</span>
      {sourceInfo && <SourceCitation source={sourceInfo} className="ml-1" />}
    </div>
  );
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
  if (Array.isArray(value)) {
    return value.join(', ');
  }
  if (typeof value === 'boolean') {
    return value ? 'Yes' : 'No';
  }
  if (value instanceof Date) {
    return value.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  }
  // Check if it looks like an ISO date string
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}/.test(value)) {
    try {
      const date = new Date(value);
      if (!isNaN(date.getTime())) {
        return date.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
        });
      }
    } catch {
      // Fall through to default
    }
  }
  return String(value);
}
