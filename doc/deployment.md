# Deployment

## Compose

```bash
pnpm install
docker compose up -d --build
curl -fsS http://127.0.0.1:3000/api/health
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm db:migrate
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm lists:seed
```

`pnpm lists:seed` fetches ARCEP MAJNUM (Licence Ouverte 2.0), writes `0892*` demarchage prefixes into `list_entries`, and bumps `schema_meta.lists.revision`. Use `ARCEP_SEED_FIXTURE=1` or `--fixture` offline.

Acceptance: `pnpm lists:acceptance` (wipe volumes → migrate → admin session → seed → `GET /api/lists/sync`).

Services: Postgres 17 + API image from `modules/app/Dockerfile`. Ports and env defaults match `.env.example`.
