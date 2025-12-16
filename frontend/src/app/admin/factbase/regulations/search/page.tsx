'use client';

/**
 * Federal Register Document Search Page
 *
 * Search and import Federal Register documents.
 * Uses the reusable SearchImportPanel component with Federal Register-specific configuration.
 */

import { useState, useCallback, useEffect } from 'react';
import { FileText, Building2, Calendar, ExternalLink, CheckCircle2 } from 'lucide-react';
import { SearchImportPanel } from '@/components/admin/SearchImportPanel';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { federalRegisterApi } from '@/lib/api/federal-register';
import type { FilterConfig, FilterOption, ImportResult } from '@/types/search-import';
import type {
  FederalRegisterSearchResult,
  FederalRegisterAgency,
} from '@/types/federal-register';
import { DOCUMENT_TYPE_OPTIONS, formatDate } from '@/types/federal-register';

/**
 * Extend FederalRegisterSearchResult to satisfy Record<string, unknown> constraint
 */
interface FederalRegisterDocument extends FederalRegisterSearchResult, Record<string, unknown> {}

/**
 * Federal Register Search Page Component
 */
export default function FederalRegisterSearchPage() {
  const [agencies, setAgencies] = useState<FederalRegisterAgency[]>([]);
  const [agencyOptions, setAgencyOptions] = useState<FilterOption[]>([]);
  const [lastImportResult, setLastImportResult] = useState<ImportResult & {
    linkedAgencies?: number;
    linkedAgencyNames?: string[];
    unmatchedAgencyNames?: string[];
  } | null>(null);

  // Load agencies for the filter dropdown
  useEffect(() => {
    async function loadAgencies() {
      try {
        const agencyList = await federalRegisterApi.getAgencies();
        setAgencies(agencyList);
        setAgencyOptions(
          agencyList
            .filter(a => a.name)
            .sort((a, b) => a.name.localeCompare(b.name))
            .map(a => ({
              value: a.id.toString(),
              label: a.shortName || a.name,
            }))
        );
      } catch (err) {
        console.error('Failed to load agencies:', err);
      }
    }
    loadAgencies();
  }, []);

  /**
   * Filter configuration for Federal Register search
   */
  const filterConfig: FilterConfig[] = [
    {
      id: 'documentType',
      label: 'Document Type',
      type: 'select',
      options: [...DOCUMENT_TYPE_OPTIONS],
      placeholder: 'All types',
    },
    {
      id: 'agencyId',
      label: 'Agency',
      type: 'select',
      options: agencyOptions,
      placeholder: 'All agencies',
    },
    {
      id: 'dateRange',
      label: 'Publication Date',
      type: 'date-range',
      placeholder: 'Select dates',
    },
  ];

  // Handle import
  const handleImport = useCallback(async (
    doc: FederalRegisterDocument,
    source: string
  ): Promise<ImportResult> => {
    try {
      const result = await federalRegisterApi.importDocument({
        documentNumber: doc.documentNumber,
        forceOverwrite: true,
      });

      if (result.error) {
        setLastImportResult(null);
        return {
          id: result.id || '',
          created: false,
          updated: false,
          error: result.error,
        };
      }

      // Store the full result with agency linkage info
      const importResult = {
        id: result.id || '',
        created: result.created,
        updated: result.updated,
        linkedAgencies: result.linkedAgencies,
        linkedAgencyNames: result.linkedAgencyNames,
        unmatchedAgencyNames: result.unmatchedAgencyNames,
      };
      setLastImportResult(importResult);

      return {
        id: result.id || '',
        created: result.created,
        updated: result.updated,
      };
    } catch (err) {
      setLastImportResult(null);
      return {
        id: '',
        created: false,
        updated: false,
        error: err instanceof Error ? err.message : 'Import failed',
      };
    }
  }, []);

  // Check for duplicates
  const checkDuplicate = useCallback(async (doc: FederalRegisterDocument): Promise<string | null> => {
    const result = await federalRegisterApi.checkDocumentExists(doc.documentNumber);
    return result.exists ? result.id : null;
  }, []);

  /**
   * Result renderer for Federal Register documents
   */
  function renderDocument(doc: FederalRegisterDocument) {
    const typeColor = {
      'Rule': 'bg-green-600',
      'Proposed Rule': 'bg-yellow-600',
      'Notice': 'bg-blue-600',
      'Presidential Document': 'bg-purple-600',
    }[doc.documentType] || 'bg-gray-500';

    return (
      <div className="space-y-2">
        <div className="flex items-start gap-2">
          <FileText className="h-5 w-5 mt-0.5 text-muted-foreground shrink-0" />
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="font-medium line-clamp-2">{doc.title}</span>
            </div>
            <div className="flex items-center gap-2 mt-1">
              <Badge className={`${typeColor} text-white text-xs`}>
                {doc.documentType}
              </Badge>
              <span className="text-xs text-muted-foreground">
                {doc.documentNumber}
              </span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-4 text-sm text-muted-foreground">
          <div className="flex items-center gap-1">
            <Calendar className="h-3.5 w-3.5" />
            <span>{formatDate(doc.publicationDate)}</span>
          </div>
          {doc.agencies && doc.agencies.length > 0 && (
            <div className="flex items-center gap-1">
              <Building2 className="h-3.5 w-3.5" />
              <span className="truncate max-w-[200px]">
                {doc.agencies.slice(0, 2).join(', ')}
                {doc.agencies.length > 2 && ` +${doc.agencies.length - 2} more`}
              </span>
            </div>
          )}
        </div>

        {doc.htmlUrl && (
          <a
            href={doc.htmlUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1 text-xs text-primary hover:underline"
            onClick={(e) => e.stopPropagation()}
          >
            <ExternalLink className="h-3 w-3" />
            View on Federal Register
          </a>
        )}
      </div>
    );
  }

  // Custom search endpoint
  const searchEndpoint = '/api/admin/search/federal-register/documents';

  return (
    <div className="container mx-auto max-w-6xl py-6 space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Search Federal Register</CardTitle>
          <CardDescription>
            Search for documents from the Federal Register and import them as regulations.
            Duplicate detection will warn you if a document has already been imported.
            Agencies will be automatically linked to existing government organizations.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Agency linkage result display */}
          {lastImportResult && (lastImportResult.linkedAgencies !== undefined || lastImportResult.unmatchedAgencyNames?.length) && (
            <div className="mb-4 p-3 bg-muted rounded-md space-y-2">
              <div className="flex items-center gap-2 text-sm font-medium">
                <CheckCircle2 className="h-4 w-4 text-green-600" />
                Import completed - Agency Linkage Results
              </div>
              {lastImportResult.linkedAgencies !== undefined && lastImportResult.linkedAgencies > 0 && (
                <div className="text-sm">
                  <span className="text-green-600 font-medium">
                    {lastImportResult.linkedAgencies} {lastImportResult.linkedAgencies === 1 ? 'agency' : 'agencies'} linked:
                  </span>{' '}
                  {lastImportResult.linkedAgencyNames?.join(', ')}
                </div>
              )}
              {lastImportResult.unmatchedAgencyNames && lastImportResult.unmatchedAgencyNames.length > 0 && (
                <div className="text-sm">
                  <span className="text-amber-600 font-medium">
                    {lastImportResult.unmatchedAgencyNames.length} {lastImportResult.unmatchedAgencyNames.length === 1 ? 'agency' : 'agencies'} unmatched:
                  </span>{' '}
                  <span className="text-muted-foreground">
                    {lastImportResult.unmatchedAgencyNames.join(', ')}
                  </span>
                </div>
              )}
            </div>
          )}

          <SearchImportPanel<FederalRegisterDocument>
            apiName="Federal Register"
            searchEndpoint={searchEndpoint}
            filterConfig={filterConfig}
            resultRenderer={renderDocument}
            onImport={handleImport}
            duplicateChecker={checkDuplicate}
            searchPlaceholder="Search documents by keyword..."
            emptyMessage="No documents found. Try adjusting your search criteria."
            pageSize={20}
          />
        </CardContent>
      </Card>
    </div>
  );
}
