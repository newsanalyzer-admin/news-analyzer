# Story ARCH-1.7: Update DTOs and Controllers

## Status

**Status:** Draft
**Priority:** P1
**Estimate:** 4 story points
**Phase:** 5

## Story

**As a** developer,
**I want** DTOs and controllers updated for backward compatibility,
**So that** API consumers don't experience breaking changes.

## Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| AC1 | `IndividualDTO` created with all biographical fields | |
| AC2 | `MemberDTO` includes individual data + Congressional fields (flattened for backward compat) | |
| AC3 | `PresidencyDTO` continues to include president name/bio (from Individual) | |
| AC4 | `JudgeDTO` continues to include judge name/bio (from Individual) | |
| AC5 | API response structure unchanged (field names preserved) | |
| AC6 | Controller tests verify backward compatibility | |
| AC7 | OpenAPI documentation updated | |
| AC8 | API version remains v1 (no breaking changes at endpoint level) (MOD-6) | |
| AC9 | OpenAPI schemas updated with deprecation notices on any person-specific endpoints (MOD-6) | |

## Tasks / Subtasks

- [ ] **Task 1: Create IndividualDTO** (AC1)
  - [ ] Create `backend/src/main/java/org/newsanalyzer/dto/IndividualDTO.java`
  - [ ] Include all biographical fields
  - [ ] Add mapper from Individual entity

- [ ] **Task 2: Update MemberDTO** (AC2, AC5)
  - [ ] Flatten Individual + CongressionalMember data
  - [ ] Keep same field names as current PersonDTO/MemberDTO
  - [ ] Create mapper that joins both entities
  - [ ] Ensure JSON structure unchanged

- [ ] **Task 3: Update PresidencyDTO** (AC3, AC5)
  - [ ] Update to fetch president data from Individual
  - [ ] Keep same field names: `presidentFullName`, `presidentFirstName`, etc.
  - [ ] Update mapper to use Individual relationship

- [ ] **Task 4: Update JudgeDTO** (AC4, AC5)
  - [ ] Update to fetch judge data from Individual
  - [ ] Keep same field names
  - [ ] Update mapper

- [ ] **Task 5: Update Controllers**
  - [ ] Update `MemberController` to use new DTOs
  - [ ] Update `PresidencyController` to use updated DTOs
  - [ ] Update `JudgeController` to use updated DTOs
  - [ ] Ensure no endpoint path changes (AC8)

- [ ] **Task 6: Write Backward Compatibility Tests** (AC6)
  - [ ] Create tests that verify JSON response structure
  - [ ] Compare with snapshots of expected responses
  - [ ] Test all affected endpoints

- [ ] **Task 7: Update OpenAPI Documentation** (AC7, AC9)
  - [ ] Update schema definitions
  - [ ] Add deprecation notices if any endpoints are deprecated
  - [ ] Regenerate OpenAPI spec

## Dev Notes

### Source Tree Reference

```
backend/src/main/java/org/newsanalyzer/dto/
├── IndividualDTO.java         # NEW
├── MemberDTO.java             # UPDATE (or create if doesn't exist)
├── PresidencyDTO.java         # UPDATE mapper
├── JudgeDTO.java              # UPDATE mapper
└── ...

backend/src/main/java/org/newsanalyzer/controller/
├── MemberController.java      # UPDATE
├── PresidencyController.java  # UPDATE
├── JudgeController.java       # UPDATE
└── ...

backend/src/test/java/org/newsanalyzer/controller/
└── *ControllerTest.java       # UPDATE with backward compat tests
```

### Backward Compatibility Strategy

The key is **DTO flattening** - combine data from Individual + role-specific entity into a single flat DTO that matches the current API response structure.

### IndividualDTO

```java
public record IndividualDTO(
    UUID id,
    String firstName,
    String lastName,
    String middleName,
    String suffix,
    String fullName,
    LocalDate birthDate,
    LocalDate deathDate,
    String birthPlace,
    String gender,
    String imageUrl,
    String party,
    boolean isLiving,
    JsonNode externalIds,
    JsonNode socialMedia
) {
    public static IndividualDTO from(Individual individual) {
        return new IndividualDTO(
            individual.getId(),
            individual.getFirstName(),
            individual.getLastName(),
            individual.getMiddleName(),
            individual.getSuffix(),
            individual.getFullName(),
            individual.getBirthDate(),
            individual.getDeathDate(),
            individual.getBirthPlace(),
            individual.getGender(),
            individual.getImageUrl(),
            individual.getParty(),
            individual.isLiving(),
            individual.getExternalIds(),
            individual.getSocialMedia()
        );
    }
}
```

### MemberDTO (Flattened)

```java
public record MemberDTO(
    UUID id,
    // From Individual (biographical)
    String firstName,
    String lastName,
    String middleName,
    String suffix,
    String fullName,
    LocalDate birthDate,
    LocalDate deathDate,
    String birthPlace,
    String imageUrl,
    boolean isLiving,
    // From CongressionalMember (Congress-specific)
    String bioguideId,
    String chamber,
    String state,
    String party,
    LocalDateTime congressLastSync
) {
    public static MemberDTO from(CongressionalMember member) {
        Individual ind = member.getIndividual();
        return new MemberDTO(
            member.getId(),
            ind.getFirstName(),
            ind.getLastName(),
            ind.getMiddleName(),
            ind.getSuffix(),
            ind.getFullName(),
            ind.getBirthDate(),
            ind.getDeathDate(),
            ind.getBirthPlace(),
            ind.getImageUrl(),
            ind.isLiving(),
            member.getBioguideId(),
            member.getChamber() != null ? member.getChamber().name() : null,
            member.getState(),
            ind.getParty(), // Use individual's current party
            member.getCongressLastSync()
        );
    }
}
```

### PresidencyDTO Update

```java
// In PresidencyDTO mapper, change from:
dto.setPresidentFullName(presidency.getPerson().getFullName());

// To:
dto.setPresidentFullName(presidency.getIndividual().getFullName());
```

### Backward Compatibility Test Example

```java
@Test
void memberEndpoint_returnsBackwardCompatibleResponse() throws Exception {
    mockMvc.perform(get("/api/members/{bioguideId}", "A000123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").exists())
        .andExpect(jsonPath("$.lastName").exists())
        .andExpect(jsonPath("$.bioguideId").value("A000123"))
        .andExpect(jsonPath("$.chamber").exists())
        .andExpect(jsonPath("$.state").exists())
        // Verify NO new unexpected fields that would break clients
        .andExpect(jsonPath("$.individualId").doesNotExist());
}
```

### Testing

**Test Locations:**
- `backend/src/test/java/org/newsanalyzer/controller/*ControllerTest.java`

**Test Requirements:**
- Verify JSON response structure matches existing API
- No new required fields added
- No field renames
- Endpoint paths unchanged

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
