# Changelog

All notable changes to ShrinkMedia are documented here, following
[Semantic Versioning](https://semver.org/). Sections: **Added**, **Changed**,
**Fixed**, **Removed**. The full per-sprint narrative lives in
`docs/sprints/`.

## [Unreleased] — 2026-09-05 (Ecosystem Directive: RSI governance + measurement + skills workbench + host tiers + DataBank vault MVP)

> **No version bump — ShrinkMedia is in maintenance mode** (v0.9.0 released). This sprint answers the
> 2026-09-05 directive (RSI requirements, ergodic + processing-vs-storage detail, 80/20 DataBank-first
> roadmap, hardware optionality) **in code and doctrine**, and ships the **DataBank vault MVP** to its
> own repo — the first working piece of the ecosystem.

### Added

- **ADR-016** — Ecosystem deployment profiles: `T0_TINY/T1_MID/T2_HEAVY` host tiers, fail-closed
  backend/isolation decisions (SQLite+FTS5 default; Postgres+pgvector T2-only; containers only ≥8 GB).
  Elitebook 2540p = bare-metal Debian 13, no hypervisor (RAM tax > isolation margin on ≤8 GB).
- **`ecosystem/HostTier` + `HostClassifier` decision layer** — the phone becomes the "which machine
  runs what" referee. 12 new JVM tests (suite 85 → **97**).
- **`docs/operations/skills-workbench.md`** — the "how we build" upgrade: audited Matt Pocock's
  `mattpocock/skills` (MIT, `3cca18b`), adopted 12 pieces (wayfinder decision-map, two-axis
  code-review, seams-first TDD, to-spec, diagnosing-bugs loop, wait-what re-pitch, …) re-cut to our
  invariants, with a **Reuse Ledger** (S-xx counted per use; a piece enters the SOP only after ≥2
  real uses). `AGENTS.md §13` binds it.
- **`docs/operations/measurement.md`** — metrics dictionary (M1 layer wall-clock → M10 reviewer
  honesty), the per-layer **LayerLog**, and the **compression ledger** (naive-estimate ÷ achieved).
  `SOP §6` now points at it.
- **`docs/operations/rsia-program.md` + `rsia-authorizations.md`** — the RSI requirements
  (R-01…R-08): provenance, blast radius, reversibility, evidence gate, telemetry, invariant-trade
  ADR, timeline continuity, path-safety. **Owner is the sole authorizer; dial = 0 (OFF).** No agent
  may self-authorize. `AGENTS.md §14` binds it. Ledger tracks accepted authorizations (none yet).
- **`docs/operations/ecosystem-orientation.md`** — expanded: ergodic deep-dive (worked multiplicative
  example: the +36%-EV bet that kills 65% of single timelines) + the **full processing-vs-storage
  explainer** (library-archives vs reader-team; the exact seams; why brain-in-storage and per-app
  storage are both rejected).
- **`docs/operations/ecosystem-roadmap.md`** — rewritten as the **80/20 sequence**: DataBank daily-
  use proof first (checkpoint A), then server layer, then stargate, then Forge, then EasyTutor, then
  virtual-me; every cut named with the hours it saves.
- **`docs/operations/shrinkmedia-stop-point.md`** — decision upgraded to **CONFIRMED ACTIVE
  maintenance mode**; safe-to-stop with two conditions.
- **DataBank** repo (`dmuhoro/DataBank`): **`dvault` vault MVP** — python-stdlib-only, SQLite+FTS5,
  WAL, append-only, supersede-not-delete, JSONL journal audit, fail-closed (`put` refuses blank /
  unknown supersede; `check` = records == journal + integrity); CLI `capture/query/list/count/check/
  reindex`; **19 tests green** + runbook (`docs/RUNBOOK.md`) + Elitebook deploy doc
  (`docs/DEPLOY.md`). Pushed to its own repo (SSH-signed commits).

### Changed

- **Suite** 85 → **97 JVM tests** (12 new HostClassifier), 0 failures; `lintDebug` 0 errors; merged
  DEBUG+RELEASE manifests still declare **no `android.permission.INTERNET`**.
- **`docs/current-state.md`** C25 row; **release-readiness** Sprint-21 gate; **sprint-cross-reference**
  Sprint 21 row; **case study #3** (`docs/evidence/2026-09-05_ecosystem_directive_sprint.md`) runs the
  new workbench units on real work (S-01…S-06).

### Fixed

- None (maintenance mode; no production code change to the media/AI paths).

## [Unreleased] — 2026-09-05 (Sprint 22 — Ecosystem Execute: DataBank proven + hardened, Forge step 1, Daftari installable, ShrinkMedia docs)

> **No version bump — maintenance mode continues** (v0.9.0). The 2026-09-05 *execute* directive ran
> sequentially across the ecosystem: DataBank proved on this machine (Ubuntu) and hardened; Forge
> shipped its step-1 state machine; Daftari fixed its installability root cause and proved offline
> survival on the production build; ShrinkMedia produced the hardening audit + checkpoint map.
> Full record in `docs/sprints/sprint-22-ecosystem-execute.md`.

### Added

- **`docs/operations/hardening.md`** — cross-repo anti-fragility audit: where each ecosystem claim
  is proven at the real boundary vs still needs a host/credentials, plus the failure-class guard map
  (corrupt vault, oversized record, browser-shortcut install, offline drops, killed service, codec
  gap, task-state violation) and what a PASS does *not* mean.
- **`docs/operations/checkpoint-map.md`** — the numbered, exit-tested path to "DataBank + ShrinkMedia
  function end-to-end without crashing": Checkpoint 0 (done, Ubuntu proof) through #7 (fault drills),
  with the honest estimate (~1–2 focused weeks toward the connected verdict; gated ~2/3 of the way
  by the always-on host + a real handset).

### Changed

- **`docs/current-state.md`** — C26 row (Sprint 22 ecosystem execute track).
- **`docs/release-readiness.md`** — Sprint-22 gate summary; **`docs/sprint-cross-reference.md`** —
  Sprint 22 row. CI re-runs on push (4 jobs; manifests still no INTERNET).

### Fixed

- None (ShrinkMedia production code untouched this sprint by design).

## [0.9.0] — 2026-09-05 (Personal Intelligence Foundation + Ecosystem Go-Live)

> Capability block (versionCode 10), per the locked scheme `0.x.0` = capability milestone. Ships the
> **Personal Intelligence foundation** (ADR-015) and the **ecosystem go-live decisions**, and marks
> **ShrinkMedia's safest stop** — the app enters **maintenance mode** after this release; new feature
> work moves to the Forge (builder) and DataBank (vault) repos, which are now created and scaffolded.
> No INTERNET is added; the default build stays private and on-device.

> This is the app's contribution to "bring the digital realm to life": a phone that can read a photo
> of a note, decide on its own whether the intent is clear or needs one targeted question, and route
> the thought (save-to-vault / recall-from-corpus / learn-via-EasyTutor / clarify / refuse-loudly).
> The vision-era "vividly describe the image" captioning is **not claimed** — OCR is real, the
> vision model is a documented ASPIRATIONAL seam (C11 wall / ModelRouter). The on-device vault is
> in-memory this release; syncing to DataBank is a future Connected-mode step on owner hardware.

### Added

- **ADR-015** (`docs/adr/ADR-015-personal-intelligence-gateway.md`): storage and processing are
  **separate**, joined by the `vault.*` contract (MCP, ADR-013). The vault is the brain's memory,
  not its body — append-only, supersede-not-delete ("delete old ways of thinking" = version, never
  data loss), so past/present/future timelines stay one line. Best move for the owner's future:
  replace the brain freely without touching life-data.
- **Vault categories the owner asked for**: `NoteRecord` (`notedAt` = date noted, `reappearedAt` =
  when it showed up again, `followedBy` = what came after, `thread`, `supersedes`, source
  PHOTO/VOICE/TYPED/CAMERA) + `NoteClassifier` (deterministic successor link + re-appearance
  detection by idea-marker overlap; **honest: keyword-match, not semantic**). 9 JVM tests.
- **Image Insight (note-insight) flow** — `InstructionAider`: OCR-on-device → typed decision
  SelfExplanatory (proceed) / NeedsClarification (exactly one question, then proceed) /
  Refused (OCR failure — never guess). 8 JVM tests.
- **PersonalIntelligenceAgent** — the virtual-me *decision* layer: Recall (own-corpus evidence,
  never invented) / Learn (topic routed to EasyTutor — education stays in its domain) /
  SaveToVault (incl. re-captures, never dropped) / Clarify / Refused. 8 JVM tests.
- **UI — "Personal Intelligence — note insight" card** (Elite AI tab): pick a note photo → OCR →
  clarify-or-proceed → save to the on-device vault with the agent's routing shown.
- **Ecosystem go-live docs**: `docs/operations/ecosystem-orientation.md` (honest answers to the
  directive: RSI = gated self-improvement, ergodic value = path-safe compounding, processing-vs-
  storage decision, EasyTutor placement, virtual-me flow, impulse-vs-response framing),
  `docs/operations/ecosystem-roadmap.md` (per-project size/complexity/time + connect sequence),
  `docs/operations/shrinkmedia-stop-point.md` (incomplete-task inventory; maintenance-mode stop).
- **Forge + DataBank repos created** (private: `dmuhoro/Forge`, `dmuhoro/DataBank`) with purpose /
  invariants / README scaffolds — the next programs begin.

### Changed

- **Version** 0.8.0 → **0.9.0**, versionCode 9 → **10**.
- **`docs/current-state.md`**: C24 row (Personal Intelligence foundation); corrected a **stale
  orientation note** that said compression/PDF helpers were "device-untested" while C3/C4/C9/C10/C14
  hold verified device PASS — doc hygiene, assertion set unchanged.
- **Suite** 60 → **85 JVM tests** (25 new personal tests), 0 failures; `lintDebug` 0 errors; merged
  DEBUG+RELEASE manifests still declare **no `android.permission.INTERNET`**.

### Fixed

- *InstructionAider clarify-loop discarding the transcript* (caught by test): an ambiguous scan now
  keeps its text so the answer resolves to SelfExplanatory; a no-text scan resolves to Refused
  (terminates, never loops).
- *PersonalIntelligenceAgent recall gate swallowing re-captures* (caught by test): a re-affirmed
  idea is stored as a reappearing vault record (reappearedAt), never silently dropped.

## [0.8.0] — 2026-09-04 (Operational Workflow — the SOP + Forge portable engine)

> **Catch-up release.** This is the first GitHub release since **v0.6.0**. The v0.7.0 and v0.7.1
> milestones (WebP + SDK36 + on-device AI surface + privacy, then the Connected-mode C17
> foundation + ecosystem design) were implemented, tested, documented, and shipped in the codebase
> but **never tagged/released**. v0.8.0 therefore bundles that unreleased work **plus** the new
> Operational Workflow engine into one coherent capability milestone, so the published version
> matches what the release actually contains (ADR-014 versioning §"honest thoughts"): `0.x.0` =
> capability block, `0.x.y` = patch on a shipped block, versionCode always monotonic.

Process/engineering + capability release (versionCode 9). Ships **ADR-014** + `docs/operations/SOP.md`
(the codified SDLC for operating like an organisation: intake → PRD → architecture → build →
verify → review → evidence/docs → release gate → commit/push → lessons), and the **portable
Forge workflow engine** implemented in ShrinkMedia as **case study #1** (to be lifted into the
future Forge repo unchanged, ADR-013/014).

