'use client';

/**
 * SearchFilters Component
 *
 * Renders dynamic filters based on configuration.
 * Supports text, select, multi-select, and date-range filter types.
 */

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
import type { SearchFiltersProps, FilterConfig, FilterValues, DateRange } from '@/types/search-import';

/**
 * Render a single text filter
 */
function TextFilter({
  filter,
  value,
  onChange,
}: {
  filter: FilterConfig;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div className="space-y-1">
      <label className="text-xs font-medium text-muted-foreground">
        {filter.label}
      </label>
      <Input
        placeholder={filter.placeholder || `Enter ${filter.label.toLowerCase()}`}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="h-8"
      />
    </div>
  );
}

/**
 * Render a single select filter
 */
function SelectFilter({
  filter,
  value,
  onChange,
}: {
  filter: FilterConfig;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div className="space-y-1">
      <label className="text-xs font-medium text-muted-foreground">
        {filter.label}
      </label>
      <Select value={value} onValueChange={onChange}>
        <SelectTrigger className="h-8">
          <SelectValue placeholder={filter.placeholder || `Select ${filter.label.toLowerCase()}`} />
        </SelectTrigger>
        <SelectContent>
          {filter.options?.map((option) => (
            <SelectItem key={option.value} value={option.value}>
              {option.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}

/**
 * Render a multi-select filter using checkboxes
 */
function MultiSelectFilter({
  filter,
  value,
  onChange,
}: {
  filter: FilterConfig;
  value: string[];
  onChange: (value: string[]) => void;
}) {
  const toggleOption = (optionValue: string) => {
    if (value.includes(optionValue)) {
      onChange(value.filter((v) => v !== optionValue));
    } else {
      onChange([...value, optionValue]);
    }
  };

  return (
    <div className="space-y-1">
      <label className="text-xs font-medium text-muted-foreground">
        {filter.label}
      </label>
      <div className="flex flex-wrap gap-1">
        {filter.options?.map((option) => {
          const isSelected = value.includes(option.value);
          return (
            <button
              key={option.value}
              type="button"
              onClick={() => toggleOption(option.value)}
              className={`rounded-full px-2 py-0.5 text-xs transition-colors ${
                isSelected
                  ? 'bg-primary text-primary-foreground'
                  : 'bg-muted text-muted-foreground hover:bg-muted/80'
              }`}
            >
              {option.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}

/**
 * Render a date range filter
 */
function DateRangeFilter({
  filter,
  value,
  onChange,
}: {
  filter: FilterConfig;
  value: DateRange;
  onChange: (value: DateRange) => void;
}) {
  const formatDate = (date: Date | undefined): string => {
    if (!date) return '';
    return date.toISOString().split('T')[0];
  };

  const parseDate = (dateStr: string): Date | undefined => {
    if (!dateStr) return undefined;
    return new Date(dateStr);
  };

  return (
    <div className="space-y-1">
      <label className="text-xs font-medium text-muted-foreground">
        {filter.label}
      </label>
      <div className="flex items-center gap-2">
        <Input
          type="date"
          value={formatDate(value.from)}
          onChange={(e) => onChange({ ...value, from: parseDate(e.target.value) })}
          className="h-8"
        />
        <span className="text-xs text-muted-foreground">to</span>
        <Input
          type="date"
          value={formatDate(value.to)}
          onChange={(e) => onChange({ ...value, to: parseDate(e.target.value) })}
          className="h-8"
        />
      </div>
    </div>
  );
}

/**
 * SearchFilters component
 * Renders dynamic filters based on configuration
 */
export function SearchFilters({
  filters,
  values,
  onChange,
  onClear,
}: SearchFiltersProps) {
  const hasActiveFilters = Object.values(values).some((v) => {
    if (v === undefined || v === '') return false;
    if (Array.isArray(v) && v.length === 0) return false;
    if (typeof v === 'object' && 'from' in v && !v.from && !v.to) return false;
    return true;
  });

  const handleFilterChange = (filterId: string, value: string | string[] | DateRange) => {
    onChange({
      ...values,
      [filterId]: value,
    });
  };

  const handleClear = () => {
    const clearedValues: FilterValues = {};
    filters.forEach((f) => {
      clearedValues[f.id] = f.type === 'multi-select' ? [] : f.type === 'date-range' ? {} : '';
    });
    onChange(clearedValues);
    onClear?.();
  };

  if (filters.length === 0) {
    return null;
  }

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-4">
        {filters.map((filter) => {
          const value = values[filter.id];

          switch (filter.type) {
            case 'text':
              return (
                <div key={filter.id} className="min-w-[150px] flex-1">
                  <TextFilter
                    filter={filter}
                    value={(value as string) || ''}
                    onChange={(v) => handleFilterChange(filter.id, v)}
                  />
                </div>
              );
            case 'select':
              return (
                <div key={filter.id} className="min-w-[150px] flex-1">
                  <SelectFilter
                    filter={filter}
                    value={(value as string) || ''}
                    onChange={(v) => handleFilterChange(filter.id, v)}
                  />
                </div>
              );
            case 'multi-select':
              return (
                <div key={filter.id} className="min-w-[200px] flex-1">
                  <MultiSelectFilter
                    filter={filter}
                    value={(value as string[]) || []}
                    onChange={(v) => handleFilterChange(filter.id, v)}
                  />
                </div>
              );
            case 'date-range':
              return (
                <div key={filter.id} className="min-w-[280px] flex-1">
                  <DateRangeFilter
                    filter={filter}
                    value={(value as DateRange) || {}}
                    onChange={(v) => handleFilterChange(filter.id, v)}
                  />
                </div>
              );
            default:
              return null;
          }
        })}
      </div>

      {hasActiveFilters && (
        <Button
          variant="ghost"
          size="sm"
          onClick={handleClear}
          className="h-7 text-xs"
        >
          <X className="mr-1 h-3 w-3" />
          Clear filters
        </Button>
      )}
    </div>
  );
}
