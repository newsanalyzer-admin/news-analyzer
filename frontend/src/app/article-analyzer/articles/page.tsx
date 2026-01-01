'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { FileText, AlertCircle, Loader2, Search, ArrowUpDown, Calendar, ExternalLink } from 'lucide-react';
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
import { useArticles, type Article, type ArticleSortField } from '@/hooks/useArticles';

/**
 * Sort options for articles
 */
const sortOptions: { value: ArticleSortField; label: string }[] = [
  { value: 'analyzed_at', label: 'Date Analyzed' },
  { value: 'published_date', label: 'Published Date' },
  { value: 'title', label: 'Title' },
];

/**
 * Format a date string for display
 */
function formatDate(dateString?: string): string {
  if (!dateString) return 'N/A';
  try {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return 'N/A';
  }
}

/**
 * Format a date string with time for display
 */
function formatDateTime(dateString?: string): string {
  if (!dateString) return 'N/A';
  try {
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return 'N/A';
  }
}

/**
 * Get status badge variant and text
 */
function getStatusBadge(status?: string): { variant: 'default' | 'secondary' | 'destructive' | 'outline'; text: string } {
  switch (status) {
    case 'completed':
      return { variant: 'default', text: 'Completed' };
    case 'analyzing':
      return { variant: 'secondary', text: 'Analyzing' };
    case 'pending':
      return { variant: 'outline', text: 'Pending' };
    case 'failed':
      return { variant: 'destructive', text: 'Failed' };
    default:
      return { variant: 'outline', text: 'Unknown' };
  }
}

/**
 * Articles list page - displays analyzed articles from the analysis layer.
 */
