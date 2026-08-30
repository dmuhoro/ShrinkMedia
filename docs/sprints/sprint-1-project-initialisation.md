# Sprint 1 — Project Initialisation & Build Scaffold

**Theme/Result:** Root Gradle build, app module, manifest, and base repository
files that give ShrinkMedia a compilable skeleton.

## Summary

| Capability | Status | Evidence |
|------------|--------|----------|
| Root Gradle build (AGP 8.7.2, Kotlin 2.0.21, Compose plugin) | ✅ | `build.gradle.kts`, `settings.gradle.kts` |
| App module (SDK 24–35, Compose BOM, Coil, FFmpegKit, DataStore deps) | ✅ | `app/build.gradle.kts` |
| Manifest with FileProvider (no INTERNET permission) | ✅ | `app/src/main/AndroidManifest.xml` |
| Git hygiene (ignore node_modules/build/env) | ✅ | `.gitignore` |

## Part 1 — Build scaffold

**Problem:** No structure existed for a Kotlin/Compose + Vite/TS dual repo.

**Fix:** Layered root `build.gradle.kts` with `apply false`, plugin
management in `settings.gradle.kts`, and a single `:app` Android module.

**Files:**
- `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`
- `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`,
  `app/src/main/res/values/strings.xml`, `app/src/main/res/xml/file_paths.xml`

## Test Evidence Summary

| Test | Result |
|------|--------|
| Repo syncs in Android Studio (JDK 17+) | ✅ (recorded informally; CI gate added later) |

## Artifacts

- Commits: `chore: initialize project and configure build`.

## Cross-Reference

`docs/current-state.md` C1/C8. Device build gate remains BLOCKED until the
wrapper + SDK are installed (release-readiness).