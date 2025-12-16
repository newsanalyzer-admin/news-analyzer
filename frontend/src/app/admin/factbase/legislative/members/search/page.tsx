'use client';

/**
 * Congress.gov Member Search Page
 *
 * Search and import Congressional member data from Congress.gov API.
 * Uses the reusable SearchImportPanel component with Congress-specific configuration.
 */

import { useState, useCallback } from 'react';
import { AlertTriangle } from 'lucide-react';
import { SearchImportPanel } from '@/components/admin/SearchImportPanel';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { congressSearchApi } from '@/lib/api/congress-search';
import { membersApi } from '@/lib/api/members';
import { US_STATES } from '@/lib/constants/states';
import type { FilterConfig, ImportResult } from '@/types/search-import';
import type {
  CongressMemberSearchResult,
  CongressSearchResponse,
} from '@/types/congress-search';
import {
  PARTY_OPTIONS,
  CHAMBER_OPTIONS,
  CONGRESS_OPTIONS,
} from '@/types/congress-search';

/**
 * Extend CongressMemberSearchResult to satisfy Record<string, unknown> constraint
 */
interface CongressMember extends CongressMemberSearchResult, Record<string, unknown> {}

/**
 * Filter configuration for Congress.gov search
 */
const filterConfig: FilterConfig[] = [
  {
    id: 'state',
    label: 'State',
    type: 'select',
    options: US_STATES,
    placeholder: 'All states',
  },
  {
    id: 'party',
    label: 'Party',
    type: 'select',
    options: [...PARTY_OPTIONS],
    placeholder: 'All parties',
  },
  {
    id: 'chamber',
    label: 'Chamber',
    type: 'select',
    options: [...CHAMBER_OPTIONS],
    placeholder: 'All chambers',
  },
  {
    id: 'congress',
    label: 'Congress',
    type: 'select',
    options: [...CONGRESS_OPTIONS],
    placeholder: 'Current',
  },
];

/**
 * Result renderer for Congress members
 */
function renderMember(member: CongressMember) {
  const partyColor = {
    D: 'bg-blue-500',
    R: 'bg-red-500',
    I: 'bg-purple-500',
    L: 'bg-yellow-500',
  }[member.party] || 'bg-gray-500';

  return (
    <div className="flex items-start gap-3">
      {member.imageUrl && (
        <img
          src={member.imageUrl}
          alt={member.name}
          className="h-12 w-12 rounded-full object-cover"
        />
      )}
      <div className="flex-1">
        <div className="flex items-center gap-2">
          <span className="font-medium">{member.name}</span>
          <Badge className={`${partyColor} text-white`}>{member.party}</Badge>
          {member.currentMember && (
            <Badge variant="outline" className="text-green-600 border-green-600">
              Current
            </Badge>
          )}
        </div>
        <div className="mt-1 text-sm text-muted-foreground">
          {member.chamber === 'senate' ? 'Senator' : 'Representative'} from {member.state}
          {member.district && `, District ${member.district}`}
        </div>
      </div>
    </div>
  );
}

/**
 * Congress.gov Search Page Component
 */
export default function CongressSearchPage() {
  const [rateLimitRemaining, setRateLimitRemaining] = useState<number | null>(null);
  const [rateLimitWarning, setRateLimitWarning] = useState(false);

  // Handle import
  const handleImport = useCallback(async (
    member: CongressMember,
    source: string
  ): Promise<ImportResult> => {
    try {
      const result = await congressSearchApi.importMember({
        bioguideId: member.bioguideId,
        forceOverwrite: true,
      });

      if (result.error) {
        return {
          id: result.id || '',
          created: false,
          updated: false,
          error: result.error,
        };
      }

      return {
        id: result.id || '',
        created: result.created,
        updated: result.updated,
      };
    } catch (err) {
      return {
        id: '',
        created: false,
        updated: false,
        error: err instanceof Error ? err.message : 'Import failed',
      };
    }
  }, []);

  // Check for duplicates
  const checkDuplicate = useCallback(async (member: CongressMember): Promise<string | null> => {
    const result = await congressSearchApi.checkMemberExists(member.bioguideId);
    return result.exists ? result.id : null;
  }, []);

  // Get existing record for comparison
  const getExistingRecord = useCallback(async (id: string): Promise<CongressMember | null> => {
    try {
      // The id here is the local Person UUID, but we need to get by bioguideId
      // The SearchImportPanel passes duplicateId which is the Person UUID
      // We need to fetch the person and convert to CongressMember format
      const person = await membersApi.getByBioguideId(id);
      if (!person) return null;

      return {
        bioguideId: person.bioguideId,
        name: `${person.lastName}, ${person.firstName}`,
        firstName: person.firstName,
        lastName: person.lastName,
        state: person.state || '',
        party: person.party || '',
        chamber: person.chamber?.toLowerCase() || '',
        currentMember: true,
        imageUrl: person.imageUrl || undefined,
      } as CongressMember;
    } catch {
      return null;
    }
  }, []);

  // Handle merge
  const handleMerge = useCallback(async (
    existing: CongressMember,
    incoming: CongressMember,
    selectedFields: (keyof CongressMember)[]
  ): Promise<ImportResult> => {
    // For now, just do a full import/overwrite
    return handleImport(incoming, 'Congress.gov');
  }, [handleImport]);

  // Custom search endpoint that updates rate limit state
  const searchEndpoint = '/api/admin/search/congress/members';

  return (
    <div className="container mx-auto max-w-6xl py-6 space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Search Congress.gov</CardTitle>
          <CardDescription>
            Search for Congressional members from Congress.gov and import them into the local database.
            Duplicate detection will warn you if a member already exists.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Rate limit warning */}
          {rateLimitWarning && rateLimitRemaining !== null && rateLimitRemaining < 100 && (
            <div className="mb-4 p-3 bg-destructive/10 border border-destructive rounded-md flex items-center gap-2 text-destructive">
              <AlertTriangle className="h-4 w-4" />
              <span>
                Rate limit warning: Only {rateLimitRemaining} requests remaining this hour.
                Search carefully to avoid hitting the limit.
              </span>
            </div>
          )}

          {/* Rate limit indicator */}
          {rateLimitRemaining !== null && (
            <div className="mb-4 text-sm text-muted-foreground">
              API Rate Limit: {rateLimitRemaining} requests remaining
              {rateLimitRemaining < 100 && (
                <Badge variant="destructive" className="ml-2">Low</Badge>
              )}
            </div>
          )}

          <SearchImportPanel<CongressMember>
            apiName="Congress.gov"
            searchEndpoint={searchEndpoint}
            filterConfig={filterConfig}
            resultRenderer={renderMember}
            onImport={handleImport}
            duplicateChecker={checkDuplicate}
            getExistingRecord={getExistingRecord}
            onMerge={handleMerge}
            searchPlaceholder="Search members by name..."
            emptyMessage="No members found. Try adjusting your search criteria."
            pageSize={20}
          />
        </CardContent>
      </Card>
    </div>
  );
}
