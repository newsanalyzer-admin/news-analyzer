'use client';

/**
 * Legislative Branch Organizations Page
 *
 * Browse legislative branch government organizations.
 */

import { BranchOrgsPage } from '../BranchOrgsPage';
import { getPageDescriptionOrDefault } from '@/lib/page-descriptions';

export default function LegislativeBranchPage() {
  const { title, description } = getPageDescriptionOrDefault('/factbase/organizations/legislative');

  return (
    <BranchOrgsPage
      branch="legislative"
      title={title}
      description={description}
      breadcrumbs={[
        { label: 'Factbase', href: '/factbase' },
        { label: 'Organizations', href: '/factbase/organizations' },
        { label: 'Legislative Branch' },
      ]}
    />
  );
}
