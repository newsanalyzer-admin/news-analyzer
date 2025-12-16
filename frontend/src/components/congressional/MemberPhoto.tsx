'use client';

/**
 * MemberPhoto Component
 *
 * Displays a member's photo with initials fallback.
 */

import Image from 'next/image';
import { useState } from 'react';

interface MemberPhotoProps {
  imageUrl?: string | null;
  firstName: string;
  lastName: string;
  size?: number;
  className?: string;
}

function getInitials(firstName: string, lastName: string): string {
  return `${firstName[0] || ''}${lastName[0] || ''}`.toUpperCase();
}

export function MemberPhoto({
  imageUrl,
  firstName,
  lastName,
  size = 40,
  className = '',
}: MemberPhotoProps) {
  const [hasError, setHasError] = useState(false);
  const initials = getInitials(firstName, lastName);

  if (!imageUrl || hasError) {
    return (
      <div
        className={`flex items-center justify-center rounded-full bg-gray-200 text-gray-600 font-medium ${className}`}
        style={{ width: size, height: size, fontSize: size * 0.4 }}
      >
        {initials}
      </div>
    );
  }

  return (
    <Image
      src={imageUrl}
      alt={`${firstName} ${lastName}`}
      width={size}
      height={size}
      className={`rounded-full object-cover ${className}`}
      onError={() => setHasError(true)}
    />
  );
}
