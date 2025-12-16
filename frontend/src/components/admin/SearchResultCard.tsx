'use client';

/**
 * SearchResultCard Component
 *
 * Displays a single search result with source attribution and action buttons.
 * Supports Preview, Import, and Compare (for duplicates) actions.
 */

import { Eye, Download, GitCompare, Loader2, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { SearchResultCardProps } from '@/types/search-import';

/**
 * SearchResultCard component
 * Displays a search result with actions
 */
export function SearchResultCard<T>({
  result,
  resultRenderer,
  onPreview,
  onImport,
  onCompare,
  isImporting = false,
}: SearchResultCardProps<T>) {
  const hasDuplicate = !!result.duplicateId;

  return (
    <Card className="transition-shadow hover:shadow-md">
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-4">
          {/* Result content */}
          <div className="min-w-0 flex-1">
            {/* Source badge */}
            <div className="mb-2 flex items-center gap-2">
              <Badge variant="secondary" className="text-xs">
                From {result.source}
              </Badge>
              {result.sourceUrl && (
                <a
                  href={result.sourceUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-xs text-muted-foreground hover:text-primary"
                >
                  <ExternalLink className="h-3 w-3" />
                </a>
              )}
              {hasDuplicate && (
                <Badge variant="outline" className="text-xs text-amber-600">
                  Duplicate detected
                </Badge>
              )}
            </div>

            {/* Custom rendered content */}
            <div>{resultRenderer(result.data)}</div>
          </div>

          {/* Action buttons */}
          <div className="flex shrink-0 gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPreview(result)}
              disabled={isImporting}
            >
              <Eye className="mr-1 h-3 w-3" />
              Preview
            </Button>

            {hasDuplicate && onCompare ? (
              <Button
                variant="secondary"
                size="sm"
                onClick={() => onCompare(result)}
                disabled={isImporting}
              >
                <GitCompare className="mr-1 h-3 w-3" />
                Compare
              </Button>
            ) : (
              <Button
                variant="default"
                size="sm"
                onClick={() => onImport(result)}
                disabled={isImporting}
              >
                {isImporting ? (
                  <>
                    <Loader2 className="mr-1 h-3 w-3 animate-spin" />
                    Importing...
                  </>
                ) : (
                  <>
                    <Download className="mr-1 h-3 w-3" />
                    Import
                  </>
                )}
              </Button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
