# Vision & positioning

#product

## What this product is

Open-source Android call filter for **call-center number ranges** and **spam**, with a **self-hosted multi-user backend**, default FR call-center lists (ARCEP-derived), moderated community proposals, temporary or permanent personal blocks, and analytics that inform humans — not silent auto-publish.

Normative MVP: [[../../prd/PRD-1-mvp-and-vision|PRD-1]].

## Audience

- **Primary users**: people tired of sequential number blocking for call centers / spam.
- **Operators / admins**: anyone running an instance who moderates proposals and configures retention.

## Non-goals (MVP)

See [[../../prd/PRD-1-mvp-and-vision|PRD-1]] and [[../decisions/ADR-001-mvp-product-constraints|ADR-001]].

## 12–24 month direction (non-normative)

- EU list expansion; optional Play Store; ML/scoring as **suggestions** for moderators
- Instance federation; iOS if APIs allow; SMS/RCS; STIR/SHAKEN
- OIDC/SSO; GraphQL only if REST hurts on mobile
- Stronger ops monitoring beyond the admin event feed

## Related

- [[../decisions/ADR-001-mvp-product-constraints|ADR-001]] … [[../decisions/ADR-008-distribution-channels|ADR-008]]
- [[../features/index|Feature catalog]]
