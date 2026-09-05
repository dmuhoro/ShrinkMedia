# Ecosystem Orientation — the honest answers to the 2026-09-05 Directive

> Companion to **ADR-015** (`docs/adr/ADR-015-personal-intelligence-gateway.md`). This is the
> "clarify what will happen once we add this new context" deliverable, in writing so the reasoning
> is durable and auditable.

## 1. What does the eco-system become with this context?

It stops being a media tool with a Connected-mode flag and becomes a **founder's operating
system** with one phone portal (ShrinkMedia), one vault (DataBank), one builder (Forge), one
teacher (EasyTutor), and one overseer (virtual me) — all speaking the same `vault.*` contract
(ADR-013). Today, what is **real** in code is the *decision layer* (this sprint); what is real on
his own hardware is still to be built (see `ecosystem-roadmap.md`).

## 2. Honest perspective on Recursive Self-Improvement (RSI)

- RSI is **real but mundane**: the value is a *feedback loop where the system's own outputs raise
  the system's quality* — evals → fix → re-run. Every serious AI team does this; it is not magic.
- The dangerous form is **unconstrained self-modification** (a loop that rewrites its own policy
  without a gate). That is where reliability disappears. We reject it outright.
- The safe form, which this repo already implements, is **gated self-improvement**: the SOP loop
  (each task → verify → reviewer-gate → evidence → lessons), phone/Forge proposing, a gate
  approving, CI + tests as the mechanical referee, and **you** as the ultimate owner. That is why
  "install new ways of thinking" is implemented as *supersede, never destroy* (ADR-015 §3).
- So: RSI is adopted **as a process**, not promised **as a product**. The virtual-me overseer that
  "corrects any agent that diverts from the end-goal" is exactly that gate, implemented later, and
  stays ASPIRATIONAL until it runs (Constitution §12).

## 3. Ergodic properties → the compounding value

- An **ergodic** process is one where *time-averages equal expectation*: the outcome you get by
  repeating a bet over time equals the average over many people at one instant. Personal life is
  **non-ergodic**: you get exactly one timeline, so path-dependent ruin (lose everything once =
  lose your retirement) is fatal even if the "expected value" looks good.
- The design consequence for a single-owner eco-system: **favor strategies that are good on a
  repeated, single-player basis** — small compoundable wins — and **never take existentially
  destructive risks**. Concretely in the vault:
  - append-only, supersede-not-delete (a "delete old thinking" is a version, not data loss);
  - reversible operations (undo, explicit refuse, no silent drops);
  - fail-closed defaults (OFF by default, consent to go ON);
  - every task writes a lesson → the *next* task inherits the win (compounding).
- That is how "a compounding system that works in my favor all or most of the time" is engineered:
  not by luck, but by making each repeated decision path-safe and recorded.

## 4. Processing vs storage — the decision (your 2026-05-29 question)

**Separate systems, joined by a contract; the vault is memory, not a body.** Storage (DataBank,
append-only, durable, indexable) owns the data; the processing brain (Forge + virtual me) reads
and writes it via `vault.get/put/index/query` (ADR-013 MCP). The brain can be replaced freely
without touching life-data; a brain inside storage would couple data format to brain version and
make "install new ways of thinking" dangerous. Full rationale + alternatives: **ADR-015 §1**.

## 5. Records, categories, timelines

`NoteRecord` (vault) = { id, source (PHOTO/VOICE/TYPED/CAMERA), text, notedAt, reappearedAt,
followedBy, thread, supersedes } (ADR-015 §2). Your categories map 1:1: *date noted* → `notedAt`;
*when it showed up again* → `reappearedAt` (classifier detects by recurring idea markers); *what
came after* → `followedBy` (chronological successor link). Past/present/future are one timeline
because nothing is deleted and new thoughts *supersede* old ones — the whole line stays searchable.

## 6. Virtual me: what it does when you ask it something

`PersonalIntelligenceAgent` answers: (1) already-known → **Recall** (evidence snippet from your
own corpus, never invented); (2) learning intent → **Learn** (topic routed to EasyTutor, education
stays in its domain — your scope-creep instinct is right); (3) first-time / (re-)captured thought →
**SaveToVault** (classified + linked); (4) ambiguous → **Clarify** (exactly one question);
(5) precondition failed → **Refused** with reason. Today this is on-device decision logic; the
"reasoning in his own words" needs the home-lab model via ModelRouter (ADR-014) = ASPIRATIONAL.

## 7. EasyTutor integration (your question)

EasyTutor is **its own product on top of DataBank**, connected by the same contract: the vault owns
the learner profile (what he's learned, when, how it re-appeared); the agent's `Learn` intent hands
EasyTutor a topic; EasyTutor owns all subject matter. The eco-system never absorbs education
(scope-creep guard you already flagged). Experience-wise: you ask anything → the eco-system either
answers you from your own vault or **coaches you through EasyTutor** — one loop, no context loss.

## 8. The human questions (impulse vs response, mental state, Maslow)

Honest framing so the build stays grounded:
- **Impulse vs response**: impulse is stimulus-driven (reactive, subcortical, fast); response is
  stimulus-paused-then-chosen (a beat between trigger and action where the prefrontal system
  selects). This is the *pause* the SOP's Reviewer gate mechanises at 10,000-ft scale — "refuse,
  then route".
- **Mental state**: what you carry into an interaction is the current state of your nervous system
  (arousal × interpretation) — regulated by physical state, prior thoughts, and attention. The
  eco-system's job is to *record* more of your thoughts (vault) and *teach* you the pause
  (EasyTutor) — it cannot claim to regulate your biology.
- **Personal development / Maslow**: EasyTutor's domain (physiological → safety → belonging →
  esteem → self-actualisation, as *products/curricula*). The vault feeds it the "who I am / who I
  can be" raw material. We will not pretend ShrinkMedia or DataBank is a psychology product.

## 9. How the pieces work together (the "experience that changes everything")

ShrinkMedia (stargate, phone) → you snap a note → OCR → clarify-or-proceed → virtual me routes:
save / recall / learn. The vault (DataBank) absorbs everything searchable across timelines; Forge
uses that + the SOP to build the rest. The experience is: **every thought is captured once, findable
forever, and acted on — never silently dropped.** What is ASPIRATIONAL (vision description,
self-directed overseer, real self-hosted transport) stays listed as such until it runs.