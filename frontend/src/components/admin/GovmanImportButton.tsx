'use client';

/**
 * GOVMAN Import Button Component
 *
 * Button with file upload dialog for importing government organizations from GOVMAN XML.
 * Includes preview mode, confirmation dialog, and expandable error details.
 */

import { useState, useRef } from 'react';
import {
  Upload,
  Loader2,
  FileUp,
  AlertCircle,
  CheckCircle,
  X,
  ChevronDown,
  ChevronUp,
  FileWarning,
  Info,
} from 'lucide-react';
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
import { useGovmanImport } from '@/hooks/useGovmanImport';
import type { GovmanImportResult } from '@/types/govman';

type ImportStage = 'select' | 'preview' | 'confirm' | 'importing' | 'result';

export function GovmanImportButton() {
  const [open, setOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [importResult, setImportResult] = useState<GovmanImportResult | null>(null);
  const [stage, setStage] = useState<ImportStage>('select');
  const [showErrors, setShowErrors] = useState(false);
  const [xmlValid, setXmlValid] = useState<boolean | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { toast } = useToast();

  const govmanImport = useGovmanImport();
  const isLoading = govmanImport.isPending;

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  const validateXmlFile = async (file: File): Promise<boolean> => {
    try {
      const text = await file.slice(0, 100).text();
      return text.trim().startsWith('<?xml') || text.trim().startsWith('<');
    } catch {
      return false;
    }
  };

  const handleFileSelect = async (file: File) => {
    if (file && file.name.toLowerCase().endsWith('.xml')) {
      setSelectedFile(file);
      setImportResult(null);
      setStage('select');
      setShowErrors(false);

      // Validate XML structure
      const isValid = await validateXmlFile(file);
      setXmlValid(isValid);
    } else {
      toast({
        title: 'Invalid file type',
        description: 'Please select an XML file',
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

  const handlePreview = () => {
    if (selectedFile) {
      setStage('preview');
    }
  };

  const handleConfirm = () => {
    setStage('confirm');
  };

  const handleImport = async () => {
    if (!selectedFile) return;

    setStage('importing');

    try {
      const result = await govmanImport.mutateAsync(selectedFile);
      setImportResult(result);
      setStage('result');

      const hasErrors = result.errors > 0;
      const successCount = result.imported + result.updated;

      if (successCount > 0 && !hasErrors) {
        toast({
          title: 'Import successful',
          description: `Imported: ${result.imported}, Updated: ${result.updated}, Skipped: ${result.skipped}`,
          variant: 'success',
        });
      } else if (successCount > 0 && hasErrors) {
        toast({
          title: 'Import completed with errors',
          description: `${successCount} records processed, ${result.errors} error(s)`,
          variant: 'default',
        });
      } else if (hasErrors) {
        toast({
          title: 'Import failed',
          description: `${result.errors} error(s) occurred`,
          variant: 'destructive',
        });
      }
    } catch (error) {
      setStage('select');
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
    setStage('select');
    setShowErrors(false);
    setXmlValid(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const clearFile = (e: React.MouseEvent) => {
    e.stopPropagation();
    setSelectedFile(null);
    setImportResult(null);
    setStage('select');
    setXmlValid(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const renderPreview = () => {
    if (!selectedFile) return null;

    return (
      <div className="mt-4 rounded border border-blue-500/50 bg-blue-50 p-4 dark:bg-blue-950/20">
        <div className="mb-3 flex items-center gap-2 text-sm font-medium text-blue-700 dark:text-blue-400">
          <Info className="h-4 w-4" />
          File Preview
        </div>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">File name:</span>
            <span className="font-medium">{selectedFile.name}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">File size:</span>
            <span className="font-medium">{formatFileSize(selectedFile.size)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">XML valid:</span>
            <span className={`font-medium ${xmlValid ? 'text-green-600' : 'text-red-600'}`}>
              {xmlValid ? 'Yes' : 'No'}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Status:</span>
            <span className="font-medium text-green-600">Ready to import</span>
          </div>
        </div>
        {selectedFile.size > 15 * 1024 * 1024 && (
          <div className="mt-3 flex items-center gap-2 text-xs text-destructive">
            <AlertCircle className="h-3 w-3" />
            File exceeds 15MB limit
          </div>
        )}
      </div>
    );
  };

  const renderConfirmation = () => (
    <div className="mt-4 rounded border border-amber-500/50 bg-amber-50 p-4 dark:bg-amber-950/20">
      <div className="mb-3 flex items-center gap-2 text-sm font-medium text-amber-700 dark:text-amber-400">
        <FileWarning className="h-4 w-4" />
        Confirm Import
      </div>
      <p className="text-sm text-muted-foreground">
        You are about to import government organizations from:
      </p>
      <p className="mt-1 font-medium">{selectedFile?.name}</p>
      <p className="mt-3 text-sm text-muted-foreground">
        This will create new records and update existing ones. Duplicate records will be skipped.
      </p>
      <p className="mt-2 text-xs text-amber-600 dark:text-amber-400">
        This action may take several seconds for large files.
      </p>
    </div>
  );

  const renderErrorDetails = (errors: string[]) => (
    <div className="mt-4">
      <button
        onClick={() => setShowErrors(!showErrors)}
        className="flex w-full items-center justify-between rounded border border-destructive/50 bg-destructive/10 p-3 text-sm font-medium text-destructive hover:bg-destructive/20"
      >
        <div className="flex items-center gap-2">
          <AlertCircle className="h-4 w-4" />
          Error Details ({errors.length})
        </div>
        {showErrors ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
      </button>
      {showErrors && (
        <div className="mt-2 max-h-48 overflow-y-auto rounded border border-destructive/30 bg-destructive/5 p-3">
          <ul className="space-y-1 text-xs">
            {errors.map((err, idx) => (
              <li key={idx} className="text-muted-foreground">
                {err}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );

  const renderSuccessResult = (result: GovmanImportResult) => (
    <div className="mt-4 rounded border border-green-500/50 bg-green-50 p-4 dark:bg-green-950/20">
      <div className="mb-3 flex items-center gap-2 text-sm font-medium text-green-700 dark:text-green-400">
        <CheckCircle className="h-4 w-4" />
        Import Complete
      </div>
      <div className="grid grid-cols-5 gap-2 text-sm">
        <div className="text-center">
          <div className="text-lg font-bold text-muted-foreground">{result.total}</div>
          <div className="text-xs text-muted-foreground">Total</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-bold text-green-600">{result.imported}</div>
          <div className="text-xs text-muted-foreground">Imported</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-bold text-blue-600">{result.updated}</div>
          <div className="text-xs text-muted-foreground">Updated</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-bold text-gray-500">{result.skipped}</div>
          <div className="text-xs text-muted-foreground">Skipped</div>
        </div>
        <div className="text-center">
          <div className={`text-lg font-bold ${result.errors > 0 ? 'text-red-600' : 'text-gray-400'}`}>
            {result.errors}
          </div>
          <div className="text-xs text-muted-foreground">Errors</div>
        </div>
      </div>
      {result.durationSeconds !== null && (
        <div className="mt-3 text-center text-xs text-muted-foreground">
          Completed in {result.durationSeconds} second{result.durationSeconds !== 1 ? 's' : ''}
          {' • '}
          {result.successRate.toFixed(1)}% success rate
        </div>
      )}
      {result.errors > 0 && result.errorDetails.length > 0 && renderErrorDetails(result.errorDetails)}
    </div>
  );

  const renderFooter = () => {
    if (stage === 'result') {
      return (
        <DialogFooter>
          <Button onClick={handleClose}>Close</Button>
        </DialogFooter>
      );
    }

    if (stage === 'importing') {
      return (
        <DialogFooter>
          <Button disabled>
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            Importing...
          </Button>
        </DialogFooter>
      );
    }

    if (stage === 'confirm') {
      return (
        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={() => setStage('preview')}>
            Back
          </Button>
          <Button onClick={handleImport}>
            Confirm Import
          </Button>
        </DialogFooter>
      );
    }

    if (stage === 'preview') {
      return (
        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={() => setStage('select')}>
            Back
          </Button>
          <Button
            onClick={handleConfirm}
            disabled={xmlValid !== true || (selectedFile !== null && selectedFile.size > 15 * 1024 * 1024)}
          >
            Continue to Import
          </Button>
        </DialogFooter>
      );
    }

    // Default: select stage
    return (
      <DialogFooter className="gap-2 sm:gap-0">
        <Button variant="outline" onClick={handleClose}>
          Cancel
        </Button>
        <Button
          variant="outline"
          onClick={handlePreview}
          disabled={!selectedFile}
        >
          Preview
        </Button>
        <Button
          onClick={handleConfirm}
          disabled={!selectedFile || xmlValid !== true}
        >
          Import
        </Button>
      </DialogFooter>
    );
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => (isOpen ? setOpen(true) : handleClose())}>
      <DialogTrigger asChild>
        <Button className="gap-2">
          <FileUp className="h-4 w-4" />
          Import GOVMAN XML
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <FileUp className="h-5 w-5" />
            Import GOVMAN XML
          </DialogTitle>
          <DialogDescription>
            Upload a Government Manual XML file to import official government organization structure.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* Drop zone - always visible unless importing */}
          {stage !== 'importing' && stage !== 'result' && (
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
                accept=".xml,application/xml,text/xml"
                className="hidden"
                onChange={handleInputChange}
              />
              <Upload className="mx-auto h-8 w-8 text-muted-foreground" />
              <p className="mt-2 text-sm text-muted-foreground">
                Drag and drop an XML file here, or click to browse
              </p>
              {selectedFile && (
                <div className="mt-3 flex items-center justify-center gap-2 text-sm font-medium">
                  <FileUp className="h-4 w-4" />
                  {selectedFile.name}
                  <span className="text-muted-foreground">({formatFileSize(selectedFile.size)})</span>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-5 w-5 p-0"
                    onClick={clearFile}
                  >
                    <X className="h-3 w-3" />
                  </Button>
                </div>
              )}
            </div>
          )}

          {/* Info about GOVMAN format */}
          {stage === 'select' && !selectedFile && (
            <div className="text-xs text-muted-foreground">
              <p className="font-medium">Supported format:</p>
              <p>Government Manual XML from GovInfo (GOVMAN)</p>
              <p className="mt-1">Maximum file size: 15MB</p>
            </div>
          )}

          {/* Preview panel */}
          {stage === 'preview' && renderPreview()}

          {/* Confirmation panel */}
          {stage === 'confirm' && renderConfirmation()}

          {/* Importing state */}
          {stage === 'importing' && (
            <div className="flex flex-col items-center py-8">
              <Loader2 className="h-12 w-12 animate-spin text-primary" />
              <p className="mt-4 text-sm text-muted-foreground">
                Importing government organizations...
              </p>
              <p className="mt-1 text-xs text-muted-foreground">
                This may take several seconds for large files.
              </p>
            </div>
          )}

          {/* Result panel */}
          {stage === 'result' && importResult && renderSuccessResult(importResult)}
        </div>

        {renderFooter()}
      </DialogContent>
    </Dialog>
  );
}
