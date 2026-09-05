# Measurement — the "make everything measurable" system (2026-09-05)

> The 2026-09-05 directive: *"Let's add the upgrade to how we build and make everything measurable,
> then we'll proceed with execution and building… so that we can do high quality leverage work in
> less time."* This doc defines the metrics dictionary, the per-layer log, and the compression
> ledger. It is a companion to `SOP.md §6` (telemetry) and `skills-workbench.md` (reuse counting).

## 1. Principles

1. **Counted, not narrated.** A claim like "we compressed the plan" or "reuse compounds" is only
   true when a number in this system says so and an evidence file cites the run.
2. **Measured on the one path** (non-ergodic, ADR-015): we track the *actual* timeline (start →
   green), never a hypothetical team-time. Optimizing the path we live, not an ensemble average.
3. **Published per sprint.** Every sprint doc carries a LayerLog; the telemetry plate updates as
   numbers change, with citations (Constitution VII).
4. **A number's meaning is stable.** Metrics move only when work moves them — never by editing the
   definition to look better (that is the tautology rule from S-03 in file-shape: a metric that
   recomputes its own truth proves nothing).

## 2. Metrics dictionary (stable definitions)

| ID | Metric | Definition | Measured how |
|----|--------|-----------|--------------|
| M1 | **Layer wall-clock** | Start timestamp → gates-green timestamps for one layer | `date` at start/end; recorded in the LayerLog row |
| M2 | **Test delta** | New tests added per layer (assertions that live in green runs) | `grep -c`/suite count before/after |
| M3 | **Defect tightness** | Production defects caught BY the layer's own tests (count) | named test that drove a production fix |
| M4 | **Gate status** | lint errors, merged-manifest no-INTERNET probe, CI jobs | `lintDebug`/`rg`/`gh run view` |
| M5 | **Commit velocity** | SSH-signed commits pushed per committed day | git log |
| M6 | **Compression ratio** | naive-sequential-estimate wall-clock ÷ achieved wall-clock for the same declared scope | LayerLog: estimate row vs actual row |
| M7 | **Reuse count** | S-xx ledger rows in `skills-workbench.md` per layer | ledger rows |
| M8 | **Evidence artifacts** | `docs/evidence/*` files + current-state rows touched per layer | list + diff |
| M9 | **Calibration level** | SOP scale-model step (0 ≈ 50% … 90–100%) with the evidence that moved it | telemetry plate |
| M10 | **Reviewer honesty** | false-merge and false-block counts from the Reviewer gate | ForgeTask decisions logged |

## 3. LayerLog (the per-layer measurement artifact)

Every SOP layer produces one block (in the sprint doc or the case-study evidence file):

```
[Layer <N> — <name>]
start:            <UTC>
gates-green:      <UTC>   (tests + lint + manifest probe)
M1 wall-clock:    <minutes>
M2 tests delta:   +<n>  (suite <before> → <after>, 0 failures)
M3 defects caught: <n>   (named tests + what production bug they caught)
M4 gate:          lint <errors>/<warnings> · merged-debug no-INTERNET <ok/fail> · CI <jobs passed after push>
M6 estimate:      <naive sequential estimate for the same scope>
M6 achieved:      <actual above>
M6 ratio:         x.y×  (≥1.0 = the 80/20/reuse spending plan held)
M7 reuse:         S-xx used: <ids>
M8 evidence:      <files>
M10 reviewer:     review pass/failed-here count
```

## 4. Compression ledger (cumulative proof of "do it in a shorter timeframe")

The directive is to *incrementally reduce* time-to-ecosystem-alive. That is tracked cumulatively:

| Sprint | Scope | Naive sequential estimate | Achieved (M1) | Compression | 80/20 cuts applied |
|--------|-------|---------------------------|---------------|-------------|--------------------|
| 20 (v0.9.0) | Personal Intelligence foundation | ~4–5 h | ~52 min | ~5× | MVP scopes, no transport, decision-layer-first |
| 21 (this sprint) | Ecosystem directive: skills + measurement + RSI + roadmap + hardware + DataBank MVP | (filled in case study #3) | (filled) | (filled) | stdlib-only vault; no server/transport; no VM; SQLite not PG |

Cumulative time saved is the sum over rows of (estimate − achieved). When the future Forge runs,
this ledger becomes its own automation (SOP Phase 9 writes it directly).

## 5. Cadence

- Every sprint: LayerLog row + reuse ledger rows + compression leading number to the Owner in the
  sprint's close-out.)
- Monthly: telemetry plate in `SOP.md §6` is re-anchored only with evidence (never narrative).
- A number that stops being meaningful is retired in this file, with a one-line reason.