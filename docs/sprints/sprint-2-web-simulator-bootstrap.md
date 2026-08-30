# Sprint 2 — Web Simulator Bootstrap

**Theme/Result:** Vite + React + TypeScript + Tailwind browser surface that
simulates the compressor UI and exposes the native implementation as code tabs.

## Summary

| Capability | Status | Evidence |
|------------|--------|----------|
| Vite dev/build toolchain | ✅ | `vite.config.ts`, `tsconfig.json`, `package.json` |
| Phone-frame live preview + batch/undo/audit flows | ✅ | `src/App.tsx` |
| Code tabs (gradle / service / datastore) | ✅ | `src/App.tsx` |
| Gemini helper dependency, env template | ✅ | `package.json`, `.env.example` (placeholder keys only) |

## Part 1 — Simulator surface

**Problem:** The native app needed a browser-previewable twin for demos and
fast UX iteration, without pretending to do real compression.

**Fix:** Built the simulator per ADR-006 — simulated numbers explicitly
labelled, pure helpers later extracted to `src/lib/` (Sprint 5).

**Files:**
- `package.json`, `tsconfig.json`, `vite.config.ts`, `index.html`
- `src/App.tsx`, `src/main.tsx`, `src/index.css`
- `.env.example`

## Test Evidence Summary

| Test | Result |
|------|--------|
| `npm install` — 0 vulnerabilities | ✅ (Sprint 5 re-verified) |
| `npm run build` — production bundle | ✅ (Sprint 5 re-verified) |

## Artifacts

- Commits: `feat: initialize Media Compressor web project`.

## Cross-Reference

ADR-006 (web simulator harness), `docs/current-state.md` C2. Simulator encodes
honesty: no fake capability claims (Article VI.7).