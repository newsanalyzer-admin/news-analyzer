'use client';

/**
 * MemberProfile Component
 *
 * Profile header showing member photo, name, party, state, and status.
 */

import { Badge } from '@/components/ui/badge';
import { MemberPhoto } from './MemberPhoto';
import { isCurrentlyServing } from '@/lib/utils/term-helpers';
import type { Person, PositionHolding } from '@/types/member';

interface MemberProfileProps {
  member: Person;
  terms: PositionHolding[];
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

export function MemberProfile({ member, terms }: MemberProfileProps) {
  const fullName = [
    member.firstName,
    member.middleName,
    member.lastName,
    member.suffix,
  ]
    .filter(Boolean)
    .join(' ');

  const isServing = isCurrentlyServing(terms);
  const partyColor = partyColors[member.party || ''] || 'bg-gray-100 text-gray-800 border-gray-200';
  const title = member.chamber ? chamberLabels[member.chamber] || member.chamber : '';

  return (
    <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6">
      {/* Large Photo */}
      <MemberPhoto
        imageUrl={member.imageUrl}
        firstName={member.firstName}
        lastName={member.lastName}
        size={128}
        className="flex-shrink-0"
      />

      {/* Info */}
      <div className="flex-1 text-center sm:text-left">
        <h1 className="text-2xl sm:text-3xl font-bold mb-2">{fullName}</h1>

        {(title || member.state) && (
          <p className="text-lg text-muted-foreground mb-3">
            {title}
            {title && member.state && ' from '}
            {member.state}
          </p>
        )}

        {/* Badges */}
        <div className="flex flex-wrap gap-2 justify-center sm:justify-start">
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
          {isServing ? (
            <Badge variant="outline" className="bg-green-100 text-green-800 border-green-200">
              Currently Serving
            </Badge>
          ) : (
            <Badge variant="outline" className="bg-gray-100 text-gray-600 border-gray-200">
              Former Member
            </Badge>
          )}
        </div>
      </div>
    </div>
  );
}
