'use client';

/**
 * CommitteeFilters Component
 *
 * Filter controls for committees: Chamber, Type, and Search.
 * Persists filters in URL params for shareable links.
 */

import { useState, useEffect } from 'react';
import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { useDebounce } from '@/hooks/useDebounce';

const CHAMBERS = [
  { value: 'ALL', label: 'All Chambers' },
  { value: 'SENATE', label: 'Senate' },
  { value: 'HOUSE', label: 'House' },
  { value: 'JOINT', label: 'Joint' },
];

const COMMITTEE_TYPES = [
  { value: 'ALL', label: 'All Types' },
  { value: 'STANDING', label: 'Standing' },
  { value: 'SELECT', label: 'Select' },
  { value: 'SPECIAL', label: 'Special' },
  { value: 'JOINT', label: 'Joint' },
  { value: 'SUBCOMMITTEE', label: 'Subcommittee' },
];

export function CommitteeFilters() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  // Local state for search input
  const [searchInput, setSearchInput] = useState(searchParams.get('search') || '');
  const debouncedSearch = useDebounce(searchInput, 300);

  // Get current filter values from URL
  const currentChamber = searchParams.get('chamber') || 'ALL';
  const currentType = searchParams.get('type') || 'ALL';

  // Update URL when filters change
  const updateParams = (key: string, value: string) => {
    const params = new URLSearchParams(searchParams.toString());
    if (value && value !== 'ALL') {
      params.set(key, value);
    } else {
      params.delete(key);
    }
    router.push(`${pathname}?${params.toString()}`);
  };

  // Update search param when debounced search changes
  useEffect(() => {
    const params = new URLSearchParams(searchParams.toString());
    if (debouncedSearch && debouncedSearch.length >= 2) {
      params.set('search', debouncedSearch);
    } else {
      params.delete('search');
    }
    router.push(`${pathname}?${params.toString()}`);
  }, [debouncedSearch, pathname, router, searchParams]);

  const handleClearFilters = () => {
    setSearchInput('');
    router.push(pathname);
  };

  const hasActiveFilters =
    currentChamber !== 'ALL' || currentType !== 'ALL' || searchInput.length > 0;

  return (
    <div className="flex flex-wrap gap-4 mb-6">
      {/* Search Input */}
      <div className="flex-1 min-w-[200px] max-w-md">
        <Input
          type="text"
          placeholder="Search committees..."
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          className="w-full"
        />
      </div>

      {/* Chamber Filter */}
      <Select
        value={currentChamber}
        onValueChange={(value) => updateParams('chamber', value)}
      >
        <SelectTrigger className="w-[160px]">
          <SelectValue placeholder="Chamber" />
        </SelectTrigger>
        <SelectContent>
          {CHAMBERS.map((chamber) => (
            <SelectItem key={chamber.value} value={chamber.value}>
              {chamber.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {/* Type Filter */}
      <Select
        value={currentType}
        onValueChange={(value) => updateParams('type', value)}
      >
        <SelectTrigger className="w-[180px]">
          <SelectValue placeholder="Committee Type" />
        </SelectTrigger>
        <SelectContent>
          {COMMITTEE_TYPES.map((type) => (
            <SelectItem key={type.value} value={type.value}>
              {type.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {/* Clear Filters */}
      {hasActiveFilters && (
        <Button variant="outline" onClick={handleClearFilters}>
          Clear Filters
        </Button>
      )}
    </div>
  );
}
