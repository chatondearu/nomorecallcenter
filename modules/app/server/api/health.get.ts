export default defineEventHandler(() => {
  return {
    ok: true,
    service: 'nmcc-api',
    ts: new Date().toISOString(),
  }
})
