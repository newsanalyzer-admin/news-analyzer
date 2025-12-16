'use client';

/**
 * Member Detail Page
 *
 * Displays comprehensive details about a Congressional member.
 */

import { Suspense } from 'react';
import { notFound } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { useMember, useMemberTerms, useMemberCommittees } from '@/hooks/useMembers';
import { MemberProfile } from '@/components/congressional/MemberProfile';
import { MemberSocialMedia } from '@/components/congressional/MemberSocialMedia';
import { TermTimeline } from '@/components/congressional/TermTimeline';
import { MemberCommittees } from '@/components/congressional/MemberCommittees';
import { ExternalIds } from '@/components/congressional/ExternalIds';

interface MemberDetailPageProps {
  params: { bioguideId: string };
}

function MemberDetailContent({ bioguideId }: { bioguideId: string }) {
  const { data: member, isLoading, error } = useMember(bioguideId);
  const { data: terms, isLoading: termsLoading } = useMemberTerms(bioguideId);
  const { data: committeesData, isLoading: committeesLoading } = useMemberCommittees(bioguideId, { size: 100 });

  if (isLoading) {
    return <MemberDetailSkeleton />;
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] text-center">
        <div className="text-6xl mb-4">&#9888;&#65039;</div>
        <h1 className="text-2xl font-bold mb-2">Error Loading Member</h1>
        <p className="text-muted-foreground mb-6 max-w-md">
          We encountered an error while loading this member&apos;s information.
        </p>
        <Button onClick={() => window.location.reload()}>Try Again</Button>
      </div>
    );
  }

  if (!member) {
    notFound();
  }

  // terms is directly PositionHolding[] (not a Page)
  const termsList = terms || [];
  const committees = committeesData?.content || [];

  return (
    <div className="space-y-6">
      {/* Back Navigation */}
      <Link
        href="/members"
        className="inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Members
      </Link>

      {/* Profile Header */}
      <MemberProfile member={member} terms={termsList} />

      {/* Tabbed Content */}
      <Tabs defaultValue="overview" className="w-full">
        <TabsList className="grid w-full grid-cols-4 lg:w-auto lg:inline-grid">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="terms">Terms</TabsTrigger>
          <TabsTrigger value="committees">Committees</TabsTrigger>
          <TabsTrigger value="links">External Links</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="mt-6">
          <div className="grid gap-6 md:grid-cols-2">
            <div>
              <h3 className="text-lg font-semibold mb-4">Social Media</h3>
              <MemberSocialMedia socialMedia={member.socialMedia} />
            </div>
            <div>
              <h3 className="text-lg font-semibold mb-4">Quick Facts</h3>
              <QuickFacts member={member} termsCount={termsList.length} committeesCount={committees.length} />
            </div>
          </div>
        </TabsContent>

        <TabsContent value="terms" className="mt-6">
          <h3 className="text-lg font-semibold mb-4">Term History</h3>
          <TermTimeline
            terms={termsList}
            person={member}
            isLoading={termsLoading}
          />
        </TabsContent>

        <TabsContent value="committees" className="mt-6">
          <h3 className="text-lg font-semibold mb-4">Committee Assignments</h3>
          <MemberCommittees
            committees={committees}
            isLoading={committeesLoading}
          />
        </TabsContent>

        <TabsContent value="links" className="mt-6">
          <h3 className="text-lg font-semibold mb-4">External Resources</h3>
          <ExternalIds externalIds={member.externalIds} bioguideId={member.bioguideId} />
        </TabsContent>
      </Tabs>
    </div>
  );
}

function QuickFacts({
  member,
  termsCount,
  committeesCount,
}: {
  member: { party?: string; state?: string; chamber?: string; birthDate?: string };
  termsCount: number;
  committeesCount: number;
}) {
  return (
    <dl className="space-y-3">
      {member.party && (
        <div className="flex justify-between">
          <dt className="text-muted-foreground">Party</dt>
          <dd className="font-medium">{member.party}</dd>
        </div>
      )}
      {member.state && (
        <div className="flex justify-between">
          <dt className="text-muted-foreground">State</dt>
          <dd className="font-medium">{member.state}</dd>
        </div>
      )}
      {member.chamber && (
        <div className="flex justify-between">
          <dt className="text-muted-foreground">Chamber</dt>
          <dd className="font-medium">{member.chamber === 'SENATE' ? 'Senate' : 'House'}</dd>
        </div>
      )}
      <div className="flex justify-between">
        <dt className="text-muted-foreground">Total Terms</dt>
        <dd className="font-medium">{termsCount}</dd>
      </div>
      <div className="flex justify-between">
        <dt className="text-muted-foreground">Committee Assignments</dt>
        <dd className="font-medium">{committeesCount}</dd>
      </div>
      {member.birthDate && (
        <div className="flex justify-between">
          <dt className="text-muted-foreground">Born</dt>
          <dd className="font-medium">{new Date(member.birthDate).toLocaleDateString()}</dd>
        </div>
      )}
    </dl>
  );
}

function MemberDetailSkeleton() {
  return (
    <div className="space-y-6">
      <Skeleton className="h-5 w-32" />
      <div className="flex flex-col sm:flex-row gap-6">
        <Skeleton className="h-32 w-32 rounded-full" />
        <div className="flex-1 space-y-3">
          <Skeleton className="h-8 w-64" />
          <Skeleton className="h-5 w-48" />
          <div className="flex gap-2">
            <Skeleton className="h-6 w-20" />
            <Skeleton className="h-6 w-20" />
          </div>
        </div>
      </div>
      <Skeleton className="h-10 w-full max-w-md" />
      <div className="space-y-4">
        <Skeleton className="h-6 w-32" />
        <Skeleton className="h-40 w-full" />
      </div>
    </div>
  );
}

export default function MemberDetailPage({ params }: MemberDetailPageProps) {
  return (
    <main className="container mx-auto py-8 px-4">
      <Suspense fallback={<MemberDetailSkeleton />}>
        <MemberDetailContent bioguideId={params.bioguideId} />
      </Suspense>
    </main>
  );
}
