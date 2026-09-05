# Sprint 21 — Ecosystem Directive: RSI governance + measurement + skills workbench + host tiers + DataBank vault MVP

- **Date:** 2026-09-05
- **Output:** No app release (`UNRELEASED` in CHANGELOG) — ShrinkMedia is in **maintenance mode**
  (v0.9.0 shipped). Ecosystem-track infrastructure sprint.
- **Goal:** answer and encode the 2026-09-05 directive (RSI requirements with the Owner as sole
  authorizer; ergodic + processing-vs-storage deep dives; 80/20 DataBank-first roadmap; hardware
  optionality; "steal like an artist" upgrade + measurement) and ship the **first working ecosystem
  piece**: the DataBank vault MVP.
- **Evidence:** case study #3 — `docs/evidence/2026-09-05_ecosystem_directive_sprint.md`
- **Skills units used:** S-01…S-06 (ledger rows in `docs/operations/skills-workbench.md` §3)

## Layers (complete = code + docs + evidence)

| Layer | Deliverable(s) | Status |
|-------|----------------|--------|
| L1 | `skills-workbench.md` (catalog + adoption + reuse ledger) + `AGENTS.md §13` | ✅ done |
| L2 | `measurement.md` (M-metrics, LayerLog, compression ledger) + `SOP.md §6` pointer | ✅ done |
| L3 | `rsia-program.md` (R-01…R-08 + authorization card + readiness table + enforcement) + `rsia-authorizations.md` (dial 0) + `AGENTS.md §14` | ✅ done |
| L4 | `ecosystem-orientation.md` §3–§4 expanded (ergodic deep-dive; full processing-vs-storage explainer) | ✅ done |
| L5 | `ecosystem-roadmap.md` rewrite (80/20, decision map, sequential connect, checkpoint A) + `shrinkmedia-stop-point.md` upgraded (maintenance ACTIVE) | ✅ done |
| L6 | **ADR-016** (T0/T1/T2 host tiers; Elitebook = bare Debian 13, no hypervisor) | ✅ done |
| L7 | Code: `ecosystem/HostTier.kt` + `HostClassifierTest.kt` (12 tests) | ✅ done + green |
| L8 | **DataBank vault MVP** (`dvault`, 19 tests) + README + `RUNBOOK.md` + `DEPLOY.md`; pushed to `dmuhoro/DataBank` | ✅ done + pushed |
| L9 | CHANGELOG `[Unreleased]` + `current-state.md` C25 + `release-readiness` Sprint-21 gate + `sprint-cross-reference` + case study #3 + this sprint doc | ✅ done |
| L10 | Full gates (97 JVM, lint 0, assembleDebug, no-INTERNET), commits SSH-signed, pushed, CI | ✅ gate + push done; CI (below) |

## Commits (all SSH-signed, %G? = G)

**ShrinkMedia** (individual, one piece per commit):

1. `feat(ecosystem): ...` — HostTier/HostClassifier decision layer + 12 tests
2. `docs(operations): ...` — skills-workbench + AGENTS §13 (L1) + measurement + SOP §6 (L2) + RSI
   program/ledger + AGENTS §14 (L3)
3. `docs(architecture): ...` — orientation deep-dives (L4) + roadmap rewrite + stop-point upgrade (L5)
4. `docs(adr): ...` — ADR-016 (L6)
5. `docs(sprint): ...` — CHANGELOG + current-state + readiness + cross-ref + case study #3 + this
   sprint doc (L9)

**DataBank** (2 commits):

1. `feat(vault): dvault MVP — vault.* contract, journal-backed, 19 tests` (`12d153a`)
2. `docs: readme + runbook + Elitebook deploy decision` (`fa6abd2`)

## Gates (observed)

| Gate | Result |
|------|--------|
| `:app:testDebugUnitTest` | **97 tests, 0 failures** |
| `:app:lintDebug` | **0 errors** |
| `:app:assembleDebug` | **BUILD SUCCESSFUL** |
| `:app:processReleaseMainManifest` | **BUILD SUCCESSFUL**; merged RELEASE manifest still no INTERNET |
| merged DEBUG manifest `rg INTERNET` | **no match** |
| DataBank `python3 -m unittest discover -s tests` | **19 tests OK** |
| CI after push | see `gh run list` (4 jobs) |
| No app release | intended: version stays 0.9.0/versionCode 10 |

## Evidence reading

- Case study #3 (commands + observed results): `docs/evidence/2026-09-05_ecosystem_directive_sprint.md`
- LayerLog + compression row: same file, §LayerLog.
- Reuse ledger rows: `docs/operations/skills-workbench.md` §3.
- RSI dial + ledger: `docs/operations/rsia-authorizations.md` (dial index 0, no rows).

## Honest boundaries (not hidden)

- No network server/transport yet: DataBank is local-first; stargate connected action is still the
  next milestone (roadmap sequence 2–3) and needs the owner's hardware daily-loop first.
- Host tiers are decision code, not a deployed system; the virtualization answer in
  `DataBank/docs/DEPLOY.md` is a decision + instructions, not a live server.
- RSI = requirements + gate written; nothing is authorized (dial 0). ASPIRATIONAL keeps its label
  (virtual-me reasoning, model runtimes) until tier-2 hardware exists.
- The 80/20 time claim (≈ 3–4× vs naive) is a LayerLog estimate grounded in the single committed
  work day — measured against the calibration-0 velocity, not imagined labour.