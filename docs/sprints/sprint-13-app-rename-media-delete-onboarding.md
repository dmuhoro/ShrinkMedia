# Sprint 13 — App Rename + Media Delete + First-Run Onboarding + Liveliness Ideas (EXECUTED)

**Status:** EXECUTED (2026-09-02)
**Version Target:** v0.5.0 (versionCode 5)

## Focus

User directions after v0.4.0:
1. **Rename** the app — the device launcher label is "Media Compressor"; it should be **ShrinkMedia**.
2. **Delete compressed media** — a button on the right of "Your media library" to enter a
   multi-select mode and delete files (with confirmation), so the user can free up space.
3. **Make the app more lively** — develop concrete ideas (`docs/ideas.md` I002) and, as the
   first implemented slice, ship **bit-sized first-run onboarding pointers** (privacy,
   finding compressed copies, freeing space via Select/delete).

## Deliverables & Evidence

### Layer 1 — App Rename (launcher label → ShrinkMedia)

| Task | Change | File |
|------|--------|------|
| Launcher label | `app_name` → "ShrinkMedia" | `app/src/main/res/values/strings.xml` |
| Foreground notification | "Compressing…" → "ShrinkMedia" | `BatchCompressionService.kt` |
| Web-sim parity | title/meta + visible `h1`/mark label → ShrinkMedia | `index.html`, `src/App.tsx` |

Historical docs record the old label for the release they describe — past state is
documented, not rewritten.

### Layer 2 — Media Library Multi-Select Delete

| Task | Change | File |
|------|--------|------|
| **Select** button | Right-aligned in the "Your media library" header; toggles selection mode (Select ↔ Done) | `MainActivity.kt` (`MediaTab`) |
| Selection mode | Per-card `Checkbox` (replaces the Compress button while selecting); selected cards get an error-color border | `MainActivity.kt` (`MediaFileCard`) |
| Action bar | Count / Cancel / **Delete** (error color, disabled when nothing selected) | `MainActivity.kt` (`MediaTab`) |
| Consent | Confirmation `AlertDialog` before *any* delete | `MainActivity.kt` (`MediaTab`) |
| API 30+ delete | `MediaStore.createDeleteRequest` → **system consent dialog**; launched via `ActivityResultContracts.StartIntentSenderForResult` + `IntentSenderRequest`; `RESULT_OK` → files removed from library | `MainActivity.kt` (`buildDeleteRequest`, `applyMediaDeletion`) |
| API <30 fallback | Direct `contentResolver.delete` per row with explicit partial/full-failure toasts (no silent drop) | `MainActivity.kt` (`deleteLegacy`) |

Key API fact (found by `javap` on `android-35/android.jar`): `MediaStore.createDeleteRequest`
returns `android.app.PendingIntent` (not `Intent`) — hence the `StartIntentSenderForResult`
launcher. `Delete` is only reachable after the confirm dialog; `Cancel` restores the library.

### Layer 3 — First-Run Onboarding Pointers

| Task | Change | File |
|------|--------|------|
| Onboarding card | "Get to know ShrinkMedia" with 3 bullet pointers + **Got it** | `MainActivity.kt` (`MediaTab`) |
| Persistence | Additive DataStore key `ONBOARDING_DISMISSED`; default `false` → card shows until dismissed (fail-closed) | `SettingsDataStore.kt` |
| Unit test | `onboardingDismissedDefaultsToFalseFailClosed` | `CompressionQualityUnitTest.kt` |

### Layer 4 — Liveliness Ideas (docs, not code)

- `docs/ideas.md` **I002** — "make the app more lively" idea radar (see
  `docs/ideas.md`). Only the first-run onboarding pointer is implemented this sprint;
  the rest are staged, costed options with privacy/scope notes.

## Validation & Verification Checklist

- [x] Web-sim parity gates: `npm run lint`, `npm test` (18), `npm run build` green
- [x] `compileDebugKotlin` PASS
- [x] `testDebugUnitTest` PASS (incl. new onboarding-default test)
- [x] `assembleDebug` PASS
- [x] `compileDebugAndroidTestKotlin` PASS
- [x] `assembleRelease` (R8) PASS — repacked for v0.5.0
- [x] `lintDebug` PASS
- [x] Device (API 36 `49IZ6DJ7SONNQOBE`): v0.5.0 debug install (`adb install -r --user 0`),
      launch, `MainActivity` top resumed, no crashes
- [x] On-device text proof (uiautomator dump): top bar **ShrinkMedia**, **Get to know
      ShrinkMedia** onboarding card (+ 3 bullets + **Got it**), **Select**, "Your media library"
- [x] Onboarding persistence proof: `onboarding_dismissed` key committed `08 01` (bool true)
      to the DataStore file; clean-datastore relaunch shows the card again
- [x] Release artifact: `versionCode 5` / `versionName 0.5.0`, label `ShrinkMedia`,
      `apksigner verify` PASS (production cert SHA-256 `21569322706156fe...`)
- [x] All commits SSH-signed; pushed to `origin/main`; tag `v0.5.0`; GitHub Release `v0.5.0`
      with signed APK

## Honest Device Gap (reported, not hidden)

`adb shell input tap` is denied on this device (`SecurityException` — no `INJECT_EVENTS`),
and `pm clear` is similarly restricted. The system-consent delete flow therefore needs a
**human hand** to tap through Select → Delete → confirm on-device; that final tap-path is
recorded as pending manual verification. Everything short of that is verified: rendering
(uiautomator), consent wiring (compile + `PendingIntent` construction), persistence
(DataStore commit observed), and legacy delete logic (compile + code review).

## Cross-Reference

- Evidence: `docs/evidence/2026-09-02_app_rename_media_delete_onboarding.md`
- `docs/current-state.md` (C19)
- `docs/architecture.md` (Media Eng. + Settings rows)
- `docs/decisions.md` (D007)
- `CHANGELOG.md` v0.5.0
- `docs/ideas.md` (I002)
- `docs/sprint-cross-reference.md`