# Sprint 18 — Connected-mode Layer-1 foundation + personal-ecosystem design

**Status:** EXECUTED (2026-09-04)
**Scope:** lay the fail-closed foundation that turns ShrinkMedia into the phone **portal** of the
owner's personal ecosystem — an additive, OFF-by-default **Connected mode** (ADR-012/013
Layer-1) plus the ecosystem design docs (ADR-013 + `docs/ecosystem.md`) that map DataBank
(separate repo, self-hosted server) + Forge + MCP to the owner's "Founder's Engine" vision.
**No INTERNET permission is added; the default build stays offline and private.**

## Focus

1. **Connected mode = the real fail-closed seam (ADR-012/013 L1).** The default build must keep
   NO INTERNET while still having a real, tested, gated place where a future connected action
   would slot in. We built that seam for real:
   - **`SettingsDataStore.kt`**: additive `connected_mode` (OFF default) + `connected_consent_shown`
     (false default) keys, mappers, and setters. Fail-closed, additive, never removed (AGENTS §3).
   - **`ConnectedRepository`** (new): a typed fail-closed gateway. `ModeState`
     (OFF / CONSENT_REQUIRED / ON) + `ConnectResult` sealed class (Allowed / Off / Refused / Error)
     with **no silent drops** (Constitution I.4/I.6). `run(...)` executes a connected block only
     when EVERY gate holds — mode ON + consent shown + explicitly invoked; a block throw surfaces a
     typed `Error`, never a swallowed failure.
   - **Consent UX**: `ConnectedModeCard` in the Elite AI tab — honest state, a switch, and a
     first-enable **AlertDialog** requiring explicit acknowledgment of the ADR-012 privacy
     disclosure before Connected mode turns ON. `Not now` fails closed.
2. **Real-path proof (Constitution §11, AGENTS §1).** Evidence exercises the real boundary, not a
   helper:
   - `ConnectedRepositoryUnitTest` — 8 JVM tests proving the fail-closed decision matrix
     (OFF refuses + block not run, consent-gated, explicit-invocation-gated, all-gates→Allows,
     typed error on throw, ModeState mapping).
   - `ConnectedSettingsRoundTripTest` (new, on-device) — drives the **real** `SettingsRepository`
     over the on-device Preferences DataStore: asserts OFF-by-default, write→read-back
     persistence, and that a **fresh** repository reads `connected_mode`/`connected_consent_shown`
     from disk (durable persistence, not an in-memory cache).
   - Full instrumented suite on the Redmi API-36 handset: **OK (16 tests)** (was 13; +3 from the
     round-trip).
3. **CI no-INTERNET guard strengthened to the default build.** New step in `ci.yml` asserts the
   **merged DEBUG manifest** (the default install) declares no `android.permission.INTERNET`,
   alongside the existing merged-release guard — so a dependency that merges INTERNET into the
   default build fails CI (real boundary).
4. **Ecosystem design.** ADR-013 + `docs/ecosystem.md` answer the owner's questions and guide
   all future work:
   - **DataBank is a separate project/repo** (a server, not an APK) — independent lifecycle,
     shared only the **MCP** `vault.*` contract with ShrinkMedia (the portal). This is the
     architecture chosen for clarity over building a server inside the mobile app.
   - **Sequencing L1→L5**; honest **can/cannot automate** boundary; **80/20 schedule** (L1–L4 ≈
     first ~2 months for daily-operational value); **compute floor** (RTX 3090+ 24 GB + Mac mini
     ≈ $2,500–4,500); the **Founder's Engine** pillars (product factory, safety net,
     virtual-me/guardian, metacognition, polymath knowledge base).

## What shipped

| Change | Files | Proof |
|--------|-------|-------|
| Additive connected settings (OFF/consent-shown) | `SettingsDataStore.kt` | JVM + on-device round-trip |
| Fail-closed `ConnectedRepository` | `ConnectedRepository.kt` | 8 JVM tests green |
| Consent UX (`ConnectedModeCard` + AlertDialog) | `MainActivity.kt` | assembleDebug + lint green |
| Instrumented real-path round-trip | `ConnectedSettingsRoundTripTest.kt` | on-device **OK (16 tests)** |
| CI default-build no-INTERNET guard | `.github/workflows/ci.yml` | merged debug manifest PASS locally |
| Ecosystem design + vision | `docs/ecosystem.md`, `ADR-013` | cited below |
| Version 0.7.1 / versionCode 8 | `app/build.gradle.kts` | badging |

## Honest status (what is NOT claimed)

- **Connected mode has NO real connected action yet.** This sprint is the **Layer-1 foundation**
  (seam + consent + persisted settings + verified). The actual **DataBank transfer contract** and
  the **connected (INTERNET) build variant** are **L2+ per ADR-013 §5** and are a dedicated future
  program that requires the owner's hardware/network/credentials.
- **NO INTERNET permission is added** — the default debug + release merged manifests both declare
  none (CI-guarded real boundary). The on-device privacy invariant is unchanged.
- The virtual-me / guardian / autonomous-factory vision in `docs/ecosystem.md` §7 is the **long
  tail**; the automatable parts are milestone-gated, the research-grade parts stay ASPIRATIONAL
  until implemented *and* verified (Constitution §12).

## Verification

- `./gradlew :app:testDebugUnitTest` → **20 tests, 0 failures** (12 prior + 8 ConnectedRepository).
- `./gradlew :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest` → **BUILD SUCCESSFUL**;
  `lintDebug` **0 errors** (warnings informational).
- `adb shell am instrument -w -r -e package com.shrinkmedia.compressor ...` → **OK (16 tests)** on
  API-36 (incl. the 3 new Connected-mode round-trip tests).
- Merged **debug** manifest: no `android.permission.INTERNET` (CI guard runs clean locally).
- `./gradlew :app:assembleRelease` (R8) → BUILD SUCCESSFUL; merged **release** manifest no INTERNET.

## Evidence

- `docs/evidence/2026-09-04_c17_connected_foundation.md`
- `docs/evidence/2026-09-04_ecosystem_vision_ocr_synthesis.md`
- `docs/adr/ADR-013-personal-ecosystem-databank-portal.md`

## Outstanding (explicit, not hidden)

- **DataBank server (separate repo)** — L2, requires owner's hardware/network/credentials.
- **Connected (INTERNET) build variant** — L2+, behind explicit consent.
- **MCP adapters for Daftari / Hermes-Forge / Forge.ai; Forge orchestrator** — L3+.
- Real Gemini Nano inference proof (needs a Nano-capable device; C11 honest).
- Keystore off-machine copy + release tagging/Play distribution (human steps).
