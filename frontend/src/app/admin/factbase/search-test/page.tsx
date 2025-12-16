'use client';

/**
 * Search Test Page
 *
 * Test page for verifying SearchImportPanel component functionality.
 * Uses mock data to demonstrate all features without requiring backend.
 */

import { useState } from 'react';
import { SearchImportPanel } from '@/components/admin/SearchImportPanel';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { FilterConfig, ImportResult } from '@/types/search-import';

/**
 * Mock member type for testing
 */
interface MockMember extends Record<string, unknown> {
  id: string;
  name: string;
  party: string;
  state: string;
  chamber: string;
  district?: string;
  phone?: string;
  website?: string;
  startDate: string;
}

/**
 * Mock filter configuration
 */
const mockFilters: FilterConfig[] = [
  {
    id: 'chamber',
    label: 'Chamber',
    type: 'select',
    options: [
      { value: 'house', label: 'House' },
      { value: 'senate', label: 'Senate' },
    ],
    placeholder: 'All chambers',
  },
  {
    id: 'party',
    label: 'Party',
    type: 'multi-select',
    options: [
      { value: 'D', label: 'Democrat' },
      { value: 'R', label: 'Republican' },
      { value: 'I', label: 'Independent' },
    ],
  },
  {
    id: 'state',
    label: 'State',
    type: 'text',
    placeholder: 'e.g., CA, NY',
  },
];

/**
 * Mock existing member for duplicate testing
 */
const existingMember: MockMember = {
  id: 'existing-123',
  name: 'John Smith',
  party: 'D',
  state: 'CA',
  chamber: 'house',
  district: '12',
  phone: '(202) 555-0100',
  website: 'https://johnsmith.house.gov',
  startDate: '2019-01-03',
};

/**
 * Result renderer for mock members
 */
function renderMember(member: MockMember) {
  return (
    <div>
      <div className="flex items-center gap-2">
        <span className="font-medium">{member.name}</span>
        <Badge variant={member.party === 'D' ? 'default' : member.party === 'R' ? 'destructive' : 'secondary'}>
          {member.party}
        </Badge>
      </div>
      <div className="mt-1 text-sm text-muted-foreground">
        {member.chamber === 'house' ? 'Representative' : 'Senator'} from {member.state}
        {member.district && `, District ${member.district}`}
      </div>
    </div>
  );
}

/**
 * SearchTestPage component
 */
export default function SearchTestPage() {
  const [importLog, setImportLog] = useState<string[]>([]);

  const addLog = (message: string) => {
    setImportLog((prev) => [...prev, `[${new Date().toLocaleTimeString()}] ${message}`]);
  };

  // Mock import handler
  const handleImport = async (data: MockMember, source: string): Promise<ImportResult> => {
    addLog(`Importing ${data.name} from ${source}...`);

    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 1000));

    addLog(`Successfully imported ${data.name}`);
    return {
      id: `imported-${Date.now()}`,
      created: true,
      updated: false,
    };
  };

  // Mock duplicate checker
  const checkDuplicate = async (data: MockMember): Promise<string | null> => {
    // Simulate checking for duplicates - return existing ID if name matches
    if (data.name.toLowerCase().includes('smith')) {
      addLog(`Duplicate detected for ${data.name}`);
      return 'existing-123';
    }
    return null;
  };

  // Mock get existing record
  const getExistingRecord = async (id: string): Promise<MockMember | null> => {
    if (id === 'existing-123') {
      return existingMember;
    }
    return null;
  };

  // Mock merge handler
  const handleMerge = async (
    existing: MockMember,
    incoming: MockMember,
    selectedFields: (keyof MockMember)[]
  ): Promise<ImportResult> => {
    addLog(`Merging ${incoming.name} with existing record. Fields: ${selectedFields.join(', ')}`);

    await new Promise((resolve) => setTimeout(resolve, 1000));

    addLog(`Successfully merged record`);
    return {
      id: existing.id,
      created: false,
      updated: true,
    };
  };

  return (
    <div className="container mx-auto max-w-6xl py-8">
      <Card>
        <CardHeader>
          <CardTitle>Search Import Panel Test</CardTitle>
          <CardDescription>
            Test page for the SearchImportPanel component. Uses mock data and handlers.
            Try searching for &quot;Smith&quot; to trigger duplicate detection.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            {/* Search panel */}
            <div className="lg:col-span-2">
              <SearchImportPanel<MockMember>
                apiName="Mock Congress API"
                searchEndpoint="/api/mock/search/members"
                filterConfig={mockFilters}
                resultRenderer={renderMember}
                onImport={handleImport}
                duplicateChecker={checkDuplicate}
                getExistingRecord={getExistingRecord}
                onMerge={handleMerge}
                searchPlaceholder="Search members by name..."
                emptyMessage="No members found. This is a mock API - any search will return empty."
              />
            </div>

            {/* Import log */}
            <div className="space-y-4">
              <h3 className="font-medium">Import Log</h3>
              <div className="max-h-96 overflow-y-auto rounded-md border bg-muted/30 p-3">
                {importLog.length === 0 ? (
                  <p className="text-sm text-muted-foreground">
                    No actions yet. Try importing a record.
                  </p>
                ) : (
                  <div className="space-y-1">
                    {importLog.map((log, index) => (
                      <p key={index} className="text-xs">
                        {log}
                      </p>
                    ))}
                  </div>
                )}
              </div>

              {importLog.length > 0 && (
                <button
                  onClick={() => setImportLog([])}
                  className="text-xs text-muted-foreground hover:text-foreground"
                >
                  Clear log
                </button>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Component documentation */}
      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="text-lg">Component Features</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 text-sm">
          <div>
            <h4 className="font-medium">Search &amp; Filter (AC1, AC2, AC3)</h4>
            <p className="text-muted-foreground">
              Configurable search with dynamic filters (text, select, multi-select, date-range).
            </p>
          </div>
          <div>
            <h4 className="font-medium">Source Attribution (AC4)</h4>
            <p className="text-muted-foreground">
              Each result displays source badge and optional link to original record.
            </p>
          </div>
          <div>
            <h4 className="font-medium">Action Buttons (AC5)</h4>
            <p className="text-muted-foreground">
              Preview, Import, and Compare buttons on each result card.
            </p>
          </div>
          <div>
            <h4 className="font-medium">Preview Modal (AC6)</h4>
            <p className="text-muted-foreground">
              Shows all fields with optional inline editing before import.
            </p>
          </div>
          <div>
            <h4 className="font-medium">Import Flow (AC7)</h4>
            <p className="text-muted-foreground">
              Confirmation dialog with success/error feedback via toast.
            </p>
          </div>
          <div>
            <h4 className="font-medium">Merge Conflict (AC8)</h4>
            <p className="text-muted-foreground">
              Side-by-side comparison with Keep/Replace/Merge options.
            </p>
          </div>
          <div>
            <h4 className="font-medium">State Handling (AC9)</h4>
            <p className="text-muted-foreground">
              Loading, error (with retry), and empty states handled gracefully.
            </p>
          </div>
          <div>
            <h4 className="font-medium">TypeScript Generics (AC10)</h4>
            <p className="text-muted-foreground">
              Fully typed with generics for result type safety.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
