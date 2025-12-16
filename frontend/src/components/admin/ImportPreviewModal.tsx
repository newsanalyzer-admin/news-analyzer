'use client';

/**
 * ImportPreviewModal Component
 *
 * Modal for previewing and optionally editing data before import.
 * Shows all fields from external API with editable fields highlighted.
 */

import { useState, useEffect } from 'react';
import { Download, Loader2, Edit2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import type { ImportPreviewModalProps } from '@/types/search-import';

/**
 * Default field renderer - converts values to display strings
 */
function defaultFieldRenderer<T>(field: keyof T, value: T[keyof T]): string {
  if (value === null || value === undefined) return '-';
  if (typeof value === 'boolean') return value ? 'Yes' : 'No';
  if (value instanceof Date) return value.toLocaleDateString();
  if (Array.isArray(value)) return value.join(', ');
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}

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
 * ImportPreviewModal component
 */
export function ImportPreviewModal<T extends Record<string, unknown>>({
  open,
  onClose,
  previewData,
  fieldRenderer = defaultFieldRenderer,
  editableFields = [],
  onConfirmImport,
  isImporting = false,
}: ImportPreviewModalProps<T>) {
  const [editedData, setEditedData] = useState<T | null>(null);
  const [editingField, setEditingField] = useState<keyof T | null>(null);
  const [showConfirm, setShowConfirm] = useState(false);

  // Reset state when preview data changes
  useEffect(() => {
    if (previewData) {
      setEditedData({ ...previewData.data });
      setEditingField(null);
      setShowConfirm(false);
    }
  }, [previewData]);

  if (!previewData || !editedData) return null;

  const fields = Object.keys(previewData.data) as (keyof T)[];
  const isEditable = (field: keyof T) => editableFields.includes(field);

  const handleFieldChange = (field: keyof T, value: string) => {
    setEditedData((prev) => {
      if (!prev) return prev;
      return { ...prev, [field]: value };
    });
  };

  const handleImportClick = () => {
    setShowConfirm(true);
  };

  const handleConfirm = async () => {
    if (editedData) {
      await onConfirmImport(editedData);
      setShowConfirm(false);
    }
  };

  const handleCancel = () => {
    setShowConfirm(false);
    setEditingField(null);
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onClose()}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Download className="h-5 w-5" />
            Import Preview
          </DialogTitle>
          <DialogDescription>
            Review the data from {previewData.source} before importing.
            {editableFields.length > 0 && (
              <> Fields marked with <Edit2 className="inline h-3 w-3" /> can be edited.</>
            )}
          </DialogDescription>
        </DialogHeader>

        {/* Confirmation warning */}
        {showConfirm && (
          <div className="rounded-md border border-amber-500/50 bg-amber-50 p-4 dark:bg-amber-950/20">
            <div className="flex items-center gap-2 text-sm font-medium text-amber-700 dark:text-amber-400">
              <AlertCircle className="h-4 w-4" />
              Confirm Import
            </div>
            <p className="mt-2 text-sm text-muted-foreground">
              Are you sure you want to import this record? This action cannot be undone.
            </p>
          </div>
        )}

        {/* Field list */}
        {!showConfirm && (
          <div className="space-y-3 py-4">
            <div className="mb-2 flex items-center gap-2">
              <Badge variant="secondary" className="text-xs">
                Source: {previewData.source}
              </Badge>
            </div>

            <div className="space-y-2">
              {fields.map((field) => {
                const editable = isEditable(field);
                const isEditing = editingField === field;
                const value = editedData[field];
                const displayValue = fieldRenderer(field, value);

                return (
                  <div
                    key={String(field)}
                    className="flex items-start gap-4 rounded-md border p-3"
                  >
                    <div className="min-w-[140px] shrink-0">
                      <span className="text-sm font-medium">
                        {formatFieldName(String(field))}
                      </span>
                      {editable && (
                        <Edit2 className="ml-1 inline h-3 w-3 text-muted-foreground" />
                      )}
                    </div>
                    <div className="flex-1">
                      {editable && isEditing ? (
                        <div className="flex gap-2">
                          <Input
                            value={String(value ?? '')}
                            onChange={(e) => handleFieldChange(field, e.target.value)}
                            className="h-8"
                            autoFocus
                          />
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setEditingField(null)}
                          >
                            Done
                          </Button>
                        </div>
                      ) : (
                        <div className="flex items-center gap-2">
                          <span className="text-sm text-muted-foreground">
                            {displayValue}
                          </span>
                          {editable && (
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-6 px-2"
                              onClick={() => setEditingField(field)}
                            >
                              Edit
                            </Button>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        <DialogFooter className="gap-2 sm:gap-0">
          {showConfirm ? (
            <>
              <Button variant="outline" onClick={handleCancel} disabled={isImporting}>
                Cancel
              </Button>
              <Button onClick={handleConfirm} disabled={isImporting}>
                {isImporting ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Importing...
                  </>
                ) : (
                  'Confirm Import'
                )}
              </Button>
            </>
          ) : (
            <>
              <Button variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button onClick={handleImportClick}>
                <Download className="mr-2 h-4 w-4" />
                Import
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
