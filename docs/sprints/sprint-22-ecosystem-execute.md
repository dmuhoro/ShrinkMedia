# Sprint 22 — Ecosystem Execute Directive (2026-09-05)

> **Scope:** the 2026-09-05 execute directive, sequential across every repo —
> **DataBank** (Ubuntu proof + portability + hardening), **Forge** (brief step 1: task schema +
> state machine, zero AI), **Daftari** (installable-PWA root cause + offline verification + Growth
> Engine rename + pre-existing red fixes), **ShrinkMedia** (docs-only, maintenance mode).
> One repo at a time, one layer at a time, tests green before moving on, individual SSH-signed
> commits, pushed, CI green, then stop.

## DataBank — portability proven + anti-fragility hardening (v0.2.0)

- **Ubuntu proof on the current machine** (Python 3.14): full CLI smoke run — capture/query/list/
  count/check all pass, journal==records, integrity ok, `VACUUM INTO` snapshot created + reopened
  with all 3 records — `docs/PORTABILITY.md` + `docs/evidence/2026-09-05_ubuntu_run_proof.md`.
- **Portability CI matrix** ubuntu + macOS + windows (`fail-fast: false`), same 23-test suite.
- **Hardening** (fail-closed, never silent): corrupt store refused on open (`PRAGMA quick_check`),
  oversized records refused (`MAX_TEXT_LEN` 200_000), `dvault backup` consistent verified snapshots,
  `wal_checkpoint(TRUNCATE)` on close, post-close use refused. Suite 19 → **23 tests**.
- Commits: `842da36` (ci matrix), `e9a9c0a` (feat hardening), `894a3bf` (docs portability + evidence).
  Pushed (`e7a2a18..894a3bf`).

## Forge — Step 1 shipped (brief §9: task schema + state machine, zero AI)

- `forge/task.py` (schema per brief §2), `forge/engine.py` (deterministic state machine:
  queued→retrieving→building→reviewing→changes_requested→building|merged, or blocked; terminal
  never resurrects; attempts cap → blocked routes to the human, never a silent drop), 
  `forge/journal.py` (append-only fsync'd JSONL; corrupt line = refusal, never a skip),
  `forge/cli.py` (`new/to/show/list/verify`). **16 contract tests** incl. CLI subprocess boundary.
- Live evidence run (merged path, blocked-with-reason, resurrection refusal, journal verify):
  `docs/evidence/2026-09-05_step1_state_machine.md`.
- `docs/kickoff.md` — why Railway/Supabase/pgvector/Docker/Vercel are deferred until each §9 exit
  criterion; **honest:** the "hand-run 5 real Daftari tasks" exit is *not claimed* — false would be
  claiming it with synthetic tasks. Owner brief preserved verbatim in-repo.
- Commits: `76df25a` (feat core), `f097020` (docs). Pushed (`91b8ad0..f097020`).

## Daftari — v6.4.0 (installable PWA + offline verification + growth rename)

- **Root cause of "opens the browser not an app":** manifest shipped **SVG-only icons**; Android
  Chrome requires **PNG** for installability. Fixed: 8-bit PNGs (192/512/maskable 512) +
  `apple-touch-icon-180`; manifest → `image/png`; verified in the built `dist/manifest.webmanifest`.
- **Production-build verification** (`e2e/pwa-prod.spec.ts` + `playwright.prod.config.ts`, runs
  `vite build` + `vite preview`): 5 specs — PNG manifest, apple-touch reachable, SW registered
  **and controlling the page**, full offline reload keeps shell + IndexedDB, an offline sale is
  persisted with `synced=0` and survives reload + failed sync (no silent drop). Spec reflects the
  real SW life-cycle (first visit installs, second navigation is controlled).
- **Dev e2e** 10 specs still green; suite total now **369 unit tests / 34 files** (brief's "54
  tests" claim was stale — verified, not trusted).
- **Pre-existing red fixed, not silenced:** HEAD shipped 2 failing unit tests (Vitest hoisting in
  AcademyScreen; stale WhatsApp-bold regex in GrowthShareScreen) + 21 missing translation keys in
  OnboardingScreen (raw keys would render). All fixed with real bilingual strings + standard test
  patterns — assertions kept meaningful.
- **Brianna removed** (`AGENTS`-consistent): `briannaContent.ts` → `growthContent.ts`,
  `BRIANNA_STORY_TEMPLATES` → `GROWTH_STORY_TEMPLATES`, UI labels → "Growth Engine"; v6.3.0 history
  docs left as records.
- **Docs:** ADR-010 (installable PWA), ADR-011 (platform future-plan, gate-protected), 
  `offline-verified.md` (evidence + manual on-device checklist), `hardening.md`, 
  `platform-expansion.md`, sprint log, CHANGELOG v6.4.0, ROADMAP/README.
- Commits: `60a1332` (pwa fix), `2585bfb` (i18n), `0644916` (red tests), `2f32d95` (growth rename),
  `0ca0748` (e2e prod), `279cf60` (ci/version), `529f594` (docs) + 3 follow-ups (deletion, root
  docs). Pushed (`b4abde7..e6c8552`), tree clean.
- **Honest:** remote **upload** to Supabase needs real credentials (not in CI) — local persist/
  retry/never-drop proven; **real install** is a human Chrome gesture — manual checklist provided.

## ShrinkMedia — docs-only (maintenance mode, no app code, no release)

- `docs/operations/hardening.md` — ecosystem anti-fragility (proven-at-boundary vs needs-host).
- `docs/operations/checkpoint-map.md` — the numbered path to "end-to-end without crashing".
- This sprint record + CHANGELOG `[Unreleased]` entry + `current-state.md` C26 + release-readiness
  Sprint-22 gate + sprint-cross-reference row.
- Gates (no app change): 97 JVM tests, lint 0 errors, assembleDebug + processReleaseMainManifest
  green, merged manifests still **no INTERNET**.

## Honest boundaries (Sprint 22)

- The three-repo executed track is **buildable/testable** state now, not "risk complete":
  - Forge step-1 exit (5 real Daftari tasks) is open by design — the owner's tasks, not synthetic.
  - Daftari installed-on-device verdict awaits the manual checklist on a real handset.
  - DataBank running 24/7 on the always-on host awaits Checkpoint A (owner hardware) + transport.
- RSI stays **OFF (dial 0)** — nothing in this sprint self-authorizes; the Owner remains sole
  approver (`docs/operations/rsia-program.md`).
- No dependency added to ShrinkMedia; Daftari/Forge/DataBank remain stdlib/stack-prudent per their
  own AGENTS (every deferral justified in `Forge/docs/kickoff.md`).