'use client';

/**
 * EnrichmentPreviewModal Component
 *
 * Shows a preview of what fields will be added/updated when enriching
 * a Person record from the Legislators Repo data.
 */

import { Loader2, Check, Plus, AlertCircle, X } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import type { LegislatorSearchResult, EnrichmentPreview } from '@/types/legislators-search';

interface EnrichmentPreviewModalProps {
  open: boolean;
  onClose: () => void;
  legislator: LegislatorSearchResult | null;
  preview: EnrichmentPreview | null;
  isLoading: boolean;
  isEnriching: boolean;
  onConfirm: () => void;
}

export function EnrichmentPreviewModal({
  open,
  onClose,
  legislator,
  preview,
  isLoading,
  isEnriching,
  onConfirm,
}: EnrichmentPreviewModalProps) {
  // No local match scenario
  const hasNoLocalMatch = preview && !preview.localMatch;

  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onClose()}>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            Enrichment Preview
            {legislator && (
              <Badge variant="outline" className="ml-2 font-normal">
                {legislator.bioguideId}
              </Badge>
            )}
          </DialogTitle>
          <DialogDescription>
            Preview changes that will be made to the local Person record.
          </DialogDescription>
        </DialogHeader>

        {/* Loading state */}
        {isLoading && (
          <div className="flex h-60 items-center justify-center">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            <span className="ml-2 text-muted-foreground">Loading preview...</span>
          </div>
        )}

        {/* No local match warning */}
        {!isLoading && hasNoLocalMatch && (
          <div className="flex flex-col items-center justify-center py-8 space-y-4">
            <AlertCircle className="h-12 w-12 text-yellow-500" />
            <div className="text-center">
              <p className="font-medium text-lg">No Local Match Found</p>
              <p className="text-sm text-muted-foreground mt-2">
                There is no Person record with bioguideId <code className="bg-muted px-1 rounded">{legislator?.bioguideId}</code> in the local database.
              </p>
              <p className="text-sm text-muted-foreground mt-2">
                To enrich this legislator, you must first import them from Congress.gov.
              </p>
            </div>
          </div>
        )}

        {/* Preview content */}
        {!isLoading && preview && preview.localMatch && (
          <ScrollArea className="flex-1 pr-4">
            <div className="space-y-6">
              {/* Current Person Info */}
              {preview.currentPerson && (
                <div>
                  <h4 className="font-medium mb-2">Local Person Record</h4>
                  <div className="bg-muted rounded-lg p-3 space-y-1 text-sm">
                    <div><span className="text-muted-foreground">ID:</span> {preview.currentPerson.id}</div>
                    <div><span className="text-muted-foreground">Name:</span> {preview.currentPerson.name}</div>
                    {preview.currentPerson.enrichmentSource && (
                      <div>
                        <span className="text-muted-foreground">Last Enrichment:</span>{' '}
                        {preview.currentPerson.enrichmentSource}
                        {preview.currentPerson.enrichmentVersion && (
                          <span className="text-xs ml-1">({preview.currentPerson.enrichmentVersion.slice(0, 7)})</span>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              )}

              <hr className="border-t border-border" />

              {/* Changes Summary */}
              <div>
                <h4 className="font-medium mb-2">Changes Summary</h4>
                <div className="flex gap-4">
                  <div className="flex items-center gap-2">
                    <Plus className="h-4 w-4 text-green-500" />
                    <span>{preview.fieldsToAdd.length} fields to add</span>
                  </div>
                  {preview.fieldsToUpdate.length > 0 && (
                    <div className="flex items-center gap-2">
                      <Check className="h-4 w-4 text-blue-500" />
                      <span>{preview.fieldsToUpdate.length} fields to update</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Fields to Add */}
              {preview.fieldsToAdd.length > 0 && (
                <div>
                  <h4 className="font-medium mb-2 text-green-600 flex items-center gap-1">
                    <Plus className="h-4 w-4" />
                    New Fields
                  </h4>
                  <div className="space-y-2">
                    {preview.fieldsToAdd.map((field) => (
                      <FieldDiff
                        key={field}
                        field={field}
                        currentValue={getNestedValue(preview.currentPerson, field)}
                        newValue={getNestedValue(preview.newData, field)}
                        type="add"
                      />
                    ))}
                  </div>
                </div>
              )}

              {/* Fields to Update */}
              {preview.fieldsToUpdate.length > 0 && (
                <div>
                  <h4 className="font-medium mb-2 text-blue-600 flex items-center gap-1">
                    <Check className="h-4 w-4" />
                    Fields to Update
                  </h4>
                  <div className="space-y-2">
                    {preview.fieldsToUpdate.map((field) => (
                      <FieldDiff
                        key={field}
                        field={field}
                        currentValue={getNestedValue(preview.currentPerson, field)}
                        newValue={getNestedValue(preview.newData, field)}
                        type="update"
                      />
                    ))}
                  </div>
                </div>
              )}

              {/* No changes */}
              {preview.totalChanges === 0 && (
                <div className="text-center py-4 text-muted-foreground">
                  <Check className="h-8 w-8 mx-auto mb-2 text-green-500" />
                  <p>No new data to add. Record is already up to date.</p>
                </div>
              )}
            </div>
          </ScrollArea>
        )}

        <DialogFooter className="pt-4">
          <Button variant="outline" onClick={onClose} disabled={isEnriching}>
            <X className="h-4 w-4 mr-2" />
            Cancel
          </Button>
          {preview && preview.localMatch && preview.totalChanges > 0 && (
            <Button onClick={onConfirm} disabled={isEnriching}>
              {isEnriching ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Enriching...
                </>
              ) : (
                <>
                  <Check className="h-4 w-4 mr-2" />
                  Apply {preview.totalChanges} Changes
                </>
              )}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/**
 * Single field diff display
 */
function FieldDiff({
  field,
  currentValue,
  newValue,
  type,
}: {
  field: string;
  currentValue: unknown;
  newValue: unknown;
  type: 'add' | 'update';
}) {
  const formattedNew = formatValue(newValue);
  const formattedCurrent = formatValue(currentValue);

  return (
    <div className="border rounded-lg p-3 text-sm">
      <div className="font-medium text-muted-foreground mb-1">{field}</div>
      <div className="space-y-1">
        {type === 'update' && formattedCurrent && (
          <div className="flex items-center gap-2">
            <Badge variant="outline" className="text-red-500 border-red-300">Current</Badge>
            <span className="line-through text-muted-foreground">{formattedCurrent}</span>
          </div>
        )}
        <div className="flex items-center gap-2">
          <Badge variant="outline" className={type === 'add' ? 'text-green-500 border-green-300' : 'text-blue-500 border-blue-300'}>
            {type === 'add' ? 'New' : 'Updated'}
          </Badge>
          <span>{formattedNew}</span>
        </div>
      </div>
    </div>
  );
}

/**
 * Get nested value from object using dot notation
 */
function getNestedValue(obj: unknown, path: string): unknown {
  if (!obj || typeof obj !== 'object') return undefined;

  const parts = path.split('.');
  let current: unknown = obj;

  for (const part of parts) {
    if (current === null || current === undefined) return undefined;
    if (typeof current !== 'object') return undefined;
    current = (current as Record<string, unknown>)[part];
  }

  return current;
}

/**
 * Format value for display
 */
function formatValue(value: unknown): string {
  if (value === null || value === undefined) return '';
  if (Array.isArray(value)) return value.join(', ');
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}
