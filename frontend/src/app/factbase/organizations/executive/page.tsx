'use client';

/**
 * Executive Branch Organizations Page
 *
 * Browse executive branch government organizations.
 */

import { BranchOrgsPage } from '../BranchOrgsPage';
import { getPageDescriptionOrDefault } from '@/lib/page-descriptions';

export default function ExecutiveBranchPage() {
  const { title, description } = getPageDescriptionOrDefault('/factbase/organizations/executive');

  return (
    <BranchOrgsPage
      branch="executive"
      title={title}
      description={description}
      breadcrumbs={[
        { label: 'Factbase', href: '/factbase' },
        { label: 'Organizations', href: '/factbase/organizations' },
        { label: 'Executive Branch' },
      ]}
    />
  );
}
