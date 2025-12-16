# Story FB-1-UI.4: Member Detail Page

## Status

**Done**

## Story

**As a** fact-checker or researcher,
**I want** to view comprehensive details about a Congressional member including their term history, committee assignments, and social media presence,
**so that** I can verify claims about their service and access their official communication channels.

## Acceptance Criteria

1. A new `/members/[bioguideId]` dynamic page displays full member profile
2. Profile header shows: Large photo, full name, party, state, chamber, current status
3. Contact/social media section displays Twitter, Facebook, and YouTube links with icons
4. Term history timeline shows all Congressional terms with dates and positions
5. Committee assignments section lists current committee memberships with roles
6. External IDs section shows cross-reference identifiers (FEC, GovTrack, etc.)
7. "Back to Members" navigation link returns to the members list
8. Loading state uses shadcn/ui Skeleton for profile layout
9. Error state (member not found) displays 404-style message
10. Page uses shadcn/ui Tabs to organize sections (Overview, Terms, Committees)
11. Social media links open in new tab with appropriate icons
12. Term timeline is visually clear with current term highlighted
13. Page is responsive and works on mobile devices
14. Existing pages have no regression

## Tasks / Subtasks

- [x] **Task 1: Create dynamic route page** (AC: 1)
  - [x] Create `app/members/[bioguideId]/page.tsx`
  - [x] Create `app/members/[bioguideId]/loading.tsx`
  - [x] Create `app/members/[bioguideId]/not-found.tsx`
  - [x] Create `app/members/[bioguideId]/error.tsx`

- [x] **Task 2: Create MemberProfile component** (AC: 2)
  - [x] Create `components/congressional/MemberProfile.tsx`
  - [x] Display large member photo (128px+) with fallback
  - [x] Show full name with any suffix (Jr., III, etc.)
  - [x] Display party badge with color
  - [x] Show state and chamber
  - [x] Indicate if currently serving

- [x] **Task 3: Create MemberSocialMedia component** (AC: 3, 11)
  - [x] Create `components/congressional/MemberSocialMedia.tsx`
  - [x] Display Twitter/X link with icon (if available)
  - [x] Display Facebook link with icon (if available)
  - [x] Display YouTube link with icon (if available)
  - [x] Use Lucide icons for social platforms
  - [x] Links open in new tab with `rel="noopener noreferrer"`
  - [x] Handle gracefully when social media not available

- [x] **Task 4: Create TermTimeline component** (AC: 4, 12)
  - [x] Create `components/congressional/TermTimeline.tsx`
  - [x] Display all terms from `useMemberTerms()` hook
  - [x] Show term label (e.g., "118th Congress (2023-present)")
  - [x] Indicate chamber for each term (Senator/Representative)
  - [x] Highlight current term visually
  - [x] Sort by date (most recent first)
  - [x] Show state and district (for House) per term

- [x] **Task 5: Create MemberCommittees component** (AC: 5)
  - [x] Create `components/congressional/MemberCommittees.tsx`
  - [x] Display current committee assignments from `useMemberCommittees()`
  - [x] Show committee name and role (Member, Chair, Ranking Member, etc.)
  - [x] Group by chamber if member serves in both (rare but possible)
  - [x] Link to committee page if available

- [x] **Task 6: Create ExternalIds component** (AC: 6)
  - [x] Create `components/congressional/ExternalIds.tsx`
  - [x] Display FEC ID(s) with link to FEC website
  - [x] Display GovTrack ID with link
  - [x] Display OpenSecrets ID with link
  - [x] Display VoteSmart ID with link
  - [x] Display Wikipedia link if available
  - [x] Handle missing IDs gracefully

- [x] **Task 7: Implement tabbed layout** (AC: 10)
  - [x] Use shadcn/ui Tabs component
  - [x] Tab 1: Overview (profile, social media)
  - [x] Tab 2: Term History (timeline)
  - [x] Tab 3: Committees (assignments)
  - [x] Optional Tab 4: External Links (IDs)

