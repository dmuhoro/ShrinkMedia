# ShrinkMedia vs the 2026 Consumer-App Standard — Honest Benchmark

Date: 2026-09-03 · Owner: Daniel (Lead)
Research: websearch of Play Console policy + Android quality guidelines + 2026 image-compression
competitor round-ups. Sources cited inline.

## 1. The 2026 Play Store / consumer-app "baseline" (what a real-world app must have)

| Requirement | Rule (2026) | ShrinkMedia today | Verdict |
|---|---|---|---|
| **Target API level** | NEW apps must target **Android 16 (API 36)**; existing apps must target **35+** by 2026-08-31. Below → hidden from new users on newer OS. | `targetSdk = 35` (v0.6.0) | ⚠️ **Compliant as existing; NOT enough for a fresh Play submission.** Must bump to 36 for a store launch. |
| **Data-safety form + privacy policy** | Mandatory for every app, even if it "collects no data". **On-device-only processing does NOT need disclosure** (big advantage for us). | No form, no privacy-policy doc yet | ⚠️ Missing (human/console task, but doc is code-adjacent work we can do). |
| **App bundle (AAB)** | Mandatory for new apps on Play. | `bundleRelease` produces a signed AAB (verified) | ✅ Ready (needs STORE secrets + Play upload). |
| **Minimize permissions** | Only core-use-case permissions; graceful degradation. | Base release = NO INTERNET, minimal perms | ✅ Already ahead. |
| **Third-party AI compliance** | AI/SDK data handling now in scope; transient/on-device is lowest-risk. | On-device OCR now; Nano + Connected-mode designed | ✅ Low-risk by design (ADR-011/012). |

## 2. Competitive benchmark — image/media compression tools (2026)

Research (zeropng.com, orthogonal.info benchmark, getimgpic.com, App Store/Play listings):

| Tool | Architecture | Offline? | Batch | Formats | Privacy | ShrinkMedia relative |
|---|---|---|---|---|---|---|
| **Squoosh** (Google) | Client-side WASM | Partially | **No (1 file)** | JPEG/WebP/AVIF | ✅ local | We add **batch + native video + PDF + OCR** |
| **ZeroPNG** | Client-side PWA | ✅ | Unlimited | Img + HEIC | ✅ local | Philosophical peer; **web-only, no video/PDF** |
| **iLoveIMG / TinyPNG / CloudConvert** | **Cloud** (upload) | ❌ | Limited/free caps | Varies | ⚠️ uploads | We are **offline + no limits + no upload** |
| **VSCO** (consumer photo editor) | Cloud-backed, **AI iOS-only** | ❌ (core) | Presets/batch | Film editing | ⚠️ | Brand-ahead, but **paid + AI not on Android even** |
| **XnConvert** | Desktop local | ✅ | Unlimited | 500+ | ✅ local | Desktop-only; we are a **native phone app** |

**Honest compression-quality note:** we are *not* claiming better encoders than Squoosh/ZeroPNG
(MozJPEG/WebP/AVIF can beat our JPEG in some cases). Our advantage is **native + batch + video +
PDF + offline + no-INTERNET in ONE installable app** — a combination none of the web tools have.

## 3. Where ShrinkMedia is AHEAD (evidence-backed)

- **Truly offline, no-INTERNET, no-upload compression/video/PDF/OCR** — the privacy-by-construction
  model the Play Data-safety rules treat as the *lowest* disclosure burden. Most competitors are
  forced-cloud with free-tier caps.
- **Unlimited batch** on-device (no 15/day, 20/month, 30-at-once caps) — matches/beats even the
  best local web tool, and adds **video + PDF + OCR** they lack.
- **Signed, R8-minified, production-keystore build** with a working release pipeline + CI merged-
  manifest INTERNET guard (real boundary) — many indie apps don't enforce privacy at the artifact.

## 4. Where ShrinkMedia is BEHIND (honest — do not hand-wave)

1. **Not on Play Store** (sideload/GitHub only) — the single biggest distribution gap for a
   consumer app. Blocked by human Play-Console work + `targetSdk` 36 for a fresh submission.
2. **No data-safety form / privacy-policy / store listing assets** (screenshots, feature graphic,
   content rating).
3. **`targetSdk = 35`** — fine for existing, but a **new** Play app must be **36**.
4. **Single-device verification (API 36)** — the 2026 bar expects a device matrix (API 24–36) and
   graceful degradation on mixed hardware.
5. **AI is design-only** (ADR-011/012) — no live Nano or cloud AI yet. VSCO's AI is gated behind
   paid iOS; we haven't shipped any AI surface at all.
6. **C5 batch on-device run blocked** by device storage — a real gap, not cosmetic.
7. **No web-app parity** for the compression features (web sim is a savings calculator, not a
   tool), and no iOS port.

## 5. Bottom line (brutal but fair)

- **Engineering/architectural maturity: ahead of most indie and many consumer apps.** A genuinely
  private, offline, multi-format, signed, verifiably no-INTERNET native utility is rare in 2026,
  where the norm (TinyPNG, iLoveIMG, VSCO) is cloud-with-upload, quotas, and often AI walled behind
  paid tiers.
- **Consumer reach/distribution: behind.** Without Play Store + data-safety + API-36 + multi-device
  verification, it remains a capable sideloadable toolkit, not a (store-distributed) consumer app.
- **AI: not yet shipping** — designed but unveried; the *plan* positions us to do local-first better
  than cloud-first competitors, but that is unproven until a sprint lands it.

## 6. Actionable gaps this empowers (feeds the next patch sprint, L4)

- Bump `targetSdk`/`compileSdk` to **36** (Play new-app bar) with a device pass.
- Author an in-app + hosted **privacy policy** and Play **data-safety** disclosure (on-device-only
  → minimal). These are docs we can draft now (code-adjacent).
- Prepare store assets + content rating + the signed AAB for a closed track (human upload; docs/CI
  can prestage).
- Broaden the device matrix (emulator API 24–36) once storage/emulator permits; clear C5's storage
  blocker.
- Consider an offline WebP/AVIF output option to close the "Squoosh codec lead" on images; keep
  the honest claim (native + batch + offline, not "best encoder").

## Related

`docs/release-readiness.md`, `docs/release-roadmap.md` (steps 10/11/12), `docs/personal-intelligence.md`
(ADR-011/012), `.github/workflows/ci.yml` (merged-manifest INTERNET guard).