'use client';

import { useParams, useRouter } from 'next/navigation';
import { FileText, ArrowLeft, Clock, AlertCircle, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useArticle } from '@/hooks/useArticles';

/**
 * Article detail page - placeholder for future implementation.
 * Will show full article analysis including extracted entities and claims.
 */
export default function ArticleDetailPage() {
  const params = useParams();
  const router = useRouter();
  const articleId = params.id ? Number(params.id) : null;

  const { data: article, isLoading, error } = useArticle(articleId);

  return (
    <div className="container py-8">
      {/* Back button */}
      <Button
        variant="ghost"
        size="sm"
        className="mb-6"
        onClick={() => router.push('/article-analyzer/articles')}
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        Back to Articles
      </Button>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          <span className="ml-2 text-muted-foreground">Loading article...</span>
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <AlertCircle className="h-12 w-12 text-destructive mb-4" />
          <h3 className="font-semibold text-lg mb-2">Failed to load article</h3>
          <p className="text-muted-foreground mb-4">{error.message}</p>
          <Button onClick={() => router.push('/article-analyzer/articles')}>
            Back to Articles
          </Button>
        </div>
      ) : !article ? (
        // Placeholder for when API doesn't exist yet
        <div className="space-y-6">
          {/* Header */}
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 rounded-lg bg-primary/10 text-primary">
              <FileText className="h-6 w-6" />
            </div>
            <div>
              <h1 className="text-2xl font-bold">Article #{articleId}</h1>
              <p className="text-muted-foreground">Article Detail View</p>
            </div>
          </div>

          {/* Coming soon notice */}
          <div className="p-6 rounded-lg border-2 border-dashed border-muted-foreground/25 bg-muted/50">
            <div className="flex items-center gap-3 mb-4">
              <Clock className="h-8 w-8 text-muted-foreground" />
              <div>
                <h2 className="font-semibold text-lg">Article Detail Coming Soon</h2>
                <p className="text-muted-foreground">
                  This feature is part of Phase 4: Bias Detection & Analysis
                </p>
              </div>
            </div>

            <div className="space-y-4 text-sm text-muted-foreground">
              <p>The article detail page will include:</p>
              <ul className="list-disc list-inside space-y-2 ml-4">
                <li>Full article text with entity highlighting</li>
                <li>Extracted entities linked to Knowledge Base records</li>
                <li>Identified claims and their verification status</li>
                <li>News source information and credibility indicators</li>
                <li>Bias analysis results and indicators</li>
                <li>Related articles and fact-checks</li>
              </ul>
            </div>

            <div className="mt-6 flex gap-3">
              <Badge variant="outline">Phase 4</Badge>
              <Badge variant="secondary">Planned</Badge>
            </div>
          </div>
        </div>
      ) : (
        // Real article data (when API exists)
        <div className="space-y-6">
          {/* Header */}
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 rounded-lg bg-primary/10 text-primary">
              <FileText className="h-6 w-6" />
            </div>
            <div>
              <h1 className="text-2xl font-bold">{article.title}</h1>
              <p className="text-muted-foreground">
                {article.source_name || 'Unknown Source'}
              </p>
            </div>
          </div>

          {/* Article metadata */}
          <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
            {article.published_date && (
              <span>Published: {new Date(article.published_date).toLocaleDateString()}</span>
            )}
            <span>Analyzed: {new Date(article.analyzed_at).toLocaleString()}</span>
            <Badge variant="secondary">{article.entity_count ?? 0} entities</Badge>
          </div>

          {/* Content placeholder */}
          <div className="p-6 rounded-lg border bg-muted/50">
            <p className="text-muted-foreground">
              Full article analysis content will be displayed here in a future update.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
