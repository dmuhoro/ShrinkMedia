# ShrinkMedia — Incomplete-Tasks Inventory & Safest Stop Decision

> The 2026-09-05 directive: *"I want to know the incomplete tasks that we have left in ShrinkMedia
> so that we decide the safest stop for it, in order to proceed building the eco-system."* This
> decides the stop and lets the team move to Forge/DataBank/EasyTutor without ghost trips back.

## What is actually left (audited vs `docs/current-state.md` C1–C23, 2026-09-05)

| # | Outstanding item | Why it is not done | Who/What un-blocks it | Severity |
|---|---|---|---|---|
| 1 | **C11 on-device Nano inference proof** | Redmi API-36 has **no AICore**; live probe returned `UNAVAILABLE`. Code path is real + build-verified; only the hardware proof is missing | A Nano/AICore-capable device (owner) | must-not-claim (already honest) |
| 2 | **C17 real connected action (actual INTERNET path behind consent)** | no-INTERNET invariant; the gateway/consent/flow exist (L1) but requires the DataBank server + owner reachability/credentials | DataBank server on home-lab (roadmap item 1) | next programme, not a ShrinkMedia gap |
| 3 | **Web portal `deploy-web.yml` auto-deploy** | code-complete; needs one-time `VERCEL_TOKEN` secret in GitHub | owner adds secret | human 30s step |
| 4 | **Release keystore off-machine backup** | runbook exists (`docs/runbooks/keystore-backup.md`); the copy is human | owner executes once | MUST-DO (permanent app identity) |
| 5 | **Bug-fix releases only** (post-stop) | — | process decision | standing |
| 6 | Stale orientation note | "…helpers are shippable but device-untested" contradicts verified PASS rows (C3/C4/C9/C10/C14) | corrected this sprint | doc hygiene |

## The safest stop — decision (CONFIRMED ACTIVE as of v0.9.0, 2026-09-05)

**ShrinkMedia is in maintenance mode. It IS safe to stop and move to the eco-system** — with two
honest conditions:

1. **It is safe to stop *building***, because it is feature-complete for its declared purpose (a
   private, on-device media/document/AI toolkit and the ecosystem's stargate). ShrinkMedia releases
   v0.9.y patched (bug-fix only) and stays the phone face of the eco-system; the stargate door is
   built and its key (the connected adapter) is forged with DataBank, not inside this repo.
2. **It is NOT safe to *forget***: items 1–4 above stay on the owner's plate permanently (keystore
   backup MUST-DO; C17 becomes the stargate milestone). Maintenance-mode discipline is that nothing
   ShrinkMedia-related changes except: a **real bug fix** with evidence, a **stargate/connection
   task after DataBank exists**, or an **ADR** (never a drive-by).

Every future ShrinkMedia change is therefore governed by the same SDLC as any other repo — it is
just low-volume. The **host-tier decision layer added 2026-09-05** (`ecosystem/HostTier`, ADR-016)
was planted *inside* ShrinkMedia so the phone stays the natural place where the "which machine runs
what" decision lives — that is planning infrastructure, not new product scope.

**Honest boundary:** stopping ShrinkMedia does not mean the eco-system is alive — it means the
*vessel* is done. Alive happens when DataBank + stargate action + virtual-me routing run on owner
hardware (roadmap sequence, ≈ 7–12 focused weeks + the owner's daily-loop proof). RSI/Autonomy is a
separate ledger and stays OFF (dial 0) until the Owner authorizes (see `docs/operations/rsia-program.md`).