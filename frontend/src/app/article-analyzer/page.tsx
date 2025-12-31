'use client';

import Link from 'next/link';
import { FileText, List, Database, ChevronRight, Clock } from 'lucide-react';
import { cn } from '@/lib/utils';

/**
 * Feature card configuration
 */
interface FeatureCardProps {
  title: string;
  description: string;
  href: string;
  icon: React.ReactNode;
  disabled?: boolean;
  comingSoon?: boolean;
}

/**
 * Feature card component for Article Analyzer landing page
 */
function FeatureCard({ title, description, href, icon, disabled, comingSoon }: FeatureCardProps) {
  const CardWrapper = disabled ? 'div' : Link;

  return (
    <CardWrapper
      href={disabled ? undefined! : href}
      className={cn(
        'group flex flex-col p-6 rounded-lg border bg-card',
        disabled
          ? 'opacity-60 cursor-not-allowed'
          : 'hover:border-primary hover:shadow-md transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'
      )}
    >
      <div className="flex items-start justify-between mb-4">
        <div className={cn(
          'p-3 rounded-lg',
          disabled ? 'bg-muted text-muted-foreground' : 'bg-primary/10 text-primary'
        )}>
          {icon}
        </div>
        {comingSoon ? (
          <span className="flex items-center gap-1 text-xs bg-amber-500/10 text-amber-600 dark:text-amber-400 px-2 py-1 rounded">
            <Clock className="h-3 w-3" />
            Coming Soon
          </span>
        ) : (
          <ChevronRight className={cn(
            'h-5 w-5 text-muted-foreground transition-colors',
            !disabled && 'group-hover:text-primary'
          )} />
        )}
      </div>
      <h3 className={cn(
        'text-lg font-semibold mb-2 transition-colors',
        !disabled && 'group-hover:text-primary'
      )}>
        {title}
      </h3>
      <p className="text-sm text-muted-foreground flex-grow">
        {description}
      </p>
    </CardWrapper>
  );
}

/**
 * Article Analyzer landing page.
 * Shows available features for article analysis workflows.
 */
export default function ArticleAnalyzerPage() {
  const features: FeatureCardProps[] = [
    {
      title: 'Analyze Article',
      description: 'Submit a new article URL or text for analysis. Extract entities, detect bias, and identify logical fallacies.',
      href: '/article-analyzer/analyze',
      icon: <FileText className="h-6 w-6" />,
      disabled: true,
      comingSoon: true,
    },
    {
      title: 'Articles',
      description: 'Browse previously analyzed articles. View analysis results, extracted entities, and bias reports.',
      href: '/article-analyzer/articles',
      icon: <List className="h-6 w-6" />,
    },
    {
      title: 'Extracted Entities',
      description: 'Browse all entities extracted from analyzed articles. These are identified mentions, not authoritative reference data.',
      href: '/article-analyzer/entities',
      icon: <Database className="h-6 w-6" />,
    },
  ];

  return (
    <div className="container py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Article Analyzer</h1>
        <p className="text-muted-foreground max-w-2xl">
          Analyze news articles for factual accuracy, logical fallacies, and cognitive biases.
          Submit articles for analysis and browse the results.
        </p>
      </div>

      {/* Info banner */}
      <div className="mb-8 p-4 rounded-lg bg-blue-500/10 border border-blue-500/20">
        <p className="text-sm">
          <strong className="text-blue-600 dark:text-blue-400">Analysis Layer:</strong>{' '}
          This section contains extracted and analyzed data from articles. For authoritative
          reference data about government organizations, people, and committees, visit the{' '}
          <Link href="/knowledge-base" className="text-primary hover:underline font-medium">
            Knowledge Base
          </Link>.
        </p>
      </div>

      {/* Feature Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {features.map((feature) => (
          <FeatureCard key={feature.href} {...feature} />
        ))}
      </div>

      {/* How it works */}
      <div className="mt-12">
        <h2 className="text-xl font-semibold mb-4">How Article Analysis Works</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="p-4 rounded-lg border bg-card">
            <div className="text-2xl font-bold text-primary mb-2">1</div>
            <h3 className="font-medium mb-1">Submit Article</h3>
            <p className="text-sm text-muted-foreground">
              Paste an article URL or text to begin analysis.
            </p>
          </div>
          <div className="p-4 rounded-lg border bg-card">
            <div className="text-2xl font-bold text-primary mb-2">2</div>
            <h3 className="font-medium mb-1">Extract & Analyze</h3>
            <p className="text-sm text-muted-foreground">
              AI identifies entities, claims, and potential biases.
            </p>
          </div>
          <div className="p-4 rounded-lg border bg-card">
            <div className="text-2xl font-bold text-primary mb-2">3</div>
            <h3 className="font-medium mb-1">Review Results</h3>
            <p className="text-sm text-muted-foreground">
              View detailed analysis with evidence citations.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
