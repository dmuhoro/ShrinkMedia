# ShrinkMedia

> Private media & document toolkit for Android — compression, PDF tools, and
> on-device text recognition (OCR). Every byte is processed **on-device**; the
> app declares **no INTERNET permission**.

**Live site:** [shrinkmedia.vercel.app](https://shrinkmedia.vercel.app) — an
honest, interactive preview of the native toolkit (ADR-006 web-simulator
harness; simulated numbers, separate from the no-INTERNET Android app).

[![Vercel](https://img.shields.io/badge/Live%20site-shrinkmedia.vercel.app-black?logo=vercel&logoColor=white)](https://shrinkmedia.vercel.app)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

## Status

| Capability | Tracked in | Status |
|------------|-----------|--------|
| Engineering governance | `docs/current-state.md` | ✅ Implemented & verified |
| Native image compression | `docs/current-state.md` | ✅ Implemented & **device-verified** |
| Native video compression (FFmpegKit) | `docs/current-state.md` | ✅ Implemented |
| Foreground batch service (battery-aware) | `docs/current-state.md` | ✅ Implemented & **device-verified** (queue never drops) |
| DataStore settings persistence | `docs/current-state.md` | ✅ Implemented & **device-verified** |
| PDF merge / split / build / text extract | `docs/current-state.md` | ✅ Implemented |
| On-device OCR (ML Kit, scanned text) | `docs/current-state.md` | ✅ Implemented & **device-verified** (reads "SHRINKMEDI" on API 36) |
| Batch failure surfacing + on-device audit log | `docs/current-state.md` | ✅ Implemented & **device-verified** |
| Web simulator (Vite + React + TS) | `docs/current-state.md` | ✅ Implemented, verified, **live on Vercel** |
| App rename + media delete + first-run onboarding | `docs/current-state.md` | ✅ v0.5.0, **device-verified** label/onboarding render |
| Live web presence (Vercel + GitHub metadata) | `docs/current-state.md` | ✅ Sprint 14, `shrinkmedia.vercel.app` |
| AICore local-model handoff | `docs/current-state.md` | ⚠️ ASPIRATIONAL (staged v2, ADR-010) |

> Honesty over optimism: implemented means code + verification; ASPIRATIONAL
> means designed but not yet wired and verified. **Device-verified** means the
> real instrumented pipeline passed on an API-36 phone
> (`docs/evidence/2026-08-31_device_verification.md`). Nothing is marked done
> on narrative alone (Constitution Article VII).

## Live Site

The browser simulator is deployed as a static SPA on **Vercel**:
**[shrinkmedia.vercel.app](https://shrinkmedia.vercel.app)**. It mirrors the
native toolkit (phone-frame preview, code tabs, honest batch/savings model)
and is the marketing surface for the Android app. It makes **no network calls
and reads no runtime secrets** — a pure static front-end. The community repo
is [dmuhoro/ShrinkMedia](https://github.com/dmuhoro/ShrinkMedia).

## What It Does

- **Compress** images (in-memory sampling + JPEG quality) and videos
  (FFmpegKit `libx264` + `aac`, CRF & bitrate caps) at Low / Medium / High.
- **Batch** multiple files through a foreground service with real-time progress.
  The queue pauses automatically on low battery (opt-in) and **never drops an
  item** — verified on hardware.
- **Autosave** output to public `Pictures/ShrinkMedia` / `Movies/ShrinkMedia`
  (opt-in), or keep it in the app cache — verified on hardware.
- **Documents**: image-to-PDF portfolios, PDF merge, PDF page split, PDF
  metrics, and embedded-text extraction via `android.graphics.pdf`.
- **Scan reader (OCR)**: on-device text recognition via ML Kit
  (`OcrHelper`), returning typed results with explicit success / empty /
  failure states — no silent drops, no cloud.
- **Audit**: compression reductions and timings are summarised in the UI;
  totals persist via DataStore. Batch **failures** write a timestamped
  on-device `batch-audit.log` and are surfaced with a reason in the completion
  notification (no silent drops, Constitution Article I.6).

## Repo Map

```
app/src/main/java/com/shrinkmedia/compressor/
  MainActivity.kt               Toolkit UI + compression/PDF/OCR engine
  OcrHelper.kt                  On-device ML Kit text recognition (typed-null)
  SettingsDataStore.kt          Persisted settings (SettingsRepository)
  BatchCompressionService.kt    Foreground batch service + pause controller + audit
src/                            Web simulator (Vite + React + TS + Tailwind)
docs/                           Constitution, ADRs, sprints, evidence, runbooks
.github/workflows/ci.yml        Quality gates
```

## Architecture

Native app: **Kotlin + Jetpack Compose (Material 3)**, ViewModel-driven
`UiState`, Coil for media rendering, DataStore for settings, FFmpegKit (x264) for
video, ML Kit text recognition for OCR. All processing is
synchronous-within-coroutines on `Dispatchers.IO`. Web: Vite + React +
TypeScript simulator mirroring the native flows.

See `docs/architecture.md` and `docs/current-state.md` (read both before any
task).

## Build & Run

### Android (Android Studio / JDK 17+ / SDK 35)

```bash
./gradlew assembleDebug   # via the Gradle wrapper
./gradlew test
```

Run on any device or emulator running **Android 7.0+ (API 24–35)** (also runs
on newer releases via the standard targetSdk baseline). The full on-device
instrumented gate:

```bash
./gradlew :app:connectedDebugAndroidTest   # requires a device + USB install enabled
```

### Web simulator

```bash
npm install
npm run dev        # http://localhost:3000
npm run lint       # tsc --noEmit
npm test           # Vitest
npm run build      # production bundle
npm run preview    # serve the production bundle locally
```

Deploy to Vercel production with the CLI (lean upload via `.vercelignore`,
config in `vercel.json`):

```bash
vercel --prod --yes      # production build → dist/ → shrinkmedia.vercel.app
```

CI auto-deploys on push to `main` when the `VERCEL_*` secrets are configured
(fail-closed: without them, only the quality gates run — see
`.github/workflows/deploy-web.yml`).

## Governance

- **Constitution** — `docs/engineering/CONSTITUTION.md` (supreme authority).
- **Operating rules** — `AGENTS.md`.
- **Decisions** — `docs/adr/` (ADRs, incl. ADR-009 OCR / ADR-010 v2 bridge) and
  `docs/decisions.md`.
- **Sprint records** — `docs/sprints/` (records 1–9; Sprint 8 device gate is
  EXECUTED/PASS).
- **Evidence** — `docs/evidence/` (incl. `2026-08-31_device_verification.md`);
  release gates in `docs/release-readiness.md`; step-by-step path in
  `docs/release-roadmap.md`.
- **Contribute** — `CONTRIBUTING.md` (all commits must be SSH-signed).

## License

MIT — © 2026 Daniel Muhoro. See `LICENSE`.
