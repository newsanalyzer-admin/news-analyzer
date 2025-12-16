# Story ADMIN-1.5: GOVMAN Import UI

## Status

**Done**

---

## Story

**As an** administrator,
**I want** a UI to upload and import GOVMAN XML files,
**so that** I can populate the factbase with official government structure data.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Route `/admin/factbase/executive/govman` displays GOVMAN import page |
| AC2 | Page includes file upload component accepting .xml files |
| AC3 | Upload validates file is XML before sending to backend |
| AC4 | Progress indicator shows during import operation |
| AC5 | Results display: total parsed, imported, updated, skipped (duplicates), errors |
| AC6 | Error details are expandable to show specific validation failures |
| AC7 | Preview mode available: parse and display records without importing |
| AC8 | Confirmation dialog before actual import with record count |

---

## Integration Verification

| # | Verification |
|---|--------------|
| IV1 | After import, Government Organizations page shows new records |
| IV2 | Branch filter correctly categorizes imported organizations |
| IV3 | Import does not affect other admin functions |

---

## Tasks / Subtasks

- [x] **Task 1: Create GOVMAN Import Page** (AC1)
  - [x] Create route at `frontend/src/app/admin/factbase/executive/govman/page.tsx`
  - [x] Add AdminBreadcrumb navigation (Admin > Factbase > Executive > GOVMAN Import)
  - [x] Add page header with icon (FileUp) and title "GOVMAN XML Import"
  - [x] Add description text explaining the GOVMAN import feature

- [x] **Task 2: Create GovmanImportButton Component** (AC2, AC3, AC4, AC5, AC6)
  - [x] Create `frontend/src/components/admin/GovmanImportButton.tsx`
  - [x] Follow `CsvImportButton.tsx` pattern for Dialog-based file upload
  - [x] Add drag-and-drop zone accepting only `.xml` files
  - [x] Validate file extension is `.xml` before enabling import
  - [x] Add file size display (backend limit is 10MB)
  - [x] Show Loader2 spinner during import operation
  - [x] Display results grid: Total, Imported, Updated, Skipped, Errors
  - [x] Add expandable error details section using expandable button

- [x] **Task 3: Create useGovmanImport Hook** (AC4)
  - [x] Create `frontend/src/hooks/useGovmanImport.ts`
  - [x] Use React Query `useMutation` for POST to `/api/admin/import/govman`
  - [x] Handle multipart form data file upload
  - [x] Return mutation state (isPending, isError, data)
  - [x] Add `useGovmanStatus` query hook for GET `/api/admin/import/govman/status`

- [x] **Task 4: Create GovmanImportResult TypeScript Types**
  - [x] Add types to `frontend/src/types/govman.ts`
  - [x] Define `GovmanImportResult` interface matching backend DTO
  - [x] Define `GovmanImportStatus` interface for status endpoint

- [x] **Task 5: Add Preview Mode** (AC7)
  - [x] Add "Preview" button in addition to "Import" button
  - [x] **Implementation: File info display** (backend dry-run not in scope)
  - [x] Display file name and size when file is selected
  - [x] Perform client-side XML validation (check file starts with `<?xml`)
  - [x] Show "Ready to import" status with file details
  - [x] Preview button shows file info panel without triggering import

- [x] **Task 6: Add Import Confirmation Dialog** (AC8)
  - [x] Show confirmation dialog before actual import
  - [x] Display warning about number of records to be processed
  - [x] Include Cancel and Confirm buttons
  - [x] Only proceed with import after user confirmation

- [x] **Task 7: Update Executive Branch Hub Page**
  - [x] Add GOVMAN Import card to `/admin/factbase/executive/page.tsx`
  - [x] Use FileUp icon for GOVMAN card
  - [x] Link to `/admin/factbase/executive/govman`
  - [x] Description: "Import official government organization structure from GOVMAN XML"

- [x] **Task 8: Update Sidebar Navigation** *(Already Complete)*
  - [x] "GOVMAN Import" menu item already exists in AdminSidebar.tsx (line 33)
  - [x] Routes to `/admin/factbase/executive/govman`
  - [x] No changes needed - verify link works after page creation

- [x] **Task 9: Integration Testing**
  - [x] TypeScript type check passes
  - [x] ESLint check passes
  - [x] Next.js build succeeds
  - [x] Page renders at /admin/factbase/executive/govman
  - [x] All UI components build correctly

---

## Dev Notes

### Source Tree - Relevant Files

**Backend (Already Created in ADMIN-1.4):**
```
backend/src/main/java/org/newsanalyzer/
├── controller/
│   └── GovmanImportController.java          # POST /api/admin/import/govman
├── dto/
│   └── GovmanImportResult.java              # Response DTO
├── service/
│   └── GovmanXmlImportService.java          # XML parsing and import
```