- [x] **Task 8: Implement navigation** (AC: 7)
  - [x] Add "← Back to Members" link at top using `<Link href="/members">`
  - [x] Note: Filter preservation is out of scope - use simple `/members` link

- [x] **Task 9: Loading and error states** (AC: 8, 9)
  - [x] Create skeleton layout matching profile structure
  - [x] Handle 404 when member not found
  - [x] Handle API error with retry option

- [x] **Task 10: Responsive design** (AC: 13)
  - [x] Test on mobile viewport sizes
  - [x] Stack profile photo and info on mobile
  - [x] Ensure tabs work on touch devices
  - [x] Timeline readable on narrow screens

- [x] **Task 11: Testing** (AC: 14)
  - [x] Test with real member BioGuide IDs
  - [x] Test with member with full social media
  - [x] Test with member missing social media
  - [x] Test with member with long term history
  - [x] Verify existing pages work

## Dev Notes

### Dependencies

This story depends on:
- FB-1-UI.1: Types, API client, hooks (`useMember`, `useMemberTerms`, `useMemberCommittees`)
- FB-1-UI.2: Navigation patterns, **reuse `MemberPhoto.tsx`** for photo with fallback
- shadcn/ui: Card, Tabs, Badge, Skeleton, Button

### File Structure

```
frontend/src/
├── app/
│   └── members/
│       └── [bioguideId]/
│           ├── page.tsx          # Member detail page
│           ├── loading.tsx       # Loading skeleton
│           ├── not-found.tsx     # 404 page
│           └── error.tsx         # Error boundary
├── components/
│   └── congressional/
│       ├── MemberProfile.tsx     # Header with photo (reuse MemberPhoto from FB-1-UI.2)
│       ├── MemberSocialMedia.tsx # Social links
│       ├── TermTimeline.tsx      # Term history
│       ├── MemberCommittees.tsx  # Committee list
│       └── ExternalIds.tsx       # Cross-reference links
└── lib/
    └── utils/
        ├── social-links.ts       # buildSocialUrls helper
        ├── external-links.ts     # externalIdUrls constants
        └── term-helpers.ts       # isCurrentTerm, isCurrentlyServing, formatTermDisplay
```

### Social Media Data Structure

```typescript
// From Person.socialMedia (JSONB)
interface SocialMedia {
  twitter?: string;      // Handle without @
  facebook?: string;     // Page name or ID
  youtube?: string;      // Channel name
  youtube_id?: string;   // Channel ID
}

// Build URLs
const socialUrls = {
  twitter: (handle: string) => `https://twitter.com/${handle}`,
  facebook: (page: string) => `https://facebook.com/${page}`,
  youtube: (id: string) => `https://youtube.com/channel/${id}`,
};
```

### Social Media Icons and URL Building

Use Lucide icons (Twitter icon is still valid - X rebrand icon not available in Lucide):
```tsx
import { Twitter, Facebook, Youtube, ExternalLink } from 'lucide-react';
```

**YouTube URL Logic:** The enrichment data provides both `youtube` (channel name/handle) and `youtube_id` (channel ID). Use this priority:
```typescript
// Place in: lib/utils/social-links.ts
export function buildSocialUrls(socialMedia: SocialMedia | null | undefined) {
  if (!socialMedia) return {};

  return {
    twitter: socialMedia.twitter
      ? `https://twitter.com/${socialMedia.twitter}`
      : undefined,
    facebook: socialMedia.facebook
      ? `https://facebook.com/${socialMedia.facebook}`
      : undefined,
    // Prefer youtube_id (channel ID) over youtube (handle) for reliable URLs
    youtube: socialMedia.youtube_id
      ? `https://youtube.com/channel/${socialMedia.youtube_id}`
      : socialMedia.youtube
        ? `https://youtube.com/@${socialMedia.youtube}`
        : undefined,
  };
}
```

### External IDs Data Structure

The `Person.externalIds` field is a JSONB object populated by the legislators-repo enrichment. Expected structure:

```typescript
// From Person.externalIds (JSONB) - populated by LegislatorsEnrichmentService
interface ExternalIds {
  bioguide?: string;        // Same as bioguideId (redundant but present)
  fec?: string[];           // FEC candidate IDs (array - members may have multiple)
  govtrack?: number;        // GovTrack numeric ID
  opensecrets?: string;     // OpenSecrets CID
  votesmart?: number;       // VoteSmart candidate ID
  wikipedia?: string;       // Wikipedia article title (e.g., "Bernie_Sanders")
  ballotpedia?: string;     // Ballotpedia article name
  icpsr?: number;           // ICPSR ID for academic research
  thomas?: string;          // Legacy THOMAS ID
  lis?: string;             // LIS ID (Senate only)
  cspan?: number;           // C-SPAN ID
  maplight?: number;        // MapLight ID
  house_history?: number;   // House History ID
}

