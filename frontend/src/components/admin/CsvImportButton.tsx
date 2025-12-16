'use client';

/**
 * CSV Import Button Component
 *
 * Button with file upload dialog for importing government organizations from CSV.
 */

import { useState, useRef } from 'react';
import { Upload, Loader2, FileText, AlertCircle, CheckCircle, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import { useGovOrgCsvImport } from '@/hooks/useGovernmentOrgs';
import type { CsvImportResult, CsvValidationError } from '@/types/government-org';

export function CsvImportButton() {
  const [open, setOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [importResult, setImportResult] = useState<CsvImportResult | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { toast } = useToast();

  const csvImport = useGovOrgCsvImport();
  const isLoading = csvImport.isPending;

  const handleFileSelect = (file: File) => {
    if (file && (file.type === 'text/csv' || file.name.endsWith('.csv'))) {
      setSelectedFile(file);
      setImportResult(null);
    } else {
      toast({
        title: 'Invalid file type',
        description: 'Please select a CSV file',
        variant: 'destructive',
      });
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      handleFileSelect(file);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (file) {
      handleFileSelect(file);
    }
  };

  const handleImport = async () => {
    if (!selectedFile) return;

    try {
      const result = await csvImport.mutateAsync(selectedFile);
      setImportResult(result);

      if (result.success) {
        toast({
          title: 'Import successful',
          description: `Added: ${result.added}, Updated: ${result.updated}, Skipped: ${result.skipped}`,
          variant: 'success',
        });
      } else if (result.validationErrors && result.validationErrors.length > 0) {
        toast({
          title: 'Validation errors',
          description: `${result.validationErrors.length} validation error(s) found`,
          variant: 'destructive',
        });
      } else {
        toast({
          title: 'Import failed',
          description: result.errorMessages?.[0] || 'Unknown error occurred',
          variant: 'destructive',
        });
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
      toast({
        title: 'Import failed',
        description: errorMessage,
        variant: 'destructive',
      });
    }
  };

  const handleClose = () => {
    setOpen(false);
    setSelectedFile(null);
    setImportResult(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const renderValidationErrors = (errors: CsvValidationError[]) => (
    <div className="mt-4 max-h-48 overflow-y-auto rounded border border-destructive/50 bg-destructive/10 p-3">
      <div className="mb-2 flex items-center gap-2 text-sm font-medium text-destructive">
        <AlertCircle className="h-4 w-4" />
        Validation Errors ({errors.length})
      </div>
      <ul className="space-y-1 text-xs">
        {errors.map((err, idx) => (
          <li key={idx} className="text-muted-foreground">
            <span className="font-medium">Line {err.line}</span>: {err.field} - {err.message}
            {err.value && <span className="text-destructive"> (value: &quot;{err.value}&quot;)</span>}
          </li>
        ))}
      </ul>
    </div>
  );

  const renderSuccessResult = (result: CsvImportResult) => (
    <div className="mt-4 rounded border border-green-500/50 bg-green-50 p-3 dark:bg-green-950/20">
      <div className="mb-2 flex items-center gap-2 text-sm font-medium text-green-700 dark:text-green-400">
        <CheckCircle className="h-4 w-4" />
        Import Successful
      </div>
      <div className="grid grid-cols-3 gap-2 text-sm">
        <div className="text-center">
          <div className="text-lg font-bold text-green-600">{result.added}</div>
          <div className="text-xs text-muted-foreground">Added</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-bold text-blue-600">{result.updated}</div>
          <div className="text-xs text-muted-foreground">Updated</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-bold text-gray-500">{result.skipped}</div>
          <div className="text-xs text-muted-foreground">Skipped</div>
        </div>
      </div>
    </div>
  );

  return (
    <Dialog open={open} onOpenChange={(isOpen) => isOpen ? setOpen(true) : handleClose()}>
      <DialogTrigger asChild>
        <Button variant="outline" className="gap-2">
          <Upload className="h-4 w-4" />
          Import CSV
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Import Government Organizations
          </DialogTitle>
          <DialogDescription>
            Upload a CSV file to import Legislative and Judicial branch organizations.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* Drop zone */}
          <div
            className={`cursor-pointer rounded-lg border-2 border-dashed p-6 text-center transition-colors ${
              isDragging
                ? 'border-primary bg-primary/5'
                : 'border-muted-foreground/25 hover:border-primary/50'
            }`}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".csv,text/csv"
              className="hidden"
              onChange={handleInputChange}
            />
            <Upload className="mx-auto h-8 w-8 text-muted-foreground" />
            <p className="mt-2 text-sm text-muted-foreground">
              Drag and drop a CSV file here, or click to browse
            </p>
            {selectedFile && (
              <div className="mt-3 flex items-center justify-center gap-2 text-sm font-medium">
                <FileText className="h-4 w-4" />
                {selectedFile.name}
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-5 w-5 p-0"
                  onClick={(e) => {
                    e.stopPropagation();
                    setSelectedFile(null);
                    setImportResult(null);
                    if (fileInputRef.current) {
                      fileInputRef.current.value = '';
                    }
                  }}
                >
                  <X className="h-3 w-3" />
                </Button>
              </div>
            )}
          </div>

          {/* CSV Format info */}
          <div className="text-xs text-muted-foreground">
            <p className="font-medium">Required columns:</p>
            <p>officialName, branch, orgType</p>
            <p className="mt-1 font-medium">Optional columns:</p>
            <p>acronym, orgLevel, parentId, establishedDate, dissolvedDate, websiteUrl, jurisdictionAreas</p>
          </div>

          {/* Import result */}
          {importResult && (
            importResult.success
              ? renderSuccessResult(importResult)
              : importResult.validationErrors && importResult.validationErrors.length > 0
                ? renderValidationErrors(importResult.validationErrors)
                : importResult.errorMessages && importResult.errorMessages.length > 0 && (
                    <div className="mt-4 rounded border border-destructive/50 bg-destructive/10 p-3">
                      <div className="flex items-center gap-2 text-sm font-medium text-destructive">
                        <AlertCircle className="h-4 w-4" />
                        {importResult.errorMessages[0]}
                      </div>
                    </div>
                  )
          )}
        </div>

        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={handleClose}>
            {importResult?.success ? 'Close' : 'Cancel'}
          </Button>
          {!importResult?.success && (
            <Button
              onClick={handleImport}
              disabled={!selectedFile || isLoading}
            >
              {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Import
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
