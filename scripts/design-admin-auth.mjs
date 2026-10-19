#!/usr/bin/env node
/**
 * Save Playwright storage state after admin login (for design-loop captures).
 */
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { launchBrowser } from './playwright-browser.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const base = (process.env.DEV_URL_ADMIN || 'http://127.0.0.1:3001').replace(/\/$/, '')
const email = process.env.DESIGN_ADMIN_EMAIL || 'admin@example.com'
const password = process.env.DESIGN_ADMIN_PASSWORD || 'AdminPass123!'
const outDir = path.join(root, '.playwright')
const statePath = path.join(outDir, 'admin.json')

await fs.mkdir(outDir, { recursive: true })

const browser = await launchBrowser()
const context = await browser.newContext()
const page = await context.newPage()

await page.goto(`${base}/login`, { waitUntil: 'networkidle' })
await page.getByLabel('E-mail').fill(email)
await page.getByLabel('Mot de passe').fill(password)
await page.getByRole('button', { name: 'Se connecter' }).click()
await page.waitForURL(url => !url.pathname.endsWith('/login'), { timeout: 15_000 })

await context.storageState({ path: statePath })
await browser.close()

console.log(`[design:auth] saved ${statePath}`)
