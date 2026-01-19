'use client';

/**
 * CommitteeMembersDialog Component
 *
 * Modal dialog showing committee members list.
 */

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { useCommitteeMembers, useCommittee } from '@/hooks/useCommittees';
import type { MembershipRole } from '@/types/committee';

interface CommitteeMembersDialogProps {
  committeeCode: string | null;
  isOpen: boolean;
  onClose: () => void;
}

const roleColors: Record<MembershipRole, string> = {
  CHAIR: 'bg-yellow-100 text-yellow-800 border-yellow-200',
  VICE_CHAIR: 'bg-blue-100 text-blue-800 border-blue-200',
  RANKING_MEMBER: 'bg-purple-100 text-purple-800 border-purple-200',
  MEMBER: 'bg-gray-100 text-gray-700 border-gray-200',
  EX_OFFICIO: 'bg-orange-100 text-orange-800 border-orange-200',
};

const roleLabels: Record<MembershipRole, string> = {
  CHAIR: 'Chair',
  VICE_CHAIR: 'Vice Chair',
  RANKING_MEMBER: 'Ranking Member',
  MEMBER: 'Member',
  EX_OFFICIO: 'Ex Officio',
};

export function CommitteeMembersDialog({
  committeeCode,
  isOpen,
  onClose,
}: CommitteeMembersDialogProps) {
  const { data: committee, isLoading: committeeLoading } = useCommittee(
    committeeCode || ''
  );
  const { data: membersData, isLoading: membersLoading } = useCommitteeMembers(
    committeeCode || '',
    { size: 100 }
  );

  const isLoading = committeeLoading || membersLoading;
  const members = membersData?.content || [];

  // Sort members by role importance
  const sortedMembers = [...members].sort((a, b) => {
    const roleOrder: Record<MembershipRole, number> = {
      CHAIR: 0,
      VICE_CHAIR: 1,
      RANKING_MEMBER: 2,
      EX_OFFICIO: 3,
      MEMBER: 4,
    };
    return (roleOrder[a.role] || 99) - (roleOrder[b.role] || 99);
  });

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle>
            {committeeLoading ? (
              <Skeleton className="h-6 w-64" />
            ) : (
              committee?.name || 'Committee Members'
            )}
          </DialogTitle>
          <DialogDescription>
            {isLoading ? (
              <Skeleton className="h-4 w-32" />
            ) : (
              `${members.length} member${members.length !== 1 ? 's' : ''}`
            )}
          </DialogDescription>
        </DialogHeader>

        <div className="flex-1 overflow-y-auto">
          {isLoading ? (
            <MembersSkeleton />
          ) : members.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <div className="text-3xl mb-2">👥</div>
              <p className="text-muted-foreground">No members found</p>
            </div>
          ) : (
            <div className="space-y-2">
              {sortedMembers.map((membership) => (
                <div
                  key={membership.id}
                  className="flex items-center justify-between p-3 border rounded-lg hover:bg-gray-50"
                >
                  <div className="flex-1 min-w-0">
                    <div className="font-medium truncate">
                      {membership.member.firstName} {membership.member.lastName}
                      {membership.member.suffix && ` ${membership.member.suffix}`}
                    </div>
                    <div className="text-sm text-muted-foreground">
                      {membership.member.party && (
                        <span className="mr-2">{membership.member.party}</span>
                      )}
                      {membership.member.state && (
                        <span>{membership.member.state}</span>
                      )}
                    </div>
                  </div>
                  <Badge
                    variant="outline"
                    className={roleColors[membership.role]}
                  >
                    {roleLabels[membership.role]}
                  </Badge>
                </div>
              ))}
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}

function MembersSkeleton() {
  return (
    <div className="space-y-2">
      {Array.from({ length: 6 }).map((_, i) => (
        <div
          key={i}
          className="flex items-center justify-between p-3 border rounded-lg"
        >
          <div className="flex-1">
            <Skeleton className="h-5 w-40 mb-1" />
            <Skeleton className="h-4 w-24" />
          </div>
          <Skeleton className="h-6 w-20" />
        </div>
      ))}
    </div>
  );
}
