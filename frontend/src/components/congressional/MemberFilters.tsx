'use client';

/**
 * MemberFilters Component
 *
 * Filter controls for the members listing page.
 */

import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { useCallback } from 'react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { US_STATES } from '@/lib/constants/states';
import { useDebounce } from '@/hooks/useDebounce';
import { useState, useEffect } from 'react';

const CHAMBERS = [
  { value: 'ALL', label: 'All Chambers' },
  { value: 'SENATE', label: 'Senate' },
  { value: 'HOUSE', label: 'House' },
];

const PARTIES = [
  { value: 'ALL', label: 'All Parties' },
  { value: 'Democrat', label: 'Democrat' },
  { value: 'Republican', label: 'Republican' },
  { value: 'Independent', label: 'Independent' },
];

interface MemberFiltersProps {
  onSearchChange?: (search: string) => void;
}

export function MemberFilters({ onSearchChange }: MemberFiltersProps) {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  const [searchInput, setSearchInput] = useState(
    searchParams.get('search') || ''
  );
  const debouncedSearch = useDebounce(searchInput, 300);

  const chamber = searchParams.get('chamber') || 'ALL';
  const state = searchParams.get('state') || 'ALL';
  const party = searchParams.get('party') || 'ALL';

  const updateParams = useCallback(
    (key: string, value: string) => {
      const params = new URLSearchParams(searchParams.toString());

      if (value === 'ALL' || value === '') {
        params.delete(key);
      } else {
        params.set(key, value);
      }

      // Reset to page 0 when filters change
      params.delete('page');

      router.push(`${pathname}?${params.toString()}`);
    },
    [searchParams, router, pathname]
  );

  // Update URL when debounced search changes
  useEffect(() => {
    updateParams('search', debouncedSearch);
    onSearchChange?.(debouncedSearch);
  }, [debouncedSearch, updateParams, onSearchChange]);

  const handleClearFilters = () => {
    setSearchInput('');
    router.push(pathname);
  };

  const hasFilters =
    chamber !== 'ALL' ||
    state !== 'ALL' ||
    party !== 'ALL' ||
    searchInput !== '';

  return (
    <div className="flex flex-wrap gap-4 mb-6">
      {/* Search Input */}
      <div className="flex-1 min-w-[200px] max-w-md">
        <Input
          type="text"
          placeholder="Search by name..."
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          className="w-full"
        />
      </div>

      {/* Chamber Filter */}
      <Select value={chamber} onValueChange={(v) => updateParams('chamber', v)}>
        <SelectTrigger className="w-[150px]">
          <SelectValue placeholder="Chamber" />
        </SelectTrigger>
        <SelectContent>
          {CHAMBERS.map((c) => (
            <SelectItem key={c.value} value={c.value}>
              {c.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {/* State Filter */}
      <Select value={state} onValueChange={(v) => updateParams('state', v)}>
        <SelectTrigger className="w-[180px]">
          <SelectValue placeholder="State" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">All States</SelectItem>
          {US_STATES.map((s) => (
            <SelectItem key={s.value} value={s.value}>
              {s.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {/* Party Filter */}
      <Select value={party} onValueChange={(v) => updateParams('party', v)}>
        <SelectTrigger className="w-[150px]">
          <SelectValue placeholder="Party" />
        </SelectTrigger>
        <SelectContent>
          {PARTIES.map((p) => (
            <SelectItem key={p.value} value={p.value}>
              {p.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {/* Clear Filters */}
      {hasFilters && (
        <Button variant="outline" onClick={handleClearFilters}>
          Clear Filters
        </Button>
      )}
    </div>
  );
}
