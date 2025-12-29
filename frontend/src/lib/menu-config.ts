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
 * - Level 0: Knowledge Base (root)
 * - Level 1: Categories (People, Organizations)
 * - Level 2: Subcategories (Government Officials, Federal Government)
 * - Level 3: Leaf items with hrefs (Congressional Members, etc.)
 */
export const publicMenuConfig: MenuItemData[] = [
  {
    label: 'Knowledge Base',
    icon: Database,
    children: [
      {
        label: 'People',
        icon: Users,
        children: [
          {
            label: 'Government Officials',
            icon: UserCheck,
            children: [
              { label: 'Congressional Members', href: '/knowledge-base/people?type=members' },
              { label: 'Executive Appointees', href: '/knowledge-base/people?type=appointees' },
              { label: 'Federal Judges', href: '/knowledge-base/people?type=judges' },
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
              { label: 'Executive Branch', href: '/knowledge-base/organizations?branch=executive', icon: Landmark },
              { label: 'Legislative Branch', href: '/knowledge-base/organizations?branch=legislative', icon: Scale },
              { label: 'Judicial Branch', href: '/knowledge-base/organizations?branch=judicial', icon: Gavel },
            ],
          },
        ],
      },
    ],
  },
];

/**
 * Flattened menu config without the Knowledge Base wrapper.
 * Use this when the sidebar header already displays "Knowledge Base".
 */
export const publicMenuItemsFlat: MenuItemData[] = publicMenuConfig[0]?.children ?? [];