**Frontend - Existing Files to Reference:**
```
frontend/src/
├── app/admin/
│   ├── factbase/executive/
│   │   ├── page.tsx                         # Hub page (add GOVMAN card here)
│   │   ├── agencies/page.tsx                # Pattern for subpage
│   │   └── positions/page.tsx               # Pattern for subpage
│   └── layout.tsx                           # AdminLayout with sidebar
├── components/admin/
│   ├── AdminSidebar.tsx                     # Add GOVMAN menu item
│   ├── AdminBreadcrumb.tsx                  # Use for navigation
│   └── CsvImportButton.tsx                  # Pattern for import dialog
├── hooks/
│   └── useGovernmentOrgs.ts                 # Pattern: useGovOrgCsvImport hook
└── types/
    └── government-org.ts                    # Pattern: CsvImportResult type
```

**Files to Create:**
```
frontend/src/
├── app/admin/factbase/executive/
│   └── govman/
│       └── page.tsx                         # NEW: GOVMAN import page
├── components/admin/
│   └── GovmanImportButton.tsx               # NEW: Import dialog component
├── hooks/
│   └── useGovmanImport.ts                   # NEW: Import mutation hook
└── types/
    └── govman.ts                            # NEW: TypeScript types (or add to government-org.ts)
```

### API Endpoints (from ADMIN-1.4)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/admin/import/govman` | Upload and import XML file |
| GET | `/api/admin/import/govman/status` | Check import status |
| GET | `/api/admin/import/govman/last-result` | Get last import result |

### Backend Response Format (GovmanImportResult)

```typescript
interface GovmanImportResult {
  startTime: string;        // ISO timestamp
  endTime: string;          // ISO timestamp
  total: number;            // Total entities parsed
  imported: number;         // New records created
  updated: number;          // Existing records updated
  skipped: number;          // Duplicates skipped
  errors: number;           // Validation errors
  errorDetails: string[];   // Error messages
  durationSeconds: number;  // Processing time
  successRate: number;      // Percentage (0-100)
}
```

### Component Patterns to Follow

**CsvImportButton Pattern:**
- Dialog-based with DialogTrigger
- Drag-and-drop file zone
- File validation before upload
- Loading state with Loader2 spinner
- Success/Error result display
- useToast for notifications

**shadcn/ui Components to Use:**
- Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter
- Button (variant: default, outline, ghost)
- Card, CardHeader, CardTitle, CardDescription, CardContent
- Collapsible, CollapsibleTrigger, CollapsibleContent (for error details)
- Alert, AlertTitle, AlertDescription (for warnings)

**Icons (Lucide React):**
- FileUp - GOVMAN import
- Upload - File upload action
- Loader2 - Loading spinner
- CheckCircle - Success
- AlertCircle - Error/Warning
- ChevronDown - Collapsible toggle
- X - Close/Clear

### Tech Stack Reference

| Technology | Version | Usage |
|------------|---------|-------|
| Next.js | 14.1.0 | App Router |
| TypeScript | 5.3.3 | Type definitions |
| React Query | 5.17.19 | Server state (useMutation) |
| shadcn/ui | Latest | UI components |
| Tailwind CSS | 3.4.1 | Styling |
| Lucide React | 0.314.0 | Icons |

### Preview Mode Implementation Note

The backend does not currently support a dry-run/preview mode. Options:

**Option A (Recommended):** Implement preview as file info display
- Show file name and size
- Validate XML structure client-side (basic check)
- Display "Ready to import" message with estimated record count

**Option B (Future Enhancement):** Add backend dry-run support
- Would require backend change to add `?dryRun=true` parameter
- Not in scope for this story - can be added later

---

## Testing

### Testing Standards

| Aspect | Requirement |
|--------|-------------|
| Test Location | Manual testing (frontend test framework not established per QA-2) |
| API Integration | Verify using Swagger UI at `/swagger-ui.html` |
| Component Testing | Browser dev tools, React Query DevTools |
| Coverage | Manual verification of all acceptance criteria |

### Manual Test Cases

**Test 1: Page Renders (AC1)**
- Navigate to `/admin/factbase/executive/govman`
- Verify page displays with correct title and breadcrumb

**Test 2: File Upload Dialog (AC2, AC3)**
- Click "Import GOVMAN XML" button
- Verify dialog opens with drag-drop zone
- Try uploading non-XML file - should be rejected
- Try uploading valid XML file - should be accepted

**Test 3: Import Execution (AC4, AC5)**
- Upload valid GOVMAN test XML
- Click Import button
- Verify progress spinner shows
- Verify results display with all counts

**Test 4: Error Display (AC6)**
- Import XML with validation errors
- Verify error count displays
- Click to expand error details
- Verify individual errors are listed

**Test 5: Preview Mode (AC7)**
- Select XML file
- Click Preview button
- Verify file info displays without importing

**Test 6: Confirmation Dialog (AC8)**
- Select XML file and click Import
- Verify confirmation dialog appears
- Click Cancel - verify no import occurs
- Click Confirm - verify import proceeds

**Test 7: Integration (IV1, IV2, IV3)**
- Complete successful import
- Navigate to Government Organizations page
- Verify new records appear
- Filter by Executive branch - verify correct categorization
- Test other admin functions still work

### Sample Test XML

