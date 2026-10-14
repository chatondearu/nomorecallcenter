# API

Base URL defaults to `http://localhost:3000`. Session cookies come from Better Auth (`/api/auth/*`).

## Health

`GET /api/health` — public. Returns `{ ok: true, ... }` when the API is up.

## Auth (Better Auth)

- `POST /api/auth/sign-up/email` — first user becomes admin; later signups need `inviteToken`.
- `POST /api/auth/sign-in/email` — email/password session cookie.
- `GET /api/me` — authenticated; returns `{ user: { id, email, name, role } }`.
- `POST /api/invites` — admin only; create invite `{ email? }` → `{ token, ... }`.

## Lists sync

`GET /api/lists/sync?since=<n>` — **authenticated** (member or admin).

| Query | Meaning |
| ----- | ------- |
| `since` | Non-negative integer. Default `0`. |

**Contract**

- `since=0`: full snapshot of **active** shared list entries.
- `since>0`: entries with `revision > since`, including **inactive** tombstones (`active: false`).

Response:

```json
{
  "revision": 3,
  "since": 0,
  "entries": [
    {
      "id": "uuid",
      "pattern": "089200",
      "source": "arcep-majnum",
      "attribution": "Derived from ARCEP MAJNUM … Licence Ouverte 2.0. …",
      "sourceDate": "2026-10-14",
      "active": true,
      "revision": 3
    }
  ]
}
```

Unauthenticated callers receive `401`.

## Personal rules (TTL)

Authenticated member/admin. Server stores `expiresAt` (null = permanent). Presets: `1h` | `24h` | `7d` | `30d` | `permanent`. Match types: `exact` | `prefix`.

| Method | Path | Body / notes |
| ------ | ---- | ------------ |
| `POST` | `/api/rules` | `{ pattern, matchType, ttlPreset }` → `{ rule }` |
| `GET` | `/api/rules/sync` | Active rules only (expired omitted) → `{ rules: [...] }` |
| `POST` | `/api/rules/:id/renew` | optional `{ ttlPreset }` — restart TTL from now |
| `POST` | `/api/rules/:id/make-permanent` | sets `ttlPreset=permanent`, `expiresAt=null` |
| `DELETE` | `/api/rules/:id` | owner only |

Rule object: `{ id, pattern, matchType, ttlPreset, expiresAt, active, createdAt, updatedAt }`.

## Proposals (moderated)

Proposing never mutates the shared list until an admin approves (ADR-007).

| Method | Path | Who | Notes |
| ------ | ---- | --- | ----- |
| `POST` | `/api/proposals` | member | `{ personalRuleId }` **or** `{ pattern, matchType }` → pending proposal |
| `GET` | `/api/proposals?status=pending` | admin | `status`: `pending` (default) \| `approved` \| `rejected` \| `all` |
| `POST` | `/api/proposals/:id/approve` | admin | upserts `list_entries`, bumps list revision |
| `POST` | `/api/proposals/:id/reject` | admin | closes proposal; list unchanged |

Members receive `403` on approve/reject/list.
