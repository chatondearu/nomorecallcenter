import { chromium } from 'playwright'

/** Prefer Nix Chromium when PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH is set. */
export async function launchBrowser() {
  const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH?.trim()
  return chromium.launch(executablePath ? { executablePath } : {})
}