export default function ArticlesPage() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [sortField, setSortField] = useState<ArticleSortField>('analyzed_at');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

  // Fetch articles
  const { data: articles, isLoading, error, refetch } = useArticles();

  // Filter and sort articles
  const filteredArticles = useMemo(() => {
    let result = [...articles];

    // Filter by search query
    if (searchQuery.length >= 2) {
      const query = searchQuery.toLowerCase();
      result = result.filter(
        (article) =>
          article.title.toLowerCase().includes(query) ||
          article.source_name?.toLowerCase().includes(query)
      );
    }

    // Sort articles
    result.sort((a, b) => {
      let comparison = 0;
      switch (sortField) {
        case 'analyzed_at':
          comparison = new Date(a.analyzed_at || 0).getTime() - new Date(b.analyzed_at || 0).getTime();
          break;
        case 'published_date':
          comparison = new Date(a.published_date || 0).getTime() - new Date(b.published_date || 0).getTime();
          break;
        case 'title':
          comparison = a.title.localeCompare(b.title);
          break;
      }
      return sortDirection === 'desc' ? -comparison : comparison;
    });

    return result;
  }, [articles, searchQuery, sortField, sortDirection]);

  // Toggle sort direction
  const toggleSortDirection = () => {
    setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
  };

  return (
    <div className="container py-8">
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-2">
          <div className="p-2 rounded-lg bg-primary/10 text-primary">
            <FileText className="h-6 w-6" />
          </div>
          <h1 className="text-2xl font-bold">Analyzed Articles</h1>
        </div>
        <p className="text-muted-foreground">
          Browse articles that have been submitted for analysis. View extracted entities,
          claims, and fact-check results for each article.
        </p>
      </div>

      {/* Info banner */}
      <div className="mb-6 p-4 rounded-lg bg-blue-500/10 border border-blue-500/20">
        <p className="text-sm">
          <strong className="text-blue-600 dark:text-blue-400">Tip:</strong>{' '}
          Click on any article to view its full analysis, including extracted entities and claims.
          Use the{' '}
          <a href="/article-analyzer" className="text-primary hover:underline font-medium">
            Analyze Article
          </a>{' '}
          feature to submit new articles.
        </p>
      </div>

      {/* Stats row */}
      {!isLoading && (
        <div className="mb-6 flex flex-wrap gap-2">
          <Badge variant="outline" className="text-sm">
            Total: {articles.length}
          </Badge>
          {articles.length > 0 && filteredArticles.length !== articles.length && (
            <Badge variant="secondary" className="text-sm">
              Showing: {filteredArticles.length}
            </Badge>
          )}
        </div>
      )}

      {/* Filters row */}
      <div className="mb-6 flex flex-col sm:flex-row gap-4">
        {/* Search */}
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search articles..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>

        {/* Sort field */}
        <div className="flex items-center gap-2">
          <ArrowUpDown className="h-4 w-4 text-muted-foreground" />
          <Select value={sortField} onValueChange={(v) => setSortField(v as ArticleSortField)}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Sort by" />
            </SelectTrigger>
            <SelectContent>
              {sortOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button
            variant="outline"
            size="icon"
            onClick={toggleSortDirection}
            title={sortDirection === 'asc' ? 'Ascending' : 'Descending'}
          >
            <ArrowUpDown className={cn('h-4 w-4', sortDirection === 'asc' && 'rotate-180')} />
          </Button>
        </div>

        {/* Clear filters */}
        {searchQuery && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setSearchQuery('')}
          >
            Clear search
          </Button>
        )}
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          <span className="ml-2 text-muted-foreground">Loading articles...</span>
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <AlertCircle className="h-12 w-12 text-destructive mb-4" />
          <h3 className="font-semibold text-lg mb-2">Failed to load articles</h3>
          <p className="text-muted-foreground mb-4">{error.message}</p>
          <Button onClick={() => refetch()}>Try again</Button>
        </div>
      ) : filteredArticles.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <FileText className="h-12 w-12 text-muted-foreground mb-4" />
          <h3 className="font-semibold text-lg mb-2">No articles found</h3>
          <p className="text-muted-foreground mb-4">
            {searchQuery
              ? `No articles match "${searchQuery}"`
              : 'No articles have been analyzed yet. Submit an article to get started.'}
          </p>
          {!searchQuery && (
            <Button onClick={() => router.push('/article-analyzer')}>
              Analyze an Article
            </Button>
          )}
        </div>
      ) : (
        <div className="border rounded-lg overflow-hidden">
          <table className="w-full">
            <thead className="bg-muted/50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium">Title</th>
                <th className="px-4 py-3 text-left text-sm font-medium hidden md:table-cell">Source</th>
                <th className="px-4 py-3 text-left text-sm font-medium hidden lg:table-cell">Published</th>
                <th className="px-4 py-3 text-left text-sm font-medium">Analyzed</th>
                <th className="px-4 py-3 text-left text-sm font-medium hidden sm:table-cell">Entities</th>
                <th className="px-4 py-3 text-left text-sm font-medium hidden lg:table-cell">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {filteredArticles.map((article) => {
                const statusBadge = getStatusBadge(article.status);
                return (
                  <tr
                    key={article.id}
                    className="hover:bg-muted/50 cursor-pointer transition-colors"
                    onClick={() => router.push(`/article-analyzer/articles/${article.id}`)}
                  >
                    <td className="px-4 py-3">
                      <div className="flex flex-col">
                        <span className="font-medium line-clamp-2">{article.title}</span>
                        {article.source_url && (
                          <a
                            href={article.source_url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-xs text-muted-foreground hover:text-primary flex items-center gap-1 mt-1"
                            onClick={(e) => e.stopPropagation()}
                          >
                            <ExternalLink className="h-3 w-3" />
                            View original
                          </a>
                        )}
                      </div>
                    </td>
                    <td className="px-4 py-3 hidden md:table-cell">
                      <span className="text-sm text-muted-foreground">
                        {article.source_name || 'Unknown'}
                      </span>
                    </td>
                    <td className="px-4 py-3 hidden lg:table-cell">
                      <div className="flex items-center gap-1 text-sm text-muted-foreground">
                        <Calendar className="h-3 w-3" />
                        {formatDate(article.published_date)}
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-sm">{formatDateTime(article.analyzed_at)}</span>
                    </td>
                    <td className="px-4 py-3 hidden sm:table-cell">
                      <Badge variant="secondary">
                        {article.entity_count ?? 0}
                      </Badge>
                    </td>
                    <td className="px-4 py-3 hidden lg:table-cell">
                      <Badge variant={statusBadge.variant}>
                        {statusBadge.text}
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
