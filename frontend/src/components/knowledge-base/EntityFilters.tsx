'use client';

import { useCallback } from 'react';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { FilterConfig } from '@/lib/config/entityTypes';

/**
 * Props for the EntityFilters component
 */
export interface EntityFiltersProps {
  /** Filter configurations */
  filters: FilterConfig[];
  /** Current filter values */
  values: Record<string, string | string[] | undefined>;
  /** Filter change callback */
  onChange: (values: Record<string, string | string[] | undefined>) => void;
  /** Optional className */
  className?: string;
}

/**
 * EntityFilters - Renders filter controls based on configuration.
 * Supports select, multi-select, and text filter types.
 */
export function EntityFilters({
  filters,
  values,
  onChange,
  className,
}: EntityFiltersProps) {
  // Count active filters
  const activeFilterCount = Object.values(values).filter(
    (v) => v !== undefined && v !== '' && (Array.isArray(v) ? v.length > 0 : true)
  ).length;

  // Handle individual filter change
  const handleFilterChange = useCallback(
    (filterId: string, value: string | string[] | undefined) => {
      onChange({
        ...values,
        [filterId]: value,
      });
    },
    [values, onChange]
  );

  // Clear all filters
  const handleClearAll = useCallback(() => {
    onChange({});
  }, [onChange]);

  if (filters.length === 0) {
    return null;
  }

  return (
    <div className={cn('space-y-4', className)}>
      <div className="flex flex-wrap gap-3 items-end">
        {filters.map((filter) => (
          <FilterControl
            key={filter.id}
            filter={filter}
            value={values[filter.id]}
            onChange={(value) => handleFilterChange(filter.id, value)}
          />
        ))}

        {activeFilterCount > 0 && (
          <Button
            variant="ghost"
            size="sm"
            onClick={handleClearAll}
            className="h-9"
          >
            <X className="h-4 w-4 mr-1" />
            Clear all
            <Badge variant="secondary" className="ml-2">
              {activeFilterCount}
            </Badge>
          </Button>
        )}
      </div>
    </div>
  );
}

/**
 * Individual filter control based on type
 */
function FilterControl({
  filter,
  value,
  onChange,
}: {
  filter: FilterConfig;
  value: string | string[] | undefined;
  onChange: (value: string | string[] | undefined) => void;
}) {
  switch (filter.type) {
    case 'text':
      return (
        <div className="min-w-[200px]">
          <label className="text-sm font-medium mb-1.5 block">
            {filter.label}
          </label>
          <Input
            type="text"
            placeholder={filter.placeholder || `Filter by ${filter.label.toLowerCase()}...`}
            value={(value as string) || ''}
            onChange={(e) => onChange(e.target.value || undefined)}
            className="h-9"
          />
        </div>
      );

    case 'select':
      return (
        <div className="min-w-[150px]">
          <label className="text-sm font-medium mb-1.5 block">
            {filter.label}
          </label>
          <Select
            value={(value as string) || 'ALL'}
            onValueChange={(v) => onChange(v === 'ALL' ? undefined : v)}
          >
            <SelectTrigger className="h-9">
              <SelectValue placeholder={`All ${filter.label}`} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All</SelectItem>
              {filter.options?.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      );

    case 'multi-select':
      // For now, implement as a simple single select
      // Multi-select can be enhanced with a custom component later
      return (
        <div className="min-w-[150px]">
          <label className="text-sm font-medium mb-1.5 block">
            {filter.label}
          </label>
          <Select
            value={Array.isArray(value) ? value[0] : (value as string) || 'ALL'}
            onValueChange={(v) => onChange(v === 'ALL' ? undefined : [v])}
          >
            <SelectTrigger className="h-9">
              <SelectValue placeholder={`All ${filter.label}`} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All</SelectItem>
              {filter.options?.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      );

    default:
      return null;
  }
}
