# Contributing to ShrinkMedia

ShrinkMedia is a private, on-device media & document toolkit (Android + web
simulator). Every contribution is governed by the Engineering Constitution
(`docs/engineering/CONSTITUTION.md`) and the operating rules in `AGENTS.md`.

## Repository Map

```
app/                      Android app (Kotlin, Jetpack Compose, Material 3)
  src/main/java/com/example/mediacompressor/
    MainActivity.kt       Toolkit UI + compression/PDF engine (on-device)
    SettingsDataStore.kt  Persisted settings (SettingsRepository)
    BatchCompressionService.kt  Foreground batch compression service
src/                      Web simulator (Vite + React + TypeScript + Tailwind)
docs/                     Governance, sprints, evidence, runbooks, architecture
.ai/                      AI context + agent definitions
.github/workflows/        CI quality gates
```

## Definition of Ready (DoR)

Before a task is started it must have: a stated scope, the file(s) it touches,
the production entry point it protects (if it is a guard), and a verification
step. See `docs/sprints/` for the sprint record + active plan format.

## Definition of Done (DoD)

A task is **done** only when ALL of the following hold:

1. Code compiles (or, for web, `npm run lint` and `npm run build` pass).
2. Relevant tests exist and pass; no assertion was weakened to make the suite
   green.
3. The documentation that describes the changed behaviour is updated in the
   same commit.
4. Evidence is recorded under `docs/evidence/` (Constitution Article VII) — a
   cited command/test/step and its observed result.
5. The sprint record and `CHANGELOG.md` reflect the change.
6. The commit is SSH-signed and pushed as its own commit (`main` only).

## Commit Convention

Conventional Commits. One piece of work per commit, in the order it was done.

```
<type>(<scope>): <summary>

<body stating what changed and which doc (if any) needs a corresponding update>
```

Types: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`.

```
fix(service): uphold pause/resume queue no-drop invariant

Batch items now await isPaused resolution instead of being skipped.
docs/sprints/sprint-4-...md cross-references the fix.
```

All commits must be SSH-signed (`commit.gpgsign true`, `gpg.format ssh`). A
missing signature is an invalid commit.

## Quality Gates (CI)

The `.github/workflows/ci.yml` pipeline runs on push/PR to `main`:

1. Web: `npm ci` → `npm run lint` → `npm test` → `npm run build`
2. Android: `./gradlew assembleDebug` (requires the Gradle wrapper)

All stages must pass before merge. Never weaken a test to make a stage pass.

## Engineering Rules (non-negotiable)

- Every conversion path returns `null` on failure — callers surface it, never
  swallow it (no silent drops).
- All processing is on-device. The manifest declares **no INTERNET
  permission**; adding network access is a decision requiring an ADR.
- Settings are additive in `SettingsDataStore.kt`; booleans default to `false`,
  quality defaults to `MEDIUM`.
- `BatchCompressionPauseController.isPaused` is the single source of truth for
  queue pause/resume; a queued item is never dropped.
- New dependencies require justification against existing ones.

## Running Things

```bash
# Web simulator
npm install
npm run dev          # Vite dev server on :3000
npm run lint         # tsc --noEmit
npm test             # Vitest
npm run build        # Production build
npm run preview      # Preview the production build

# Android app (Android Studio, JDK 17+, SDK 35)
./gradlew assembleDebug
./gradlew test
```

Read `docs/current-state.md` and `docs/architecture.md` before starting. If a
task assumes a capability marked **ASPIRATIONAL**, stop and say so instead of
building on top of it silently.