import {
  Database,
  Users,
  Building2,
  UserCheck,
  Landmark,
  Scale,
  Gavel,
} from 'lucide-react';
import { MenuItemData } from '@/components/sidebar/types';

/**
 * Public sidebar menu configuration.
 *
 * Structure follows a 3-level hierarchy:
 * - Level 0: Factbase (root)
 * - Level 1: Categories (People, Organizations)
 * - Level 2: Subcategories (Current Government Officials, Federal Government)
 * - Level 3: Leaf items with hrefs (Congressional Members, etc.)
 */
export const publicMenuConfig: MenuItemData[] = [
  {
    label: 'Factbase',
    icon: Database,
    children: [
      {
        label: 'People',
        icon: Users,
        children: [
          {
            label: 'Current Government Officials',
            icon: UserCheck,
            children: [
              { label: 'Congressional Members', href: '/factbase/people/congressional-members' },
              { label: 'Executive Appointees', href: '/factbase/people/executive-appointees' },
              { label: 'Federal Judges & Justices', href: '/factbase/people/federal-judges' },
            ],
          },
        ],
      },
      {
        label: 'Organizations',
        icon: Building2,
        children: [
          {
            label: 'Federal Government',
            icon: Landmark,
            children: [
              { label: 'Executive Branch', href: '/factbase/organizations/executive', icon: Landmark },
              { label: 'Legislative Branch', href: '/factbase/organizations/legislative', icon: Scale },
              { label: 'Judicial Branch', href: '/factbase/organizations/judicial', icon: Gavel },
            ],
          },
        ],
      },
    ],
  },
];

/**
 * Flattened menu config without the Factbase wrapper.
 * Use this when the sidebar header already displays "Factbase".
 */
export const publicMenuItemsFlat: MenuItemData[] = publicMenuConfig[0]?.children ?? [];
