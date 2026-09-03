# Sprint 15 — Branding + Working Video Compression + CI-Proof (v0.6.0) (EXECUTED)

**Status:** EXECUTED (2026-09-03)
**Scope:** native release v0.6.0 — permanent AI branding (icon + OG cover), the
real-path on-device video-compression fix that closes the audit's #1 open risk,
and proof that the CI auto-deploy actually deploys with secrets set.

## Focus

1. Wire the user's ChatGPT-generated branding (launcher icon + web OG cover)
   into the build permanently.
2. Prove secrets-enabled auto-deploy by re-running the `Deploy Web to Vercel`
   workflow.
3. Close the last real audit gap — zero on-device coverage of `compressVideoFile`
   — with a genuine instrumented test, and fix what that test exposed.
4. Ship signed v0.6.0.

## Governance framing

- **On-device invariant holds.** `ffmpeg-kit-full` and `smart-exception-java` are
  both pure on-device (no network); the native manifest keeps **no INTERNET
  permission**. Branding is local asset wiring; the OG cover is a static web-hosted
  image for link previews only (web harness, ADR-006).
- **Honesty over optimism (Articles I, VII).** The new on-device test *proved* the
  shipped video-compression path was broken (stripped FFmpegKit → rc=1), and the
  fix is proven by the same test going green, not by narrative. C4/C14 in
  `current-state.md` were only flipped to ✅ **after** on-device proof existed.
- **Fail-closed preserved.** `compressVideoFile` still returns `null` (never a
  bogus file) on any encode failure; the caller surfaces it explicitly.

## Deliverables & Evidence

### Layer 1 — Secrets-enabled auto-deploy PROVEN

Manual `gh workflow run` of `deploy-web.yml` (run 33737963547) with
`VERCEL_TOKEN/ORG_ID/PROJECT_ID` set: the **`Deploy to Vercel (production)`** step
ran and succeeded, producing a new production deployment
(`shrinkmedia-...potzv5tn5-...vercel.app`), aliased live and HTTP 200. Previously
this path fail-closed-skipped because secrets were absent; now it demonstrably
deploys. `docs/evidence/2026-09-02_public_web_presence.md` + run logs.

### Layer 2 — AI branding (icon + OG cover)

- `Assets/icon.png` (1254×1254 RGBA) → legacy `mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}`
  `ic_launcher.png` (48/72/96/144/192px) + adaptive-icon foreground
  (`ic_launcher_foreground_photo.png`, 108dp, art centered in the 66/108 safe zone
  over `#2E7D6B`). APK badging: `icon='res/mipmap-anydpi-v26/ic_launcher.xml'`; APK
  installs + launches on API-36, no crashes.
- `Assets/og-cover.png` (1536×1024) → `public/og-cover.png` cropped to **1200×630**
  + `og:image`/`og:image:width|height`/`twitter:image` meta pointing at the absolute
  `https://shrinkmedia.vercel.app/og-cover.png`. Web gates green (18 tests, build).

### Layer 3 — Video-compression fix + real-path on-device proof

- **Defect (discovered on-device, not assumed):** `compressVideoFile` failed with
  `rc=1 ... Unrecognized option 'preset'`; the bundled `ffmpeg-kit:6.0.LTS` was the
  **audio-only** build (config `--disable-everything`, only wav/pcm/aresample; no
  libx264, no H.264 encoder, no MP4 muxer). Every real video encode returned `null`.
- **Fix:** dependency → `dev.ffmpegkit-maintained:ffmpeg-kit-full:8.1.7` (LGPL,
  openh264 + MP4 muxer) + `com.arthenica:smart-exception-java:0.2.1` (runtime
  companion `FFmpegKitConfig.<clinit>` needs); encoder → bitrate-driven
  `-c:v h264`; `CompressionQuality.videoCrf` → `videoMaxRate`/`videoBufSize`.
