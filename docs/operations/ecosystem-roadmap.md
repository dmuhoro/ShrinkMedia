# Ecosystem Roadmap — size, complexity, time, and the 80/20 connect sequence

> Answers the 2026-09-05 directive: *"I want to know how big and complex each of the projects are…
> then let's incrementally reduce the total time to make the eco-system alive via the stargate, by
> compressing our work with the 80/20 principle."* Estimates are honest ranges from analogous builds
> by this team (calibration-0: 104 commits/6 days ≈ ~17 commits/day when focused), not promises.
> Compression is measured, not claimed — every sprint logs estimate-vs-achieved in
> `docs/operations/measurement.md`.

## The connectable layer (unchanged)

Every product connects through the **MCP `vault.*` contract** (ADR-013): `vault.put` / `vault.get`
/ `vault.index` / `vault.query`. ShrinkMedia's Connected-mode stargate (ADR-012, OFF by default,
consent-gated) is the first client hop. **A live connection requires the DataBank server running on
owner hardware + reachability — the daily-run vault is real; the server/transport layer is the next
milestone.**

## The construction/decision map (S-05 shape: one decision ticket at a time)

```
ECOSYSTEM ACTIVATION ──────────────── decisions SO FAR
  ├── DataBank daily-use proof (MVP built & tested 2026-09-05)  ✓ decided
  │      next: run it daily on the Elitebook (owner hardware)    → DECISION 2
  ├── DataBank server layer (transport+auth+reachability)        → DECISION 3
  ├── ShrinkMedia stargate→connected (ADR-012, Tailscale)        → DECISION 4
  ├── Forge orchestrator (lift proven core)                      → DECISION 5
  ├── EasyTutor (vault-backed learner profile)                   → DECISION 6
  └── virtual me (model runtime on owner hardware)               ⏸ fog-of-war/ASPIRATIONAL
NOT yet specified: exact auth UX, encryption-at-rest key story, evals harness surface.
OUT OF SCOPE (decided): no third-party cloud; no public TLS; no self-authored autonomy (RSI-A).
```

The frontier today is **DECISION 2** (which daily cadence + hardware setup). One decision per
session, per S-05: the map never graduates an out-of-scope item.

## The 80/20 compression — what we deliberately cut (with the spend-the-time-elsewhere answer)

| 100%-shape ambition | 80/20 replacement (build this first) | Time saved (honest) |
|---------------------|---------------------------------------|---------------------|
| Hypervisor/VM layer on the Elitebook | **Bare-metal Debian 13 minimal** (T0_TINY, ADR-016) | ~1 day + RAM tax on a 2–8 GB host |
| Postgres + pgvector + object store | **SQLite + FTS5 vault** (365-ok for a life of notes; PG is a T2 optionality, same `vault.*`) | ~1–2 weeks of infra ceremony |
| Python web framework + TLS + public certs | **Stdlib-only `dvault` + Tailscale private net** (Tailscale = reachability without public ingress) | ~3–5 days + cert maintenance |
| Polished multi-app UX before proof | **CLI daily rhythm first** (`dvault capture/query/check`) | ~1 week of UI not needed to prove value |
| Full RAG/model reasoning to feel it work | **Keyword recall (deterministic, honest) first**; models stay ASPIRATIONAL until owner hardware | entire hardware dependency deferred |
| 5-way integration from day one | **DataBank-first**: prove the vault alone, then connect the stargate, then Forge | the monotonic path (below) |

## Sequential connect sequence (monotonic: each item unlocks the next, nothing skipped)

1. **DataBank daily-use proof** — vault MVP exists and self-proves (19 tests, `dvault`); the
   **Owner step**: flash the Elitebook (Debian 13, ADR-016), run it daily 2–5 minutes/day.
   *Status checkpoint A = "memory is trustworthy".* (≈ 1–2 weeks of calendar, 0–5 focused hours)
2. **DataBank server layer** — transport + auth + reachability in / outside the repo contract, still
   speaking the same `vault.*`. Tailscale-first. (≈ 1–2 focused weeks)
3. **ShrinkMedia stargate → connected action** — MCP client behind consent (ADR-012), no-INTERNET
   default preserved, release variant carries INTERNET behind explicit consent. Ends with: push a
   photo-note from ShrinkMedia into DataBank. (≈ 3–6 focused days once reachable)
4. **Forge orchestrator** — lift the proven core (ForgeTask, EcosystemIndex, LessonBook, ModelRouter)
   into a real single-repo program with evals + sandbox; internal decisions via the decision map (S-05).
   (≈ 2–4 focused weeks)
5. **EasyTutor** — vault-backed learner profile + `Learn` routing from virtual me. (≈ 3–6 focused weeks)
6. **Virtual me / DataBank brain** — RAG over the filled vault + model runtime **on owner hardware**;
   self-directed oversight stays ASPIRATIONAL until tier-2 hardware exists (ADR-016). (layered;
   can receive from 4 even before 5)

**Required by the owner (not code):** the Elitebook (or chosen tier) running DataBank daily now;
later, tier-2 compute (gaming PC, ADR-016 table) for the local model runtimes, reachability
credentials, and the off-machine keystore backup. Until item 1's checkpoint A is true, the vault is
the scripted daily tool — not a network service; the stargate stays a documented seam.

## Definition of "eco-system alive and accessible via the stargate"

Minimum meaningful milestone: **you can push a photo-note from ShrinkMedia into DataBank and ask the
virtual me a question whose answer comes from your own vault**, end-to-end, on your hardware, with
consent and no third-party cloud. With the 80/20 cuts that is ~**7–12 focused weeks** of sequential
work from today **minus the calendar time that only the Owner can spend** (the daily-loop proof is
like compounding: it needs the owner's repetition, not the coding hours). This is the honest S-curve
the SOP telemetry + compression ledger will measure against.