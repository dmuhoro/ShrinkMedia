# Changelog

All notable changes to ShrinkMedia are documented here, following
[Semantic Versioning](https://semver.org/). Sections: **Added**, **Changed**,
**Fixed**, **Removed**. The full per-sprint narrative lives in
`docs/sprints/`.

## [0.2.1] — 2026-08-31

Housekeeping release: removed the inherited Google AI Studio/Gemini artifacts
and consolidated the sprint documentation into a single, execution-ordered
folder.

### Removed
- **Google AI Studio / Gemini fingerprints**: `metadata.json` (AI Studio
  applet descriptor), `assets/.aistudio/`, `.env.example` (documented
  `GEMINI_API_KEY` / `APP_URL`), and unused `@google/genai`, `dotenv`,
  `express`, `@types/express` dependencies (regenerated `package-lock.json`,
  −121 packages). No AI Studio/Gemini references remain in the product code or
  docs.
- **Root `sprints/` folder** (mis-numbered plan files). All sprint docs now
  live in a single `docs/sprints/` (records 1–6 + active plan 7).

### Added
- Sprint 6 record (`docs/sprints/sprint-6-ai-studio-cleanup-sprint-consolidation.md`)
  covering this cleanup/consolidation work.
- Sprint 7 active plan (`docs/sprints/sprint-7-native-toolkit-hardening.md`)
  for the next sprint of device-verification work (ROADMAP Phase 2).
- Evidence `docs/evidence/2026-08-31_sprint6_cleanup.md` recording the
  fingerprint audit and green web gates.

### Changed
- `vite.config.ts` stripped of AI Studio `DISABLE_HMR`/watch logic.
- Web package renamed `react-example` → `shrinkmedia-web`.
- Docs updated to reflect a single sprint folder and a pure static web
  simulator with no runtime secrets: `README.md`, `CONTRIBUTING.md`,
  `SECURITY.md`, `.ai/VERSION` (1.1.0), `.ai/context/02, 07, 08, 09, 12`,
  `.ai/agents/builder-agent.md`, `docs/runbooks/web-simulator.md`,
  `docs/adr/ADR-006-web-simulator-harness.md`,
  `docs/sprint-cross-reference.md`, `docs/release-readiness.md`,
  `docs/sprints/sprint-2`, `docs/sprints/sprint-5`,
  `docs/evidence/2026-08-30_governance_scaffold.md`.

### Fixed
- Manifest no-INTERNET guardrail (`ci.yml`, `.ai/context/08`,
  `.ai/agents/auditor-agent.md`) previously grepped the noisy
  `INTERNET|http://|https://` pattern, which the mandatory XML namespace
  (`http://schemas.android.com/apk/res/android`) would trip — a false
  positive. It now matches only the `android.permission.INTERNET` declaration.
  (No runtime code changed.)

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
- Android build: the committed `io.github.root0as:ffmpeg-kit-lite:6.0-2`
  dependency did not exist on any repository, so `assembleDebug` could never
  resolve. Replaced with the Maven-Central-published `io.github.nikita36078:ffmpeg-kit:6.0.LTS`
  fork (same `com.arthenica.ffmpegkit` API, libx264 enabled on all ABIs).
  See `docs/evidence/2026-08-30_android_config_check.md`.

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