# ShrinkMedia — Code Standards

> Observed conventions (not invented) — what the code already does and what new
> code must keep doing. Enforced by CI where possible, by review otherwise.

## Kotlin / Android

- **Single-source toolkit:** engine helpers are top-level functions in
  `MainActivity.kt` (`compressImageFile`, `compressVideoFile`,
  `saveToPublicMediaStore`, PDF builders). New engine code stays in that file
  until a boundary (e.g. a `PdfEngine.kt`) is justified — no premature
  decomposition.
- **ViewModel owns state:** one `UiState` data class + a `ToolkitViewModel`
  (AndroidViewModel) mutates it via `.update {}`; Composable reads
  `collectAsState()`. No UI-local flags that are also app-state.
- **Typed results:** conversions return `File?`; callers `requireNotNull` /
  guard and surface the message. Never swallow `null` (Article III.1).
- **Coroutines:** engine work on `Dispatchers.IO` inside `withContext`; `delay`
  loops poll FFmpeg session state (no busy-spin, no fire-and-forget).
- **Settings:** only via `SettingsRepository`; keys live in a private object
  in `SettingsDataStore.kt`; values default `false`/`MEDIUM` (Article IV).
- **No INTERNET permission** in `AndroidManifest.xml` — keep it that way.
- **Manifest/UX constants:** notification channel/ID and constants live in
  `BatchCompressionService`'s companion.

## TypeScript / React (web simulator)

- `strict` TypeScript; components in `src/`; **pure helpers in `src/lib/`**
  (the only genuinely unit-tested surface).
- No `any`; use `unknown` + narrowing.
- Simulator numbers are labelled as simulation — never present fake compression
  as real (ADR-006 honesty rule).

## Testing

- **Web:** Vitest alongside implementation (`src/lib/*.test.ts`). Run
  `npm test`.
- **Android:** JVM/instrumentation per Phase 2; until then the gate is
  `assembleDebug` + the web helper mirror.
- Never weaken an assertion to go green (Article V.4). A diagnostic "bug
  exists" test must be marked `xfail`/`skip` + issue (Article V.3).

## Commits

Conventional Commits (`feat/fix/refactor/chore/docs/test`), one piece of work
per commit, SSH-signed, message states what changed and which doc needs an
update (AGENTS.md §8, §10).