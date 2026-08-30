# ShrinkMedia

> Private media & document toolkit for Android — compression, PDF tools, and
> local text extraction. Every byte is processed **on-device**; the app declares
> **no INTERNET permission**.

## Status

| Capability | Tracked in | Status |
|------------|-----------|--------|
| Engineering governance | `docs/current-state.md` | ✅ Implemented & verified |
| Native image compression | `docs/current-state.md` | ✅ Implemented (JVM-level verified via helper tests; device run required) |
| Native video compression (FFmpegKit) | `docs/current-state.md` | ✅ Implemented |
| Foreground batch service (battery-aware) | `docs/current-state.md` | ✅ Implemented |
| DataStore settings persistence | `docs/current-state.md` | ✅ Implemented |
| PDF merge / split / build / text extract | `docs/current-state.md` | ✅ Implemented (device run required) |
| Web simulator (Vite + React + TS) | `docs/current-state.md` | ✅ Implemented & verified |
| AICore local-model handoff | `docs/current-state.md` | ⚠️ ASPIRATIONAL |

> Honesty over optimism: implemented means code + verification; ASPIRATIONAL
> means designed but not yet wired and verified. Nothing is marked done on
> narrative alone (Constitution Article VII).

## What It Does

- **Compress** images (in-memory sampling + JPEG quality) and videos
  (FFmpegKit `libx264` + `aac`, CRF & bitrate caps) at Low / Medium / High.
- **Batch** multiple files through a foreground service with real-time progress.
  The queue pauses automatically on low battery (opt-in) and **never drops an
  item**.
- **Autosave** output to public `Pictures/ShrinkMedia` / `Movies/ShrinkMedia`
  (opt-in), or keep it in the app cache.
- **Documents**: image-to-PDF portfolios, PDF merge, PDF page split, PDF
  metrics, and embedded-text extraction via `android.graphics.pdf`.
- **Audit**: compression reductions and timings are tracked and summarised in
  the UI; totals persist via DataStore.

## Repo Map

```
app/src/main/java/com/example/mediacompressor/
  MainActivity.kt               Toolkit UI + compression/PDF engine
  SettingsDataStore.kt          Persisted settings (SettingsRepository)
  BatchCompressionService.kt    Foreground batch service + pause controller
src/                            Web simulator (Vite + React + TS + Tailwind)
docs/                           Constitution, ADRs, sprints, evidence, runbooks
sprints/                        Active sprint plans
.ai/                            AI context + agent definitions
.github/workflows/ci.yml        Quality gates
```

## Architecture

Native app: **Kotlin + Jetpack Compose (Material 3)**, ViewModel-driven
`UiState`, Coil for media rendering, DataStore for settings, FFmpegKit Lite for
video. All processing is synchronous-within-coroutines on `Dispatchers.IO`.
Web: Vite + React + TypeScript simulator mirroring the native flows.

See `docs/architecture.md` and `docs/current-state.md` (read both before any
task).

## Build & Run

### Android (Android Studio / JDK 17+ / SDK 35)

```bash
./gradlew assembleDebug   # via the Gradle wrapper
./gradlew test
```

Run on any device or emulator running **Android 7.0+ (API 24–35)**.

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
- **Decisions** — `docs/adr/` (ADRs) and `docs/decisions.md`.
- **Sprint records** — `docs/sprints/`; active plans in `sprints/`.
- **Evidence** — `docs/evidence/`; release gates in `docs/release-readiness.md`.
- **Contribute** — `CONTRIBUTING.md` (all commits must be SSH-signed).

## License

MIT — © 2026 Daniel Muhoro. See `LICENSE`.