// Helper to safely extract IDs
function getExternalId<T>(externalIds: Record<string, unknown> | undefined, key: string): T | undefined {
  return externalIds?.[key] as T | undefined;
}
```

### External ID Links

```typescript
// Place in: lib/utils/external-links.ts
export const externalIdUrls = {
  fec: (id: string) => `https://www.fec.gov/data/candidate/${id}/`,
  govtrack: (id: number) => `https://www.govtrack.us/congress/members/${id}`,
  opensecrets: (id: string) => `https://www.opensecrets.org/members-of-congress/summary?cid=${id}`,
  votesmart: (id: number) => `https://votesmart.org/candidate/${id}`,
  wikipedia: (title: string) => `https://en.wikipedia.org/wiki/${encodeURIComponent(title)}`,
  ballotpedia: (name: string) => `https://ballotpedia.org/${encodeURIComponent(name)}`,
  cspan: (id: number) => `https://www.c-span.org/person/?${id}`,
};

// Note: FEC IDs are arrays - display all or most recent
```

### API Endpoints Used

| Endpoint | Hook | Purpose |
|----------|------|---------|
| GET /api/members/{bioguideId} | `useMember` | Member profile data |
| GET /api/members/{bioguideId}/terms | `useMemberTerms` | Term history |
| GET /api/members/{bioguideId}/committees | `useMemberCommittees` | Committee assignments |

### Term/Position Data Derivation

The `PositionHolding` type contains term data but position details (chamber, title) must be derived:

```typescript
// Place in: lib/utils/term-helpers.ts
import type { PositionHolding, Person } from '@/types';

/**
 * Check if a term is current (still serving)
 */
export function isCurrentTerm(term: PositionHolding): boolean {
  return term.endDate === null || term.endDate === undefined;
}

/**
 * Check if member is currently serving in any position
 */
export function isCurrentlyServing(terms: PositionHolding[]): boolean {
  return terms.some(isCurrentTerm);
}

/**
 * Derive chamber and title from term data
 * The termLabel field (if populated) contains this info, e.g., "118th Congress (2023-present)"
 * For chamber, use the Person.chamber field as current chamber
 * For historical terms, parse from termLabel or use position lookup
 */
export function getTermDisplayInfo(term: PositionHolding, person: Person): {
  label: string;
  chamber: 'Senate' | 'House';
  isCurrent: boolean;
} {
  const isCurrent = isCurrentTerm(term);

  // termLabel is pre-computed by backend if available
  const label = term.termLabel || `Congress ${term.congress} (${term.startDate} - ${term.endDate || 'present'})`;

  // Chamber from person's current chamber (for current term) or infer from context
  // Note: Historical chamber changes are rare but possible
  const chamber = person.chamber === 'SENATE' ? 'Senate' : 'House';

  return { label, chamber, isCurrent };
}

/**
 * Format term for display
 */
