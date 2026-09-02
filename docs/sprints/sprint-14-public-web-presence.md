# Sprint 14 — Public Web Presence: Vercel Deploy, GitHub Metadata, Productized README (EXECUTED)

**Status:** EXECUTED (2026-09-02)
**Scope:** web presence + repo metadata (no Android version bump — product-toward-world, not a v0.6.0 release)

## Focus

Make the repo "more elite" and ready for the world to see:
1. **Better repository description** + homepage link + **topics** for GitHub discovery.
2. **Live website deploy** of the web simulator via **Vercel** (public URL).
3. **Topics** added.
4. Make it a **product ready for the world**: productized README, live-site link, automated deploy.

## Governance framing

- **On-device invariant holds.** The deployed surface is the **web simulator harness**
  (ADR-006), a pure static front-end with **no network calls and no runtime secrets**.
  It is intentionally separate from the no-INTERNET Android app; the CI no-INTERNET
  guardrail continues to enforce the native manifest.
- **Fail-closed deploy.** `deploy-web.yml` runs the quality gates (lint/test/build),
  then deploys **only if** `VERCEL_TOKEN/ORG_ID/PROJECT_ID` secrets are present;
  absent secrets ⇒ gates still pass, no deploy, site unchanged.

## Deliverables & Evidence

### Layer 1 — Web polish (SEO + favicon + deploy config)

- `index.html`: descriptive title/description, `og:site_name`, twitter meta,
  `theme-color`, `favicon` link.
- New `public/favicon.svg` (brand mark).
- New `vercel.json` (framework vite, `outputDirectory dist`, SPA fallback rewrite).
- `.gitignore`: `.vercel/` (org/project ids flow through CI secrets).
- Web package bumped to `v0.4.0` (web-presence milestone; native stays v0.5.0).

Verified: `npm run lint` clean, `npm test` 18/18, `npm run build` ok.

### Layer 2 — Vercel production deploy

- First attempt pushed 361 MB (whole repo) → added `.vercelignore` (excludes
  `.git/.github/node_modules/dist/build/app/docs` + keystore/secret material).
- `vercel --prod --yes --name shrinkmedia` → **production**
  `https://shrinkmedia.vercel.app` (aliased).
- Verified: `curl` HTTP 200; title/description/favicon present in served HTML;
  SPA fallback (deep link) returns 200.

### Layer 3 — GitHub metadata (repo-level, not a commit)

- Description: "ShrinkMedia — private on-device media & document toolkit for Android.
  Compress images & videos, build/split PDFs, OCR scanned text. No uploads, no INTERNET
  permission in the app."
- Homepage: `https://shrinkmedia.vercel.app`.
- **20 topics**: android · kotlin · jetpack-compose · media-compression ·
  image-compression · video-compression · ffmpeg · pdf-tools · ocr ·
  machine-learning · privacy · on-device · offline-first · mediastore · datastore ·
  react · typescript · web-simulator · vercel · github-actions.
  (A `datasnote` typo introduced then corrected — verified absent.)

### Layer 4 — Productized README

- Live-site link + "Live Site" section (honest ADR-006 harness framing).
- Badges (Vercel live, MIT, Kotlin/Compose, PRs welcome).
- Status rows for Sprint 13 v0.5.0 and Sprint 14 live-presence.
- Web build/run/deploy instructions incl. CI auto-deploy note.

### Layer 5 — CI auto-deploy (`deploy-web.yml`)

Fail-closed, path-scoped to web sources, gates-then-deploy-if-secrets. Org/project
ids captured for secret provisioning: `team_sGSR187e4tniaRMCYJ8yb0JG` /
`prj_y711FCFxfaMlAkTrBR6R85XUBlz9`.

## Validation & Verification Checklist

- [x] Web gates green: `npm run lint`, `npm test` (18), `npm run build`
- [x] `shrinkmedia.vercel.app` live — HTTP 200, correct title/description/favicon, SPA fallback ok
- [x] `gh repo view` — description + homepage + 20 topics (no `datasnote` typo)
- [x] README renders with live-site link + badges
- [x] `deploy-web.yml` fail-closed (secrets-absent ⇒ no deploy) — code-complete; runtime verification pending secret provision
- [x] `.vercelignore` excludes all secrets (keystore.properties, *.jks/*.keystore) and heavyweight dirs
- [x] No `.env*` / secret material tracked
- [x] Android gates re-run (untouched path stays green): compileDebugKotlin / testDebugUnitTest
- [x] All commits SSH-signed; pushed to `origin/main`

## Honest Gaps (not hidden)

- **CI auto-deploy runtime not yet exercised**: `VERCEL_*` secrets are not in the repo
  (I can't mint a Vercel token). Until you set them, deploys are manual via the CLI;
  the workflow is fail-closed so CI stays green without them.
- **OG social-card image**: no dedicated 1200×630 preview image — the site cards show the
  default Vercel/GitHub rendering. Staged (descoped herein, `docs/ideas.md`).

## Cross-Reference

- Evidence: `docs/evidence/2026-09-02_public_web_presence.md`
- `docs/current-state.md` (C20)
- `docs/architecture.md` (Web simulator row)
- `docs/decisions.md` (D008 — web presence / fail-closed auto-deploy)
- `docs/runbooks/web-simulator.md` (deploy steps + CI)
- `CHANGELOG.md` v0.4.0 (web entry)
- `docs/sprint-cross-reference.md` (Sprint 14 row)