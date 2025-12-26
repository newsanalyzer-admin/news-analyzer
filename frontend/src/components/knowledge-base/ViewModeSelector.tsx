'use client';

import { useRouter, usePathname, useSearchParams } from 'next/navigation';
import { List, GitBranch } from 'lucide-react';
import { getEntityTypeConfig, ViewMode } from '@/lib/config/entityTypes';
import { cn } from '@/lib/utils';
import { useCallback, useRef, useMemo } from 'react';

interface ViewModeSelectorProps {
  className?: string;
}

const viewModeConfig: Record<ViewMode, { icon: typeof List; label: string }> = {
  list: { icon: List, label: 'List' },
  hierarchy: { icon: GitBranch, label: 'Hierarchy' },
};

/**
 * Selector component for switching between view modes (List, Hierarchy) in the Knowledge Explorer.
 * Updates URL query param ?view= when a mode is selected.
 * Only shows views that are supported by the current entity type.
 */
export function ViewModeSelector({ className }: ViewModeSelectorProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const buttonsRef = useRef<(HTMLButtonElement | null)[]>([]);

  // Extract current entity type from pathname
  const currentEntityType = pathname.split('/')[2];
  const entityConfig = useMemo(
    () => getEntityTypeConfig(currentEntityType || ''),
    [currentEntityType]
  );

  const supportedViews = entityConfig?.supportedViews ?? [];
  const defaultView = entityConfig?.defaultView ?? 'list';
  const currentView = (searchParams.get('view') as ViewMode) || defaultView;

  const handleSelect = useCallback(
    (viewMode: ViewMode) => {
      const params = new URLSearchParams(searchParams.toString());
      if (viewMode === defaultView) {
        params.delete('view');
      } else {
        params.set('view', viewMode);
      }
      const queryString = params.toString();
      router.replace(`${pathname}${queryString ? `?${queryString}` : ''}`);
    },
    [router, pathname, searchParams, defaultView]
  );

  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent, index: number) => {
      let newIndex = index;

      switch (event.key) {
        case 'ArrowRight':
        case 'ArrowDown':
          event.preventDefault();
          newIndex = (index + 1) % supportedViews.length;
          break;
        case 'ArrowLeft':
        case 'ArrowUp':
          event.preventDefault();
          newIndex = (index - 1 + supportedViews.length) % supportedViews.length;
          break;
        case 'Home':
          event.preventDefault();
          newIndex = 0;
          break;
        case 'End':
          event.preventDefault();
          newIndex = supportedViews.length - 1;
          break;
        default:
          return;
      }

      buttonsRef.current[newIndex]?.focus();
    },
    [supportedViews.length]
  );

  // If entity type not found or only supports one view, don't render
  if (!entityConfig || supportedViews.length <= 1) {
    return null;
  }

  return (
    <div
      className={cn('inline-flex items-center rounded-lg bg-muted p-1', className)}
      role="tablist"
      aria-label="View mode"
    >
      {supportedViews.map((viewMode, index) => {
        const config = viewModeConfig[viewMode];
        const Icon = config.icon;
        const isActive = currentView === viewMode;

        return (
          <button
            key={viewMode}
            ref={(el) => {
              buttonsRef.current[index] = el;
            }}
            onClick={() => handleSelect(viewMode)}
            onKeyDown={(e) => handleKeyDown(e, index)}
            role="tab"
            aria-selected={isActive}
            tabIndex={isActive ? 0 : -1}
            className={cn(
              'inline-flex items-center gap-2 whitespace-nowrap rounded-md px-3 py-1.5',
              'text-sm font-medium transition-all',
              'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
              isActive
                ? 'bg-background text-foreground shadow'
                : 'text-muted-foreground hover:text-foreground hover:bg-background/50'
            )}
          >
            <Icon className="h-4 w-4" aria-hidden="true" />
            <span className="hidden sm:inline">{config.label}</span>
          </button>
        );
      })}
    </div>
  );
}
