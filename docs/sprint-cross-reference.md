# Sprint Cross-Reference

> Audit: every sprint's claims vs actual code/test evidence. Built to catch
> narrative that outruns reality (Constitution Article I.1, VI.7). Updated as
> sprints close.

| Sprint | Claimed capability | Real evidence | Verdict |
|--------|---------------------|---------------|---------|
| 1 — Initialisation | Gradle/AGP scaffold + manifest | `build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`, `AndroidManifest.xml` at HEAD | ✅ Code exists |
| 1 — Initialisation | "Runs in Android Studio" | Informal only; no CI/device build recorded | ⚠️ Unverified on hardware |
| 2 — Web bootstrap | Vite+TS build | `npm run build` green (Sprint 5) | ✅ Verified |
| 3 — Foreground service | Service + notifications | `BatchCompressionService.kt` at HEAD | ✅ Code exists |
| 3 — Foreground service | Notification UX on device | No emulator/device run recorded | ⚠️ Unverified on hardware |
| 4 — Toolkit consolidation | 3-tab UI + ViewModel | `MainActivity.kt` at HEAD (187 lines, `UiState`) | ✅ Code exists |
| 4 — Battery-aware pause | `isPaused` controller + receiver | `BatchCompressionPauseController`, `ACTION_BATTERY_LOW` at HEAD | ✅ Code exists |
| 4 — Battery-aware pause | "No queue item dropped" | `executeBatchProcessing` awaits `isPaused.first { !it }` | ✅ Code enforces; device walkthrough pending |
| 4 — Live autosave | Per-file DataStore read | `settingsRepo.userSettingsFlow.first()` inside loop | ✅ Code exists |
| 5 — Governance | Constitution/ADRs/sprints/evidence/CI | Files at HEAD | ✅ Code exists |
| 5 — Web gates green | lint/test/build | `docs/evidence/2026-08-30_web_lint_test_build.md` | ✅ Cited |
| 5 — Android gate | `assembleDebug` | BLOCKED (SDK install pending) | ⚠️ Open |

## Rules That Keep This Honest

1. A row claims a **capability**, the right column cites **code or a test
   result**, and the verdict is ✅ only when the citation is real.
2. "Code exists" and "verified on hardware" are different verdicts — the table
   never conflates them.
3. Any sprint whose claims outgrow this table gets flagged, not silently
   rewritten.