> The SOP makes "how we build" explicit and executable: it turns the implicit SDLC into a
> deterministic pipeline. The engine (`ForgeTask`, `EcosystemIndex`, `LessonBook`, `ModelRouter`)
> encodes exactly that lifecycle so the process is auditable, not a poster. The open-source-AI
> edge (online → free open-weight model; offline → local fallback) is added as a **decision seam
> only** — NO INTERNET is added and the default build stays offline and private.

### Added
- **`docs/operations/SOP.md`** — the Standard Operating Procedure governing the whole SDLC:
  operating doctrine (PM/Principal/EM/Lead hats + ten invariants incl. fail-closed, real-boundary,
  no silent drops, evidence-over-narrative), the deterministic task lifecycle (phases 0–9), the
  per-phase artifact table, a **Definition of Done checklist** for every task, the Reviewer-gate
  review checklist + evals discipline, cadence, and the **productivity/scale telemetry model**
  (measured velocity, not guesses; calibration-0 anchored to real git history — 104 commits / 18
  sprints / 14 ADRs / 27 evidence files over 6 committed days; model steps 60–100%).
- **ADR-014** — Operational Workflow as a **portable, repo-neutral core** implemented first in
  ShrinkMedia (case study #1) and carried into the Forge repo later; the **ModelRouter seam**
  (free open-source AI online, local fallback offline — Connected-mode-only, OFF by default).
- **`ForgeTask`** (`app/src/main/java/com/shrinkmedia/compressor/forge/ForgeTask.kt`) — the
  deterministic state machine encoding the SOP lifecycle (`queued → retrieving → building →
  reviewing → changes_requested → merged` / `blocked`); **no silent drops** (blocked routes to the
  human, never auto-retried); terminal states never resurrect; full history. 12 unit tests.
- **`EcosystemIndex`** (`forge/EcosystemIndex.kt`) — the local, deterministic, offline
  "search-up-the-ecosystem" corpus seed: chunked docs → inverted index → ranked keyword search
  with snippets; fail-closed (empty corpus/blank query ⇒ empty, never null); idempotent adds. 11
  unit tests. **Honest scope:** a keyword index (not semantic retrieval — that is the later Forge
  RAG layer).
- **`LessonBook`** (`forge/LessonBook.kt`) — Phase-9 lessons-learned capture written into the
  corpus so every completed task is retrievable next time (never re-invent the wheel); validated
  non-blank mandatory fields; duplicate-id idempotent. 6 unit tests.
- **`ModelRouter`** (`forge/ModelRouter.kt`) — the typed, fail-closed routing decision
  (`AllowedRemote` open-weight online / `AllowedLocal` offline fallback / `Off` / `Refused` /
  `Unavailable` with reason). Preconditions mirror `ConnectedRepository` exactly; **decision only,
  no network call, no INTERNET in the default build**. 11 unit tests.
- **Case study #1 evidence** (`docs/evidence/2026-09-04_forge_l1_operational_workflow.md`) — the
  EcosystemIndex task executed **through** the SOP phases 0→9, producing the working corpus + a
  captured lesson (`case-001-ecosystem-index`).
- **Catch-up release content** (implemented in v0.7.0/v0.7.1, now published): WebP output, SDK-36
  toolchain, on-device AI surface + IO-thread wiring (ADR-011), Connected-mode C17 foundation
  (additive OFF-default settings + fail-closed `ConnectedRepository` + consent UX), ecosystem
  design (ADR-013 + `docs/ecosystem.md`), CI no-INTERNET guard on the merged debug manifest, and
  the on-device feature benchmark. See the `[0.7.1]` / `[0.7.0]` sections below for full detail —
  those entries are now also shipped in this release.

### Changed
- **Full suite green: 60 JVM unit tests, 0 failures** (was 20); lintDebug **0 errors**;
  assembleDebug green. Debug merged manifest still declares **no INTERNET**.
- `docs/current-state.md` — added C22 (Forge workflow engine) / C23 (SOP as governing process).
- Version bumped to **0.8.0** (`versionCode 9`).

### Honest status
- The engine is **decision/test logic only** — pure Kotlin, zero I/O, zero network. The open-weight
  and local inference that the ModelRouter *routes to* require the owner's home-lab GPU /
  DataBank-Forge hosts (ADR-013 L2+) and remain a dedicated future program. The SOP §6 telemetry
  model is the honest waypoint that turns "percent of building-at-scale potential" from an
  assumption into a measured number.
- **Released-version alignment fixed:** the previous GitHub "Latest" was v0.6.0 while code had
  advanced to v0.7.1 unreleased. v0.8.0 reconciles code ↔ published state so the distribution trail
  matches capability. `0.x.y` is reserved for genuine follow-up patches on a shipped block.

## [0.7.1] — 2026-09-04 (Connected-mode Layer-1 foundation + personal-ecosystem design)

Native release (versionCode 8). Ships the **fail-closed foundation** for ShrinkMedia as the phone
portal of the owner's personal ecosystem: an additive, OFF-by-default **Connected mode**
(`connected_mode` / `connected_consent_shown`) with a typed, fail-closed
`ConnectedRepository` gateway, a first-run **privacy-disclosure consent UX**, and a CI guard that
asserts the **default debug AND release merged manifests carry NO INTERNET**. Also ships the
ecosystem design (ADR-013 + `docs/ecosystem.md`) that ties ShrinkMedia (portal) to the future
DataBank (separate repo, self-hosted server) over the MCP connectable layer. **No INTERNET is
added — the default build stays offline and private.**

### Added
- **Connected mode foundation (ADR-012/013, Layer-1)** — the real, fail-closed seam for the
  personal-ecosystem portal:
  - **Additive DataStore settings** `connected_mode` (OFF default) + `connected_consent_shown`
    (false default) in `SettingsDataStore.kt` — fail-closed, additive, never removed.
    `updateConnectedMode()` / `updateConnectedConsentShown()`.
  - **`ConnectedRepository`** — `ModeState` (OFF / CONSENT_REQUIRED / ON) decision + a typed,
    fail-closed `ConnectResult` (Allowed / Off / Refused / Error). `run(...)` only executes a
    connected block when EVERY gate holds (mode ON + consent shown + explicitly invoked); any
    failure surfaces a typed error — **no silent drops** (Constitution I.4/I.6).
  - **Real-path proof**: `ConnectedRepositoryUnitTest` (8 JVM tests) + new on-device
    `ConnectedSettingsRoundTripTest` — drives the **real** `SettingsRepository` over the on-device
    DataStore, asserts OFF-by-default, write→read-back persistence, and that a **fresh**
    repository reads `connected_mode`/`connected_consent_shown` from disk (not an in-memory cache).
    Full instrumented suite on the Redmi API-36 handset: **OK (16 tests)** (was 13).
  - **Consent UX** (`ConnectedModeCard` in the Elite AI tab): shows the honest Connected-mode
    state, a switch, and a first-enable **AlertDialog** that requires the user to acknowledge the
    ADR-012 privacy disclosure before Connected mode turns ON; `Not now` fails closed. Nothing
    connects unless explicitly enabled *and* consent is acknowledged.
- **CI no-INTERNET guard strengthened to the default build** (`.github/workflows/ci.yml`): a new
  step asserts the **merged DEBUG manifest** (the default install) declares no
  `android.permission.INTERNET`, alongside the existing merged-release guard. Real-boundary,
  fail-closed: any dependency that merges INTERNET into the default build fails CI.
- **Personal-ecosystem design (ADR-013 + `docs/ecosystem.md`)**: DataBank = **separate repo /
  server** (same founder, independent lifecycle, shared MCP `vault.*` contract — not inside the
  APK); the connectable layer speaks **MCP**; ShrinkMedia = the phone portal. Documents the "Founder's
  Engine" vision (autonomous product factory, safety net, virtual-me/guardian, metacognition,
  polymath knowledge base), the honest **can/cannot automate** boundary, the 80/20 **schedule**
  (L1–L4 ≈ first ~2 months for daily-operational value), and the **compute floor** (RTX 3090+
  24 GB + Mac mini ≈ $2,500–4,500). See `docs/sprints/sprint-18-*` + evidence.

