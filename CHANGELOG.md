# Changelog

All notable changes to ShrinkMedia are documented here, following
[Semantic Versioning](https://semver.org/). Sections: **Added**, **Changed**,
**Fixed**, **Removed**. The full per-sprint narrative lives in
`docs/sprints/`.

## [0.2.0] — 2026-08-30

Engineering foundation, unified toolkit, and battery-aware batch compression.

### Added
- **Engineering governance scaffold**: Constitution (`docs/engineering/CONSTITUTION.md`),
  ADRs (`docs/adr/`), decisions log, architecture + current-state docs,
  code-standards, sprint records, evidence log + release-readiness table,
  runbooks, active sprint plans, `.ai/` context + agent definitions,
  `.github/` CI workflow + templates.
- Root governance files: `AGENTS.md`, `CONTRIBUTING.md`, `ROADMAP.md`,
  `SECURITY.md`, `LICENSE` (MIT).
- Web tooling: Vitest with `lib/` helper module + unit tests.
- Gradle wrapper entry points for reproducible Android builds.

### Changed
- `MainActivity.kt` consolidated into a three-tab toolkit (Media / Documents /
  Elite AI) driven by a ViewModel `UiState` — compact, single-responsibility
  UI with quality presets, autosave toggle, and pause-on-low-battery toggle.
- `BatchCompressionService.kt` now battery-aware: a shared
  `BatchCompressionPauseController` state, `ACTION_BATTERY_LOW` receiver
  (registered only when the setting is enabled), and per-file reads of the
  live DataStore autosave preference.
- `SettingsDataStore.kt` persists `pauseCompressionOnLowBattery` (additive
  key, boolean default `false`).
- `README.md` rebuilt as a living product-state document.

### Fixed
- Batch compression previously read the autosave flag once at start from a
  stale intent extra; it now respects the **live** DataStore preference per
  file.

## [0.1.0] — 2026-08-30

Initial project scaffold.

### Added
- Android app skeleton (Kotlin + Jetpack Compose, Material 3, Coil, FFmpegKit
  Lite, DataStore) with single-image/video compression and quality presets.
- Foreground service for background batch compression (first cut).
- Web simulator (Vite + React + TypeScript + Tailwind) with a live preview of
  the compressor UI.
- `README.md`, `.gitignore`, `.env.example`.

### Changed
- None.

### Fixed
- None.