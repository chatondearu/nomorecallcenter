# syntax=docker/dockerfile:1
# Build context: monorepo root.

FROM node:22-bookworm-slim AS base

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
ENV CI=true
ENV COREPACK_HOME="/var/cache/corepack"
ENV XDG_CACHE_HOME="/var/cache/xdg"
ENV PNPM_PACKAGE_IMPORT_METHOD=copy

RUN apt-get update \
  && apt-get install -y --no-install-recommends ca-certificates wget \
  && rm -rf /var/lib/apt/lists/* \
  && mkdir -p "$COREPACK_HOME" "$XDG_CACHE_HOME" "$PNPM_HOME" \
  && corepack enable \
  && corepack prepare pnpm@10.15.0 --activate \
  && chmod -R a+rwX "$COREPACK_HOME" "$XDG_CACHE_HOME"

FROM base AS build

WORKDIR /workspace

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml tsconfig.base.json ./
COPY design/tokens ./design/tokens
COPY modules/db/package.json ./modules/db/
COPY modules/shared/package.json ./modules/shared/
COPY modules/app/package.json ./modules/app/

# Skip nuxt prepare until sources are copied.
RUN node -e "for (const p of ['modules/app/package.json','modules/shared/package.json']) { const fs=require('fs'); const j=JSON.parse(fs.readFileSync(p,'utf8')); if (j.scripts) j.scripts.postinstall='echo skip-nuxt-prepare'; fs.writeFileSync(p, JSON.stringify(j,null,2)+'\\n') }"

RUN --mount=type=cache,id=nmcc-pnpm-store,target=/pnpm/store \
  pnpm install --frozen-lockfile --filter @nmcc/app...

COPY modules/db ./modules/db
COPY modules/shared ./modules/shared
COPY modules/app ./modules/app

RUN --mount=type=cache,id=nmcc-pnpm-store,target=/pnpm/store \
  pnpm install --frozen-lockfile --filter @nmcc/app...

RUN pnpm --filter @nmcc/shared exec nuxt prepare \
  && pnpm --filter @nmcc/app exec nuxt prepare \
  && pnpm --filter @nmcc/app build

FROM base AS production

WORKDIR /app

ENV NODE_ENV=production \
    HOST=0.0.0.0 \
    PORT=3000

RUN groupadd --system --gid 1001 nodejs \
  && useradd --system --uid 1001 --gid nodejs nuxt

COPY --from=build --chown=nuxt:nodejs /workspace/modules/app/.output ./.output

USER nuxt
EXPOSE 3000

CMD ["node", ".output/server/index.mjs"]
