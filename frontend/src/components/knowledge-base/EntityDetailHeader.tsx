'use client';

import { ChevronLeft } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { DetailHeaderConfig } from '@/lib/config/entityTypes';

/**
 * Props for the EntityDetailHeader component
 */
export interface EntityDetailHeaderProps<T> {
  /** Header configuration */
  config: DetailHeaderConfig;
  /** Entity data */
  data: T;
  /** Entity type label for the badge */
  entityTypeLabel: string;
  /** Optional back URL (defaults to router.back()) */
  backUrl?: string;
  /** Optional className */
  className?: string;
}

/**
 * EntityDetailHeader - Displays the header section of an entity detail page.
 * Includes back button, title, subtitle, badges, and key metadata.
 */
export function EntityDetailHeader<T extends Record<string, unknown>>({
  config,
  data,
  entityTypeLabel,
  backUrl,
  className,
}: EntityDetailHeaderProps<T>) {
  const router = useRouter();

  // Get values from data
  const title = getNestedValue(data, config.titleField);
  const subtitle = config.subtitleField
    ? getNestedValue(data, config.subtitleField)
    : null;
  const badgeValue = config.badgeField
    ? getNestedValue(data, config.badgeField)
    : null;

  // Handle back navigation
  const handleBack = () => {
    if (backUrl) {
      router.push(backUrl);
    } else {
      router.back();
    }
  };

  return (
    <header className={cn('space-y-4', className)}>
      {/* Back Button */}
      <Button
        variant="ghost"
        size="sm"
        onClick={handleBack}
        className="gap-1 text-muted-foreground hover:text-foreground"
      >
        <ChevronLeft className="h-4 w-4" />
        Back to {entityTypeLabel}
      </Button>

      {/* Title Section */}
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">
          {formatValue(title)}
        </h1>

        {subtitle !== null && subtitle !== undefined && (
          <p className="text-xl text-muted-foreground">
            {formatValue(subtitle)}
          </p>
        )}

        {/* Badges */}
        <div className="flex flex-wrap gap-2 pt-2">
          {badgeValue !== null && badgeValue !== undefined && (
            config.renderBadge ? (
              <>{config.renderBadge(badgeValue)}</>
            ) : (
              <Badge variant="outline">
                {formatValue(badgeValue)}
              </Badge>
            )
          )}
        </div>

        {/* Meta Fields */}
        {config.metaFields && config.metaFields.length > 0 && (
          <div className="flex flex-wrap gap-4 pt-2 text-sm text-muted-foreground">
            {config.metaFields.map((fieldPath) => {
              const value = getNestedValue(data, fieldPath);
              if (value === null || value === undefined) return null;
              return (
                <span key={fieldPath}>
                  {formatFieldLabel(fieldPath)}: {formatValue(value)}
                </span>
              );
            })}
          </div>
        )}
      </div>
    </header>
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
  if (typeof value === 'boolean') {
    return value ? 'Yes' : 'No';
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

/**
 * Format a field path to a human-readable label
 */
function formatFieldLabel(path: string): string {
  const lastPart = path.split('.').pop() || path;
  // Convert camelCase to Title Case
  return lastPart
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (str) => str.toUpperCase())
    .trim();
}
