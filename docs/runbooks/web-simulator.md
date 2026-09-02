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

## Deploy to Vercel (live site)

The simulator is deployed to **https://shrinkmedia.vercel.app** (project
`dmuhor01/shrinkmedia`).

**Manual production deploy:**
```bash
vercel --prod --yes --name shrinkmedia   # vite build → dist/ → production
```

`vercel.json` configures the framework, `outputDirectory` (`dist`) and the SPA
fallback rewrite; `.vercelignore` keeps the upload lean (excludes
`.git/.github/node_modules/dist/build/app/docs` and all keystore/secret
material). Local `.vercel/` link artifacts are gitignored.

**CI auto-deploy** (`.github/workflows/deploy-web.yml`): on push to `main`
(path-scoped to web sources) it runs lint + tests + build, then deploys to
production **only if** the `VERCEL_TOKEN`, `VERCEL_ORG_ID`,
`VERCEL_PROJECT_ID` secrets are present — otherwise CI stays green and the
site is untouched (fail-closed, D008). The org/project ids are
`team_sGSR187e4tniaRMCYJ8yb0JG` / `prj_y711FCFxfaMlAkTrBR6R85XUBlz9` (set the
token yourself in Vercel account settings → Tokens).

The deployed site is a **pure static front-end**: no network calls, no runtime
secrets, and it stays separate from the no-INTERNET Android app (ADR-006).

## Known Failure Modes

- `tsc` errors → fix types, do not widen with `any`.
- Test failures → fix the code; never weaken the assertion.
- Build missing assets → confirm `public/` content and Tailwind plugin config
  are intact (`vite.config.ts`).