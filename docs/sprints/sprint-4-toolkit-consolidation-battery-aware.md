# Sprint 4 — Toolkit Consolidation & Battery-Aware Batch

**Theme/Result:** The single-screen compressor becomes a three-tab toolkit
(Media / Documents / Elite AI), and the batch service becomes battery-aware
with a no-drop pause/resume control.

## Summary

| Capability | Status | Evidence |
|------------|--------|----------|
| Three-tab toolkit UI driven by a ViewModel `UiState` | ✅ | `MainActivity.kt` (187 lines, compact) |
| Documents tab — image-to-PDF, merge, split, metrics, text extract | ✅ | `MainActivity.kt` helpers |
| Elite AI tab — local PDF text extraction placeholder | ✅ | `MainActivity.kt` AiTab |
| Pause-on-low-battery setting persisted (DataStore) | ✅ | `SettingsDataStore.kt` |
| Battery-aware service (receiver + shared pause controller) | ✅ | `BatchCompressionService.kt` |
| No-drop queue item invariant | ✅ | `BatchCompressionPauseController` |
| Live DataStore autosave read per file (not stale intent extra) | ✅ | `BatchCompressionService.kt` |

## Part 1 — UI consolidation

**Problem:** One 1300+-line `MainActivity` mixed state, engine, and layout,
making it untestable and hard to extend.

**Fix:** A single `UiState` + `ToolkitViewModel` owns state; Composable tabs
render it; engine functions remain top-level and typed (`File?`). Net: −2397
lines in `MainActivity.kt`.

## Part 2 — Battery-aware batch queue

**Problem:** Long batches could drain the battery; the service read autosave
once from a stale intent extra, and pause/resume had no shared source of truth.

**Fix:**
- `pauseCompressionOnLowBattery` persisted via DataStore (additive key,
  default `false`).
- The service registers an `ACTION_BATTERY_LOW` receiver **only when the
  setting is enabled**; on low battery it sets the shared
  `BatchCompressionPauseController.isPaused = true`; on `BATTERY_OKAY` /
  `POWER_CONNECTED` it clears it.
- Each queued item awaits `isPaused.first { !it }` before processing —
  **the item is never skipped** (Article III.4).
- Autosave is re-read from the **live** DataStore flow per file.

**Files:**
- `app/.../MainActivity.kt` (consolidation)
- `app/.../BatchCompressionService.kt` (battery receiver + pause controller)
- `app/.../SettingsDataStore.kt` (new key + update fn)

## Test Evidence Summary

| Test | Result |
|------|--------|
| Granular commits: setting → service → UI (each standalone-compilable) | ✅ |
| No-drop invariant readable in code at `executeBatchProcessing` awaits | ✅ |
| Device battery-pause walkthrough | ⚠️ device run pending (runbook android-build §4) |

## Artifacts

- Commits: `feat(settings): persist pause-compression-on-low-battery
  preference`, `feat(service): battery-aware foreground batch compression`,
  `refactor(app): unify media, documents and AI toolkit UI`.

## Cross-Reference

ADR-003 (foreground service), ADR-008 (PDF pipeline), Constitution Articles
III & IV, `docs/current-state.md` C3–C10.