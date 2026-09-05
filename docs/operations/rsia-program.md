# RSI-A — the Recursive Self-Improvement Authorization Program

> The 2026-09-05 directive: *"We'll put all the necessary requirements for RSI, then I'll be the one
> who authorizes when it happens — when the eco-system maintains itself and how the products we build
> get maintained, improved, etc."*
>
> **Prime rule: RSI is OFF by default and gated by the Owner. The Owner is the sole authorizer.
> No agent, Forge instance, or automated loop may approve its own action.** Any design that would
> let the system authorize itself is a Constitution violation, not a convenience. This document is
> the full requirements + readiness spec; it binds AGENTS.md §14 and the future Forge program.

## 1. What RSI means here (and what it does not)

For this ecosystem, Recursive Self-Improvement = **the ecosystem maintaining and improving the
products it builds, and the processes it builds them with**, where part of the output is a
better-able-to-build system. It has two halves:

- **Maintenance**: keep shipped products green, dependencies cared for, evals passing (the
  "grangle" loop at scale).
- **Improvement**: raise capability/efficiency — new vault categories, better recall, faster build
  loops, product features — each as a small, evidence-gated, reversible step.

It is **NOT** an unconstrained loop that rewrites its own policy or code without a gate. That form
is rejected outright (ADR-013 §9, ecosystem-orientation §2): reliability disappears the moment a
system grades its own exam by editing its own syllabus.

## 2. The authorization model (who decides)

Authority is a single-owner hierarchy, and the owner is the **user** (OP). Every self-* action
resolves to an explicit human authorization:

```
FORGE PROPOSES ──(evidence package)──▶ OWNER AUTHORIZES ──▶ EXECUTION ──▶ POST-AUDIT
      ▲                                                                        │
      └────────────── results + lessons fed back (never self-approving) ───────┘
```

- **Owner (OP) = sole authorizer.** A proposal may be authorized, amended, parked, or denied; only
  OP decides.
- **Forge = proposer + executor only under an active authorization.** It never "credential-checks
  its own upgrades"; a handy ring is broken if Forge could grant itself a next step.
- **The Reviewer gate** (SOP §4) is a *deliberation* gate, not an authorizer: it adds evidence and
  second-opinion but cannot issue an authorization.
- Delegation is possible **but bounded**: OP may authorize a bounded *standing action* (e.g.
  "run daily integrity on the vault, never touching policy"), which is revocable and narrow. There
  is no standing blanket autonomy.

## 3. Requirements (the "all the necessary requirements" — R-01…R-08)

Every RSI action — a maintain/improve step the ecosystem takes on itself or a product it
built — must satisfy **all** of these before it can be put before the Owner:

- **R-01 Provenance.** The change traces, in one place, to a requirement + (when it changes
  scope/invariants) an ADR. No "mystery improvement".
- **R-02 Blast radius.** The change is file/directory-scoped and *declares* its blast radius
  (files touched, repos, data touched). Self-* steps touching DataBank private data or ShrinkMedia's
  invariant manifest are tier-1 and require an ADR.
- **R-03 Reversibility.** Every change ships behind a revert path: git for code + the vault's
  supersede-not-delete for thinking + a timestamped backup for data. A bad change must be
  undoable in < 15 minutes. Irreversible changes (e.g. crypto key rotation) are refused unless a
  documented recovery story exists.
- **R-04 Evidence gate.** Ship-ready means: green tests on the real path + an evidence file
  (Constitution VII). The Owner authorizes on evidence, not narrative. A unit test that cannot fail
  on the real behavior is not evidence (false-confidence rule).
- **R-05 Telemetry + cost report.** The proposal states measurable gains (M-metrics,
  `measurement.md`) and costs: compute, money, blast radius, and the risk to the timeline. The
  Owner sees the delta the change is expected to produce — before deciding.
- **R-06 No invariant trade.** No RSI step may: remove INTERNET-invariance silently; turn a
  fail-closed default open; weaken or delete a test to pass; or break append-only/supersede. If the
  step *needs* one of these, it is a tier-1 decision requiring a full ADR and explicit Owner
  authorization — and it is likely denied.
