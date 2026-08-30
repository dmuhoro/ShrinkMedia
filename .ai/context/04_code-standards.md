# 04 — Code Standards

## Purpose
Condensed conventions (Kotlin + TS) every agent must follow.

## Authority Level
Operational.

## Consumers
Builder agent, reviewers.

## Source Documents
`docs/code-standards.md` (canonical).

## Update Rules
Keep in sync with `docs/code-standards.md`.

---

## Kotlin
- Engine helpers top-level in `MainActivity.kt` until a boundary is justified.
- ViewModel owns `UiState`; UI reads `collectAsState()`.
- Conversions return `File?`; callers surface `null`.
- Engine work on `Dispatchers.IO` in `withContext`; `delay`-poll FFmpeg state.
- Settings only via `SettingsRepository`.
- **No INTERNET permission in the manifest.**

## TypeScript / web
- `strict`; no `any` (use `unknown` + narrowing).
- Pure helpers under `src/lib/` (the tested surface).
- Simulation numbers are labelled as simulation.

## Tests
- `npm test` green before push; never weaken an assertion (Article V).
- A diagnostic "bug exists" test must be `xfail`/`skip` + linked issue.

## Commits
Conventional Commits, one piece of work per commit, SSH-signed, message names
the doc that needs the corresponding update.