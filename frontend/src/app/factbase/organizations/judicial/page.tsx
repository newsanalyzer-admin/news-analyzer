'use client';

/**
 * Judicial Branch Organizations Page
 *
 * Browse judicial branch government organizations.
 */

import { BranchOrgsPage } from '../BranchOrgsPage';
import { getPageDescriptionOrDefault } from '@/lib/page-descriptions';

export default function JudicialBranchPage() {
  const { title, description } = getPageDescriptionOrDefault('/factbase/organizations/judicial');

  return (
    <BranchOrgsPage
      branch="judicial"
      title={title}
      description={description}
      breadcrumbs={[
        { label: 'Factbase', href: '/factbase' },
        { label: 'Organizations', href: '/factbase/organizations' },
        { label: 'Judicial Branch' },
      ]}
    />
  );
}