### Changed
- Version bumped to **0.7.1** (`versionCode 8`).
- `docs/current-state.md`: C17 moved from **designed** → **🟡 foundation-implemented (offline-gated,
  seam only — no connected action yet)**; honest note that a real connected action / INTERNET
  variant is L2+ and still needs the owner's hardware/credentials.

### Honest status
- Connected mode is only the **Layer-1 foundation**: a fail-closed seam + consent + persisted,
  verified settings. **No actual network action exists yet** and **NO INTERNET permission is
  added** — the real DataBank transfer contract and the connected (INTERNET) variant are L2+ and
  are a dedicated future program per ADR-013 §5.

## [0.7.0] — 2026-09-03 (WebP + SDK36 + on-device AI surface + privacy)

Native release (versionCode 7). Ships the WebP output option, the SDK-36 toolchain,
the **real on-device "personal intelligence" surface** (ADR-011 via ML Kit GenAI /
Gemini Nano — build-verified, hardware-gated), and the keystore backup runbook.

### Added
- **On-device AI surface (ADR-011, L4b)**: `OnDeviceInferenceRepository`
  (fail-closed `Status` gate + `AiResult` contract, real ML Kit GenAI
  `Generation/GenerativeModel` path, guarded behind API 26, never cloud fallback)
  and an **Elite AI** tab panel that probes availability, renders each gate honestly,
  and only infers when AVAILABLE. 12 unit tests (5 new gate/result tests), lint
  0 errors. Evidence:
  `docs/evidence/2026-09-03_adr011_on_device_ai_surface.md`.
