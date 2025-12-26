/// <reference types="vitest/globals" />
import '@testing-library/jest-dom';
import { vi, beforeEach } from 'vitest';

// Mock Next.js navigation hooks
const mockPush = vi.fn();
const mockReplace = vi.fn();
const mockBack = vi.fn();
const mockForward = vi.fn();
const mockRefresh = vi.fn();
const mockPrefetch = vi.fn();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: mockReplace,
    back: mockBack,
    forward: mockForward,
    refresh: mockRefresh,
    prefetch: mockPrefetch,
  }),
  usePathname: () => '/knowledge-base/organizations',
  useSearchParams: () => new URLSearchParams(),
  notFound: vi.fn(),
  redirect: vi.fn(),
}));

// Reset mocks between tests
beforeEach(() => {
  mockPush.mockClear();
  mockReplace.mockClear();
  mockBack.mockClear();
  mockForward.mockClear();
  mockRefresh.mockClear();
  mockPrefetch.mockClear();
});

// Export mocks for use in tests
export { mockPush, mockReplace, mockBack, mockForward, mockRefresh, mockPrefetch };
