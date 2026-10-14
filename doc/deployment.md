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

## Android client (sideload)

See [`android/README.md`](../android/README.md).

1. Build debug APK with JDK 17 + Android SDK 35: `cd android && ./gradlew :app:assembleDebug`.
2. Install the APK; set **Instance URL** to your API (emulator → host often `http://10.0.2.2:3000`).
3. Sign in (email/password). Sync pulls shared lists + personal rules.
4. Grant **Call Screening** role in-app — the UI does not claim “protected” until the role is held.
5. Matcher JVM tests (no SDK): `./gradlew :matcher:test`.
