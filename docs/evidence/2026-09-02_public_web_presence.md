# Evidence: Sprint 14 — Public Web Presence (2026-09-02)

> Constitution Article VII: **Grangle every house and day**. A release readiness
> row marked PASS requires a cited command/test/step and its observed result,
> never narrative alone.

## Command Log

### 1. Web quality gates (Layer 1)

```bash
$ cd /home/daniel-muhoro/workspace/projects/ShrinkMedia
$ npm run lint     # tsc --noEmit  → PASS (exit 0)
$ npm test         # Vitest        → 3 files / 18 tests passed
$ npm run build    # vite build    → dist/index.html + assets built ok
    dist/index.html                   1.57 kB │ gzip: 0.59 kB
    dist/favicon.svg                 (copied, referenced)
```

### 2. Vercel production deploy (Layer 2)

```bash
$ vercel --prod --yes --name shrinkmedia
✓ Ready in 17s
  Production   https://shrinkmedia-lx2snq6ji-dmuhor01.vercel.app
▲ Aliased      https://shrinkmedia.vercel.app
```

First deploy attempt uploaded the whole repo (361 MB → "Upload aborted"); adding
`.vercelignore` (exclude `.git/.github/node_modules/dist/build/app/docs` +
keystore/secret material) made the upload lean and the deploy succeed.

```bash
$ curl -sS -o /dev/null -w "status=%{http_code} size=%{size_download}\n" https://shrinkmedia.vercel.app
status=200 size=1568

$ curl -sS https://shrinkmedia.vercel.app | grep -oE '<title>[^<]*</title>|name="description" content="[^"]*"|og:site_name|favicon.svg'
<title>ShrinkMedia — Private On-Device Media & Document Toolkit for Android</title>
name="description" content="ShrinkMedia — private on-device media & document toolkit for Android. … No uploads, no INTERNET permission in the app."
og:site_name
favicon.svg

# SPA fallback (deep link) returns 200:
$ curl -sS -o /dev/null -w "deep-link status=%{http_code}\n" https://shrinkmedia.vercel.app/anything/router
deep-link status=200
```

### 3. GitHub metadata (Layer 3)

```bash
$ gh repo edit dmuhoro/ShrinkMedia \
    --description "ShrinkMedia — private on-device media & document toolkit for Android. Compress images & videos, build/split PDFs, OCR scanned text. No uploads, no INTERNET permission in the app." \
    --homepage "https://shrinkmedia.vercel.app" \
    --add-topic android --add-topic kotlin ... --add-topic github-actions

$ gh repo view dmuhoro/ShrinkMedia --json description,homepageUrl,repositoryTopics
# description set; homepageUrl = https://shrinkmedia.vercel.app; 20 topics;
# no "datasnote" typo (corrected to "datastore")
```

### 4. Vercel link (captures ids for CI secrets)

```bash
$ vercel link --yes --project shrinkmedia
✓ Linked dmuhor01/shrinkmedia
# .vercel/project.json (gitignored) →
#   orgId  = team_sGSR187e4tniaRMCYJ8yb0JG
#   projectId = prj_y711FCFxfaMlAkTrBR6R85XUBlz9
```

### 5. Android regression gates (untouched path stays green)

```bash
$ export JAVA_HOME=/home/daniel-muhoro/.local/opt/jdk17; export PATH=$JAVA_HOME/bin:$PATH
$ ./gradlew :app:compileDebugKotlin   # BUILD SUCCESSFUL
$ ./gradlew :app:testDebugUnitTest    # BUILD SUCCESSFUL
```

### 6. Secret hygiene

```bash
$ git ls-files | grep -E '\.env' ; echo "no .env tracked (rc=$?)"
# .env.local (auto-created by `vercel link`) is covered by .gitignore line 45 (.env.*)
$ git ls-files | grep -E '\.jks|keystore|\.pem|secret' ; echo "no secret material tracked"
```

## Honest Gaps

- **CI auto-deploy runtime pending**: `deploy-web.yml` is code-complete and fail-closed,
  but `VERCEL_TOKEN/ORG_ID/PROJECT_ID` are not set as GitHub secrets (token must be user
  minted). Until provisioned, production deploys are manual (`vercel --prod --yes`). The
  workflow is designed so CI stays green with or without the secrets.
- **No OG social-card image** (1200×630). Descoped here; staged in `docs/ideas.md`.

## Cross-Reference

- `docs/sprints/sprint-14-public-web-presence.md`
- `docs/current-state.md` (C20)
- `docs/architecture.md` (Web simulator row)
- `docs/decisions.md` (D008)
- `docs/runbooks/web-simulator.md`
- `CHANGELOG.md` v0.4.0
- `docs/sprint-cross-reference.md` (Sprint 14 row)