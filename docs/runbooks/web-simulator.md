# Runbook: Web Simulator (Dev, Test, Build, Deploy)

## Goal

Run the browser simulator locally, verify it against the quality gates, and
produce a production bundle (deploy target: Vercel or any static host).

## Steps

### 1. Install

```bash
npm install
```

`package-lock.json` is committed; CI uses `npm ci`.

### 2. Dev server

```bash
npm run dev       # Vite on :3000 (--host --port=3000)
```

The simulator shows a phone-frame preview plus code tabs
(`build.gradle.kts`, `BatchCompressionService`, `SettingsRepository`).

### 3. Type-check (gate #1)

```bash
npm run lint      # tsc --noEmit
```

### 4. Unit tests (gate #2)

```bash
npm test          # Vitest — src/lib/*.test.ts
```

### 5. Production build (gate #3)

```bash
npm run build     # vite build → dist/
npm run preview   # serve the production bundle locally to sanity-check
```

### 6. Env vars

The web simulator is a **pure static front-end** — it makes no network calls
and reads no runtime secrets, so no env vars are required. The production
bundle in `dist/` is fully self-contained and can be served by any static
host.

## CI

`.github/workflows/ci.yml` runs `npm ci → npm run lint → npm test → npm run
build` on push/PR to `main`. All stages must pass before merge.

## Known Failure Modes

- `tsc` errors → fix types, do not widen with `any`.
- Test failures → fix the code; never weaken the assertion.
- Build missing assets → confirm `public/` content and Tailwind plugin config
  are intact (`vite.config.ts`).