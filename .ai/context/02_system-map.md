# 02 — System Map

## Purpose
File/route locations for the toolkit.

## Authority Level
Reference.

## Consumers
All agents needing file paths.

## Source Documents
`docs/architecture.md`.

## Update Rules
Update when files move or modules split.

---

## Native (Kotlin, `app/`)

| Path | Content |
|---|---|
| `app/src/main/java/com/example/mediacompressor/MainActivity.kt` | UI tabs + state + engine helpers (`compressImageFile`, `compressVideoFile`, PDF builders, saveToPublicMediaStore) |
| `app/src/main/java/com/example/mediacompressor/SettingsDataStore.kt` | `SettingsRepository`, `PersistedUserSettings`, `Context.dataStore` |
| `app/src/main/java/com/example/mediacompressor/BatchCompressionService.kt` | Foreground service + `BatchCompressionPauseController` |
| `app/src/main/AndroidManifest.xml` | Permissions (no INTERNET), service, FileProvider |
| `app/build.gradle.kts` | Dependencies (FFmpegKit, Coil, DataStore, Compose BOM) |

## Web simulator (`src/`)

| Path | Content |
|---|---|
| `src/App.tsx` | Simulator UI (preview + code tabs + batch/undo/audit) |
| `src/lib/compression.ts` (+ `.test.ts`) | Pure compression-ratio helpers |
| `src/lib/format.ts` (+ `.test.ts`) | Size formatting helpers |

## Governance

| Path | Content |
|---|---|
| `docs/` | Constitution, adr/, decisions, current-state, architecture, evidence, runbooks, releases |
| `docs/sprints/` | Sprint records (1–6) + active plan (7) |
| `.ai/` | VERSION, agents/, context/ |
| `.github/workflows/ci.yml` | Quality gates |