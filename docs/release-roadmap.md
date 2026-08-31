# ShrinkMedia — Release Roadmap

> The literal path from "code exists" to "shipped and verified." Every step in
> this roadmap has a measured, evidencible exit (Constitution Article VII). A
> row is only ✅ when code **and** citation exist — never on narrative alone.

## Posture

- **On-device only:** no INTERNET permission, no uploads. Every media/PDF/text
  path must run locally (ADR — structural invariant).
- **Fail closed:** release signing defaults to unsigned and refuses to produce
  an artifact rather than ship an unverifiable one.
- **Hardware verification is the last step.** All code, plumbing, CI, release
  config, and docs land first; only then do we spend device/emulator cycles
  proving the real paths (Constitution Article I.4).

## Lane / Status Legend

- ✅ **Real** — implemented and verified with evidence.
- ⚠️ **Implemented, device run pending** — code compiles; hardware proof is
  the final gate (Sprint 8).
- 🟡 **Designed / partial** — some plumbing, not complete.
- 🔵 **Planned** — in an active sprint plan, not yet started.

## The Roadmap

| # | Step | Owner lane | Status | Exit gate |
|---|------|-----------|--------|-----------|
| 1 | Engineering governance scaffold (Constitution, ADRs, evidence, CI) | Governance | ✅ | `docs/` tree + CI merge + evidence log (Sprint 5, v0.2.0) |
| 2 | Web simulator bootstrap (Vite + TS) | Web | ✅ | `npm run lint`/`test`/`build` green (Sprint 5/6) |
| 3 | Native media + document toolkit (single/batch compression, FGS, battery pause, autosave, PDF) | Native | ⚠️ | `assembleDebug` green; device run = Sprint 8 (Sprints 1, 3, 4) |
| 4 | Honest web model: pure batch/savings lib + tests | Web | ✅ | `src/lib/batch.ts` + 7 tests; 18 total `npm test` (Sprint 7) |
| 5 | Real-path Android tests written (JVM + instrumented) | Native/Test | ⚠️ | JVM unit tests + instrumented APK compile green; device run = Sprint 8 (Sprint 7) |
| 6 | Production namespace `com.shrinkmedia.compressor` | Release | ✅ | APK badging shows `com.shrinkmedia.compressor` v0.2.1 (Sprint 7) |
| 7 | R8-minified signed release APK/AAB | Release | ✅ | `apksigner verify` PASS on `app-release.apk`; `bundleRelease` → signed AAB (Sprint 7, verified with dev keystore) |
| 8 | Launcher icon + label | Release | ✅ | `@mipmap/ic_launcher`/`@string/app_name` resolve; aapt badging lists label (Sprint 7) |
| 9 | CI off deprecated Node-20 actions + signed release-AAB job | Governance/CI | ✅ | Workflow YAML parses; release job gated fail-closed on secrets (Sprint 7) |
| 10 | Device verification — final gate | Test/QA | 🔵 | Instrumented suite green on API 24–35; battery-pause walkthrough (0 dropped); autosave verified; evidence filed (Sprint 8) |
| 11 | Real keystore + Play Console AAB to a closed track | Release | 🔵 | Human-owned: provision production keystore, upload signed AAB, record track | 
| 12 | AICore device-model handoff + OCR | AI | 🟡 | AICore still ASPIRATIONAL/staged v2 (ADR-010). **OCR implemented** (ADR-009: ML Kit) — flies with step 10's device run |

## What Is NOT On This Roadmap

- Any networked/cloud compression (violates the on-device invariant).
- iOS/desktop ports before Phases 1–3 are verified.
- Google-Tools Bridge (Drive/Docs/cloud-Gemini) — deliberately staged OUT of v1
  as opt-in connected mode (ADR-010); the on-device default stays fail-closed.

## Current Outstanding Blocker(s)

1. **Device/emulator runtime verification (steps 5/10):** the instrumented
   suite is written and compiles but has not run on hardware. This is the final
   Sprint 8 gate — nothing below is claimed shipped until these rows flip to ✅.
2. **Production keystore (step 11):** human-owned. The build is configured to
   read a gitignored `keystore.properties`; a real keystore must be provisioned
   and its secrets added to CI to produce a distributable signed AAB.
3. **AICore handoff (step 12):** intentionally staged out of v1 (ADR-010); the
   connected mode requires an ADR + INTERNET capability change. OCR (ADR-009)
   is no longer a blocker — it ships with step 10's device run.

## Definition of "Release-Ready"

- All ⚠️ rows above flipped to ✅ with evidence citations (no unverified rows).
- Release-readiness table fully PASS with citations (`docs/release-readiness.md`).
- CHANGELOG + sprint records up to date.