- **R-07 Timeline continuity.** Even a full system upgrade preserves the vault: the brain is
  replaceable, the memory is not (ADR-015). RSI never migrates or "improves" life-data into a shape
  the old tools cannot still read.
- **R-08 Non-ergodic posture.** Each step must be path-safe (ADR-015 §3): the proposed "improve"
  must be good on a repeated single-player basis, not a lottery that sometimes pays the rent.
  One catastrophic failure outweighs ten gains, so expected-value pitches without time-average
  safety are refused.

## 4. The authorization artifact

When Forge files a proposal, the Owner gets one readable page (the Authorization Card):

```
AUTHORIZATION REQUEST  AR-<n>-<slug>
What changes:          <1-line>
Why:                   <requirement trace R-01>
Blast radius:          <files/repos/data>            (R-02)
Revert path:           <git + vault + backup steps>  (R-03)
Evidence:              <test seam + evidence file>   (R-04)
Telemetry:             <M-metrics delta, costs>      (R-05)
Invariants touched:    <none | → tier-1 ADR>         (R-06)
Path-safety:           <repeated-basis rationale>    (R-08)
---
Approved / Amended / Parked / Denied  (OP signs)
Expiry: <one-time or date-bounded>     Scope: <repos/files>
```

Authorizations are recorded in an **authorization ledger** (the page above, archived in
`docs/operations/rsia-authorizations.md`), so "who said yes, when, to what" is always auditable.

## 5. What the Owner's authorization unlocks (the maintain/improve pipeline)

Once authorized (per the requirements), the ecosystem may:

1. **Maintain products** (patched, dependency care, green-keeping) — each change still evidence-
   gated and individually merged, but authorized as a bounded standing action.
2. **Improve products** (new capabilities, efficiency) — proposal → authorization → build → post-
   audit, one at a time.
3. **Improve the build-loop itself** (better Reviewer, faster evals, new units into the workbench) —
   this is the *recursive* part; it sits behind the same gate and R-02/R-06 get extra scrutiny
   because the thing being changed is the thing that proposes.
4. **Retire or supersede old thinking** (via vault supersede, never delete) — including retiring its
   own older approaches.

## 6. How "when it happens" is reached (readiness for the Owner to authorize)

The Owner authorizes when **the program can prove the loop is safe**, not before. The concrete
readiness gate:

| Readiness item | Status today | Becomes authorize-able when… |
|----------------|--------------|------------------------------|
| Governance + evidence discipline | ✅ live (Constitution/SOP, case studies #1–#3) | already true |
| Reviewer honesty metric (false-merge/block) | 🟡 SOP-bind | the numbers are logged per decision and reviewed |
| Forge proposal → Command Shackle sandbox | 🟡 designed | Forge can run a change in an isolated, reversibility-proven sandbox |
| DataBank-backed memory + telemetry | 🟡 MVP now; server later | the vault runs daily and `check` reports whole (data path safe) |
| Owner approval hardware/UI | 🟡 designed | the Authorization Card is a real surface (see §4) |

The **autonomy dial** is: `0 (no self-*) → 1 (bounded standing maintenance) → 2 (bounded
improvement) → 3 (careful build-loop improvements)`. The dial only moves with the READINESS table
green + an explicit Owner decision per step — never by default.

## 7. Enforcement in code today

- `AGENTS.md §14` binds every agent: self-authorization is forbidden; this document is mandatory
  reading before anything labeled self-maintenance/self-improvement.
- The workflow engine (`ForgeTask`, ADR-014) encodes gates; the Authorization Card degrades to a
  data shape (status + approvals) in the future Forge program — but the *authority* never lives in
  the program. It always lives with the Owner.
- Current posture: **dial = 0.** Nothing in the ecosystem modifies or improves itself today except
  by the ordinary SOP pipeline, which always terminates at a human-visible, evidence-backed commit.
  That pipeline is the seed of the gated form — the gate is already the one human has.