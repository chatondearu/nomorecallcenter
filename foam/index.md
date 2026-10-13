# Project memory

Map of Content (MOC). Start here before product or architecture decisions.

## How to use

1. **New feature** → `cda-prd` → `prd/PRD-NNN-slug.md`, link ADRs.
2. **Implementation** → `cda-plan` → `plans/PLAN-NNN-slug.md`.
3. **Technical choice** → `foam/decisions/ADR-NNN-slug.md`.
4. **Sync kanban** → `import-kanban.sh` refreshes [[features/shipped|shipped]] / [[features/backlog|backlog]].

## Product

- [[product/vision|Vision & positioning]]

## Features

- [[features/index|Feature catalog]]
- [[features/shipped|Shipped]]
- [[features/backlog|Backlog]]

## Architecture

- _(Add topic notes under `foam/architecture/` and link ADRs here.)_

## Decisions (ADRs)

Index: [[decisions/index|All ADRs]]

- [[decisions/ADR-001-mvp-product-constraints|ADR-001]] — MVP product constraints (accepted)
- [[decisions/ADR-002-default-list-sources|ADR-002]] — Default FR list sources (accepted)
- [[decisions/ADR-003-android-client-stack|ADR-003]] — Android client stack (accepted)
- [[decisions/ADR-004-backend-stack|ADR-004]] — Backend stack BAJO-shaped (accepted)
- [[decisions/ADR-005-auth-model|ADR-005]] — Auth model MVP (accepted)
- [[decisions/ADR-006-mvp-stats-surface|ADR-006]] — MVP stats surface (accepted)
- [[decisions/ADR-007-block-ttl-model|ADR-007]] — Personal block TTL model (accepted)
- [[decisions/ADR-008-distribution-channels|ADR-008]] — Distribution channels MVP (accepted)

## PRDs & plans

| PRD | Status |
| --- | ------ |
| [[../prd/PRD-1-mvp-and-vision\|PRD-1]] MVP + vision | review |

- PRD template: `prd/template.md`
- Plan template: `plans/template.md`

## Tags

`#product` `#architecture` `#deployment` `#data` `#auth`
| [[../prd/PRD-1-mvp-and-vision|PRD-1-mvp-and-vision]] | draft |
