# Evidence — Connected-mode Layer-1 foundation (ADR-012/013) + real-path proof

**Date:** 2026-09-04
**Status:** PASS for the Layer-1 foundation — an additive, fail-closed **Connected mode** seam is
implemented, unit-tested, **verified on-device through the real DataStore**, and the default
**debug + release merged manifests still declare NO INTERNET** (CI-guarded). Honest limit: this
sprint ships the **seam + consent + persisted settings**, NOT a live connected action — that is L2+
per ADR-013 §5 and needs the owner's hardware/credentials.

## What was built (the real production code, not helpers)

- **`SettingsDataStore.kt`** — additive `connected_mode` (OFF default) + `connected_consent_shown`
  (false default) keys, mappers, and `updateConnectedMode()` / `updateConnectedConsentShown()`.
  Fail-closed + additive (AGENTS §3); nothing may assume Connected mode is on.
- **`ConnectedRepository.kt`** (new) — the fail-closed gateway. `ModeState`
  (OFF / CONSENT_REQUIRED / ON) + a typed `ConnectResult` sealed class
  (Allowed / Off / Refused / Error). `run(connectedModeEnabled, consentShown, explicitlyInvoked, block)`
  executes the connected block **only** when EVERY gate holds (mode ON + consent shown + explicitly
  invoked); a block throw surfaces a typed `Error` — **no silent drops** (Constitution I.4/I.6).
- **`MainActivity.kt`** — `ConnectedModeCard` in the Elite AI tab: honest state, a switch, and a
  first-enable **AlertDialog** requiring explicit acknowledgment of the ADR-012 privacy disclosure
  before Connected mode turns ON (`Not now` fails closed). ViewModel: `setConnectedMode()`,
  `acknowledgeConnectedConsent()`.
- **`.github/workflows/ci.yml`** — new step asserting the **merged DEBUG manifest** (the default
  install) declares no `android.permission.INTERNET`, alongside the existing merged-release guard.

## Proof (evidence exercises the real path)

### JVM unit tests — `ConnectedRepositoryUnitTest` (8 tests, all passed)
`./gradlew :app:testDebugUnitTest` → **20 tests, 0 failures** (12 prior + 8 ConnectedRepository).
Cases: `modeState_ConsentRequired_WhenEnabledButNotAcknowledged`, `modeState_On_OnlyWhenEnabledAndConsentShown`,
`modeState_OffWhenDisabled_EvenIfConsentShown`, `enabledButNoConsent_Refused_BlockNotRun`,
`offByDefault_Refuses_AndDoesNotRunBlock`, `enabledConsentedAndExplicitlyInvoked_Allows_AndRunsBlock`,
`enabledAndConsented_ButNotExplicitlyInvoked_Refused`, `blockThrows_SurfacesTypedError_NotSilent`.

### On-device instrumented round-trip — `ConnectedSettingsRoundTripTest` (3 tests)
Drives the **real** `SettingsRepository` over the on-device Preferences DataStore
(`user_settings`), proving:
1. `connected_mode` **defaults to OFF** (fail closed) and consent defaults to not-shown.
2. `updateConnectedMode(true)` + `updateConnectedConsentShown(true)` **persist and read back**
   through the production flow.
3. A **fresh** `SettingsRepository` reads the persisted value **from disk** (not an in-memory cache).

Command: `adb shell am instrument -w -r -e package com.shrinkmedia.compressor ...` on serial
`49IZ6DJ7SONNQOBE` (Redmi, API 36) → **OK (16 tests)** (was 13; +3 round-trip). All three
round-trip cases appear in the run log with success.

### Build / lint gates
- `./gradlew :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest` → **BUILD SUCCESSFUL**;
  `lintDebug` **0 errors** (warnings informational).
- Merged **debug** manifest: `rg android.permission.INTERNET` → **no match** (the new CI guard runs
  clean locally).
- `./gradlew :app:assembleRelease` (R8) → BUILD SUCCESSFUL; merged **release** manifest no INTERNET.

## Honest boundary
- Connected mode is a **foundation**: a fail-closed seam + consent + persisted, verified settings.
- **NO INTERNET permission is added** in any default build. The on-device privacy invariant is
  unchanged (manifest declares no INTERNET; CI is the real-boundary guard).
- The actual **DataBank transfer contract** and the **connected (INTERNET) build variant** are L2+
  per ADR-013 §5 — a dedicated future program requiring the owner's hardware/network/credentials.

## Cross-reference
- Sprint: `docs/sprints/sprint-18-v0.7.1-connected-foundation-ecosystem.md`
- ADRs: `docs/adr/ADR-012-connected-mode-cloud-ai-opt-in.md`, `ADR-013-personal-ecosystem-databank-portal.md`
- Current state: `docs/current-state.md` C17 (foundation-implemented), C21 (ecosystem design).
- Ecosystem vision: `docs/ecosystem.md`.
