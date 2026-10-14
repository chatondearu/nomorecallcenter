# Configuration

Values consumed by the stack (see also `.env.example`).

| Variable | Used by | Notes |
| -------- | ------- | ----- |
| `DATABASE_URL` | `@nmcc/db` migrate/seed, API | Postgres connection string |
| `POSTGRES_*` | Compose | DB name/user/password/port |
| `API_PORT` | Compose / scripts | Host port for the API (default `3000`) |
| `DOCKER_IMAGE_API` | Compose | API image name/tag (default `nmcc-api`; GHCR e.g. `ghcr.io/<owner>/nmcc-api:main`) |
| `BETTER_AUTH_SECRET` | API | Required in real deployments |
| `BETTER_AUTH_URL` | API | Public base URL for Better Auth |
| `BOOTSTRAP_ADMIN_EMAIL` | API | Optional; restrict first admin email |
| `ARCEP_MAJNUM_URL` | `pnpm lists:seed` | Override MAJNUM CSV URL (default ARCEP extranet) |
| `ARCEP_SEED_FIXTURE` | `pnpm lists:seed` | Set `1` to seed a single `0892` fixture without network |
| `EVENTS_RETENTION_DAYS` | API | Days to keep call events (default `90`); older rows purged/omitted |
| `ADMIN_URL` | API (Better Auth) | Optional public admin origin (e.g. `http://localhost:3001`) added to trusted origins |
| `API_PROXY_TARGET` | `@nmcc/admin` | Upstream API for Nitro `/api/**` proxy (default `http://127.0.0.1:3000`) |
| `NUXT_PUBLIC_API_BASE` | `@nmcc/admin` | Leave empty to use same-origin proxy; set only for special absolute-API setups |
