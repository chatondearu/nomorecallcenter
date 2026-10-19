#!/usr/bin/env node
/**
 * Capture all routes from design/routes.json (cda-design-loop).
 */
import { spawnSync } from 'node:child_process'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const routesPath = path.join(root, 'design', 'routes.json')
const outRoot = path.join(root, 'design', 'shots')

const raw = JSON.parse(await fs.readFile(routesPath, 'utf8'))

function resolveBase(envKey, fallback) {
  const fromEnv = process.env[envKey]
  if (fromEnv)
    return fromEnv.replace(/\/$/, '')
  return fallback.replace(/\/$/, '')
}

function slug(routePath) {
  if (routePath === '/')
    return 'home'
  return routePath.replace(/^\//, '').replace(/\//g, '_')
}

const groups = [
  {
    name: 'site',
    base: resolveBase(raw.site.baseEnv, raw.site.baseDefault),
    paths: raw.site.paths,
    storage: null,
  },
  {
    name: 'admin',
    base: resolveBase(raw.admin.baseEnv, raw.admin.baseDefault),
    paths: raw.admin.paths,
    storage: raw.admin.storageState
      ? path.join(root, raw.admin.storageState)
      : null,
  },
]

await fs.mkdir(outRoot, { recursive: true })

const shotScript = path.join(root, 'scripts', 'cda-design-shot.sh')

for (const group of groups) {
  for (const routePath of group.paths) {
    const url = `${group.base}${routePath}`
    const dir = path.join(outRoot, group.name, slug(routePath))
    const args = [shotScript, url, dir]
    if (group.storage) {
      try {
        await fs.access(group.storage)
        args.push(group.storage)
      }
      catch {
        console.warn(`[design:capture] skip auth for ${url} — run pnpm design:auth first`)
      }
    }
    console.log(`[design:capture] ${url} → ${dir}`)
    const result = spawnSync('bash', args, { stdio: 'inherit', cwd: root })
    if (result.status !== 0)
      process.exit(result.status ?? 1)
  }
}

console.log(`[design:capture] done → ${outRoot}`)
