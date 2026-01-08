# Story ARCH-1.9: Verification and Cleanup

## Status

**Status:** Draft
**Priority:** P2
**Estimate:** 2 story points
**Phase:** Final

## Story

**As a** developer,
**I want** the refactor verified and legacy code cleaned up,
**So that** the codebase is clean and maintainable.

## Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| AC1 | All backend tests pass (unit + integration) | |
| AC2 | All frontend tests pass | |
| AC3 | Manual verification of key workflows | |
| AC4 | Old `Person` entity removed (replaced by Individual + CongressionalMember) | |
| AC5 | Old `PersonRepository` removed or deprecated | |
| AC6 | Documentation updated (entity diagrams, API docs) | |
| AC7 | ROADMAP.md updated with ARCH-1 completion | |

## Tasks / Subtasks

- [ ] **Task 1: Run Full Backend Test Suite** (AC1)
  - [ ] Run `./mvnw test` for unit tests
  - [ ] Run integration tests with Testcontainers
  - [ ] Fix any failing tests
  - [ ] Verify test coverage meets thresholds

- [ ] **Task 2: Run Full Frontend Test Suite** (AC2)
  - [ ] Run `pnpm test`
  - [ ] Run `pnpm exec tsc --noEmit` for type checking
  - [ ] Fix any failing tests or type errors
  - [ ] Verify test coverage meets thresholds

- [ ] **Task 3: Manual Verification** (AC3)
  - [ ] Test Congress member sync workflow
  - [ ] Test Presidential administration pages
  - [ ] Test Judge data display
  - [ ] Test Executive Order sync
  - [ ] Verify API responses match expected structure

- [ ] **Task 4: Remove Legacy Person Code** (AC4, AC5)
  - [ ] Delete `Person.java` entity (if fully replaced)
  - [ ] Delete `PersonRepository.java` (if fully replaced)
  - [ ] Delete `PersonService.java` (if fully replaced)
  - [ ] Delete `PersonDTO.java` (if not needed for backward compat)
  - [ ] Remove any unused imports across codebase
  - [ ] Run tests to verify no breaks

- [ ] **Task 5: Update Documentation** (AC6)
  - [ ] Update entity relationship diagram in architecture docs
  - [ ] Update API documentation with new DTOs
  - [ ] Update data model documentation
  - [ ] Review and update any affected README files

- [ ] **Task 6: Update ROADMAP** (AC7)
  - [ ] Mark ARCH-1 epic as COMPLETE in ROADMAP.md
  - [ ] Add completion date to changelog
  - [ ] Update any blocked items (e.g., KB-2)

## Dev Notes

### Source Tree Reference

```
Files to potentially DELETE:
backend/src/main/java/org/newsanalyzer/
├── model/
│   └── Person.java              # DELETE (replaced by Individual + CongressionalMember)
├── repository/
│   └── PersonRepository.java    # DELETE (replaced by IndividualRepository + CongressionalMemberRepository)
├── service/
│   └── PersonService.java       # DELETE (replaced by IndividualService + CongressionalMemberService)
└── dto/
    └── PersonDTO.java           # DELETE or DEPRECATE

Documentation to UPDATE:
docs/
├── architecture/
│   └── data-model.md           # UPDATE entity diagram
├── api/
│   └── openapi.yaml            # VERIFY updated schemas
└── ROADMAP.md                  # MARK ARCH-1 complete
```

### Manual Verification Checklist

| Workflow | Endpoint | Expected Behavior |
|----------|----------|-------------------|
| Congress Sync | POST /api/admin/sync/congress | Creates/updates CongressionalMember + Individual |
| President Page | GET /api/knowledge-base/presidencies | Returns president data from Individual |
| Member List | GET /api/members | Returns MemberDTO (flattened Individual + CongressionalMember) |
| Judge List | GET /api/knowledge-base/judges | Returns judge data linked to Individual |
| EO Sync | POST /api/admin/sync/executive-orders | Links EOs to presidencies (via Individual) |

### Cleanup Validation

**Before deleting any file, verify:**
1. No imports reference the file
2. No tests depend on the file
3. No other entities/services use it
4. Build succeeds after deletion

**Grep commands to verify no usage:**
```bash
# Check for Person entity usage
grep -r "import.*Person" --include="*.java" src/

# Check for PersonRepository usage
grep -r "PersonRepository" --include="*.java" src/

# Check for PersonService usage
grep -r "PersonService" --include="*.java" src/
```

### Documentation Updates

**Entity Diagram Update:**
```
┌─────────────────┐
│   Individual    │  ← Master person record
├─────────────────┤
│ id (PK)         │
│ firstName       │
│ lastName        │
│ birthDate       │
│ ...             │
└────────┬────────┘
         │
    ┌────┴────┬─────────────┬────────────────┐
    │         │             │                │
    ▼         ▼             ▼                ▼
┌────────┐ ┌────────┐ ┌───────────┐ ┌──────────────┐
│Congress│ │Presidency│ │Position  │ │Future Roles │
│Member  │ │         │ │Holding   │ │(CEO, etc.)  │
└────────┘ └─────────┘ └──────────┘ └──────────────┘
```

### Testing

**Test Commands:**
```bash
# Backend
cd backend
./mvnw clean test

# Frontend
cd frontend
pnpm test
pnpm exec tsc --noEmit
```

**Test Requirements:**
- All tests pass (0 failures)
- No type errors
- Coverage thresholds met
- No runtime errors in manual verification

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-08 | 1.0 | Initial story creation from epic | Sarah (PO) |

## Dev Agent Record

### Agent Model Used
*To be populated during implementation*

### Debug Log References
*To be populated during implementation*

### Completion Notes List
*To be populated during implementation*

### File List
*To be populated during implementation*

## QA Results
*To be populated after QA review*
