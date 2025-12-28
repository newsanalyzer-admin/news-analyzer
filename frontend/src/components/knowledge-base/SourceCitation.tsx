'use client';

import { Info, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import type { SourceInfo } from '@/lib/config/entityTypes';

/**
 * Props for the SourceCitation component
 */
export interface SourceCitationProps {
  /** Source information */
  source: SourceInfo;
  /** Optional className */
  className?: string;
}

/**
 * SourceCitation - Displays a small icon that expands to show source details.
 * Used next to facts that have associated source citations.
 */
export function SourceCitation({ source, className }: SourceCitationProps) {
  // Format the retrieved date
  const formattedDate = source.retrievedAt
    ? formatDate(source.retrievedAt)
    : null;

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className={`h-5 w-5 text-muted-foreground hover:text-foreground ${className || ''}`}
          aria-label={`Source: ${source.name}`}
        >
          <Info className="h-3.5 w-3.5" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-80" align="start">
        <div className="space-y-3">
          <div>
            <h4 className="text-sm font-semibold">Source</h4>
            <p className="text-sm text-muted-foreground">{source.name}</p>
          </div>

          {source.url && (
            <div>
              <h4 className="text-sm font-semibold">Link</h4>
              <a
                href={source.url}
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-primary hover:underline inline-flex items-center gap-1"
              >
                <ExternalLink className="h-3 w-3" />
                View source
              </a>
            </div>
          )}

          {formattedDate && (
            <div>
              <h4 className="text-sm font-semibold">Retrieved</h4>
              <p className="text-sm text-muted-foreground">{formattedDate}</p>
            </div>
          )}

          {source.dataSource && (
            <div>
              <h4 className="text-sm font-semibold">Data Source</h4>
              <p className="text-sm text-muted-foreground">{source.dataSource}</p>
            </div>
          )}
        </div>
      </PopoverContent>
    </Popover>
  );
}

/**
 * Format an ISO date string to a readable format
 */
function formatDate(dateStr: string): string {
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  } catch {
    return dateStr;
  }
}
