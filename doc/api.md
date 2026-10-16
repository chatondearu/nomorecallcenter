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
| `POST` | `/api/rules` | `{ pattern, matchType, ttlPreset, effect? }` → `{ rule }`. `effect` is `allow` or `block` (default `block`). The same pattern may exist once per effect. |
| `GET` | `/api/rules/sync` | Active rules only (expired omitted) → `{ rules: [...] }` |
| `POST` | `/api/rules/:id/renew` | optional `{ ttlPreset }` — restart TTL from now |
| `POST` | `/api/rules/:id/make-permanent` | sets `ttlPreset=permanent`, `expiresAt=null` |
| `DELETE` | `/api/rules/:id` | owner only |

Rule object: `{ id, pattern, matchType, effect, ttlPreset, expiresAt, active, createdAt, updatedAt }`.

Allow rules are personal only. `POST /api/proposals` with an allow rule returns `400` (shared lists stay block-only).

## Proposals (moderated)

Proposing never mutates the shared list until an admin approves (ADR-007).

| Method | Path | Who | Notes |
| ------ | ---- | --- | ----- |
| `POST` | `/api/proposals` | member | `{ personalRuleId }` **or** `{ pattern, matchType }` → pending proposal |
| `GET` | `/api/proposals?status=pending` | admin | `status`: `pending` (default) \| `approved` \| `rejected` \| `all` |
| `POST` | `/api/proposals/:id/approve` | admin | upserts `list_entries`, bumps list revision |
| `POST` | `/api/proposals/:id/reject` | admin | closes proposal; list unchanged |

Members receive `403` on approve/reject/list.

## Call events + stats

Privacy (ADR-010): ingest may store the **clear normalized number** (`number`). Member stats, aggregates, and the default admin feed return `hash` (SHA-256 hex of those digits) and never the clear value. An instance admin reveals one event with `POST /api/admin/events/:id/reveal`. Prefix stays 2–8 digits. Retention via `EVENTS_RETENTION_DAYS` (default `90`) deletes the row, clear number included. Old rows without a stored number reveal as `""`.

### Ingest

`POST /api/events` — authenticated.

Body:

```json
{
  "prefix": "0892",
  "number": "0612345678",
  "hash": "<64-char sha256 hex>",
  "action": "blocked",
  "occurredAt": "2026-10-14T12:00:00.000Z"
}
```

- At least one of `prefix`, `hash` (alias `numberHash`), or `number` is required.
- `number`: clear caller (2–15 digits). Stored normalized (FR national `0…` → `33…`). The display hash is SHA-256 of those digits and replaces a client-supplied hash.
- `action`: `screened` | `blocked` | `allowed`.
- `occurredAt` optional (defaults to server now).
- Fields `phone` and `fullNumber` → `400`. A prefix longer than 8 digits → `400`.

Response: `{ event: { id, prefix, hash, action, occurredAt, userId } }` — no clear number.

### Admin reveal

`POST /api/admin/events/:id/reveal` — **admin only** (`403` for members).

Returns `{ number }` for that one event. `number` is `""` when the row has no stored clear value. Not a list endpoint.

### Personal stats

`GET /api/stats/me` — authenticated.

```json
{
  "scope": "personal",
  "retentionCutoff": "...",
  "counters": {
    "7d": { "screened": 0, "blocked": 1, "allowed": 0 },
    "30d": { "screened": 0, "blocked": 1, "allowed": 0 }
  },
  "topPrefixes": [{ "prefix": "0892", "count": 1 }],
  "recent": [{ "id": "...", "prefix": "0892", "hash": null, "action": "blocked", "occurredAt": "..." }]
}
```

### Instance stats

`GET /api/stats/instance` — authenticated (member or admin).

Same shape as personal with `scope: "instance"` and aggregates across users. Recent items omit `userId` for members.

Admin extras on the same response:

```json
{
  "proposalFunnel": { "submitted": 3, "pending": 1, "approved": 1, "rejected": 1 },
  "eventVolume": 42
}
```

`submitted` = pending + approved + rejected.

### Admin live feed (poll)

`GET /api/admin/events/feed?limit=50` — **admin only** (`403` for members).

Returns `{ retentionCutoff, events: [{ id, prefix, hash, action, userId, occurredAt }] }` ordered newest-first (max `limit` 200).