- **Keystore off-machine backup runbook (L6)**: `docs/runbooks/keystore-backup.md`
  + verified SHA256 anchor (`2ecf8c80...dca2d5b`). Evidence:
  `docs/evidence/2026-09-03_keystore_off_machine_backup.md`.
- **C5 real-path batch contract test** (+ test seam `executeBatchProcessingForTest`
  forwarding to the real `executeBatchProcessing` loop): proves a queued batch item
  is held at the pause gate then completed exactly once — never dropped/skipped.
  Closes the AGENTS §1 false-confidence gap in the batch path. Evidence:
  `docs/evidence/2026-09-03_batch_real_path_contract_test.md`.
- **AI architecture (ADR-011, ADR-012, `docs/personal-intelligence.md`)**: the
  sequenced on-device (Gemini Nano/AICore, ADR-011) and opt-in **Connected mode**
  (cloud AI + Google Bridge, ADR-012) designs for the offline/online/cloud
  "personal intelligence" vision. Architecture-only, grounded in 2026 research
  (`docs/evidence/2026-09-03_ai_architecture_adr011_012.md`).
- **CI release signing secrets wired** (`STORE_FILE`/`STORE_PASSWORD`/`KEY_ALIAS`/
  `KEY_PASSWORD` via `gh secret set`) so the fail-closed signed-AAB job can
  actually run; verified `bundleRelease` produces a signed AAB locally.
- **WebP output option** (`compressImageFileAsWebP`, additive/back-compatible,
  lossy-default fail-closed): modern offline format closes the benchmark gap vs
  Squoosh/ZeroPNG on WebP/AVIF; unit-tested.
- **Privacy policy** (`docs/PRIVACY.md`): on-device-only, no-Internet-default
  disclosure + third-party lib table; Play Store data-safety prereq.
- **2026 marketplace benchmark** (`docs/marketplace-2026-benchmark.md`): honest
  where-ShrinkMedia-stands vs the Play bar (target API 36, data-safety, AAB) and
  compression competitors.
- **On-device feature benchmark** (`OnDevicePerformanceBenchmark`): drives the real
  `compressImageFile` / `compressImageFileAsWebP` / `compressVideoFile` /
  `createPdfFromImages` / `mergePdfDocuments` / `extractRawTextFromUri` /
  `OcrHelper.recognizeText` on hardware with representative inputs, measuring wall-clock
  and verifying valid output inside a no-hang bound. **On the Redmi API-36 test handset
  all 7 complete in under ~3 s (heaviest = video ~2.8 s for a 4 s clip) — no hang, no
  degradation.** Evidence: `docs/evidence/2026-09-04_on_device_feature_benchmark.md`.
- **AI IO-thread wiring (future-device readiness)**: `OnDeviceInferenceRepository`
  `checkStatus()` and `generateContent(...)` now run on `Dispatchers.IO`, so when a
  Nano-capable device is present the on-device model runs off the main thread and does
  not degrade the app. Non-capable devices still fail closed (`UNAVAILABLE`, no cost).

### Changed
- **Toolchain (L2, L4a)**: compile/target SDK bumped to **API 36** (AGP 8.9.1,
  Gradle 8.11.1 — later AGP 8.10.0 for Kotlin 2.2 R8 support); **Kotlin
  2.0.21 → 2.2.0** required so ML Kit GenAI metadata links.
- `docs/current-state.md`: C11/C17 moved from bare **ASPIRATIONAL** to **designed**
  (still unverified); C5 updated with the real storage-bound blocker instead of a
  vague "device run Sprint 8".

### Fixed
- **C5 real-path batch test now executes on hardware.** The instrumented contract
  test (drives the real `executeBatchProcessing` loop) previously "compiled but
  never ran" — blocked by device storage. Once the device had space, running it
  surfaced **two genuine seam defects** that are now fixed in production code,
  not masked in the test:
  - `attachTestContext(Context)` attaches a real app base context and reuses the
    same `initRuntimeDependencies()` (notification manager + channel) as `onCreate`,
    so the loop's audit-logging and compression plumbing run for real (no null
    `applicationContext` NPE, no uninitialized `notificationManager`).
  - The end-of-run `stopForeground`/`stopSelf` is guarded behind `startedBySystem`
    (true only when Android actually started the service), preventing an
    unattached-service `NullPointerException` — production teardown is unchanged.
  On-device result: **OK (4 tests)** for the batch suite, **OK (13 tests)** for the
  full instrumented suite (incl. the new feature benchmark), 12 JVM unit tests green,
  lint 0 errors. Evidence:
  `docs/evidence/2026-09-03_batch_real_path_contract_test.md`. The original
  2026-09-03 storage block is superseded; C5 is now **✅ on-device PASS**.

### Honest status
- The on-device AI surface is **build-verified** (compiles, gates fail-closed on the
  real library, release shrinks clean with NO INTERNET) but **not hardware-proven**
  — a real Gemini Nano inference requires a Nano/AICore-capable device (L5). C5's
  device run — previously blocked by storage — is now **completed on-device PASS**.

## [0.6.0] — 2026-09-03 (branding + real working video compression)

Native release (versionCode 6). Ships three things: the ChatGPT-generated
**branding** (app icon everywhere + social OG cover on the web site), a genuine
**real-path start-to-finish video-compression fix** (the feature previously could
not encode any video), and the on-device proof that closes the audit's #1 open
risk.

> The shipped v0.5.0 "video compression" could not actually compress video: the
> bundled FFmpegKit was the audio-only "Lite" build with no H.264 encoder and no
> MP4 muxer. Every encode failed (rc=1) and `compressVideoFile` returned `null`.
> This is fixed and on-device proven in 0.6.0.

