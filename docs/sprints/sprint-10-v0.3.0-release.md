# Sprint 10 — v0.3.0 Signed Release + GitHub Sideload Distribution (EXECUTED)

**Status:** EXECUTED (2026-08-31)
**Version Target:** v0.3.0 (versionCode 3) — delivered as an on-device-safe,
SSH-signed early real-world release via GitHub Releases.

## Focus

Take the device-verified v0.3.0 build and make it a real, distributable, signed
artifact a human can install — without weakening the on-device / no-INTERNET
invariant. This closes the final release blocker (production keystore) and the
distribution path chosen by the user (direct APK sideload hosted via GitHub
Releases; **not** Google Play).

1. **On-device OCR final walkthrough + bug fix** (closes the last v1 honesty
   asterisk on OCR).
2. **Production keystore** generated outside the repo (never committable),
   `keystore.properties` gitignored (fail-closed if absent).
3. **Signed, R8-minified v0.3.0 release APK** built and `apksigner`-verified.
4. **GitHub Release `v0.3.0`** created from tag, signed APK attached (sideload).

## Deliverables & Evidence

### Layer B — On-device OCR walkthrough + bug fix
- **Test `OcrInstrumentedTest.recognizeText_readsLargeHighContrastText_onDevice`
  (commit `5095d89`)** — draws high-contrast "ShrinkMedia", drives the REAL
  `OcrHelper.recognizeText` on API-36, asserts the unambiguous core `SHRINKMED`.
  Device reads "SHRINKMEDI" (9/10 glyphs); the bundled Latin model consistently
  drops a trailing glyph on large monochrome synthetic text (documented
  model-fidelity quirk, not a wiring defect).
- **Production bug fix (commit `3b5c134`)** — `decodeBounded` chained
  `openInputStream(uri)?.use { decodeStream(..., bounds) } ?: return null`, but
  bounds-only decode (`inJustDecodeBounds=true`) always returns null, so OCR
  returned null before the downstream `outWidth<=0` check. Removed the
  erroneous elvis; `recognizeText` now returns recognized text on device.
- **Evidence:** `docs/evidence/2026-08-31_device_verification.md` (device suite
  bumped to **9/9** PASS including real OCR).

### Layer E — Docs (commit `cb6d5c9` + this record)
- `evidence`, `current-state.md` (C12 → verified), `release-readiness.md`
  (OCR device-walkthrough row + OCR bug-fix row), `README.md` (OCR →
  device-verified), `CHANGELOG.md` (corrected note: ML Kit **bundled** Latin
  model, no download).

### Layer F — Production keystore + signed release APK + GitHub Release
- **Keystore** `~/.android/keystores/shrinkmedia-release.jks` (PKCS12, 2048-bit
  RSA, SHA256withRSA, 10,000-day validity, alias `shrinkmedia`). Keystore +
  passwords live **outside the repo**; user must back them up off-machine.
- **`keystore.properties`** (repo root, **gitignored**, fail-closed if absent)
  points the release `SigningConfig` at the keystore.
- **Signed APK** `app/build/outputs/apk/release/app-release.apk`:
  - `aapt dump badging`: `versionName='0.3.0'` `versionCode='3'`
    `applicationId='com.shrinkmedia.compressor'`; label "Media Compressor".
  - `apksigner verify --print-certs`: certificate SHA-256
    `21569322706156fe02111cc5a8ce0f62e33c5229516710f7f078105c6cbb580a` (matches
    generated keystore).
  - R8 minified; seeds.txt/usage.txt/mapping.txt keep ML Kit + `OcrHelper`.
- **GitHub Release `v0.3.0`** from tag `v0.3.0`; signed APK attached for
  sideload. Distribution path: direct APK download (user chose sideload, not
  Play).

## Validation & Verification Checklist
- [x] All Android gates green (unit, assembleRelease, minifyReleaseWithR8).
- [x] Web simulator lint/test(18)/build green.
- [x] On-device suite **9/9** PASS incl real OCR (API-36).
- [x] `apksigner verify` PASS on the signed v0.3.0 APK (production keystore).
- [x] Signed APK installs + launches on the API-36 device (sideload proof).
- [x] Sprints + CHANGELOG + README updated.
- [x] All commits SSH-signed; pushed to `origin/main`; tag + Release created.

## Cross-Reference

`docs/evidence/2026-08-31_v0.3.0_signed_release.md`;
`docs/evidence/2026-08-31_device_verification.md` (9/9);
`docs/current-state.md` C13 (production-keystore signed release);
`docs/release-readiness.md` (v0.3.0 gate);
`docs/release-roadmap.md` step 7;
`CHANGELOG.md` v0.3.0.
