# ADR-016 — Ecosystem Deployment Profiles: host tiers, backends, virtualization decision

- **Status:** Accepted (2026-09-05)
- **Deciders:** Owner (OP), Lead Engineer (ShrinkMedia — this sprint's decision layer)
- **Technical story:** The ecosystem must run on a **captive, heterogeneous home-lab** — from a
  2010-era HP Elitebook 2540p (2 cores, 2–8 GB DDR3) up to a future gaming PC. The same `vault.*`
  contract must behave identically regardless of which host a component lands on, and the choice of
  machine must be expressible and checkable, not an implicit assumption.

## 1. Context and problem

The 2026-09-05 directive asks for hardware **optionality**: *"it's entirely up to me what machine
to choose — old laptop → gaming PC — which then determines the compute we need."* A single "intended
deployment" (e.g. "Postgres VM on a 32 GB box") would be wrong for the actual owner hardware and
would silently over-provision an Elitebook. Without an explicit decision, individual components
would each guess their own runtime assumptions — the resident evil of a privately homogeneous
stack that pretends to be an option.

## 2. Decision

Adopt a **three-tier host model (T0_TINY / T1_MID / T2_HEAVY)** with a **fail-closed classifier**,
implemented as `com.shrinkmedia.compressor.ecosystem.HostClassifier` in ShrinkMedia (the decision
layer the phone already carries):

| Tier | Host shape | RAM | Cores | Default DB backend | Isolation | Concurrency | Local-model runtime |
|------|-----------|-----|-------|--------------------|-----------|-------------|---------------------|
| **T0_TINY** | 2010-era laptop / SBC (incl. Elitebook 2540p) | < 4 GB | ≥ 2 | **SQLite + FTS5** (bundled; stdlib `dvault`) | model-poisoning-aware single-cluster | 1 (plus system) | refused (no evidence of viability) |
| **T1_MID** | mid home-lab (mini PC, 8–16 GB) | ≥ 4 GB | ≥ 4 | **SQLite + FTS5** | Podman **or** native systemd (RAM decides) | 2 | refused until proved |
| **T2_HEAVY** | gaming PC / 2× GPU tower (≥ 32 GB) | ≥ 16 GB | ≥ 8 | **Postgres + pgvector** (optionality for recall) | containers only | 4 | allowed (model runtime in-process) |

**Classifier rules (fail-closed):**
- `supportsBackend(SQLITE_FTS5)`: always true; `supportsBackend(POSTGRES_PGVECTOR)`: **only T2+**
  (Postgres is an optionality, never a default).
- `isolation`: T0 = native systemd strict; containers only allowed when **≥ 8 GB RAM** (the Podman
  runtime tax on a 2–4 GB host is not ergodic — ADR-015 §3).
- `recommendedProfile` returns the tier's defaults above; unknown specs **refuse** with a reason
  (`refusalReason`), never silently assume a machine (no silent drops, AGENTS.md §1-4).
- Thresholds (bytes handled in code): MIN_RAM_MB = 1536, MIN_CORES = 2, T1_RAM_MB = 4096,
  T1_CORES = 4, T2_RAM_MB = 16384, T2_CORES = 8, CONTAINER_RAM_MB = 8192.

**Elitebook 2540p answer (the owner's literal question):** install **Debian 13 (Trixie) as a
bare-metal minimal server — do NOT use virtualization to "save RAM".** On ≤ 4 GB, a hypervisor
consumes more RAM/CPU than the isolation margin it buys; bare Debian 13 + native systemd + zram is
the T0_TINY profile. Full tradeoff sheet: `DataBank/docs/DEPLOY.md`.

## 3. Consequences (what this ADR buys)

**Positive**
- The same DataBank/Forge/EasyTutor components run unmodified across machines: tier is a runtime
  property of the host, not a fork of the code.
- Optionality is *stated and checked*: the phone's HostClassifier refuses an impossible profile
  loudly, and future deployment docs can trust an advertised profile.
- Postgres stays a T2 optionality — we get a vault live on an Elitebook this week, not after a
  database migration class.
- Containers are allowed exactly where the RAM tax is safe (fail-closed on host memory).

**Negative / tradeoffs**
- T0_TINY accepts weaker recall/power (keyword idea-markers; SQLite FTS5) — honest limitation,
  PG/pgvector and model reasoning exist only behind T2.
- Decision logic lives in ShrinkMedia (an Android module) even though it governs the ecosystem: it
  is the one piece the owner always has on the phone (the stargate), so it is the correct home —
  but it must be re-exposed by any server/CLI that needs it.

## 4. Alternatives considered

1. **"Just target the future gaming PC."** Rejected: bets the whole roadmap on hardware the owner
   has not bought; violates the directive's optionality.
2. **Heavy virtualization everywhere (KVM/Proxmox per service).** Rejected on T0: RAM tax >
   isolation margin on 2–8 GB; would make the Elitebook a paperweight.
3. **All-Postgres + containers as the default.** Rejected: same false default problem; a captive
   host does not need a database cluster to hold a life of notes correctly.
4. **No classifier; document targets.** Rejected: silent-assumption risk returns the moment a doc rots.

## 5. Links

- HostTier/HostClassifier (the decision layer): `app/src/main/java/com/shrinkmedia/compressor/ecosystem/HostTier.kt`
- Vault MVP + deploy runbook: `DataBank` repo (`docs/DEPLOY.md`, `docs/RUNBOOK.md`)
- 80/20 sequence: `docs/operations/ecosystem-roadmap.md`; ergodic framing: ADR-015 §3, `ecosystem-orientation.md` §3.