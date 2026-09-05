# Case study #3 — Sprint 21: the Ecosystem Directive turnaround (S-01…S-06 as evidence)

> Constitution Article VII: evidence, not narrative. This file is the proof record for the
> 2026-09-05 ecosystem-directive sprint (CHANGELOG `[Unreleased]`, `docs/sprints/sprint-21-*`).
> It also acts as the **skills-workbench walk-the-talk**: this exact sprint exercises adopted units
> S-01 (to-spec shape), S-02 (two-axis review concept), S-03 (seams-first TDD), S-04 (bug loop),
> S-05 (decision map in the roadmap), S-06 (primary-source research) — rows logged in
> `docs/operations/skills-workbench.md` §3 Reuse Ledger.

## LayerLog (measurement.md format)

```
[Layer L1–L8 — directive turnaround | skills+measurement+RSI+roadmap+ADR-016+HostTier code+DataBank]
start:            2026-09-05
gates-green:      2026-09-05
M1 wall-clock:    measured below per gate; aggregate spans a single committed day (~17 commits/day calibration)
M2 tests delta:   +12 (JVM 85 → 97; instrumented unchanged) — ShrinkMedia
                  +19 (DataBank dvault suite, standalone repo)
M3 defects caught: 0 production defects shipped (maintenance mode; the HostClassifier TDD red-phase
                  caught spec-vs-code drift while writing, not a shipped fault)
M4 gate:          lintDebug 0 errors · merged-DEBUG manifest no INTERNET · merged-RELEASE no INTERNET ·
                  processReleaseMainManifest green · CI results after push (see below)
M5 commit velocity: ShrinkMedia + DataBank commits this sprint SSH-signed (%G? = G)
M6 estimate:      naive-sequential estimate for the same scope ≈ 3–4 focused days
M6 achieved:      one committed day (focused build + docs + DataBank)
M6 ratio:         ≈ x3–4×  (80/20 cuts + reuse units)
M7 reuse:         S-01, S-02, S-03, S-04, S-05, S-06 used this sprint (see workbench ledger)
M8 evidence:      this file + sprint-21 + release-readiness Sprint-21 gate + current-state C25
M10 reviewer:     review gate passed (deliberation, not authorization — RSI dial stays 0)
```

## Evidence — ShrinkMedia (commands and observed results)

Gate run (JAVA_HOME=jdk17, full JVM + lint + debug assemble + release manifest):

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:processReleaseMainManifest
# observed (log /tmp/s21-gate.log):
# :app:testDebugUnitTest  — 97 tests, 0 failures
# :app:lintDebug         — 0 errors
# :app:assembleDebug     — BUILD SUCCESSFUL
# :app:processReleaseMainManifest — BUILD SUCCESSFUL
```

HostClassifier single-lane proof (before the code was committed, so the code is evidence here):

```bash
./gradlew :app:testDebugUnitTest --tests 'com.shrinkmedia.compressor.ecosystem.HostClassifierTest'
# BUILD SUCCESSFUL in 32s — 12 tests (fail-closed refusal, tier resolution, profile selection)
```

No-INTERNET merge check (guarded by CI on every push; used by the Sprint-21 gate):

```bash
rg -n "android.permission.INTERNET" app/build/intermediates/merged_manifest/*/AndroidManifest.xml
# expected: no matches (debug and release) — observed no matches; CI re-checks on push.
```

## Evidence — DataBank (`dmuhoro/DataBank`, pushed this sprint)

```bash
cd /tmp/opencode/DataBank-work
python3 -m unittest discover -s tests -v      # 19 tests — OK
python3 -m dvault capture "first thought to remember" --thread thoughts   # typed capture works
python3 -m dvault query "first"                                           # ranked hit
python3 -m dvault count && python3 -m dvault check                        # records==journal, integrity ok
git log --format='%G? %h %s' --oneline      # 12d153a, fa6abd2  — 'G' (valid SSH signatures)
```

## Evidence — skills/measurement/RSI (primary sources reviewed, S-06)

- `mattpocock/skills` at `3cca18b` (MIT) — catalog of ~60 units reviewed; adoption table + licenses
  in `docs/operations/skills-workbench.md`.
- Directive answers are in the docs (orientation §3–§4, roadmap rewrite, stop-point decision) and the
  closing message of sprint-21.

## Honest boundaries (agreed, not hidden)

- Bump **no app version** — ShrinkMedia is in maintenance mode; this sprint is ecosystem-track infra.
- DataBank is a **local-first MVP** (no transport/auth/reachability yet) — the RSI readiness table
  (§6 in rsia-program.md) and roadmap checkpoint A are explicit about it.
- Host tiers are **decision logic** (JVM unit-verified), not yet a deployed system; virtualization
  tradeoff sheet is in `DataBank/docs/DEPLOY.md`.
- Nothing in this sprint authorizes self-* — RSI dial remains **0**; ledger has zero entries.