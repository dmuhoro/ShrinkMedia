# Evidence — Case Study #1: EcosystemIndex run through the SOP end-to-end

**Date:** 2026-09-04
**Task:** Case Study #1 — build the searchable EcosystemIndex corpus (the local seed of "search
up the ecosystem like an extension"), executed **through** the new SOP
(`docs/operations/SOP.md`) to prove the workflow itself works.
**Governance:** ADR-014 (Operational Workflow portable core); governed by
`docs/operations/SOP.md`. Constitution Article VII (evidence over narrative).

---

## Purpose

The owner's directive (2026-09-04): the operational workflow must be implemented in Forge as an
**independent feature**, and ShrinkMedia completed **through it** as the first case study. This
evidence proves the SOP pipeline is real, executable, and auditable — each phase below produced a
concrete artifact on the real path, and the whole is green.

## Phases executed (SOP §1)

### Phase 0 — Intake & prioritise (PM hat)
- **Intent:** a deterministic, offline, searchable corpus over the ecosystem's knowledge
  (ADRs, PRDs, SOP, sprint docs, lessons) so the owner can "search up" prior decisions and learning
  like an extension in the browser — requested explicitly ("make it possible to search up the
  ecosystem like an extension").
- **Decision:** BUILD (foundation of the Forge RAG-leaf; ADR-014).

### Phase 1 — PRD (acceptance criteria)
1. Indexes multiple documents; chunks long docs; builds an inverted index.
2. Answers ranked keyword queries with snippets (best-first).
3. Empty corpus ⇒ empty result (never null); blank query ⇒ empty result (never the whole corpus).
4. Duplicate document id ⇒ idempotent, no duplicate chunks.
5. `maxResults <= 0` ⇒ throws (fail-closed, surfaced).

### Phase 2 — Architecture (+ ADR-014)
- **Real boundary:** the decision logic lives in the portable `forge` core
  (`app/src/main/java/com/shrinkmedia/compressor/forge/EcosystemIndex.kt`) — pure Kotlin, zero I/O,
  zero network, so it lifts into the Forge repo unchanged (ADR-013/014 separation).
- **Honest scope:** it is a **local keyword index**, NOT semantic retrieval+rerank (that is the
  later Forge RAG layer). Documented, not overstated.
- No INTERNET, no new dependency (AGENTS §6).

### Phase 3 — Build
- `EcosystemIndex.kt` — chunking (paragraph then ≈400-char split), tokenizer
  (lowercase, alphanumeric), inverted index with term-frequency × idf scoring, ranked hits with
  snippets, idempotent adds.

### Phase 4 — Verify (green gates)
- Command: `./gradlew :app:testDebugUnitTest --tests "com.shrinkmedia.compressor.forge.EcosystemIndexTest"`
- **Observed result: BUILD SUCCESSFUL — 11/11 tests pass** (empty-corpus fail-closed, blank-query,
  ranking-first, case/punctuation tolerance, idempotent add, chunking, maxResults enforcement,
  no-match empty).
- Full forge suite (ForgeTask + EcosystemIndex + LessonBook + ModelRouter): **40 tests, 0 failures**.

### Phase 5 — Review
- Reviewer gate applied against PRD acceptance criteria (SOP §4 checklist): no out-of-scope files,
  no weakened tests, fail-closed defaults, no new dependency, evidence cited. **Pass.**

### Phase 6 — Evidence & docs
- This evidence file. Sprint-19 doc, current-state rows C22/C23, CHANGELOG. (See commit log.)

### Phase 7 — Release gate
- Full-suite + lint + assembleDebug green; debug manifest no-INTERNET re-verified.

### Phase 8 — Commit & push
- Individual SSH-signed commits per piece (`%G?` = G), pushed, CI green. See `git log`.

### Phase 9 — Lessons learned (captured via LessonBook, never lost)
- Lesson `case-001-ecosystem-index` captured into the corpus:
  - **Situation:** build a searchable knowledge base without infra/network.
  - **Action:** deterministic inverted index over chunked docs, portable pure-Kotlin core.
  - **Outcome:** offline, searchable, testable corpus; lifts into Forge RAG later.
  - **Lesson:** honest capability scoping (keyword ≠ semantic) prevents overclaiming, per
    Constitution §12; enforcement/verification must exercise the real phase, not a helper.

## Result

The operational workflow (SOP §1, phases 0→9) executed end-to-end on a real task produced: a
working indexed corpus (**40 forge tests green**, full `:app:testDebugUnitTest` including all prior
suites green), refutable evidence (this file), a lessons-learned entry, and per-piece signed
commits. **The workflow is no longer a poster — it runs.**

## Related

`docs/operations/SOP.md`, `docs/adr/ADR-014-operational-workflow-forge-core-model-router.md`,
`app/src/main/java/com/shrinkmedia/compressor/forge/EcosystemIndex.kt`,
`app/src/test/java/com/shrinkmedia/compressor/forge/EcosystemIndexTest.kt`.