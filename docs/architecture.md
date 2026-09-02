# ShrinkMedia Architecture

> Authoritative system map. Read alongside `docs/current-state.md` before any
> task (AGENTS.md §7). This describes what the code actually does; anything
> marked **ASPIRATIONAL** is designed but not yet implemented and verified.

## System Vision

ShrinkMedia is a **single-process, on-device toolkit**: a Kotlin/Compose
Android app whose three tabs (Media, Documents, Elite AI) share one ViewModel,
one settings store, and one background queue. A browser-side simulator mirrors
the UI.

```
┌──────────────────────────────  ANDROID APP  ───────────────────────────────┐
│  UI LAYER (Jetpack Compose, Material 3)                                     │
│    ToolkitApp ── MediaTab │ DocumentsTab │ AiTab                            │
│        state: UiState (StateFlow)     actions: ToolkitViewModel             │
├──────────────────────────────────────────────────────────────────────────────┤
│  VIEWMODEL LAYER                                                             │
│    ToolkitViewModel (AndroidViewModel)                                      │
│      collects settings.userSettingsFlow → UiState                           │
│      delegates work() to engine helpers on Dispatchers.IO                   │
├──────────────┬────────────────────────────────────────────┬──────────────────────────────┤
│  MEDIA ENG.  │  DOCUMENT ENG. (iText 7)                   │  SETTINGS / QUEUE             │
│  compressImageFile (BitmapFactory sampling)  │  createPdfFromImages (vector) │
│  compressVideoFile (FFmpegKit async session) │  mergePdfDocuments (page-exact) │
│  saveToPublicMediaStore (MediaStore insert)  │  extractRawTextFromUri (iText, │
│  getUserMediaFiles (MediaStore gallery query)│    LocationTextExtractionStrategy) │
│  MediaFile / MediaFileCard (Coil thumbnails) │  readPdfMetrics (PdfRenderer) │
│              └── foreground batch path ──►   │  splitPdfIntoPages (bitmap)    │
│                                  │  PdfPreviewState (Open/Save/Discard) │
│                                              │  SettingsRepository            │
│                                              │    (DataStore Preferences)     │
│                                              │  BatchCompressionPauseController
│                                              │  BatchCompressionService       │
└──────────────┴────────────────────────────────────────────┴──────────────────────────────┘
                      │ all on-device, no INTERNET permission
┌──────────────────────────────  WEB SIMULATOR  ──────────────────────────────┐
│ Vite + React + TS + Tailwind  (phone-frame preview + code tabs + lib/)      │
└──────────────────────────────────────────────────────────────────────────────┘
```

## Boundaries & Data Flow

1. **Pickers** (`ActivityResultContracts`) return grant-scoped `Uri`s — no
   storage permission (ADR-005).
2. **ViewModel** inflates quality/autosave/pause state from
   `SettingsRepository.userSettingsFlow`; UI actions call suspend
   `work(output)` helpers.
3. **Engine helpers** return typed `File?` (null = failure) — callers surface
   it (Constitution Article III).
4. **Batch path** starts `BatchCompressionService` (foreground, `dataSync`);
   it reruns the same engine helpers per URI, reading **live** DataStore for
   autosave, and gates items on `isPaused`.
5. **Outputs** default to cache; autosave (opt-in) inserts into MediaStore.
6. **Media gallery** (`getUserMediaFiles`) queries `MediaStore.Images/Video` on
   `Dispatchers.IO` at ViewModel init; thumbnails load via Coil
   `rememberAsyncImagePainter` from local content-provider URIs (no storage
   permission, no INTERNET).
7. **Media delete is user-consented and fail-closed**: `Select` toggles a
   selection mode in the media library; Delete requires a confirm dialog; API 30+
   delegates to `MediaStore.createDeleteRequest` (returns a `PendingIntent` shown
   as the **system** consent dialog via `StartIntentSenderForResult`), API <30 falls
   back to typed direct `contentResolver.delete`. Non-OK results are surfaced, never
   silently dropped.
8. **First-run onboarding** is a `UiState` flag defaulting to `false` (fail-closed:
   card shows until dismissed); "Got it" persists it through the additive
   `ONBOARDING_DISMISSED` DataStore key.

## Key Modules & Files

| Module | File | Responsibility |
|--------|------|----------------|
| UI shell | `app/.../MainActivity.kt` | Tabs, state, pickers, engine helpers; MediaTab media gallery (`getUserMediaFiles`/`MediaFileCard`, vertical quality radio UX, multi-select **Select/Delete** via `MediaStore.createDeleteRequest` consent + first-run onboarding card); DocumentsTab PDF preview (`PdfPreviewState`); `LocationTextExtractionStrategy` extraction |
| Settings | `app/.../SettingsDataStore.kt` | `PersistedUserSettings`, repository (additive `ONBOARDING_DISMISSED` key; fail-closed defaults) |
| Batch queue | `app/.../BatchCompressionService.kt` | Foreground loop + pause controller + battery receiver |
| Simulator | `src/App.tsx` | Web preview + code tabs; deployed as a static SPA on Vercel (`vercel.json`, `.vercelignore`; `shrinkmedia.vercel.app`) |
| Simulator lib | `src/lib/*` | Pure helpers (testable) |

## Quality Presets (single source of truth: `CompressionQuality`)

| Preset | JPEG q | Max dim | Video CRF | Video bitrate |
|--------|--------|---------|-----------|---------------|
| LOW    | 55 | 1280 | 32 | 800k |
| MEDIUM | 75 | 1920 | 28 | 1500k |
| HIGH   | 90 | 2560 | 23 | 2500k |

## Known Gaps

- **AICore handoff** is ASPIRATIONAL — `AiTab` describes it, nothing ships
  (staged v2, ADR-010).
- **Web simulator** compresses with simulated numbers — it previews native
  behaviour, it does not reprocess real bytes (ADR-006).
- History/audit detail lives in the in-memory UI list; only cumulative totals
  persist in DataStore. Batch **failures** now also write a timestamped audit
  line to the app-sandbox `batch-audit.log` (BatchFailureAudit, on-device).

## Evolution Rules

- Any change to the manifest permission surface (especially adding INTERNET)
  requires an ADR (Constitution Article II.2).
- Any change to engine helpers keeps the typed-result contract.
- New tabs/subsystems update this map + `docs/current-state.md` in the same
  commit.