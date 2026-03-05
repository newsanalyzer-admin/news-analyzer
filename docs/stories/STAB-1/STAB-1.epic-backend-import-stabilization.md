# Epic STAB-1: Backend Import & Sync Stabilization

## Status

**In Progress**

---

## Epic Summary

This epic addresses 24 identified defects and fragility issues across all 8 backend import/sync pathways. Work is organized into three priority tiers: Critical (actively breaking or data-corrupting), High (will break on external API changes or edge cases), and Medium (operational robustness improvements). All other epics (ARCH-1, KB-2, Phase 2) are paused until this epic is complete.

---

## Business Value

- **Reliability**: Fix actively broken sync pathways (Federal Register agency sync confirmed failing)
- **Security**: Close XXE vulnerability in GOVMAN XML import
- **Resilience**: Prevent future breakage when external APIs evolve
- **Data Integrity**: Eliminate race conditions that allow concurrent imports to corrupt data
- **Operational Visibility**: Surface errors clearly instead of masking them as "not found"

---

## Context / Trigger

The Federal Register Government Agency sync was reported failing on 2026-02-26 with:
```
Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)
```

A full audit of all import/sync services revealed 6 Critical, 8 High, and 10 Medium severity issues across the codebase. Root cause of the reported failure: the `FederalRegisterAgency` DTO defines `logo` as `String` but the live API returns it as a nested JSON object for some agencies.

---

## Scope

### Affected Services (8 import/sync pathways)

| Pathway | Type | Issues Found |
|---------|------|-------------|
| Federal Register Agency Sync | REST API | C1, C2, C3, C6, M1 |
| Federal Register Document Import | REST API | C3, C6, H7, M1 |
| Congress.gov Member Import | REST API | C6, H1, H2, M1, M2 |
| Committee Sync | REST API | H3 |
| GOVMAN XML Import | File Upload | C4, C5, H8, M9 |
| US Code XML Import | File Upload | C5, H6, M10 |
| PLUM CSV Import | URL Download | C5, H5, M4 |
| FJC CSV Import | URL Download | C5, H4, M5 |
| Gov Org CSV Import | File Upload | C5, M6 |

### Out of Scope

- New features or UI changes
- Database schema changes
- Frontend modifications (unless needed for error display)
- ARCH-1, KB-2, Phase 2 work

---

## Stories — Tier 1: Critical (Actively Breaking)

| Story | Title | Points | Audit Refs | Target |
|-------|-------|--------|------------|--------|
| STAB-1.1 | Fix Federal Register DTO Deserialization | 3 | C1, C2, C3 | FederalRegisterAgency DTO, FederalRegisterDocument |
| STAB-1.2 | Fix XXE Vulnerability in GOVMAN XML Import | 2 | C4 | GovmanXmlImportService |
| STAB-1.3 | Fix Concurrent Import Race Conditions | 3 | C5 | AdminSyncController, GovmanImportController, StatuteImportController |
| STAB-1.4 | Add HTTP Timeouts to API Clients | 2 | C6 | FederalRegisterClient, CongressApiClient |

**Tier 1 Total: 10 points**

---

## Stories — Tier 2: High (Breaks on External Changes)

| Story | Title | Points | Audit Refs | Target |
|-------|-------|--------|------------|--------|
| STAB-1.5 | Harden Congress.gov Import Services | 5 | H1, H2, H3 | CongressMemberDetailDTO, MemberSyncService, TermSyncService, CommitteeSyncService |
| STAB-1.6 | Harden CSV Import Services | 5 | H4, H5, M4, M5, M6 | FjcCsvImportService, PlumCsvImportService, GovOrgCsvImportService |
| STAB-1.7 | Fix XML Parser Robustness and Resource Management | 3 | H6, H7, H8 | UslmXmlParser, FederalRegisterClient, GovmanImportController |

**Tier 2 Total: 13 points**

---

## Stories — Tier 3: Medium (Operational Robustness)

| Story | Title | Points | Audit Refs | Target |
|-------|-------|--------|------------|--------|
| STAB-1.8 | Improve HTTP Retry Logic and Error Differentiation | 3 | M1, M2 | FederalRegisterClient, CongressApiClient |
| STAB-1.9 | Fix Security Gaps and Silent Failure Alerting | 3 | M3, M7, M8 | Schedulers, JudgeController, CSV date parsing |
| STAB-1.10 | Performance and Memory Optimizations | 3 | M9, M10 | GovmanXmlImportService, UsCodeImportService |

**Tier 3 Total: 9 points**

---

## Epic Totals

| Tier | Stories | Points |
|------|---------|--------|
| Tier 1: Critical | 4 | 10 |
| Tier 2: High | 3 | 13 |
| Tier 3: Medium | 3 | 9 |
| **Total** | **10** | **32** |

---

## Dependencies

- **Blocks**: ARCH-1, KB-2, Phase 2 (all paused until STAB-1 complete)
- **Blocked By**: None
- **External**: Federal Register API (live), Congress.gov API (live), OPM PLUM CSV (live), FJC CSV (live)

---

## Completion Criteria

1. All 8 import/sync pathways execute without errors against live external sources
2. Federal Register agency sync specifically confirmed working (the reported bug)
3. XXE vulnerability closed
4. No race conditions possible on concurrent import requests
5. All API clients have configured timeouts
6. Existing test suites pass; new tests added for each fix
7. Manual verification of each sync/import via admin dashboard

---

## Audit Reference

Full audit findings with 24 itemized issues (C1-C6, H1-H8, M1-M10) are documented in the audit that preceded this epic. Each story references specific audit item IDs for traceability.

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Epic created from full backend import/sync audit | Sarah (PO) |