### Added
- **AI branding**: ChatGPT-generated ShrinkMedia icon wired into the Android
  launcher (legacy `mipmap-*` for all densities + adaptive-icon foreground over
  the teal `#2E7D6B` background); source art committed under `Assets/`
  (`icon.png` 1254×1254, `og-cover.png` 1536×1024).
- **Social OG cover** (web): `public/og-cover.png` (1200×630) + `og:image` /
  `twitter:image` meta on the live Vercel site for branded link previews.
- **On-device `compressVideoFile` instrumented test** — creates a real
  high-bitrate H.264 MP4 on-device and proves the production path re-encodes it
  smaller + decodable. `connectedDebugAndroidTest` now **10/10 PASS** on API-36.

### Fixed
- **Video compression actually works**: swapped `io.github.nikita36078:ffmpeg-kit
  :6.0.LTS` (audio-only) → `dev.ffmpegkit-maintained:ffmpeg-kit-full:8.1.7`
  (LGPL, openh264 H.264 encoder + MP4 muxer), added the `smart-exception-java`
  runtime companion, and changed `compressVideoFile` to bitrate-driven
  `-c:v h264` (openh264; no CRF/preset). Evidence:
  `docs/evidence/2026-09-03_video_compression_dependency_fix.md`.

### Changed
- **CI auto-deploy proven**: the `Deploy Web to Vercel` workflow, run manually
  with `VERCEL_*` secrets set, **deploys** (previously it fail-closed-skipped
  because secrets were absent). New production deployment live at
  `shrinkmedia.vercel.app`.
- **ABI coverage**: `ffmpeg-kit-full:8.1.7` ships `arm64-v8a` + `x86_64` only
  (no `armeabi-v7a`/`x86`). Modern devices are arm64; recorded honestly rather
  than hidden.
- Docs/state corrected to reality: C3/C4/C6/C7/C14/C16 now read ✅ with
  on-device PASS citations.

### Removed
- `CompressionQuality.videoCrf` (x264-specific; unusable without libx264) —
  superseded by bitrate `videoMaxRate`/`videoBufSize`.

## [0.4.0] — 2026-09-02 (web presence; native stays 0.5.0)

Public web presence: the web simulator is live on **Vercel** at
`shrinkmedia.vercel.app`, the GitHub repo got a professional description,
homepage link, and topics, and the README is productized. The deployed site is
the honest ADR-006 web-simulator harness — a pure static front-end, no network
calls, separate from the no-INTERNET Android app.

> Version note: the Android app remains **v0.5.0**. The web simulator package
> is versioned independently and is now at **0.4.0**. No native APK/version
> bump shipped in this sprint — this sprint is product-toward-world, not a
> release.

### Added
- **Vercel deploy** — `vercel.json` (vite framework, `outputDirectory dist`,
  SPA fallback), `.vercelignore` (lean upload, excludes `.git/.github/`
  `node_modules`/`dist`/`build`/`app`/`docs` and all keystore/secret material).
  Production URL: **https://shrinkmedia.vercel.app** (HTTP 200, correct
  title/description/favicon, SPA fallback verified).
- **SEO/meta + favicon** (`index.html`, `public/favicon.svg`): descriptive
  title/description, `og:site_name`, twitter meta, `theme-color`, favicon.
- **GitHub metadata** (repo-level): one-line description, homepage =
  `https://shrinkmedia.vercel.app`, **20 topics** (android, kotlin,
  jetpack-compose, media/image/video-compression, ffmpeg, pdf-tools, ocr,
  machine-learning, privacy, on-device, offline-first, mediastore, datastore,
  react, typescript, web-simulator, vercel, github-actions).
- **README productized**: live-site link + "Live Site" section, badges (vercel /
  MIT / kotlin / PRs), status rows for the v0.5.0 features and live presence,
  web build/run/deploy instructions.
- **CI auto-deploy** (`.github/workflows/deploy-web.yml`): gates-then-deploy,
  **fail-closed** on `VERCEL_TOKEN/ORG_ID/PROJECT_ID` — absent secrets ⇒ gates
  still pass, no deploy; path-scoped to web sources.
- **Docs**: Sprint 14 record + evidence; `docs/decisions.md` D008; web-simulator
  runbook deploy steps; `docs/current-state.md` C20; `docs/architecture.md`;
  `docs/sprint-cross-reference.md` Sprint 14 row.

### Changed
- Web package bumped to `0.4.0` (`package.json`/`package-lock.json`).

### Fixed
- None (no native code change; Android regression gates
  `compileDebugKotlin` + `testDebugUnitTest` re-run green).

### Note
- Privacy invariant held: deployed surface is a static web harness; the native
  app manifest still declares **no INTERNET permission** (CI guardrail enforces).
- Honest gap: CI auto-deploy is code-complete and fail-closed; it will only
  actually push to Vercel once the `VERCEL_*` secrets are set as GitHub secrets.
  Until then, deploys are manual (`vercel --prod --yes`).
- Evidence: `docs/evidence/2026-09-02_public_web_presence.md`.

## [0.5.0] — 2026-09-02

Identity + space-reclaim + first-run guidance: the app's launcher label is now
**ShrinkMedia**, the media library supports batch deletion with system consent,
and a first-run onboarding card points to the three habits that matter. All 6
Android gates and the on-device launch gate are green.

### Added
- **First-run onboarding card** ("Get to know ShrinkMedia", `MainActivity.kt` +
  `SettingsDataStore.kt`):
  - Three bit-sized pointers: on-device privacy (no INTERNET permission), where to
    find compressed copies + autosave, and freeing space via Select/delete.
  - "Got it" dismisses; persisted via the additive DataStore key
    `ONBOARDING_DISMISSED` (default `false` → fail-closed: the card shows until the
    user dismisses it). Unit test `onboardingDismissedDefaultsToFalseFailClosed`.
- **Media library multi-select delete** (`MainActivity.kt`): a **Select** button on
  the right of the "Your media library" header switches cards to selection mode
  (checkbox + error-color border), with a confirm `AlertDialog` before any delete.
- **API 30+ delete via system consent**: `MediaStore.createDeleteRequest` returns a
  `PendingIntent` (verified via `javap` on `android-35/android.jar`), launched with
  `ActivityResultContracts.StartIntentSenderForResult`; `RESULT_OK` removes the
  deleted files from the library, anything else toasts "Delete cancelled — no files
  were changed" (no silent drop).
- **API <30 fallback** `deleteLegacy`: direct `contentResolver.delete` with explicit
  full / partial / failure toasts.
- **Liveliness ideas radar** (`docs/ideas.md` I002) — staged, costed ideas for making
  the app more lively; only the onboarding pointer is implemented this sprint.

