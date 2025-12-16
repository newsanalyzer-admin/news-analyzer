/**
 * useDebounce Hook
 *
 * Debounces a value by a specified delay.
 */

import { useState, useEffect } from 'react';

/**
 * Hook to debounce a value by a specified delay
 * @param value - The value to debounce
 * @param delay - Delay in milliseconds (use 300 for search input per AC)
 */
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);

  return debouncedValue;
}
