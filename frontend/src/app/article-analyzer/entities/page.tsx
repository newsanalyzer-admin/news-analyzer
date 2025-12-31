'use client';

import { useState, useMemo } from 'react';
import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { Database, AlertCircle, Loader2, Search, Filter } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useExtractedEntities, useExtractedEntitiesByType, useExtractedEntitySearch } from '@/hooks/useExtractedEntities';
import { ENTITY_TYPE_METADATA, type EntityType } from '@/types/entity';

/**
 * Entity type filter options
 */
const entityTypeOptions: { value: EntityType | 'all'; label: string }[] = [
  { value: 'all', label: 'All Types' },
  { value: 'person', label: 'Person' },
  { value: 'organization', label: 'Organization' },
  { value: 'government_org', label: 'Government' },
  { value: 'location', label: 'Location' },
  { value: 'event', label: 'Event' },
  { value: 'legislation', label: 'Legislation' },
  { value: 'political_party', label: 'Political Party' },
  { value: 'news_media', label: 'News Media' },
  { value: 'concept', label: 'Concept' },
];

/**
 * Extracted Entities page - displays entities from the analysis layer.
 * These are entities extracted from articles, not authoritative KB data.
 */
export default function ExtractedEntitiesPage() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  // Get type filter from URL
  const typeParam = searchParams.get('type') as EntityType | null;
  const [selectedType, setSelectedType] = useState<EntityType | 'all'>(typeParam || 'all');
  const [searchQuery, setSearchQuery] = useState('');

  // Fetch entities based on type filter
  const allEntitiesQuery = useExtractedEntities();
  const typedEntitiesQuery = useExtractedEntitiesByType(
    selectedType !== 'all' ? selectedType : null
  );
  const searchEntitiesQuery = useExtractedEntitySearch(searchQuery);

  // Determine which data to display
  const { data, isLoading, error, refetch } = useMemo(() => {
    if (searchQuery.length >= 2) {
      return searchEntitiesQuery;
    }
    if (selectedType !== 'all') {
      return typedEntitiesQuery;
    }
    return allEntitiesQuery;
  }, [searchQuery, selectedType, searchEntitiesQuery, typedEntitiesQuery, allEntitiesQuery]);

  // Handle type filter change
  const handleTypeChange = (value: string) => {
    const newType = value as EntityType | 'all';
    setSelectedType(newType);

    // Update URL
    const params = new URLSearchParams(searchParams.toString());
    if (newType === 'all') {
      params.delete('type');
    } else {
      params.set('type', newType);
    }
    const queryString = params.toString();
    router.replace(queryString ? `${pathname}?${queryString}` : pathname);
  };

  // Count entities by type for stats
  const typeCounts = useMemo(() => {
    if (!allEntitiesQuery.data) return {};
    return allEntitiesQuery.data.reduce((acc, entity) => {
      acc[entity.entity_type] = (acc[entity.entity_type] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);
  }, [allEntitiesQuery.data]);

  return (
    <div className="container py-8">
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-2">
          <div className="p-2 rounded-lg bg-primary/10 text-primary">
            <Database className="h-6 w-6" />
          </div>
          <h1 className="text-2xl font-bold">Extracted Entities</h1>
        </div>
        <p className="text-muted-foreground">
          Entities extracted from analyzed articles. These are identified mentions from article text,
          not authoritative reference data.
        </p>
      </div>

      {/* Info banner */}
      <div className="mb-6 p-4 rounded-lg bg-amber-500/10 border border-amber-500/20">
        <p className="text-sm">
          <strong className="text-amber-600 dark:text-amber-400">Note:</strong>{' '}
          Extracted entities may have varying confidence levels and are not verified.
          For authoritative data, visit the{' '}
          <a href="/knowledge-base" className="text-primary hover:underline font-medium">
            Knowledge Base
          </a>.
        </p>
      </div>

      {/* Stats row */}
      {!allEntitiesQuery.isLoading && allEntitiesQuery.data && (
        <div className="mb-6 flex flex-wrap gap-2">
          <Badge variant="outline" className="text-sm">
            Total: {allEntitiesQuery.data.length}
          </Badge>
          {Object.entries(typeCounts).map(([type, count]) => {
            const meta = ENTITY_TYPE_METADATA[type as EntityType];
            return (
              <Badge
                key={type}
                variant="secondary"
                className={cn('text-sm cursor-pointer hover:opacity-80', meta?.bgColor)}
                onClick={() => handleTypeChange(type)}
              >
                {meta?.icon} {meta?.label}: {count}
              </Badge>
            );
          })}
        </div>
      )}

      {/* Filters row */}
      <div className="mb-6 flex flex-col sm:flex-row gap-4">
        {/* Search */}
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search entities..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>

        {/* Type filter */}
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4 text-muted-foreground" />
          <Select value={selectedType} onValueChange={handleTypeChange}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Filter by type" />
            </SelectTrigger>
            <SelectContent>
              {entityTypeOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Clear filters */}
        {(selectedType !== 'all' || searchQuery) && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setSelectedType('all');
              setSearchQuery('');
              router.replace(pathname);
            }}
          >
            Clear filters
          </Button>
        )}
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          <span className="ml-2 text-muted-foreground">Loading entities...</span>
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <AlertCircle className="h-12 w-12 text-destructive mb-4" />
          <h3 className="font-semibold text-lg mb-2">Failed to load entities</h3>
          <p className="text-muted-foreground mb-4">{error.message}</p>
          <Button onClick={() => refetch()}>Try again</Button>
        </div>
      ) : !data || data.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <Database className="h-12 w-12 text-muted-foreground mb-4" />
          <h3 className="font-semibold text-lg mb-2">No entities found</h3>
          <p className="text-muted-foreground">
            {searchQuery
              ? `No entities match "${searchQuery}"`
              : selectedType !== 'all'
              ? `No ${ENTITY_TYPE_METADATA[selectedType]?.label || selectedType} entities found`
              : 'No extracted entities yet. Analyze some articles to extract entities.'}
          </p>
        </div>
      ) : (
        <div className="border rounded-lg overflow-hidden">
          <table className="w-full">
            <thead className="bg-muted/50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium">Name</th>
                <th className="px-4 py-3 text-left text-sm font-medium">Type</th>
                <th className="px-4 py-3 text-left text-sm font-medium hidden md:table-cell">Schema.org Type</th>
                <th className="px-4 py-3 text-left text-sm font-medium hidden lg:table-cell">Confidence</th>
                <th className="px-4 py-3 text-left text-sm font-medium hidden lg:table-cell">Verified</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {data.map((entity) => {
                const meta = ENTITY_TYPE_METADATA[entity.entity_type];
                return (
                  <tr
                    key={entity.id}
                    className="hover:bg-muted/50 cursor-pointer transition-colors"
                    onClick={() => router.push(`/article-analyzer/entities/${entity.id}`)}
                  >
                    <td className="px-4 py-3">
                      <span className="font-medium">{entity.name}</span>
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant="secondary" className={cn('text-xs', meta?.bgColor)}>
                        {meta?.icon} {meta?.label || entity.entity_type}
                      </Badge>
                    </td>
                    <td className="px-4 py-3 hidden md:table-cell text-sm text-muted-foreground">
                      {entity.schema_org_type}
                    </td>
                    <td className="px-4 py-3 hidden lg:table-cell">
                      <div className="flex items-center gap-2">
                        <div className="w-16 h-2 bg-muted rounded-full overflow-hidden">
                          <div
                            className={cn(
                              'h-full rounded-full',
                              entity.confidence_score >= 0.8
                                ? 'bg-green-500'
                                : entity.confidence_score >= 0.5
                                ? 'bg-yellow-500'
                                : 'bg-red-500'
                            )}
                            style={{ width: `${entity.confidence_score * 100}%` }}
                          />
                        </div>
                        <span className="text-xs text-muted-foreground">
                          {Math.round(entity.confidence_score * 100)}%
                        </span>
                      </div>
                    </td>
                    <td className="px-4 py-3 hidden lg:table-cell">
                      <Badge variant={entity.verified ? 'default' : 'outline'}>
                        {entity.verified ? 'Yes' : 'No'}
                      </Badge>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
