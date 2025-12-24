# Story UI-2.4: HierarchyView Pattern Component

## Status

**Draft**

---

## Story

**As a** user exploring organizational structures,
**I want** an interactive tree visualization of hierarchical entities,
**so that** I can understand parent-child relationships and navigate the structure visually.

---

## Acceptance Criteria

1. HierarchyView renders a tree visualization from hierarchical data
2. Nodes display entity name and optional metadata (type, count of children)
3. Nodes are expandable/collapsible with visual indicator (+/-)
4. Clicking a node navigates to EntityDetail view for that entity
5. Root node(s) expand by default; deeper levels collapsed
6. Supports keyboard navigation (Arrow keys to navigate, Enter to select, Space to expand/collapse)
7. Performance acceptable for hierarchies with 1000+ nodes (lazy loading or virtualization)
8. Component is fully typed with TypeScript
9. Responsive: horizontal scroll on mobile if tree is wide
10. Visual connector lines between parent and child nodes

---

## Tasks / Subtasks

- [ ] Define HierarchyConfig interface (AC: 8)
  - [ ] Extend `frontend/src/lib/config/entityTypes.ts`
  - [ ] Define HierarchyNodeConfig (labelField, metaFields, childrenField)
  - [ ] Define expansion/collapse default settings
  - [ ] Export TypeScript types

- [ ] Create HierarchyView component (AC: 1, 5, 9)
  - [ ] Create `frontend/src/components/knowledge-base/HierarchyView.tsx`
  - [ ] Accept hierarchical data and config as props
  - [ ] Render root node(s) expanded by default
  - [ ] Implement horizontal scroll container for mobile
  - [ ] Track expanded/collapsed state per node

- [ ] Create TreeNode subcomponent (AC: 2, 3, 10)
  - [ ] Create `frontend/src/components/knowledge-base/TreeNode.tsx`
  - [ ] Display node label and optional metadata
  - [ ] Expand/collapse toggle with +/- or chevron icon
  - [ ] Render connector lines (CSS or SVG)
  - [ ] Recursive rendering for children

- [ ] Implement node click navigation (AC: 4)
  - [ ] Clicking node label navigates to detail view
  - [ ] Clicking expand icon only toggles expansion (no navigation)
  - [ ] Clear visual distinction between clickable areas

- [ ] Implement keyboard navigation (AC: 6)
  - [ ] Arrow Up/Down to move between visible nodes
  - [ ] Arrow Right to expand node
  - [ ] Arrow Left to collapse node (or move to parent)
  - [ ] Enter to navigate to detail view
  - [ ] Space to toggle expand/collapse
  - [ ] Maintain focus indicator

- [ ] Implement performance optimization (AC: 7)
  - [ ] Lazy load children on expand (if data supports)
  - [ ] OR virtualize visible nodes for large trees
  - [ ] Debounce rapid expand/collapse
  - [ ] Test with 1000+ node dataset

- [ ] Create route integration
  - [ ] Wire into `/knowledge-base/[entityType]/hierarchy/page.tsx`
  - [ ] OR render within existing page when view=hierarchy
  - [ ] Fetch hierarchy data from appropriate API endpoint

- [ ] Create barrel export (AC: 8)
  - [ ] Update `frontend/src/components/knowledge-base/index.ts`
  - [ ] Export HierarchyView and TreeNode

---

## Dev Notes

### Relevant Source Tree

```
frontend/src/
├── app/
│   └── knowledge-base/
│       └── [entityType]/
│           ├── page.tsx                  # Switches view based on ?view= param
│           └── hierarchy/
│               └── page.tsx              # Alternative: dedicated hierarchy route
├── components/
│   ├── knowledge-base/
│   │   ├── HierarchyView.tsx             # NEW - main tree component
│   │   ├── TreeNode.tsx                  # NEW - recursive node component
│   │   ├── EntityBrowser.tsx             # From UI-2.2
│   │   ├── EntityDetail.tsx              # From UI-2.3
│   │   └── index.ts                      # Barrel export
│   └── ui/
│       └── collapsible.tsx               # Shadcn collapsible (if useful)
├── lib/
│   └── config/
│       └── entityTypes.ts                # Extended with hierarchy config
└── hooks/
    └── useTreeNavigation.ts              # NEW - keyboard navigation hook
```

### Key Implementation Details

**HierarchyConfig Structure:**
```typescript
interface HierarchyConfig {
  labelField: string;                    // 'officialName'
  metaFields?: string[];                 // ['acronym', 'childCount']
  childrenField: string;                 // 'childOrganizations'
  idField: string;                       // 'id'
  defaultExpandDepth: number;            // 1 = root expanded, children collapsed
  showChildCount?: boolean;              // Show (5) next to expandable nodes
}
```

