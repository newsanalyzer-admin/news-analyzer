'use client';

/**
 * JudgeFilters Component
 *
 * Filter controls for the federal judges listing page.
 */

import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { useCallback, useState, useEffect } from 'react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { useDebounce } from '@/hooks/useDebounce';

const COURT_LEVELS = [
  { value: 'ALL', label: 'All Courts' },
  { value: 'SUPREME', label: 'Supreme Court' },
  { value: 'APPEALS', label: 'Courts of Appeals' },
  { value: 'DISTRICT', label: 'District Courts' },
];

const CIRCUITS = [
  { value: 'ALL', label: 'All Circuits' },
  { value: '1', label: '1st Circuit' },
  { value: '2', label: '2nd Circuit' },
  { value: '3', label: '3rd Circuit' },
  { value: '4', label: '4th Circuit' },
  { value: '5', label: '5th Circuit' },
  { value: '6', label: '6th Circuit' },
  { value: '7', label: '7th Circuit' },
  { value: '8', label: '8th Circuit' },
  { value: '9', label: '9th Circuit' },
  { value: '10', label: '10th Circuit' },
  { value: '11', label: '11th Circuit' },
  { value: 'DC', label: 'D.C. Circuit' },
  { value: 'FEDERAL', label: 'Federal Circuit' },
];

const STATUSES = [
  { value: 'ALL', label: 'All Status' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'SENIOR', label: 'Senior' },
];

interface JudgeFiltersProps {
  onSearchChange?: (search: string) => void;
}

export function JudgeFilters({ onSearchChange }: JudgeFiltersProps) {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  const [searchInput, setSearchInput] = useState(
    searchParams.get('search') || ''
  );
  const debouncedSearch = useDebounce(searchInput, 300);

  const courtLevel = searchParams.get('courtLevel') || 'ALL';
  const circuit = searchParams.get('circuit') || 'ALL';
  const status = searchParams.get('status') || 'ALL';

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
    courtLevel !== 'ALL' ||
    circuit !== 'ALL' ||
    status !== 'ALL' ||
    searchInput !== '';

  // Show circuit filter only for Appeals and District courts
  const showCircuitFilter = courtLevel === 'APPEALS' || courtLevel === 'DISTRICT' || courtLevel === 'ALL';

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

      {/* Court Level Filter */}
      <Select value={courtLevel} onValueChange={(v) => updateParams('courtLevel', v)}>
        <SelectTrigger className="w-[160px]">
          <SelectValue placeholder="Court Level" />
        </SelectTrigger>
        <SelectContent>
          {COURT_LEVELS.map((c) => (
            <SelectItem key={c.value} value={c.value}>
              {c.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {/* Circuit Filter */}
      {showCircuitFilter && (
        <Select value={circuit} onValueChange={(v) => updateParams('circuit', v)}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Circuit" />
          </SelectTrigger>
          <SelectContent>
            {CIRCUITS.map((c) => (
              <SelectItem key={c.value} value={c.value}>
                {c.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      )}

      {/* Status Filter */}
      <Select value={status} onValueChange={(v) => updateParams('status', v)}>
        <SelectTrigger className="w-[140px]">
          <SelectValue placeholder="Status" />
        </SelectTrigger>
        <SelectContent>
          {STATUSES.map((s) => (
            <SelectItem key={s.value} value={s.value}>
              {s.label}
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
