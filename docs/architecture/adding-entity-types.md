# Adding a New Entity Type to Knowledge Explorer

**Last Updated:** 2025-12-28

This guide explains how to add a new entity type to the Knowledge Explorer using the configuration-driven pattern.

---

## Overview

The Knowledge Explorer uses a configuration-driven approach that enables adding new entity types through configuration rather than writing custom components. The pattern consists of:

1. **EntityTypeConfig** - Defines columns, filters, views, and detail sections
2. **EntityBrowser** - Renders lists/grids based on config
3. **EntityDetail** - Renders detail pages based on config
4. **HierarchyView** - Renders tree structures for hierarchical entities (optional)

---

## Step 1: Define the TypeScript Type

First, create a type definition for your entity in `frontend/src/types/`.

```typescript
// frontend/src/types/my-entity.ts

export interface MyEntity {
  id: string;
  name: string;
  status: 'ACTIVE' | 'INACTIVE';
  category: string;
  createdAt: string;
  // ... other fields
}

// Helper functions for rendering
export function getStatusColor(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-800';
    case 'INACTIVE':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}
```

---

## Step 2: Create API Client and Hook

Create an API client and React Query hook.

```typescript
// frontend/src/lib/api/myEntity.ts

import axios from 'axios';
import type { MyEntity } from '@/types/my-entity';
import type { Page, PaginationParams } from '@/types/pagination';

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: BACKEND_URL,
  timeout: 10000,
});

export interface MyEntityListParams extends PaginationParams {
  status?: string;
  category?: string;
}

export const myEntityApi = {
  list: async (params: MyEntityListParams = {}): Promise<Page<MyEntity>> => {
    const response = await api.get<Page<MyEntity>>('/api/my-entities', { params });
    return response.data;
  },

  getById: async (id: string): Promise<MyEntity> => {
    const response = await api.get<MyEntity>(`/api/my-entities/${id}`);
    return response.data;
  },

  search: async (query: string): Promise<MyEntity[]> => {
    const response = await api.get<MyEntity[]>('/api/my-entities/search', {
      params: { query },
    });
    return response.data;
  },
};
```

```typescript
// frontend/src/hooks/useMyEntity.ts

import { useQuery } from '@tanstack/react-query';
import { myEntityApi, type MyEntityListParams } from '@/lib/api/myEntity';

export const myEntityKeys = {
  all: ['my-entities'] as const,
  lists: () => [...myEntityKeys.all, 'list'] as const,
  list: (params: MyEntityListParams) => [...myEntityKeys.lists(), params] as const,
  detail: (id: string) => [...myEntityKeys.all, 'detail', id] as const,
  search: (query: string) => [...myEntityKeys.all, 'search', query] as const,
};

export function useMyEntities(params: MyEntityListParams = {}) {
  return useQuery({
    queryKey: myEntityKeys.list(params),
    queryFn: () => myEntityApi.list(params),
  });
}

export function useMyEntity(id: string | null) {
  return useQuery({
    queryKey: myEntityKeys.detail(id!),
    queryFn: () => myEntityApi.getById(id!),
    enabled: !!id,
  });
}

export function useMyEntitySearch(query: string) {
  return useQuery({
    queryKey: myEntityKeys.search(query),
    queryFn: () => myEntityApi.search(query),
    enabled: query.length >= 2,
  });
}
```

---

## Step 3: Define Entity Type Configuration

Add your entity type configuration to `frontend/src/lib/config/entityTypes.ts`.

### Column Configuration

Columns define what appears in the list view:

```typescript
import { MyIcon } from 'lucide-react';
import { createElement } from 'react';
import type { MyEntity } from '@/types/my-entity';
import { getStatusColor } from '@/types/my-entity';

// Column configuration
const myEntityColumns: ColumnConfig<MyEntity>[] = [
  {
    id: 'name',
    label: 'Name',
    sortable: true,
    width: '30%',
  },
  {
    id: 'category',
    label: 'Category',
    sortable: true,
    width: '20%',
  },
  {
    id: 'status',
    label: 'Status',
    sortable: true,
    width: '15%',
    render: (value) => {
      const status = value as string;
      const colorClass = getStatusColor(status);
      return createElement(
        'span',
        { className: `inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${colorClass}` },
        status
      );
    },
  },
  {
    id: 'createdAt',
    label: 'Created',
    sortable: true,
    width: '20%',
    hideOnMobile: true,
    render: (value) => new Date(value as string).toLocaleDateString(),
  },
];
```

### Filter Configuration

Filters appear above the list:

```typescript
const myEntityFilters: FilterConfig[] = [
  {
    id: 'status',
    label: 'Status',
    type: 'select',
    apiParam: 'status',
    options: [
      { value: 'ACTIVE', label: 'Active' },
      { value: 'INACTIVE', label: 'Inactive' },
    ],
  },
  {
    id: 'category',
    label: 'Category',
    type: 'select',
    apiParam: 'category',
    options: [
      { value: 'type1', label: 'Type 1' },
      { value: 'type2', label: 'Type 2' },
    ],
  },
];
```

### Detail Configuration

Detail config defines sections shown on the detail page:

```typescript
const myEntityDetailConfig: EntityDetailConfig<MyEntity> = {
  header: {
    titleField: 'name',
    badgeField: 'status',
    renderBadge: (value) => {
      const status = value as string;
      const colorClass = getStatusColor(status);
      return createElement(
        'span',
        { className: `inline-flex items-center px-2.5 py-0.5 rounded text-sm font-medium ${colorClass}` },
        status
      );
    },
    metaFields: ['category'],
  },
  sections: [
    {
      id: 'overview',
      label: 'Overview',
      layout: 'grid',
      fields: [
        { id: 'category', label: 'Category' },
        { id: 'status', label: 'Status' },
        { id: 'createdAt', label: 'Created Date' },
      ],
    },
    {
      id: 'details',
      label: 'Additional Details',
      layout: 'grid',
      collapsible: true,
      defaultCollapsed: true,
      fields: [
        { id: 'description', label: 'Description', hideIfEmpty: true },
      ],
    },
  ],
};
```