**HierarchyView Props:**
```typescript
interface HierarchyViewProps {
  data: HierarchyNode[];                 // Root nodes
  config: HierarchyConfig;
  entityType: string;                    // For navigation URLs
  isLoading?: boolean;
  onNodeClick?: (node: HierarchyNode) => void;
}

interface HierarchyNode {
  id: string;
  [key: string]: any;                    // Dynamic fields based on entity
  children?: HierarchyNode[];
}
```

**TreeNode Props:**
```typescript
interface TreeNodeProps {
  node: HierarchyNode;
  config: HierarchyConfig;
  depth: number;
  entityType: string;
  isExpanded: boolean;
  isFocused: boolean;
  onToggle: (nodeId: string) => void;
  onSelect: (node: HierarchyNode) => void;
  onFocus: (nodeId: string) => void;
}
```

**CSS Connector Lines:**
```css
/* Vertical line from parent */
.tree-node::before {
  content: '';
  position: absolute;
  left: -16px;
  top: 0;
  height: 100%;
  border-left: 1px solid var(--border);
}

/* Horizontal line to node */
.tree-node::after {
  content: '';
  position: absolute;
  left: -16px;
  top: 12px;
  width: 16px;
  border-top: 1px solid var(--border);
}
```

**Keyboard Navigation Hook:**
```typescript
function useTreeNavigation(nodes: FlattenedNode[], expandedIds: Set<string>) {
  // Flatten visible tree for arrow key navigation
  // Track focused node id
  // Handle arrow keys, enter, space
  return { focusedId, handleKeyDown };
}
```

**Existing Hierarchy Implementation:**
- Check `factbase/organizations/` for any existing tree/hierarchy implementation
- May have OrgTree or similar component to reference

**API Considerations:**
- Backend may return flat list with parentId → need to build tree client-side
- Or backend returns nested structure → use directly
- Consider `/api/government-orgs/hierarchy` endpoint if exists

**Performance Strategies:**
1. **Lazy loading:** Only fetch children when node expanded
2. **Virtualization:** Use react-window or similar for 1000+ nodes
3. **Pagination:** Load children in batches if many siblings

### Architecture Reference

- UI Components: Shadcn/UI (Button for expand/collapse)
- Styling: Tailwind CSS, custom CSS for connector lines
- Icons: Lucide React (ChevronRight, ChevronDown, Folder, FolderOpen)
- State: Local useState for expanded nodes, or Zustand if complex

---

## Testing

### Test File Location
`frontend/src/components/knowledge-base/__tests__/HierarchyView.test.tsx`

### Testing Standards
- Use Vitest + React Testing Library
- Mock hierarchical data with multiple levels
- Test keyboard navigation thoroughly
- Test performance with large datasets

### Test Cases

1. **HierarchyView Rendering**
   - Renders root nodes
   - Shows loading skeleton when isLoading=true
   - Shows empty state when data=[]
   - Connector lines visible between parent and children

2. **TreeNode Display**
   - Displays label from configured field
   - Displays metadata when configured
   - Shows child count when showChildCount=true
   - Shows expand icon for nodes with children
   - No expand icon for leaf nodes

3. **Expand/Collapse**
   - Root nodes expanded by default (per defaultExpandDepth)
   - Clicking expand icon toggles children visibility
   - Expand icon rotates or changes (+/- or chevron)
   - Children animate in/out smoothly
   - Collapsed nodes don't render children in DOM

4. **Node Navigation**
   - Clicking node label navigates to detail URL
   - Clicking expand icon does NOT navigate
   - onNodeClick callback fires with correct node

5. **Keyboard Navigation**
   - Arrow Down moves focus to next visible node
   - Arrow Up moves focus to previous visible node
   - Arrow Right expands focused node (if collapsed)
   - Arrow Right moves to first child (if expanded)
   - Arrow Left collapses focused node (if expanded)
   - Arrow Left moves to parent (if collapsed)
   - Enter navigates to detail view
   - Space toggles expand/collapse
   - Focus indicator visible on focused node

6. **Performance**
   - Renders 100 nodes without lag
   - Renders 1000 nodes acceptably (< 500ms)
   - Expanding deep node doesn't freeze UI
   - Memory usage reasonable with large trees

7. **Responsive**
   - Horizontal scroll appears when tree exceeds viewport
   - Touch scrolling works on mobile
   - Tap targets large enough for touch

8. **Accessibility**
   - Tree has role="tree"
   - Nodes have role="treeitem"
   - aria-expanded indicates state
   - aria-level indicates depth
   - Focus management follows WAI-ARIA tree pattern

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-22 | 1.0 | Initial story creation | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
_To be filled by Dev Agent_

### Debug Log References
_To be filled by Dev Agent_

### Completion Notes List
_To be filled by Dev Agent_

### File List
_To be filled by Dev Agent_

---

## QA Results
_To be filled by QA Agent_
