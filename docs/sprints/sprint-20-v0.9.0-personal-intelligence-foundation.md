# Sprint 20 — Personal Intelligence Foundation + Ecosystem Go-Live (case study #2)

**Status:** EXECUTED (2026-09-05) — releases as **v0.9.0** (capability block: Personal Intelligence
Foundation + ecosystem go-live).
**Scope:** answer the 2026-09-05 ecosystem directive in one governed layer, and **test-drive the
SOP in real time**. Delivers (a) the **Personal Intelligence foundation** (ADR-015): vault
categories (`notedAt`/`reappearedAt`/`followedBy`), the **note-insight flow** ("scan a photo →
read it → clarify-or-proceed → route"), and the **PersonalIntelligenceAgent** (Recall / Learn →
EasyTutor / SaveToVault / Clarify / Refused); (b) the **ecosystem orientation + roadmap +
stop-point** decisions; (c) **Forge + DataBank repos created** and scaffolded; (d) **case study
#2** as the SOP's real-time proof. **No INTERNET is added; the default build stays private.**
Vessel (ShrinkMedia) reaches its **safest stop = maintenance mode**; new feature work moves to
Forge / DataBank / EasyTutor.

## Focus

1. **Clarify-first.** `docs/operations/ecosystem-orientation.md` answers every question from the
   directive durably: RSI (safe = gated self-improvement, never unconstrained), ergodic value
   (time-average ≠ expectation ⇒ path-safe, compounding, append-only design), processing-vs-storage
   (separate, joined by `vault.*`; the vault is memory, not the brain), EasyTutor (own product on
   the contract, education stays in its domain), virtual-me routing, and the human/habit questions.
2. **ADR-015** freezes the architecture: vault = dumb durable append-only substrate; brain = reader
   of it; supersede-not-delete = "install new ways of thinking"; honest boundary on vision/seams.
3. **Vault category engine** — `NoteRecord` (source PHOTO/VOICE/TYPED/CAMERA, `notedAt`,
   `reappearedAt`, `followedBy`, `thread`, `supersedes`) + `NoteClassifier` (deterministic
   successor link + re-appearance detection by idea-marker overlap). 9 tests.
4. **Image Insight (note-insight flow)** — `InstructionAider`: given OCR output, decide
   `SelfExplanatory` (proceed) / `NeedsClarification` (one targeted question, then proceed) /
   `Refused` (OCR failed — never guess). 8 tests.
5. **PersonalIntelligenceAgent** — routes questions/thoughts: explicit `Learn` → EasyTutor (topic
   handed over), question with a known answer → `Recall` (evidence snippet, never invented),
   re-capture → stored as a re-appearance (never dropped), first-time → `SaveToVault`, ambiguous →
   `Clarify`, broken preconditions → `Refused`. Reconciles threads for re-appearances. 8 tests.
6. **UI wiring** — new "Personal Intelligence — note insight" card in the Elite AI tab: pick a
   photo → on-device OCR → clarify-or-proceed → save to the on-device vault (count + route shown).
   Sync to DataBank is explicitly a future Connected-mode step.
7. **Ecosystem go-live decisions** — `ecosystem-roadmap.md` (per-project size/complexity/time via
   honest ranges; connect sequence; "alive" milestone), `shrinkmedia-stop-point.md` (incomplete
   task inventory; **stop = maintenance mode**; outstanding items are owner hardware/credentials,
   not missing code), and **Forge + DataBank repos created + scaffolded** (private).
8. **Case study #2 — the SOP runs itself live.** Evidence file records all 9 phases with wall-clock,
   effectiveness (25 tests catching 2 real defects) and the telemetry-model update (calibration-0:
   6 committed days / 19 sprints / 85 JVM tests / 15 ADRs).

## What shipped

| Change | Files | Proof |
|--------|-------|-------|
| ADR-015 (Personal Intelligence architecture) | `docs/adr/ADR-015-*.md` | cited below |
| Vault record + classifier | `personal/NoteRecord.kt`, `personal/NoteClassifier.kt` | 9 JVM tests green |
| Image Insight decision engine | `personal/InstructionAider.kt` | 8 JVM tests green |
| Personal Intelligence orchestrator | `personal/PersonalIntelligenceAgent.kt` | 8 JVM tests green |
| Note-insight UI flow | `MainActivity.kt` (AiTab card + VM methods) | `assembleDebug` green |
| Orientation / roadmap / stop-point | `docs/operations/ecosystem-*.md`, `docs/operations/shrinkmedia-stop-point.md` | cited below |
| Case study #2 evidence | `docs/evidence/2026-09-05_personal_intelligence_foundation_sop_test_drive.md` | full suite green |
| Forge + DataBank repo scaffolds | GitHub `dmuhoro/Forge`, `dmuhoro/DataBank` (private) | gh verified, pushed |

## Honest status (what is NOT claimed)

- **"Vividly describe images" is ASPIRATIONAL.** The note-insight flow reads the *words* (real OCR,
  ADR-009); describing *the image itself* needs a vision model = C11 wall / ModelRouter seam (owner
  hardware). The flow ships the clarify-or-proceed behaviour the owner asked for on hardware he owns.
- **The personal vault is on-device and in-memory this sprint.** Real storage + sync = DataBank
  server (roadmap item 1, owner hardware) — not built here.
- **The Virtual Me that "oversees everything when he is not looking" is ASPIRATIONAL.** The decision
  routing is real; self-directed reasoning needs the home-lab model runtime.
- **The stargate is a door, not a live portal.** Repos exist; the connected `vault.*` transport is
  L2+ behind owner hardware/credentials.

## Verification

- `./gradlew :app:testDebugUnitTest` → **85 tests, 0 failures** (60 prior + 25 new personal).
- `./gradlew :app:lintDebug` → **0 errors** (29 informational warnings, pre-existing).
- `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL**.
- Merged **debug** manifest: no `android.permission.INTERNET` (invariant held, CI-guarded).

## Evidence

- `docs/evidence/2026-09-05_personal_intelligence_foundation_sop_test_drive.md` (case study #2)
- `docs/adr/ADR-015-personal-intelligence-gateway.md`
- `docs/operations/ecosystem-orientation.md`, `docs/operations/ecosystem-roadmap.md`,
  `docs/operations/shrinkmedia-stop-point.md`

## Outstanding (explicit, not hidden)

- **DataBank server** (self-hosted, `vault.*`, auth) — roadmap item 1; owner hardware + reachability.
- **Stargate connected action** — MCP client behind consent once DataBank exists.
- **Forge orchestrator program** — lift the proven core; Planner→Reviewer + evals + sandbox.
- **EasyTutor** — own product on DataBank's learner profile; receives `Learn` intents.
- Vision description, self-directed overseer, actual inference runtime — ASPIRATIONAL, gated.

## Lessons learned (this sprint, captured via LessonBook-ready format)

- `case-002-personal-intelligence-layering` — a safe "personal intelligence" v1 is mostly decision
  logic: OCR the input, classify, route, refuse loudly; real inference/vision/transport is a later
  seam. Building the decision layer first gives a real, honest, on-device feature — and the seam
  the later brain plugs into.