# 01 — System Architecture

## Purpose
Condensed system map. Consult before any structural change.

## Authority Level
Foundational — supplements Constitution.

## Consumers
All agents.

## Dependencies
- `docs/architecture.md` (canonical)
- `docs/current-state.md`

## Source Documents
`docs/architecture.md`, `docs/adr/`.

## Update Rules
Update when layers/modules/boundaries change.

---

## Layers

```
UI (Compose) ─► ViewModel (UiState Flow) ─► Engine helpers (IO) ─► Output
                    ▲                              │
          SettingsRepository (DataStore)    BatchCompressionService
```

- **UI:** `ToolkitApp` → MediaTab / DocumentsTab / AiTab. Reads `collectAsState()`.
- **ViewModel:** `ToolkitViewModel` owns `UiState`, delegates work, persists settings.
- **Engine:** top-level typed helpers in `MainActivity.kt` (`File?` results).
- **Queue:** `BatchCompressionService` foreground loop, `isPaused` controller,
  battery receiver (registered only when the setting is on).

## Non-negotiables
1. No INTERNET permission (Article II.2).
2. Typed results; callers surface `null` (Article III.1).
3. FFmpeg sessions awaited, never fire-and-forget (Article III.3).