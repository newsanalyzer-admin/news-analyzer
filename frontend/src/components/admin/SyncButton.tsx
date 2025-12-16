'use client';

/**
 * Sync Button Component
 *
 * Button with confirmation dialog for triggering data synchronization.
 */

import { useState } from 'react';
import { Loader2 } from 'lucide-react';
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
import { useMemberSync, useEnrichmentSync } from '@/hooks/useMembers';
import { useCommitteeSync, useMembershipSync } from '@/hooks/useCommittees';
import { useGovernmentOrgSync } from '@/hooks/useGovernmentOrgs';

type SyncType = 'members' | 'committees' | 'memberships' | 'enrichment' | 'gov-orgs';

interface SyncButtonProps {
  type: SyncType;
  title: string;
  description: string;
  warning: string;
}

export function SyncButton({ type, title, description, warning }: SyncButtonProps) {
  const [open, setOpen] = useState(false);
  const { toast } = useToast();

  const memberSync = useMemberSync();
  const committeeSync = useCommitteeSync();
  const membershipSync = useMembershipSync();
  const enrichmentSync = useEnrichmentSync();
  const govOrgSync = useGovernmentOrgSync();

  const getMutation = () => {
    switch (type) {
      case 'members':
        return memberSync;
      case 'committees':
        return committeeSync;
      case 'memberships':
        return membershipSync;
      case 'enrichment':
        return enrichmentSync;
      case 'gov-orgs':
        return govOrgSync;
    }
  };

  const mutation = getMutation();
  const isLoading = mutation.isPending;

  const handleSync = async () => {
    try {
      let resultMessage = `${title} sync has been initiated. Data will update shortly.`;

      switch (type) {
        case 'members':
          await memberSync.mutateAsync();
          break;
        case 'committees':
          await committeeSync.mutateAsync();
          break;
        case 'memberships':
          // Default to 118th Congress
          await membershipSync.mutateAsync(118);
          break;
        case 'enrichment':
          // Default to non-forced enrichment
          await enrichmentSync.mutateAsync(false);
          break;
        case 'gov-orgs': {
          const result = await govOrgSync.mutateAsync();
          resultMessage = `Sync complete: ${result.added} added, ${result.updated} updated, ${result.skipped} skipped`;
          if (result.errors > 0) {
            resultMessage += `, ${result.errors} errors`;
          }
          break;
        }
      }

      toast({
        title: 'Sync triggered successfully',
        description: resultMessage,
        variant: 'success',
      });
      setOpen(false);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
      toast({
        title: 'Sync failed',
        description: errorMessage,
        variant: 'destructive',
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" disabled={isLoading}>
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {title}
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <span className="text-amber-500">&#9888;</span>
            {title}?
          </DialogTitle>
          <DialogDescription className="space-y-3 pt-2">
            <p>{description}</p>
            <ul className="list-disc pl-5 space-y-1 text-sm">
              {warning.split('.').filter(Boolean).map((point, i) => (
                <li key={i}>{point.trim()}</li>
              ))}
            </ul>
            <p className="font-medium">Are you sure you want to proceed?</p>
          </DialogDescription>
        </DialogHeader>
        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={() => setOpen(false)}>
            Cancel
          </Button>
          <Button onClick={handleSync} disabled={isLoading}>
            {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Confirm Sync
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
