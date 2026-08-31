# Sprint 9 — On-device OCR + No-Silent-Drops + Device Verification (EXECUTED)

**Status:** EXECUTED (2026-08-31)
**Version Target:** v0.3.0

## Focus

Close two honest-capability tracks and complete the Chapter 6 device gate:
1. **Real on-device OCR** (ADR-009) — replace the long-standing "no OCR engine
   wired" gap with ML Kit `text-recognition`, keeping the no-INTERNET invariant.
2. **No-silent-drops** (Constitution Article I.6 / AGENTS.md §1.4) — batch
   failures that were silently skipped now surface a reason + on-device audit
   record + completion notification.
3. **Device verification (Sprint 8 gate)** — run the real instrumented
   pipeline on an API-36 phone and record PASS.

## Deliverables & Evidence

### ADR / planning layer — commit `be1f526`
- ADR-009 (on-device OCR via ML Kit, typed-null, no INTERNET).
- ADR-010 (Google Tools Bridge staged OUT of v1; AICore stays ASPIRATIONAL).
- `docs/ideas.md` idea radar I001.

### Layer A — OCR implementation — commit `36bcfab`
- `app/build.gradle.kts`: `com.google.mlkit:text-recognition:16.0.1` (justified:
  no existing lib provides OCR; on-device/privacy-safe, no new distinction).
- `OcrHelper.kt`: typed-null `recognizeText(context, uri): String?`; bounded
  decode (MAX_DIMENSION_PX=2560) + inSampleSize; `suspendCancellableCoroutine`
  bridging the ML Kit `Task`; recognizer closed in finally.
- `MainActivity.kt`: UiState `ocrUri`/`ocrText`/`ocrStatus`; `ocrImage(uri)` +
  `AiTab` "Scan reader (OCR)" card with explicit success/empty/no-text/failure
  states (no silent drops, Article I.6).
- Verification: `compileDebugKotlin` clean; `testDebugUnitTest` +
  `assembleDebug` exit 0; `minifyReleaseWithR8` BUILD SUCCESSFUL; R8
  seeds/usage keep ML Kit + `OcrHelper`.

### Layer C — Web-sim parity — commit `85f1e9e`
- `src/App.tsx` `GRADLE_CODE` snippet updated to include the ML Kit dependency
  so the web simulator reflects the real `app/build.gradle.kts`. `npm run
  lint`/`test`(18)/`build` all PASS.

### Layer D — No-silent-drops — commit `9b26f57`
- `BatchCompressionService.executeBatchProcessing`: per-file failures now
  counted with a plain-language reason (compression threw / no valid output /
  auto-save failed); the batch continues past a failure instead of aborting or
  silently skipping.
- New `BatchFailureAudit` (shared seam) writes a timestamped line to the
  app-sandbox `batch-audit.log` — an audit record is produced (Article I.6),
  and the seam lets instrumented tests prove it on the real `filesDir` path.
- Completion notification surfaces failure count + up-to-200-char reason
  summary and a warning icon when failures occur.

### Sprint 8 device gate — evidence `docs/evidence/2026-08-31_device_verification.md`
- Cleared the MIUI "Install via USB" gate, then `connectedDebugAndroidTest` →
  8/8 PASS on API-36 hardware.
- Fixed three genuine instrumented-test defects (commit `a3b10ee`) — not
  weakened assertions: armed the pause gate, void `@Test` method bodies, and
  replaced an on-device-undecodable synthetic BMP with a lossless PNG input.

## Doc updates (Layer E) — commit `db7120d` + this record
- `docs/current-state.md`: C12 OCR → implemented; C11 AICore ASPIRATIONAL v2;
  added C16 (no-silent-drops) + C17 (Google Tools Bridge v2).
- `docs/architecture.md`, `docs/decisions.md`, `docs/release-roadmap.md`:
  OCR status corrected; AICore staged v2.
- `docs/release-readiness.md`: v0.3.0 gate table + device-verification PASS rows.
- `docs/sprints/sprint-8-device-verification-final-gate.md`: promoted to
  EXECUTED.
- `docs/sprints/sprint-9-ocr-and-no-silent-drops.md` (this record).
- `CHANGELOG.md`: v0.3.0 entry.

## Validation & Verification Checklist
- [x] All Android gates green (compile, unit, assemble, R8).
- [x] Web simulator lint/test/build green.
- [x] Connected instrumented suite green on API-36 hardware (8/8).
- [x] Evidence filed for device verification.
- [x] Sprints + CHANGELOG updated.
- [x] All commits SSH-signed.

## Cross-Reference

`docs/adr/ADR-009-on-device-ocr-mlkit.md`, `ADR-010-google-tools-bridge-staged-v2.md`;
`docs/current-state.md` C12/C16/C17; `docs/release-readiness.md` (v0.3.0 gate);
`docs/release-roadmap.md` step 12; `docs/evidence/2026-08-31_device_verification.md`.