### Register the Entity Type

Add to the `entityTypes` array:

```typescript
export const entityTypes: EntityTypeConfig[] = [
  // ... existing types
  {
    id: 'my-entities',
    label: 'My Entities',
    icon: MyIcon,
    apiEndpoint: '/api/my-entities',
    supportedViews: ['list'],  // or ['list', 'hierarchy'] if hierarchical
    defaultView: 'list',
    columns: myEntityColumns,
    filters: myEntityFilters,
    defaultSort: { column: 'name', direction: 'asc' },
    detailConfig: myEntityDetailConfig,
  },
];
```

---

## Step 4: Wire Up Data Fetching

Update the entity browser page to handle your new entity type. In `frontend/src/app/knowledge-base/[entityType]/page.tsx`:

1. Import your hooks
2. Add query parameters for your filters
3. Add data fetching logic for your entity type
4. Add to the combined data selection

See the existing implementation for organizations and people as examples.

---

## Step 5: Add Navigation Links

Update `frontend/src/lib/menu-config.ts` to add navigation:

```typescript
export const publicMenuConfig: MenuItemData[] = [
  {
    label: 'Knowledge Base',
    icon: Database,
    children: [
      // ... existing items
      {
        label: 'My Entities',
        icon: MyIcon,
        children: [
          { label: 'All My Entities', href: '/knowledge-base/my-entities' },
        ],
      },
    ],
  },
];
```

---

## Step 6: Add Redirects (if migrating)

If replacing an existing page, add redirects in `frontend/next.config.js`:

```javascript
async redirects() {
  return [
    // ... existing redirects
    {
      source: '/old-path/my-entities',
      destination: '/knowledge-base/my-entities',
      permanent: true,
    },
  ];
}
```

---

## Configuration Reference

### EntityTypeConfig Interface

```typescript
interface EntityTypeConfig<T = unknown> {
  id: string;                          // URL-friendly identifier
  label: string;                       // Display name
  icon: LucideIcon;                    // Icon from lucide-react
  apiEndpoint: string;                 // Backend API endpoint
  supportedViews: ViewMode[];          // 'list', 'grid', 'hierarchy'
  defaultView: ViewMode;               // Default view mode
  hasSubtypes?: boolean;               // For entities with subtypes (e.g., People)
  subtypes?: SubtypeConfig[];          // Subtype configurations
  defaultSubtype?: string;             // Default subtype ID
  columns?: ColumnConfig<T>[];         // List view columns
  filters?: FilterConfig[];            // Filter definitions
  defaultSort?: DefaultSort;           // Default sort
  detailConfig?: EntityDetailConfig<T>; // Detail page config
  hierarchyConfig?: HierarchyConfig;   // For hierarchy view
}
```

### ColumnConfig Interface

```typescript
interface ColumnConfig<T = unknown> {
  id: string;                          // Field ID (matches data property)
  label: string;                       // Column header
  sortable?: boolean;                  // Enable sorting
  width?: string;                      // CSS width (e.g., '20%')
  hideOnMobile?: boolean;              // Hide on small screens
  render?: (value: unknown, row: T) => ReactNode;  // Custom renderer
  accessor?: (row: T) => unknown;      // Custom value accessor
}
```

### FilterConfig Interface

```typescript
interface FilterConfig {
  id: string;                          // Filter ID
  label: string;                       // Display label
  type: 'select' | 'multiselect' | 'text';
  apiParam: string;                    // API query parameter name
  options?: FilterOption[];            // For select types
}
```

### EntityDetailConfig Interface

```typescript
interface EntityDetailConfig<T = unknown> {
  header: {
    titleField: string;                // Field for page title
    subtitleField?: string;            // Field for subtitle
    badgeField?: string;               // Field for status badge
    renderBadge?: (value: unknown) => ReactNode;
    metaFields?: string[];             // Fields shown in header meta
  };
  sections: EntityDetailSection<T>[];
}

interface EntityDetailSection<T = unknown> {
  id: string;
  label: string;
  layout: 'grid' | 'list';
  collapsible?: boolean;
  defaultCollapsed?: boolean;
  fields: EntityDetailField<T>[];
}

interface EntityDetailField<T = unknown> {
  id: string;
  label: string;
  hideIfEmpty?: boolean;
  render?: (value: unknown, entity: T) => ReactNode;
}
```

---

## Examples

### Organizations (with hierarchy)

See `frontend/src/lib/config/entityTypes.ts` for the organizations configuration, which demonstrates:
- Multiple view modes (list, hierarchy)
- Filter configuration
- Hierarchy configuration for tree view

### People (with subtypes)

See `frontend/src/lib/config/peopleConfig.ts` for the people configuration, which demonstrates:
- Subtype support (judges, members, appointees)
- Per-subtype columns and filters
- Per-subtype detail configurations
- Shared renderer functions

---

## Testing Checklist

After adding a new entity type:

- [ ] Entity appears in EntityTypeSelector
- [ ] List view loads with correct columns
- [ ] Sorting works on sortable columns
- [ ] Filters apply correctly
- [ ] Search works
- [ ] Detail page renders correctly
- [ ] Navigation links work
- [ ] Mobile responsive display
- [ ] TypeScript compiles without errors
- [ ] No console errors

---

*End of Guide*
