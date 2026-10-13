# ADR-005 — Auth model for multi-user instances (MVP)

- **Status**: accepted
- **Date**: 2026-10-13
- **Tags**: #auth #mvp #explore

## Context

MVP instances are multi-user and self-hosted (ADR-001, ADR-004). Android and Nuxt admin must share one auth story. BAJO already uses Better Auth + Drizzle.

## Decision

1. **Better Auth** with **Drizzle adapter** on PostgreSQL.
2. **Bootstrap**: first account on an empty instance becomes **admin** (optional harden via `BOOTSTRAP_ADMIN_EMAIL`).
3. **Join**: **invite-only** after bootstrap (admin-issued link/code) — no open public signup for MVP.
4. **Credentials**: **email + password** for Android and admin web. OIDC/SSO/magic-link deferred.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Open registration | Spam / abuse on demo and home instances |
| OAuth-only (Discord/Google) | Bad fit for private self-host; extra IdP dependency |
| Custom JWT stack | Higher maintenance; BAJO already standardized on Better Auth |

## Consequences

- **Positive**: Matches ADR-004 stack; clear admin bootstrap; controlled membership.
- **Negative**: Admins must issue invites; password reset flow required eventually.
- **Constraint**: Android REST clients authenticate via Better Auth session/token against Nitro API.

## References

- [Decide: Auth model for multi-user instances](https://github.com/chatondearu/nomorecallcenter/issues/7)
- [[ADR-004-backend-stack|ADR-004]]
- BAJO `ADR-003-better-auth`
