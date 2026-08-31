# ShrinkMedia — Current State

> Orientation doc: **what is real vs ASPIRATIONAL**, as of 2026-08-31.
> Read before any task (AGENTS.md §7). If a task assumes a capability marked
> **ASPIRATIONAL**, stop and say so before building on it.

Legend: ✅ implemented & verified • ⚠️ implemented, device run pending •
🟡 designed/partial • ⚠️ **ASPIRATIONAL** not wired.

## Capability Matrix

| # | Capability | Status | Evidence / Notes |
|---|-----------|--------|------------------|
| C1 | Engineering governance scaffold | ✅ | Constitution, ADRs, decisions, sprints, evidence, CI |
| C2 | Web simulator (Vite+React+TS) lint/build/tests | ✅ | `npm run lint` / `npm test` (18) / `npm run build` green (Sprint 7 evidence) |
| C3 | Image compression (bitmap sampling + JPEG) | ⚠️ | Helper `compressImageFile`; JVM-verified math mirror in `src/lib`; **device run is final Sprint 8 gate** |
| C4 | Video compression (FFmpegKit x264, preset CRF/bitrate) | ⚠️ | `compressVideoFile` awaits session state; **device run Sprint 8** |
| C5 | Foreground batch service + progress notification | ⚠️ | `BatchCompressionService` wired into Media tab (`startBatch`); **device run Sprint 8** |
| C6 | Battery-aware pause/resume (opt-in) | ⚠️ | Controller + receiver; instrumented `BatchPauseContractTest` proves the pause gate never drops an item (compiles); **device run Sprint 8** |
| C7 | Autosave to MediaStore (opt-in, no permission) | ⚠️ | `saveToPublicMediaStore` returns Boolean; instrumented test covers insert (compiles); **device run Sprint 8** |
| C8 | DataStore settings persistence | ✅ | `SettingsRepository` flow; additive keys; fail-closed defaults |
| C9 | PDF build / merge / split / metrics | ⚠️ | `android.graphics.pdf`; **device run Sprint 8** |
| C10 | PDF embedded-text extraction | ⚠️ | Heuristic; scanned PDFs report "needs OCR" honestly; **device run Sprint 8** |
| C11 | AICore device-model handoff | **ASPIRATIONAL** | `AiTab` placeholder only — no AICore dependency, no availability check (D001) |
| C12 | Real OCR (scanned PDFs/images) | **ASPIRATIONAL** | No local OCR engine wired; refusing to fake it is by design (D001 FAQ) |
| C13 | Signed, R8-minified release build (`com.shrinkmedia.compressor` v0.2.1) | ✅ | `app/build.gradle.kts` (minify + `signingConfigs.create("release")`), `apksigner verify` PASS on `app-release.apk`; `bundleRelease` → signed AAB (dev keystore); production keystore is human-owned |
| C14 | Android instrumentation tests **written** (JVM + instrumented) | ⚠️ | JVM unit tests run green; instrumented APK compiles; **execution on device = Sprint 8** |
| C15 | CI hardened to Node-24 actions + signed release-AAB job | ✅ | `.github/workflows/ci.yml` (checkout/setup-node/setup-java/cache v5, upload-artifact v7, fail-closed release job) |

## What This Means For New Work

- **The on-device invariant is structural:** the manifest has no INTERNET
  permission. Adding network = ADR (Article II.2).
- **Compression/PDF helpers are shippable but device-untested.** The final
  verification pass (**Sprint 8**) must exercise the real paths on hardware and
  record it in `docs/evidence/`.
- **Nothing under C11–C12 may be claimed as delivered.** They remain
  ASPIRATIONAL until implemented **and** verified.
- **Release signing is configured and locally proven, but the production
  keystore is a human-owned blocker.** The build fails closed (no unsigned
  artifact) until a real keystore is provided.

## Sprint Cross-Reference

Every claim above is audited sprint-by-sprint in
`docs/sprint-cross-reference.md`. The release gate table lives in
`docs/release-readiness.md`; the step-by-step path lives in
`docs/release-roadmap.md`.