- **Proof:** new instrumented test generates a real large H.264 source and asserts
  the production path returns a smaller, decodable H.264 MP4 with `onProgress=100`.
  `connectedDebugAndroidTest` **10/10 PASS** on API-36; `testDebugUnitTest`, lint,
  assembleDebug green. `docs/evidence/2026-09-03_video_compression_dependency_fix.md`.

### Layer 4 — Docs/state → reality + v0.6.0 release

- `docs/current-state.md`: C3/C4/C6/C7/C14/C16 now ✅ (each with on-device PASS
  citation); C4 documents the dependency fix + arm64/x86_64-only ABI note.
- `docs/release-readiness.md`: Sprint 8 blocker 8/8→9/9→10/10; production-keystore
  blocker struck (real keystore + apksigner PASS); version checked boxes updated.
- `CHANGELOG.md` v0.6.0 entry; this Sprint 15 record; native version bumped to
  `0.6.0` (versionCode 6); web code-tab mirror synced; signed APK released.

## Versioning

Native **0.6.0** (versionCode 6); web package stays 0.4.0 (independent harness
versioning).

## Post-release follow-through (2026-09-03, same day)

After v0.6.0 shipped, several forward pieces landed (each in its own commit):

### C5 — real-path batch contract test + physical blocker (honest)

- Added package-visible seam `BatchCompressionService.executeBatchProcessingForTest`
  that forwards to the **real** `executeBatchProcessing` (the loop `onStartCommand` drives),
  and an instrumented test proving a queued item is held at the pause gate then completed
  exactly once (never dropped/skipped). This closes an AGENTS §1 false-confidence gap — the
  old contract test hand-rolled the wait instead of exercising the real loop.
- **On-device run is physically blocked** by device storage (`INSTALL_FAILED_INSUFFICIENT_STORAGE`,
  `/data` ~100% full); C5 is honestly NOT flipped to PASS. Evidence:
  `docs/evidence/2026-09-03_batch_real_path_contract_test.md`.

### AI architecture (ADR-011 on-device Nano + ADR-012 Connected mode)

- **ADR-011** (on-device AICore/Gemini Nano): availability-gated, offline, NO INTERNET added,
  foreground-only/quota-limited (architecture only). Grounded in 2026 research
  (`docs/evidence/2026-09-03_ai_architecture_adr011_012.md`).
- **ADR-012** (opt-in **Connected mode**): OFF-by-default cloud AI + Google Bridge; requires a
  constitutional INTERNET change delivered as a separate build variant so the default stays
  no-INTERNET (architecture only).
- **`docs/personal-intelligence.md`**: the offline/online/cloud design + capability ladder
  (cooking/restaurant/meal-kit analogy) and sequenced build order.
- `docs/current-state.md`: C11/C17 flipped from bare ASPIRATIONAL to **designed** (still
  unverified).

### 2026 marketplace benchmark

- `docs/marketplace-2026-benchmark.md`: honest where-ShrinkMedia-stands vs the Play bar
  (target API 36 for new apps, data-safety, AAB) and 2026 compression competitors
  (Squoosh/ZeroPNG local; iLoveIMG/TinyPNG/CloudConvert cloud; VSCO AI iOS-only).
  Architecture is AHEAD; distribution/reach is BEHIND.

### L4 patch — WebP output + privacy policy

- **WebP** (`compressImageFileAsWebP`, additive/back-compatible, lossy-default fail-closed) closes
  the modern-format gap from the benchmark. Unit-tested (`webpDefaultsToLossy_AndLosslessIsOptIn`);
  compile, unit, lint green. Evidence: `docs/evidence/2026-09-03_webp_privacy_policy.md`.
- **Privacy policy** (`docs/PRIVACY.md`): on-device-only, no-Internet-default disclosure + third-
  party lib table + AICore/Connected-mode (designed, not active). Play bar prereq.

## Gate result

All Android gates green on the v0.6.0 build. Evidence chain per Constitution
Article VII in `docs/evidence/`.