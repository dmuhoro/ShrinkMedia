# Sprint 3 — Foreground Batch Compression Service

**Theme/Result:** A background-capable queue for multi-file compression with a
live progress notification, wired into the single-screen compressor and the
manifest.

## Summary

| Capability | Status | Evidence |
|------------|--------|----------|
| Foreground `BatchCompressionService` (dataSync) | ✅ | `BatchCompressionService.kt` |
| Progress + completion notifications | ✅ | `BatchCompressionService.kt` |
| Manifest service registration + permission | ✅ | `AndroidManifest.xml` |
| DataStore-backed settings (theme/autosave/quality/totals) | ✅ | `SettingsDataStore.kt` (initial) |

## Part 1 — Background queue

**Problem:** Batch compression of many large files could not survive the user
leaving the app, and raw single-file compression had no progress surface.

**Fix:** Introduced a foreground service (ADR-003) that reads the queue from
the intent extras, runs the engine per URI, and notifies progress/completion.

**Part 2 — Persisted settings (first cut)**

**Fix:** `SettingsRepository` over DataStore Preferences (ADR-004) with a
reactive `userSettingsFlow` collected by the UI.

**Files:**
- `app/.../BatchCompressionService.kt`
- `app/.../SettingsDataStore.kt`
- `app/.../MainActivity.kt` (single-screen, ~1300 lines at this point)
- `AndroidManifest.xml`, `app/build.gradle.kts` (service deps)

## Test Evidence Summary

| Test | Result |
|------|--------|
| Type-level wiring of service + settings compile path | ✅ (code commit stood alone) |
| Device notification/foreground behaviour | ⚠️ device run pending |

## Artifacts

- Commits: `feat: implement background batch compression service`.

## Cross-Reference

ADR-002/003/004. Sprint 4 then hardened pause/resume and refactored the UI.