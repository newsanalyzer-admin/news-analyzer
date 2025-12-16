/**
 * Pagination Types
 *
 * Spring Data pagination response types for API responses.
 */

import { z } from 'zod';

/**
 * Spring Data Page response
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // 0-indexed current page
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * Zod schema for Page response (generic factory)
 */
export function createPageSchema<T extends z.ZodTypeAny>(itemSchema: T) {
  return z.object({
    content: z.array(itemSchema),
    totalElements: z.number(),
    totalPages: z.number(),
    size: z.number(),
    number: z.number(),
    first: z.boolean(),
    last: z.boolean(),
    empty: z.boolean(),
  });
}

/**
 * Pagination request parameters
 */
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}
