'use client';

import { useRouter, usePathname } from 'next/navigation';
import { entityTypes, getEntityTypeConfig } from '@/lib/config/entityTypes';
import { cn } from '@/lib/utils';
import { useCallback, useRef } from 'react';

interface EntityTypeSelectorProps {
  className?: string;
  onNavigate?: () => void;
}

/**
 * Selector component for switching between entity types in the Knowledge Explorer.
 * Updates URL to /knowledge-base/:entityType when a type is selected.
 */
export function EntityTypeSelector({ className, onNavigate }: EntityTypeSelectorProps) {
  const router = useRouter();
  const pathname = usePathname();
  const buttonsRef = useRef<(HTMLButtonElement | null)[]>([]);

  // Extract current entity type from pathname
  const currentEntityType = pathname.split('/')[2] || entityTypes[0].id;

  const handleSelect = useCallback(
    (entityTypeId: string) => {
      router.push(`/knowledge-base/${entityTypeId}`);
      onNavigate?.();
    },
    [router, onNavigate]
  );

  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent, index: number) => {
      let newIndex = index;

      switch (event.key) {
        case 'ArrowRight':
        case 'ArrowDown':
          event.preventDefault();
          newIndex = (index + 1) % entityTypes.length;
          break;
        case 'ArrowLeft':
        case 'ArrowUp':
          event.preventDefault();
          newIndex = (index - 1 + entityTypes.length) % entityTypes.length;
          break;
        case 'Home':
          event.preventDefault();
          newIndex = 0;
          break;
        case 'End':
          event.preventDefault();
          newIndex = entityTypes.length - 1;
          break;
        default:
          return;
      }

      buttonsRef.current[newIndex]?.focus();
    },
    []
  );

  return (
    <div
      className={cn('inline-flex items-center rounded-lg bg-muted p-1', className)}
      role="tablist"
      aria-label="Entity type"
    >
      {entityTypes.map((entityType, index) => {
        const Icon = entityType.icon;
        const isActive = currentEntityType === entityType.id;

        return (
          <button
            key={entityType.id}
            ref={(el) => {
              buttonsRef.current[index] = el;
            }}
            onClick={() => handleSelect(entityType.id)}
            onKeyDown={(e) => handleKeyDown(e, index)}
            role="tab"
            aria-selected={isActive}
            aria-controls={`panel-${entityType.id}`}
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
            <span>{entityType.label}</span>
          </button>
        );
      })}
    </div>
  );
}