Use `backend/src/test/resources/govman-test.xml` created in ADMIN-1.4 for testing.

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-05 | 1.0 | Initial story creation from PRD | Sarah (PO) |
| 2025-12-05 | 1.1 | Validated and approved; clarified preview mode, marked Task 8 complete | Sarah (PO) |
| 2025-12-05 | 1.2 | Implementation complete; all tasks done, DoD checklist passed | James (Dev) |
| 2025-12-05 | 1.3 | QA review passed (Gate: PASS, Score: 85); Story approved and marked Done | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101

### Debug Log References

No debug logs required - all builds pass.

### Completion Notes List

- All 9 tasks completed successfully
- TypeScript types, React Query hooks, and UI components created following existing patterns
- GovmanImportButton follows CsvImportButton pattern with additional preview and confirmation stages
- Preview mode shows file info and XML validation (client-side)
- Confirmation dialog warns before import
- Expandable error details uses simple state-based toggle (no Collapsible component needed)
- Hub page updated with GOVMAN Import card in 3-column grid layout
- All lint checks pass, TypeScript compiles without errors, Next.js builds successfully

### File List

**New Files Created:**
- `frontend/src/types/govman.ts` - TypeScript types for GOVMAN import
- `frontend/src/hooks/useGovmanImport.ts` - React Query hooks for import API
- `frontend/src/components/admin/GovmanImportButton.tsx` - Import dialog component
- `frontend/src/app/admin/factbase/executive/govman/page.tsx` - GOVMAN import page

**Modified Files:**
- `frontend/src/app/admin/factbase/executive/page.tsx` - Added GOVMAN Import card

---

## QA Results

### Review Date: 2025-12-05

### Reviewed By: Quinn (Test Architect)

### Risk Assessment

**Risk Level: LOW** - Frontend-only story with no auth/payment/security changes. Follows established CsvImportButton pattern. < 500 lines of code. Story has 8 acceptance criteria but straightforward UI work.

### Requirements Traceability

| AC | Criterion | Implementation | Status |
|----|-----------|----------------|--------|
| AC1 | Route displays GOVMAN import page | `govman/page.tsx` with AdminBreadcrumb | ✓ Covered |
| AC2 | File upload component accepting .xml | Drag-and-drop zone with `.xml` accept | ✓ Covered |
| AC3 | Validates file is XML before sending | `validateXmlFile()` checks `<?xml` prefix | ✓ Covered |
| AC4 | Progress indicator during import | `Loader2` spinner in `importing` stage | ✓ Covered |
| AC5 | Results display counts | 5-column grid showing totals | ✓ Covered |
| AC6 | Expandable error details | `showErrors` toggle with expandable section | ✓ Covered |
| AC7 | Preview mode available | `preview` stage shows file info | ✓ Covered |
| AC8 | Confirmation dialog before import | `confirm` stage with warning | ✓ Covered |

**Coverage: 8/8 acceptance criteria fully covered**

### Code Quality Assessment

**Overall: GOOD** - Implementation follows established patterns and coding standards.

**Strengths:**
- Multi-stage dialog (`select` → `preview` → `confirm` → `importing` → `result`) provides excellent UX flow
- Comprehensive error handling with specific error codes (409, 413, 400)
- Client-side XML validation provides immediate feedback
- Follows `CsvImportButton` pattern consistently
- Good TypeScript type safety with `GovmanImportResult` and `GovmanImportStatus`
- Proper React Query integration with cache invalidation

**Minor Observations (Non-blocking):**
- `GovmanImportButton.tsx` is 485 lines - could be refactored into smaller components in future but acceptable for current scope
- Preview mode is client-side file info only (backend dry-run noted as future enhancement in story)

### Refactoring Performed

None required - code quality is acceptable.

### Compliance Check

- Coding Standards: ✓ Follows TypeScript/React standards from coding-standards.md
- Project Structure: ✓ Files in correct locations per source-tree pattern
- Testing Strategy: ✓ Manual testing per QA-2 (frontend test framework not established)
- All ACs Met: ✓ All 8 acceptance criteria implemented

### Improvements Checklist

[x] All acceptance criteria implemented correctly
[x] TypeScript types match backend DTO structure
[x] React Query hooks follow established pattern
[x] Error handling covers edge cases (409, 413, 400 status codes)
[x] UI provides clear feedback at each stage
[ ] Consider extracting stage renderers into separate components (future tech debt)
[ ] Consider adding frontend unit tests when test framework established (per QA-2)

### Security Review

**Status: PASS**
- No security concerns - this is an admin-only feature
- File validation performed before upload (extension and XML header check)
- File size limit enforced (10MB max)
- No user input directly rendered as HTML (XSS safe)
- API calls use standard fetch with FormData

### Performance Considerations

**Status: PASS**
- Large file handling: 10MB limit is appropriate for XML files
- Async file validation with slice() for header check (efficient)
- React Query cache invalidation triggers data refresh appropriately
- No unnecessary re-renders in component structure

### Files Modified During Review

None - no refactoring performed.

### Gate Status

Gate: **PASS** → docs/qa/gates/ADMIN-1.5-govman-import-ui.yml

### Recommended Status

**✓ Ready for Done** - All acceptance criteria implemented correctly, code quality is good, follows established patterns. Story can be marked as Done.
