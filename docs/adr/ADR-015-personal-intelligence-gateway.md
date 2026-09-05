# ADR-015: Personal Intelligence — Processing Around the Vault, Not Inside It

**Status:** Accepted (architecture + Layer-1 foundation)
**Date:** 2026-09-05
**Deciders:** Daniel Muhoro

## Context

The owner asked the core architecture question in his own notes (2026-05-29) and in the
eco-system directive (2026-09-05):

> "How would I design & architect the processing system and connect it to the storage facility of
> my data? … clarify to me if the storage system and the processing system will be **separate** or
> the **processing system inside the storage system** — help me make the best move here that will
> serve me in the future."

His vision (from the same notes): record any form of information (voice→text, typed notes, camera
photo/video), auto-saved the moment it is captured, searchable, organized by categories —
*(a) the date the idea/thought was noted, (b) when it showed up again, (c) what thought/idea came
after* — and accessible across **past, present and future timelines** ("they're all one, not
separate"). He wants a **processing system that is better than a storage system**, yet still wants
the storage well-organised and searchable; a compounding system that works in his favor; and the
right to **delete old ways of thinking and install new ones**.

He also asked for an agent specifically for **Personal Intelligence**: a "virtual me" guardian that
answers questions, decides whether a question should be learned further (EasyTutor) or saved as
new (DataBank), and eventually oversees the eco-system when he is not looking.

## Decision

### 1. Storage and processing are SEPARATE systems joined by a retrieval contract. The vault is the brain's memory, not its body.

- **The vault (DataBank repo) is a dumb, durable, append-only substrate**: it stores raw,
  versioned records + an index, and exposes a typed contract. It never "thinks".
- **The processing system (Forge + the virtual-me agent) sits NEXT TO the vault, not inside it**,
  and reads/writes it only through the contract (`vault.put` / `vault.get` / `vault.index` /
  `vault.query` — the MCP `vault.*` contract from ADR-013).
- **Why NOT inside**: if processing lived inside storage, (1) the storage format would be coupled
  to the processing version — you could not "install new ways of thinking" without risking the
  data itself; (2) reasoning tools/chains upgrade far faster than data must evolve; (3) a dumb,
  survivable vault outlives any model. **The vault is the memory; the agent is a reader/writer of
  it.** This is the decision that serves him in the future: he can replace the brain freely and
  the life's data is untouched.

This is not a new architecture idea — it is the standard "storage vs compute" split done right:
**indexed, versioned memory + a separable reasoning head.**

### 2. DataBank = the vault, with the categories he named.

Each record (`NoteRecord`) carries, additively and perpetually:
- `id` (stable, immutable)
- `source` (PHOTO / VOICE / TYPED / CAMERA — "record any form of information")
- `text` (normalised transcription — voice→text, OCR for photos, direct for typed)
- `notedAt` — the specific date the idea/thought was noted
- `reappearedAt` (nullable) — when the same idea showed up again (detected by the classifier)
- `followedBy` (nullable id) — what thought/idea came after it (thread predecessor link)
- `thread` (null) — grouping key for the cluster of related re-appearances

The classifier (`NoteClassifier`, Layer-1 foundation) is deterministic, offline, and honest: it
links a note to its chronological successor and detects re-appearance by **textual core-match**
(recurring idea markers) — NOT by semantic meaning (that is ASPIRATIONAL / belongs to a later
retrieval layer). It never deletes; it only adds links.

### 3. "Delete old ways of thinking and install new ones" = version, don't destroy.

The vault is **append-only with supersede**. A "new way of thinking" is a new record that
**supersedes** an old one (`supersedesId`), keeping the old record archived + searchable. Nothing
is hard-deleted by the processing system. This preserves the past/present/future timelines as one
line of thinking, while making the *active* view reflect the newest model of the owner's thinking.

### 4. The Personal Intelligence agent is a typed, decision-only orchestrator (fail-closed).

`PersonalIntelligenceAgent` (Layer-1 foundation, mirrors ADR-014 decision-only style):

- **Input**: a question or a captured note (any source).
- **Output** (sealed, never null, every refusal carries a reason):
  - `Recall` — the corpus already contains an answer (best `EcosystemIndex` hit is shown, with
    evidence snippet, never invented).
  - `Clarify` — the intent is ambiguous; it asks the owner the minimal clarifying question
    (the "Image Insight" flow, below).
  - `SaveToVault` — first-time/new thought with no ambiguity: it is classified +
    categorised (`notedAt` / `reappearedAt` / `followedBy`) and stored.
  - `Learn` — the owner wants to go deeper / learn the material: the intent is routed to
    **EasyTutor** (a separate product; education stays in its domain, per the owner's own
    anti-scope-creep instinct — the eco-system never absorbs EasyTutor's subject matter).
  - `Refused` — preconditions failed (no explicit invocation etc.), with the reason.

The agent is **decision logic only** in Layer-1: no network, no inference, no INTERNET (CI-guarded).
The real ModelRouter transport (open-weight model on the home-lab / local fallback) sits behind the
existing seam (ADR-011/014) and needs the owner's hardware — this stays ASPIRATIONAL until it runs.

### 5. Image Insight ("vividly describe images… then execute or ask").

The owner wants ShrinkMedia to scan/read an image, get context if the intent is unclear, and
proceed if it is self-explanatory. Honest Layer-1:

- **OCR is real** (ADR-009, ML Kit, on-device): photo → typed transcript, zero uploads.
- **Vivid "image description" is a vision-model capability = ASPIRATIONAL** (C11 wall; the Redmi
  has no AICore). We do NOT claim it. The Layer-1 seam is `InstructionAider` (decision engine):
  given the transcript, it returns typed `SelfExplanatory(instruction)` /
  `NeedsClarification(question)` / `Refused(reason)`. If self-explanatory → the agent proceeds
  (save + classify + route). If ambiguous → the owner answers one targeted question, then it
  proceeds. This ships the *flow* the owner wants on hardware he owns, while the vision
  description is wired as the later ModelRouter seam.

### 6. EasyTutor connection (the owner's scope-creep instinct is correct).

- EasyTutor = education/learning product, its own repo, its own domain. The eco-system and
  DataBank do **not** absorb subject-matter/knowledge-fields (that is EasyTutor's).
- The two connect through the same contract: the agent's `Learn` intent hands EasyTutor a
  **topic** (and the vault records where the curiosity came from + when). EasyTutor's learner
  profile is vault-hosted (what he has learned, when, how it reappeared). Nothing else crosses.
- Built in its own repo later (sequencing L3-L4); today only the routing seam + the honest
  statement that the actual transport is future work.

## Honest boundaries (no false confidence)

- Layer-1 foundation builds + verifies the **decision logic** for vault categorising, image
  insight, and personal-intelligence routing (this sprint). It does **not** ship real storage
  (DataBank server), real inference (virtual-me reasoning), or real education (EasyTutor).
- The virtual me as an **independent, self-directed overseer** is research-grade and stays
  **ASPIRATIONAL** until implemented and verified (Constitution §12).
- The vault contract names (`vault.put/get/index/query`) match ADR-013's MCP surface exactly;
  the connected transport behind them requires the owner's hardware/credentials (ADR-013 L2+).

## Consequences

**Positive** — the architecture he asked "help me make the best move" is now decided and recorded;
every future product (Forge, DataBank, EasyTutor, virtual me) connects to the same contract, so
nothing needs to be re-architected later; the vault can outlive any brain.

**Negative / honest** — a real working vault + brain still requires the DataBank server + owner
hardware (home lab) + a real model runtime; none of that exists yet and is a dedicated program
(see `docs/operations/ecosystem-roadmap.md`).

## Alternatives considered

- **Processing inside storage**: rejected (couples data format to brain version; can't evolve
  thinking safely; contradicts "delete old ways and install new ones" without data loss).
- **Storage inside the APK / on-device only**: rejected for the vault (the owner wants it
  reachable across timelines and products on owned hardware); on-device remains the *edge* layer.
- **Silent auto-execution on every image**: rejected — "if self-explanatory, proceed" still goes
  through the typed `SelfExplanatory` gate; ambiguous images must surface a question (no silent
  drops, Constitution §1).

## Linking

ADR-009 (OCR), ADR-011 (on-device AI), ADR-012 (Connected mode), ADR-013 (DataBank portal + MCP
`vault.*`), ADR-014 (Forge core + ModelRouter seam), `docs/current-state.md` C17/C21/C22/C23.