### Changed
- **App renamed** to **ShrinkMedia** (`app/src/main/res/values/strings.xml`
  `app_name`, `BatchCompressionService.kt` notification title, web-sim parity in
  `index.html` and `src/App.tsx`). Historical docs keep the old label for the release
  they describe.
- Version bumped to `0.5.0` (`versionCode 5`).

### Note
- All builds pass: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`,
  `compileDebugAndroidTestKotlin`, `assembleRelease` (R8), `lintDebug`; web-sim
  `npm run lint` / `npm test` (18) / `npm run build` green.
- App installs + launches on the API-36 device with no crashes; uiautomator on-device
  text proof shows the **ShrinkMedia** label, the onboarding card (+ "Got it"), and the
  **Select** button; the `onboarding_dismissed` bool commit (`08 01`) was observed in
  the sandbox DataStore.
- Manifest still declares **no INTERNET permission** (privacy invariant held).
- Honest gap: this device denies `adb shell input tap` (no `INJECT_EVENTS`) and `pm
  clear`, so the final human tap through the delete consent dialog is recorded as a
  manual verification step; everything short of that tap is verified.
- Evidence: `docs/evidence/2026-09-02_app_rename_media_delete_onboarding.md`.

## [0.4.0] — 2026-09-01

Real-world usability overhaul: the Media tab now shows the user's actual media
library with a vertical quality selector, PDF builds surface a preview dialog,
and embedded-text extraction preserves layout better. All 6 Android gates and
the on-device launch gate are green.

### Added
- **Your media library** (`MainActivity.kt` — `MediaFile`, `MediaFileCard`,
  `getUserMediaFiles`):
  - Queries `MediaStore.Images.Media` + `MediaStore.Video.Media` on
    `Dispatchers.IO` at ViewModel init (date-descending).
  - Coil thumbnails for images (`rememberAsyncImagePainter`), video icon for
    videos; shows name, type, size; per-item **Compress** button.
  - Purely local content-provider reads — no storage permission, no INTERNET.
- **Vertical quality selector**: `FilterChip` row → `Column` of `RadioButton`
  rendered **HIGH → MEDIUM → LOW** (descending) with per-preset JPEG q/max-dim
  captions; `CompressionQuality` reordered `HIGH(90,2560)`, `MEDIUM(75,1920)`,
  `LOW(55,1280)`.
- **PDF build preview** (`PdfPreviewState`): after `createPdfFromImages`, a
  card shows the file name, page count, and size with **Open** (FileProvider
  `ACTION_VIEW`, fallback toast "No PDF viewer found"), **Save to Gallery**
  (`saveToPublicMediaStore` + `addDocumentToRecent`, explicit success/failure
  toast), and **Discard** (deletes the temp file).
- **`ToolkitViewModel.showToast(message)`** — explicit SharedFlow emit for
  transient UI messages.
- **Sprint 12 docs**: `docs/sprints/sprint-12-media-gallery-quality-ux-pdf-preview.md`
  (EXECUTED), `docs/evidence/2026-09-01_media_gallery_quality_ux_pdf_preview.md`.

### Changed
- **PDF text extraction** (`extractRawTextFromUri`): `SimpleTextExtractionStrategy`
  → `LocationTextExtractionStrategy` (approximate X/Y layout preservation for
  better paragraph/column reconstruction) + `--- Page N ---` headers between
  non-blank pages. Fidelity limitation documented honestly (D006): not
  pixel-perfect; true fidelity needs `PdfRenderer` page rendering.
- Version bumped to `0.4.0` (`versionCode 4`).

### Fixed
- `MainActivity.kt` `android.graphics.Color` vs Compose `Color` conflict —
  Compose color aliased as `ComposeColor`; PDF canvas uses fully-qualified
  `android.graphics.Color.WHITE`.
- `Modifier.clip(shape)` unresolved in the Compose BOM — thumbnail tile uses
  `Modifier.background(color, RoundedCornerShape(8.dp))`, which clips rounded
  corners automatically.

### Note
- All builds pass: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`,
  `compileDebugAndroidTestKotlin`, `assembleRelease` (R8 executed), `lintDebug`.
- App installs + launches on API-36 device with no crashes (top resumed
  activity `com.shrinkmedia.compressor/.MainActivity`, logcat clean).
- Manifest still declares **no INTERNET permission** (privacy invariant held).
- Evidence: `docs/evidence/2026-09-01_media_gallery_quality_ux_pdf_preview.md`.

## [0.3.1] — 2026-09-01

PDF pipeline hardened with iText 7 (true vector build, page-exact merge, real
embedded-text extraction), UI polished with recent files management, settings
sheet, and dark/light/system theme support. All compilation gates green
including R8 minification.

### Added
- **iText 7.2.5 PDF engine** (on-device, no INTERNET):
  - `createPdfFromImages`: single `ITextLayoutDoc` + `AreaBreak(PageSize.A4)`
    per image → true vector PDF pages (crisp, no bitmap rasterization).
  - `mergePdfDocuments`: temp-file → `PdfReader(File)` → `copyPagesTo` →
    cleanup; reliable random access on Android, no `byte[]` constructor issues.
  - `extractRawTextFromUri`: iText `PdfTextExtractor` +
    `SimpleTextExtractionStrategy` on temp file; honest "scan / OCR needed"
    message when no embedded text found.
  - `readPdfMetrics` / `splitPdfIntoPages` retained on `android.graphics.pdf`
    (page count + bitmap fallback).
- **Recent files section** with expandable audit detail cards:
  - Share (system share sheet via `FileProvider`).
  - Delete → 5s undo (moves to app-sandbox trash dir, restores on undo).
  - Clear all → removes cached files + history.
  - Audit detail panel: quality preset, target bitrate, resolution scaling,
    duration, media type.
- **Settings sheet** (ModalBottomSheet):
  - Theme: System / Light / Dark (persisted).
  - OCR language: English, Spanish, French, German, Italian, Portuguese
    (6 options, additive DataStore key, default ENGLISH).
  - Batch mode toggle (advanced, off by default).
  - Autosave to gallery, Pause on low battery.
  - Privacy reminder: "All processing stays on-device. No files are uploaded.
    The app declares no INTERNET permission."
- **Snackbar toasts** via `MutableSharedFlow<String>` + `SnackbarHost` —
  transient user-facing messages replace inline status text.
- **Dark/Light/System theme** via `AppThemeMode` enum + `ThemeWrapper`
  composable; persists to DataStore.
- **OCR language parameter**: `OcrHelper.recognizeText(context, uri, language)`
  3-arg form; `OcrLanguage` enum with ML Kit language codes.
- **DataStore additive keys** (fail-closed defaults):
  - `ocr_language` (String, default `en`).
  - `enable_batch` (Boolean, default `false`).
