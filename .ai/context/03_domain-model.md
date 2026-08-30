# 03 — Domain Model & Settings

## Purpose
The toolkit's core shapes and their invariants.

## Authority Level
Foundational — Constitution Articles III & IV.

## Consumers
Builder/auditor agents touching settings or engine helpers.

## Source Documents
`docs/current-state.md`, `docs/adr/ADR-004`, `docs/adr/ADR-007`.

## Update Rules
Update when the state shape or settings schema changes.

---

## Core Shapes

| Shape | Definition | Invariant |
|---|---|---|
| `CompressionQuality` | LOW(55/1280/32/800k), MEDIUM(75/1920/28/1500k), HIGH(90/2560/23/2500k) | Single source of truth for presets |
| `UiState` | tab, busy, status, quality, mediaResult, imageUris, mergeUris, pdfUri, pdfMetrics, autoSave, pauseOnLowBattery | Only mutated via `ToolkitViewModel.update` |
| `PersistedUserSettings` | themeMode, autoSaveToMediaStore, pauseCompressionOnLowBattery, quality × 2, total bytes/count | All defaults fail-closed (`false`/`MEDIUM`) |
| `MediaResult` | name, before, output:File, isVideo | Output must exist & non-empty |
| `BatchCompressionPauseController.isPaused` | `MutableStateFlow<Boolean>` | Single source of truth for queue pause |

## Settings Keys (additive only)

`theme_mode` · `auto_save_mediastore` · `pause_compression_on_low_battery` ·
`image_quality` · `video_quality` · `total_saved_bytes` · `total_files_count`

New keys are additive; never remove while a released build may read them.