export function formatTermDisplay(term: PositionHolding, person: Person): string {
  const { chamber, isCurrent } = getTermDisplayInfo(term, person);
  const position = chamber === 'Senate' ? 'Senator' : 'Representative';
  const location = chamber === 'Senate'
    ? `from ${person.state}`
    : `from ${person.state}`; // Could add district if available

  return `${position} ${location}`;
}
```

**Note:** The backend `termLabel` field should ideally contain pre-formatted term info. If not populated, use the helpers above to construct display text.

### Term Timeline Visual Design

```
┌──────────────────────────────────────────────┐
│ ● 118th Congress (2023-present)    [CURRENT] │
│   Senator from California                     │
├──────────────────────────────────────────────┤
│ ○ 117th Congress (2021-2023)                 │
│   Senator from California                     │
├──────────────────────────────────────────────┤
│ ○ 116th Congress (2019-2021)                 │
│   Senator from California                     │
└──────────────────────────────────────────────┘
```

### Example Members for Testing

- **S000033** - Bernie Sanders (Senator, Vermont, Independent, long history)
- **P000197** - Nancy Pelosi (Representative, California, Democrat)
- **M000355** - Mitch McConnell (Senator, Kentucky, Republican)
- **O000172** - Alexandria Ocasio-Cortez (Representative, New York, Democrat)

### Testing

#### Test Scenarios
1. Load page for member with full social media profile
2. Load page for member with no social media
3. Verify term history displays chronologically
4. Verify committee memberships show correct roles
5. Click external links opens correct URLs
6. Navigate back to members list
7. Test with non-existent bioguideId (404)
8. Mobile layout renders correctly

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-29 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-11-29 | 1.1 | Validation fixes: Added externalIds structure, term/position helpers, social URL builders, clarified MemberPhoto reuse, simplified back link | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- TypeScript compilation: No errors
- ESLint: No errors on new files

### Completion Notes List
- Created dynamic route `/members/[bioguideId]` with page, loading, error, and not-found states
- Created MemberProfile component with large photo (128px), full name display, party/state/chamber badges
- Created MemberSocialMedia component with Twitter, Facebook, YouTube links using Lucide icons
- Created TermTimeline component with visual timeline, current term highlighting, sorted by date
- Created MemberCommittees component displaying committee assignments sorted by role importance
- Created ExternalIds component with links to FEC, GovTrack, OpenSecrets, VoteSmart, Wikipedia, etc.
- Created utility files: social-links.ts, external-links.ts, term-helpers.ts
- Implemented tabbed layout with Overview, Terms, Committees, External Links tabs
- Reused existing MemberPhoto component from FB-1-UI.2
- All components are responsive with mobile-friendly layouts

### File List
| File | Action |
|------|--------|
| `frontend/src/app/members/[bioguideId]/page.tsx` | Created |
| `frontend/src/app/members/[bioguideId]/loading.tsx` | Created |
| `frontend/src/app/members/[bioguideId]/not-found.tsx` | Created |
| `frontend/src/app/members/[bioguideId]/error.tsx` | Created |
| `frontend/src/components/congressional/MemberProfile.tsx` | Created |
| `frontend/src/components/congressional/MemberSocialMedia.tsx` | Created |
| `frontend/src/components/congressional/TermTimeline.tsx` | Created |
| `frontend/src/components/congressional/MemberCommittees.tsx` | Created |
| `frontend/src/components/congressional/ExternalIds.tsx` | Created |
| `frontend/src/lib/utils/social-links.ts` | Created |
| `frontend/src/lib/utils/external-links.ts` | Created |
| `frontend/src/lib/utils/term-helpers.ts` | Created |

---

## QA Results

### QA Gate Review: 2025-11-30

**Reviewer:** Quinn (Test Architect)

#### Summary
All 14 acceptance criteria verified and passing. Implementation demonstrates excellent component separation, proper TypeScript typing, and comprehensive edge case handling.

#### Highlights
- Clean utility files (social-links.ts, external-links.ts, term-helpers.ts)
- Proper reuse of MemberPhoto component from FB-1-UI.2
- External links correctly use `target="_blank"` with `rel="noopener noreferrer"`
- Current term visually highlighted with green styling
- Responsive design with appropriate breakpoints

#### Quality Score: 96/100

Gate: PASS → docs/qa/gates/FB-1-UI.4-member-detail-page.yml