- **R8 proguard fix**: `-dontwarn org.slf4j.impl.StaticLoggerBinder` in
  `app/proguard-rules.pro` (iText pulls in slf4j-api; binding is optional).

### Changed
- **PDF build/merge/extract** now use iText 7 consistently (was mixed
  `android.graphics.pdf` + heuristic text extraction).
- `mergePdfDocuments` and `extractRawTextFromUri` use temp-file pattern
  (D005) for reliable `PdfReader(File)` random access.
- `createPdfFromImages` uses single layout `Document` + `AreaBreak` per page
  (was new `PdfDocument` per image).
- `OcrHelper.recognizeText` signature extended with `language` parameter
  (default ENGLISH); `AiTab` passes selected language from state.
- `MainActivity` UI: `DocumentsTab` hero text updated to reflect iText
  embedded-text extraction; `AiTab` OCR card shows selected language label.
- Recent compression entries now include PDF outputs via `addDocumentToRecent`.

### Fixed
- **Compile errors** from iText migration resolved:
  - `PdfReader` constructor mismatches (no `byte[]` overload on Android) —
    fixed via temp-file pattern.
  - `Image` import conflict (iText vs Compose icons) — fixed with `ITextImage`
    alias.
  - `AreaBreak` API: `AreaBreak(PageSize.A4)` (was `AreaBreakType.NEXT_PAGE`).
  - Removed unused imports (`RandomAccessFile`, `RandomAccessSourceFactory`).
- **R8 release build** warning on missing `StaticLoggerBinder` — fixed with
  `-dontwarn` proguard rule.

### Note
- All builds pass: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`,
  `compileDebugAndroidTestKotlin`, `assembleRelease` (R8), `lintDebug`.
- App installs and launches on API-36 device without crashes.
- Manifest still declares **no INTERNET permission** (privacy invariant held).
- Evidence: `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md`.

## [0.3.0] — 2026-08-31

Real on-device OCR, honest batch failure surfacing, and the hardware-verified
release gate. Adds local text recognition via ML Kit (no INTERNET), surfaces
every per-file batch failure with an on-device audit record, and proves the
native engine green on an API-36 phone.

### Release & Distribution (v0.3.0, production keystore)
- **Production keystore generated** (`~/.android/keystores/shrinkmedia-release.jks`,
  PKCS12, 2048-bit RSA, SHA256withRSA, 10,000-day validity, alias `shrinkmedia`).
  Kept **outside the repo**; `keystore.properties` (repo root, **gitignored**)
  drives the release `SigningConfig` and fails closed if absent.
- **Signed release APK** `app-release.apk`: `versionName 0.3.0`, `versionCode 3`,
  `applicationId com.shrinkmedia.compressor`, R8-minified;
  `apksigner verify` PASS (cert SHA-256 `21569322706156fe...` — matches keystore).
- **Distribution:** direct APK sideload via a GitHub Release (`v0.3.0`). Not the
  Play route (per user decision). Signed APK installs + launches on API-36.
- Evidence: `docs/evidence/2026-08-31_v0.3.0_signed_release.md`.

### Added
- **On-device OCR** (ADR-009): `OcrHelper` (typed-null `recognizeText`, bounded
  decode) + ML Kit `com.google.mlkit:text-recognition:16.0.1` +
  `com.google.mlkit.vision.text.latin.TextRecognizerOptions`. New `AiTab`
  "Scan reader (OCR)" card explicitly distinguishing success / empty /
  no-text / failure (no silent drops). R8 keeps ML Kit (`seeds.txt`/`usage.txt`).
- **Google Tools Bridge planning ADR (ADR-010)** staging Drive/Docs/
  cloud-Gemini OUT of v1 as opt-in connected mode; AICore stays ASPIRATIONAL;
  `docs/ideas.md` I001.
- **Batch no-silent-drops audit** (Article I.6): `BatchFailureAudit` writes a
  timestamped `batch-audit.log` in the app sandbox; the completion notification
  now reports count + reason summary and a warning icon on any failure.
- **Device-verification evidence** (`docs/evidence/2026-08-31_device_verification.md`)
  and Sprint 8/9 records; Sprint 8 promoted to EXECUTED.
- **New instrumented test** `failure_audit_record_is_written_to_on_device_sandbox`.

### Changed
- **Sprint 8 device gate → PASS.** `connectedDebugAndroidTest` is green: 9/9
  tests on Xiaomi `25078RA3EA` (API 36) covering real compression, fail-closed
  null, gallery autosave, battery-pause (never drops), DataStore savings, the
  audit record, and **real on-device OCR** (reads "SHRINKMEDI").
- **Web-sim `GRADLE_CODE` parity** now lists the ML Kit dependency.
- State docs (`current-state.md`, `architecture.md`, `decisions.md`,
  `release-roadmap.md`, `release-readiness.md`) reflect C12 implemented, C11
  staged v2, C16/C17 added.

### Fixed
- **Three genuine instrumented-test defects** surfaced by the first device run
  (correctly fixed, not weakened): the pause-gate test now arms `isPaused=true`,
  two `@Test` methods are void (JUnit `InvalidTestClassError`), and the
  real-compression test input is a lossless PNG (decodeable on-device) instead
  of a synthetic BMP that API 36's `BitmapFactory` can't read (bounds w=-1/h=-1).
- **On-device OCR `decodeBounded` bug (device-found, commit `3b5c134`):**
  bounds-only decode (`inJustDecodeBounds=true`) always returns a null Bitmap, so
  `openInputStream(...)?.use { decodeStream(...) } ?: return null` returned null
  before checking bounds and OCR never ran. Removed the erroneous elvis; the
  downstream bounds check still covers a genuine null stream. OCR now returns
  recognized text on device.

### Note
- OCR uses ML Kit's **bundled** Latin model (`TextRecognizerOptions.DEFAULT_OPTIONS`,
  `com.google.mlkit:text-recognition`) — no model download, still fully on-device,
  no INTERNET permission in the manifest. The on-device walkthrough is captured in
  `docs/evidence/2026-08-31_device_verification.md`; the bundled model reads the
  word core "SHRINKMEDI" on large monochrome synthetic text (drops a trailing
  glyph — a model-fidelity quirk, not a wiring issue).

Housekeeping + release-hardening release: removed the inherited Google AI Studio/
Gemini artifacts, consolidated the sprint documentation into a single,
execution-ordered folder, added honest test coverage, and made the app
releasable (production namespace, R8 minification + signing, launcher icon,
hardened CI).

### Removed
- **Google AI Studio / Gemini fingerprints**: `metadata.json` (AI Studio
  applet descriptor), `assets/.aistudio/`, `.env.example` (documented
  `GEMINI_API_KEY` / `APP_URL`), and unused `@google/genai`, `dotenv`,
  `express`, `@types/express` dependencies (regenerated `package-lock.json`,
  −121 packages). No AI Studio/Gemini references remain in the product code or
  docs.
- **Root `sprints/` folder** (mis-numbered plan files). All sprint docs now
  live in a single `docs/sprints/` (records 1–7 + active plan 8).
- **`com.example.mediacompressor` namespace** replaced across Kotlin sources,
  `App.tsx` code samples, and manifest default icon.

### Added
- **Sprint 6 record** (`docs/sprints/sprint-6-ai-studio-cleanup-sprint-consolidation.md`)
  covering the cleanup/consolidation.
- **Sprint 7 record** (`docs/sprints/sprint-7-test-hardening-and-release-config.md`)
  covering honest tests + release configuration.
- **Sprint 8 plan** (`docs/sprints/sprint-8-device-verification-final-gate.md`)
  — device verification, re-sequenced as the **final** release gate.
- **`docs/release-roadmap.md`** — step-by-step release path with per-step
  status and exit gates.
- **Honest web batch model** (`src/lib/batch.ts`, `src/lib/batch.test.ts`):
  pure `buildBatchResults`/`accumulateSavings` mirroring native
  `recordCompressionSavings` semantics; `src/App.tsx` uses it (no fake savings).
- **Android JVM unit tests** (`CompressionQualityUnitTest.kt`) and **real-path
  instrumented tests** (`CompressionPipelineInstrumentedTest`,
  `BatchPauseContractTest` — fail-closed pause gate never drops an item,
  `BitmapCompressUtil`) with test deps in `app/build.gradle.kts`.
- **Production release config**: `com.shrinkmedia.compressor` namespace +
  `applicationId`, `versionCode 2` / `versionName 0.2.1`, R8
  (`isMinifyEnabled=true`) + `app/proguard-rules.pro` (FFmpegKit +
  `CompressionQuality` keep rules), optional gitignored `keystore.properties`
  signing config, adaptive launcher icon + label via `@string/app_name`.
- **Hardened CI** (`.github/workflows/ci.yml`): upgraded off deprecated
  Node-20 actions (checkout/setup-node/setup-java/cache v5,
  upload-artifact v7), Android job runs JVM unit tests + compiles instrumented
  APK, added a fail-closed signed `bundleRelease` AAB job gated on secrets.

### Changed
- `vite.config.ts` stripped of AI Studio `DISABLE_HMR`/watch logic.
- Web package renamed `react-example` → `shrinkmedia-web`.
- `ROADMAP.md`, `docs/current-state.md`, `docs/sprint-cross-reference.md`,
  `docs/release-readiness.md` updated for Sprint 7/8 re-sequencing (hardware
  verification last).
- Docs updated to reflect a single sprint folder and a pure static web
  simulator with no runtime secrets: `README.md`, `CONTRIBUTING.md`,
  `SECURITY.md`, `.ai/VERSION` (1.1.0), `.ai/context/02, 07, 08, 09, 12`,
  `.ai/agents/builder-agent.md`, `docs/runbooks/web-simulator.md`,
  `docs/adr/ADR-006-web-simulator-harness.md`,
  `docs/sprint-cross-reference.md`, `docs/release-readiness.md`,
  `docs/sprints/sprint-2`, `docs/sprints/sprint-5`,
  `docs/evidence/2026-08-30_governance_scaffold.md`.

### Fixed
- Manifest no-INTERNET guardrail (`ci.yml`, `.ai/context/08`,
  `.ai/agents/auditor-agent.md`) previously grepped the noisy
  `INTERNET|http://|https://` pattern, which the mandatory XML namespace
  (`http://schemas.android.com/apk/res/android`) would trip — a false
  positive. It now matches only the `android.permission.INTERNET` declaration.
  (No runtime code changed.)
