# ShrinkMedia

> Private media & document toolkit for Android — compression, PDF tools, and
> on-device text recognition (OCR). Every byte is processed **on-device**; the
> app declares **no INTERNET permission**.

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
| Web simulator (Vite + React + TS) | `docs/current-state.md` | ✅ Implemented & verified |
| AICore local-model handoff | `docs/current-state.md` | ⚠️ ASPIRATIONAL (staged v2, ADR-010) |

> Honesty over optimism: implemented means code + verification; ASPIRATIONAL
> means designed but not yet wired and verified. **Device-verified** means the
> real instrumented pipeline passed on an API-36 phone
> (`docs/evidence/2026-08-31_device_verification.md`). Nothing is marked done
> on narrative alone (Constitution Article VII).

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
```

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
