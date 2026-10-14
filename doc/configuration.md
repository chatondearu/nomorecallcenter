# Configuration

Values consumed by the stack (see also `.env.example`).

| Variable | Used by | Notes |
| -------- | ------- | ----- |
| `DATABASE_URL` | `@nmcc/db` migrate/seed, API | Postgres connection string |
| `POSTGRES_*` | Compose | DB name/user/password/port |
| `API_PORT` | Compose / scripts | Host port for the API (default `3000`) |
| `BETTER_AUTH_SECRET` | API | Required in real deployments |
| `BETTER_AUTH_URL` | API | Public base URL for Better Auth |
| `BOOTSTRAP_ADMIN_EMAIL` | API | Optional; restrict first admin email |
| `ARCEP_MAJNUM_URL` | `pnpm lists:seed` | Override MAJNUM CSV URL (default ARCEP extranet) |
| `ARCEP_SEED_FIXTURE` | `pnpm lists:seed` | Set `1` to seed a single `0892` fixture without network |
| `EVENTS_RETENTION_DAYS` | API | Days to keep call events (default `90`); older rows purged/omitted |
