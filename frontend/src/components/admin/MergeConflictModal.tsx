'use client';

/**
 * MergeConflictModal Component
 *
 * Side-by-side comparison of existing and incoming records.
 * Allows user to keep existing, replace with new, or selectively merge fields.
 */

import { useState, useEffect } from 'react';
import { GitCompare, Loader2, Check, ArrowRight, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import type { MergeConflictModalProps, MergeResolution } from '@/types/search-import';

/**
 * Format field name for display (camelCase to Title Case)
 */
function formatFieldName(field: string): string {
  return field
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (str) => str.toUpperCase())
    .trim();
}

/**
 * Default value formatter
 */
function formatValue(value: unknown): string {
  if (value === null || value === undefined) return '-';
  if (typeof value === 'boolean') return value ? 'Yes' : 'No';
  if (value instanceof Date) return value.toLocaleDateString();
  if (Array.isArray(value)) return value.join(', ');
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}

/**
 * MergeConflictModal component
 */
export function MergeConflictModal<T extends Record<string, unknown>>({
  open,
  onClose,
  conflictData,
  fieldRenderer,
  onResolve,
  isMerging = false,
}: MergeConflictModalProps<T>) {
  const [selectedResolution, setSelectedResolution] = useState<MergeResolution>('keep-existing');
  const [selectedFields, setSelectedFields] = useState<Set<keyof T>>(new Set());
  const [activeTab, setActiveTab] = useState<'compare' | 'merge'>('compare');

  // Reset state when conflict data changes
  useEffect(() => {
    if (conflictData) {
      setSelectedResolution('keep-existing');
      setSelectedFields(new Set());
      setActiveTab('compare');
    }
  }, [conflictData]);

  if (!conflictData) return null;

  const { existing, incoming, source, differingFields } = conflictData;
  const allFields = Object.keys(existing) as (keyof T)[];

  const renderValue = (field: keyof T, value: T[keyof T]): string => {
    if (fieldRenderer) {
      const result = fieldRenderer(field, value);
      return typeof result === 'string' ? result : formatValue(value);
    }
    return formatValue(value);
  };

  const isDifferent = (field: keyof T): boolean => {
    return differingFields.includes(field);
  };

  const toggleFieldSelection = (field: keyof T) => {
    const newSelected = new Set(selectedFields);
    if (newSelected.has(field)) {
      newSelected.delete(field);
    } else {
      newSelected.add(field);
    }
    setSelectedFields(newSelected);
  };

  const handleResolve = async () => {
    if (selectedResolution === 'merge-selected') {
      await onResolve(selectedResolution, Array.from(selectedFields));
    } else {
      await onResolve(selectedResolution);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onClose()}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-4xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <GitCompare className="h-5 w-5" />
            Merge Conflict
          </DialogTitle>
          <DialogDescription>
            A matching record already exists. Compare and choose how to resolve.
          </DialogDescription>
        </DialogHeader>

        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'compare' | 'merge')}>
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="compare">Side-by-Side</TabsTrigger>
            <TabsTrigger value="merge">Merge Fields</TabsTrigger>
          </TabsList>

          {/* Side-by-side comparison */}
          <TabsContent value="compare" className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              {/* Existing record */}
              <div>
                <div className="mb-2 flex items-center gap-2">
                  <Badge variant="outline">Existing Record</Badge>
                </div>
                <div className="space-y-2">
                  {allFields.map((field) => (
                    <div
                      key={String(field)}
                      className={`rounded-md border p-2 ${
                        isDifferent(field) ? 'border-amber-500/50 bg-amber-50/50 dark:bg-amber-950/20' : ''
                      }`}
                    >
                      <div className="text-xs font-medium text-muted-foreground">
                        {formatFieldName(String(field))}
                      </div>
                      <div className="text-sm">{renderValue(field, existing[field])}</div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Incoming record */}
              <div>
                <div className="mb-2 flex items-center gap-2">
                  <Badge variant="secondary">From {source}</Badge>
                </div>
                <div className="space-y-2">
                  {allFields.map((field) => (
                    <div
                      key={String(field)}
                      className={`rounded-md border p-2 ${
                        isDifferent(field) ? 'border-blue-500/50 bg-blue-50/50 dark:bg-blue-950/20' : ''
                      }`}
                    >
                      <div className="text-xs font-medium text-muted-foreground">
                        {formatFieldName(String(field))}
                      </div>
                      <div className="text-sm">{renderValue(field, incoming[field])}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {differingFields.length > 0 && (
              <p className="text-xs text-muted-foreground">
                Fields with differences are highlighted.
                {differingFields.length} field(s) differ between records.
              </p>
            )}
          </TabsContent>

          {/* Merge fields selection */}
          <TabsContent value="merge" className="space-y-4">
            <p className="text-sm text-muted-foreground">
              Select which fields to take from the incoming record:
            </p>

            <div className="space-y-2">
              {differingFields.map((field) => {
                const isSelected = selectedFields.has(field);
                return (
                  <button
                    key={String(field)}
                    type="button"
                    onClick={() => toggleFieldSelection(field)}
                    className={`w-full rounded-md border p-3 text-left transition-colors ${
                      isSelected
                        ? 'border-primary bg-primary/5'
                        : 'border-input hover:bg-muted/50'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="font-medium">{formatFieldName(String(field))}</div>
                      {isSelected && <Check className="h-4 w-4 text-primary" />}
                    </div>
                    <div className="mt-2 grid grid-cols-2 gap-4 text-sm">
                      <div>
                        <span className="text-xs text-muted-foreground">Current: </span>
                        <span className={isSelected ? 'line-through opacity-50' : ''}>
                          {renderValue(field, existing[field])}
                        </span>
                      </div>
                      <div>
                        <span className="text-xs text-muted-foreground">New: </span>
                        <span className={isSelected ? 'font-medium text-primary' : ''}>
                          {renderValue(field, incoming[field])}
                        </span>
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>

            {selectedFields.size > 0 && (
              <p className="text-sm text-muted-foreground">
                {selectedFields.size} field(s) selected for merge.
              </p>
            )}
          </TabsContent>
        </Tabs>

        {/* Resolution options */}
        <div className="space-y-3 border-t pt-4">
          <p className="text-sm font-medium">Choose resolution:</p>
          <div className="grid grid-cols-3 gap-2">
            <Button
              variant={selectedResolution === 'keep-existing' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSelectedResolution('keep-existing')}
              className="justify-start"
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Keep Existing
            </Button>
            <Button
              variant={selectedResolution === 'replace-with-new' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSelectedResolution('replace-with-new')}
              className="justify-start"
            >
              <ArrowRight className="mr-2 h-4 w-4" />
              Replace with New
            </Button>
            <Button
              variant={selectedResolution === 'merge-selected' ? 'default' : 'outline'}
              size="sm"
              onClick={() => {
                setSelectedResolution('merge-selected');
                setActiveTab('merge');
              }}
              className="justify-start"
              disabled={differingFields.length === 0}
            >
              <GitCompare className="mr-2 h-4 w-4" />
              Merge Selected
            </Button>
          </div>
        </div>

        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={onClose} disabled={isMerging}>
            Cancel
          </Button>
          <Button
            onClick={handleResolve}
            disabled={
              isMerging ||
              (selectedResolution === 'merge-selected' && selectedFields.size === 0)
            }
          >
            {isMerging ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Resolving...
              </>
            ) : (
              'Apply Resolution'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