- **Release builds now fail closed**: previously the release buildType had no
  signing/config path; now `assembleRelease`/`bundleRelease` produce a signed
  artifact only when a keystore is present, otherwise they refuse (no unsigned
  distribution).

### Note
- Reverse-dependency check: removing `com.example.mediacompressor` is safe
  because no released build has shipped under that namespace; `0.1.0` was an
  informal prototype with no published artifact.

## [0.2.0] — 2026-08-30

Engineering foundation, unified toolkit, and battery-aware batch compression.

### Added
- **Engineering governance scaffold**: Constitution (`docs/engineering/CONSTITUTION.md`),
  ADRs (`docs/adr/`), decisions log, architecture + current-state docs,
  code-standards, sprint records, evidence log + release-readiness table,
  runbooks, active sprint plans, `.ai/` context + agent definitions,
  `.github/` CI workflow + templates.
- Root governance files: `AGENTS.md`, `CONTRIBUTING.md`, `ROADMAP.md`,
  `SECURITY.md`, `LICENSE` (MIT).
- Web tooling: Vitest with `lib/` helper module + unit tests.
- Gradle wrapper entry points for reproducible Android builds.

### Changed
- `MainActivity.kt` consolidated into a three-tab toolkit (Media / Documents /
  Elite AI) driven by a ViewModel `UiState` — compact, single-responsibility
  UI with quality presets, autosave toggle, and pause-on-low-battery toggle.
- `BatchCompressionService.kt` now battery-aware: a shared
  `BatchCompressionPauseController` state, `ACTION_BATTERY_LOW` receiver
  (registered only when the setting is enabled), and per-file reads of the
  live DataStore autosave preference.
- `SettingsDataStore.kt` persists `pauseCompressionOnLowBattery` (additive
  key, boolean default `false`).
- `README.md` rebuilt as a living product-state document.

### Fixed
- Batch compression previously read the autosave flag once at start from a
  stale intent extra; it now respects the **live** DataStore preference per
  file.
- Android build: the committed `io.github.root0as:ffmpeg-kit-lite:6.0-2`
  dependency did not exist on any repository, so `assembleDebug` could never
  resolve. Replaced with the Maven-Central-published `io.github.nikita36078:ffmpeg-kit:6.0.LTS`
  fork (same `com.arthenica.ffmpegkit` API, libx264 enabled on all ABIs).
  See `docs/evidence/2026-08-30_android_config_check.md`.

## [0.1.0] — 2026-08-30

Initial project scaffold.

### Added
- Android app skeleton (Kotlin + Jetpack Compose, Material 3, Coil, FFmpegKit
  Lite, DataStore) with single-image/video compression and quality presets.
- Foreground service for background batch compression (first cut).
- Web simulator (Vite + React + TypeScript + Tailwind) with a live preview of
  the compressor UI.
- `README.md`, `.gitignore`, `.env.example`.

### Changed
- None.

### Fixed
- None.