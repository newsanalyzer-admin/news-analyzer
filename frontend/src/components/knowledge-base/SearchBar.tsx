'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import { Search, X } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { useDebounce } from '@/hooks/useDebounce';

/**
 * Props for the SearchBar component
 */
export interface SearchBarProps {
  /** Entity type ID (e.g., 'organizations', 'people') */
  entityType: string;
  /** Display label for the entity type (e.g., 'Organizations', 'People') */
  entityLabel: string;
  /** Optional custom placeholder text */
  placeholder?: string;
  /** Optional callback when search is triggered */
  onSearch?: (term: string) => void;
  /** Optional className for styling */
  className?: string;
}

/**
 * SearchBar component for the Knowledge Explorer.
 *
 * Features:
 * - Debounced URL updates (300ms delay)
 * - Enter key for immediate search
 * - Escape key to clear and blur
 * - Clear button (X) to reset search
 * - Entity-type-aware placeholder text
 * - Accessible with proper ARIA attributes
 */
export function SearchBar({
  entityType,
  entityLabel,
  placeholder,
  onSearch,
  className,
}: SearchBarProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const pathname = usePathname();
  const inputRef = useRef<HTMLInputElement>(null);

  // Initialize value from URL query param
  const [value, setValue] = useState(searchParams.get('q') || '');

  // Debounce value for URL updates
  const debouncedValue = useDebounce(value, 300);

  // Track if we need to skip the debounce effect (for immediate Enter key search)
  const skipDebounceRef = useRef(false);

  /**
   * Update URL with search term, preserving other query params
   */
  const updateUrl = useCallback(
    (searchValue: string) => {
      const params = new URLSearchParams(searchParams.toString());
      if (searchValue) {
        params.set('q', searchValue);
      } else {
        params.delete('q');
      }
      // Reset to page 0 when search changes
      params.delete('page');

      const queryString = params.toString();
      const newUrl = queryString ? `${pathname}?${queryString}` : pathname;

      // Use replace to avoid polluting browser history
      router.replace(newUrl);

      // Call optional callback
      onSearch?.(searchValue);
    },
    [pathname, router, searchParams, onSearch]
  );

  // Update URL when debounced value changes
  useEffect(() => {
    // Skip if we just did an immediate search (Enter key)
    if (skipDebounceRef.current) {
      skipDebounceRef.current = false;
      return;
    }
    updateUrl(debouncedValue);
  }, [debouncedValue, updateUrl]);

  // Sync input value with URL when searchParams change (e.g., browser back/forward)
  useEffect(() => {
    const urlValue = searchParams.get('q') || '';
    if (urlValue !== value) {
      setValue(urlValue);
    }
    // Only sync when searchParams change, not when value changes
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  /**
   * Handle clear button click - clears input but keeps focus
   */
  const handleClear = useCallback(() => {
    setValue('');
    updateUrl('');
    inputRef.current?.focus();
  }, [updateUrl]);

  /**
   * Handle keyboard events
   */
  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Escape') {
        // Escape: clear input, remove q param, AND blur input
        setValue('');
        updateUrl('');
        e.currentTarget.blur();
      } else if (e.key === 'Enter') {
        // Enter: immediate search (bypass debounce)
        skipDebounceRef.current = true;
        updateUrl(value);
      }
    },
    [updateUrl, value]
  );

  /**
   * Handle input change
   */
  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setValue(e.target.value);
  }, []);

  // Compute placeholder text
  const placeholderText = placeholder || `Search ${entityLabel.toLowerCase()}...`;

  return (
    <div className={cn('relative', className)} role="search">
      {/* Search icon */}
      <Search
        className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none"
        aria-hidden="true"
      />

      {/* Search input */}
      <Input
        ref={inputRef}
        type="search"
        value={value}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        placeholder={placeholderText}
        className="pl-9 pr-9"
        aria-label={`Search ${entityLabel}`}
      />

      {/* Clear button - only visible when input has value */}
      {value && (
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={handleClear}
          className="absolute right-1 top-1/2 -translate-y-1/2 h-6 w-6 p-0 hover:bg-transparent"
          aria-label="Clear search"
        >
          <X className="h-4 w-4" />
        </Button>
      )}
    </div>
  );
}
