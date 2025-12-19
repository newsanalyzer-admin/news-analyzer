'use client';

/**
 * MemberDetailPanel Component
 *
 * Slide-out panel showing detailed member information.
 */

import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { MemberPhoto } from '@/components/congressional/MemberPhoto';
import type { Person } from '@/types/member';

interface MemberDetailPanelProps {
  member: Person;
  onClose: () => void;
}

const partyColors: Record<string, string> = {
  Democrat: 'bg-blue-100 text-blue-800 border-blue-200',
  Democratic: 'bg-blue-100 text-blue-800 border-blue-200',
  Republican: 'bg-red-100 text-red-800 border-red-200',
  Independent: 'bg-purple-100 text-purple-800 border-purple-200',
};

const chamberLabels: Record<string, string> = {
  SENATE: 'Senator',
  HOUSE: 'Representative',
};

export function MemberDetailPanel({ member, onClose }: MemberDetailPanelProps) {
  const fullName = [
    member.firstName,
    member.middleName,
    member.lastName,
    member.suffix,
  ]
    .filter(Boolean)
    .join(' ');

  const partyColor = partyColors[member.party || ''] || 'bg-gray-100 text-gray-800 border-gray-200';
  const title = member.chamber ? chamberLabels[member.chamber] || member.chamber : '';

  // Extract social media from nested object
  const twitterHandle = member.socialMedia?.twitter;
  const facebookHandle = member.socialMedia?.facebook;
  const hasSocialLinks = twitterHandle || facebookHandle;

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/50 z-40"
        onClick={onClose}
      />

      {/* Panel */}
      <div className="fixed right-0 top-0 h-full w-full max-w-lg bg-background shadow-xl z-50 overflow-y-auto">
        <div className="sticky top-0 bg-background border-b p-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold">Member Details</h2>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        <div className="p-6 space-y-6">
          {/* Profile Header */}
          <div className="flex items-center gap-4">
            <MemberPhoto
              imageUrl={member.imageUrl}
              firstName={member.firstName}
              lastName={member.lastName}
              size={80}
            />
            <div>
              <h3 className="text-2xl font-bold">{fullName}</h3>
              {(title || member.state) && (
                <p className="text-muted-foreground">
                  {title}
                  {title && member.state && ' from '}
                  {member.state}
                </p>
              )}
            </div>
          </div>

          {/* Badges */}
          <div className="flex flex-wrap gap-2">
            {member.party && (
              <Badge variant="outline" className={partyColor}>
                {member.party}
              </Badge>
            )}
            {member.chamber && (
              <Badge variant="outline" className="bg-gray-100 text-gray-800 border-gray-200">
                {member.chamber === 'SENATE' ? 'Senate' : 'House'}
              </Badge>
            )}
          </div>

          <hr className="border-border" />

          {/* Basic Information */}
          <div>
            <h4 className="font-semibold mb-3">Basic Information</h4>
            <dl className="space-y-2">
              {member.bioguideId && (
                <div>
                  <dt className="text-sm text-muted-foreground">Bioguide ID</dt>
                  <dd className="font-medium">{member.bioguideId}</dd>
                </div>
              )}
              {member.state && (
                <div>
                  <dt className="text-sm text-muted-foreground">State</dt>
                  <dd className="font-medium">{member.state}</dd>
                </div>
              )}
              {member.party && (
                <div>
                  <dt className="text-sm text-muted-foreground">Party</dt>
                  <dd className="font-medium">{member.party}</dd>
                </div>
              )}
              {member.gender && (
                <div>
                  <dt className="text-sm text-muted-foreground">Gender</dt>
                  <dd className="font-medium">{member.gender}</dd>
                </div>
              )}
            </dl>
          </div>

          {/* Birth Information */}
          {member.birthDate && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">Personal Information</h4>
                <dl className="space-y-2">
                  <div>
                    <dt className="text-sm text-muted-foreground">Birth Date</dt>
                    <dd className="font-medium">{formatDate(member.birthDate)}</dd>
                  </div>
                </dl>
              </div>
            </>
          )}

          {/* Social Media Links */}
          {hasSocialLinks && (
            <>
              <hr className="border-border" />
              <div>
                <h4 className="font-semibold mb-3">Social Media</h4>
                <div className="space-y-2">
                  {twitterHandle && (
                    <a
                      href={`https://twitter.com/${twitterHandle}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block text-sm text-primary hover:underline"
                    >
                      @{twitterHandle} on X/Twitter
                    </a>
                  )}
                  {facebookHandle && (
                    <a
                      href={`https://facebook.com/${facebookHandle}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block text-sm text-primary hover:underline"
                    >
                      Facebook
                    </a>
                  )}
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </>
  );
}

function formatDate(dateStr?: string | null): string {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  } catch {
    return dateStr;
  }
}
