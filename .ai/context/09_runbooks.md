# 09 — Runbooks

## Purpose
Operational procedures condensed from `docs/runbooks/`.

## Authority Level
Operational.

## Consumers
DevOps/release agents, anyone running builds or the simulator.

## Source Documents
`docs/runbooks/android-build.md`, `docs/runbooks/web-simulator.md`.

## Update Rules
Mirror the docs runbooks.

---

## Android
```bash
./gradlew assembleDebug        # → app/build/outputs/apk/debug/
./gradlew test                 # JVM/unit tests
./gradlew installDebug         # to a device
```
Device smoke: compress image (assert smaller + audit count up) → autosave
toggle → `Pictures/ShrinkMedia` → battery-pause walkthrough → PDF
build/merge/split on this doc.

Failure modes: `SDK location not found` → set `ANDROID_HOME`/`local.properties`;
missing build-tools → `sdkmanager "build-tools;35.0.0" "platforms;android-35"`.

## Web simulator
```bash
npm install && npm run dev           # :3000
npm run lint                         # tsc --noEmit
npm test                             # Vitest
npm run build && npm run preview     # dist + local preview
```
Env: `GEMINI_API_KEY`, `APP_URL` at runtime only.

## CI
`.github/workflows/ci.yml`: `npm ci → lint → test → build` (+ Android wrapper
gate when SDK available on runners).