# Ecosystem Roadmap — size, complexity, time, and the connect sequence

> Answers the 2026-09-05 directive: *"I want to know how big and complex each of the projects are
> so that we can estimate the time it would take us to complete building them and making them ready
> to be connected to the eco-system and accessed via ShrinkMedia."* Estimates are honest ranges from
> analogous builds by this team (calibration-0: 104 commits/6 days ≈ ~17 commits/day when focused),
> not promises.

## The connectable layer

Every product connects through the **MCP `vault.*` contract** (ADR-013): `vault.put` / `vault.get`
/ `vault.index` / `vault.query`. ShrinkMedia's Connected-mode stargate (ADR-012, OFF by default,
consent-gated) is the first client hop. **A live connection requires the self-hosted DataBank
server running on owner hardware + reachability — none of that exists yet; it is L2+.**

## Project inventory

| Project | Role in eco-system | Main LOC (today) | Complexity (honest) | Remaining size estimate | Estimated focused time to "ready to connect" |
|---|---|---|---|---|---|
| **ShrinkMedia** | Phone portal / stargate / edge | main 2.7k + unit 0.7k + androidTest 0.7k | Mature; L1 complete (v0.8.0) | small: connected-MCP client adapter (L2) + release upkeep | **~2–4 weeks** for the real connected action (needs DataBank server) |
| **Forge** | Orchestrator / builder: Planner·Retriever·Builder·Reviewer·Ops + evals + safety | core engine proofed in ShrinkMedia (ForgeTask, EcosystemIndex, LessonBook, ModelRouter) | Med–high (the "how we build" brain) | server/CLI program + evals harness + sandbox; forge.repo lift of the proven core | **~3–6 weeks** to a trustworthy single-repo orchestrator; +2–4 wk for adapters to each product |
| **DataBank** | Vault: storage + index + processing-seam + auth | 0 (not yet a repo) | Med (but correctness-critical: it holds a life) | self-hosted server: object store, PgVector index, `vault.*` MCP, auth, version thin-client | **~2.5–4 weeks** to a reachable, secure local vault |
| **EasyTutor** | Education / personal development (curricula, learner profile) | 0 | Med (learning loop + pedagogy-content authoring) | learning MVP + vault-backed learner profile + topic routing from virtual me | **~4–6 weeks** for a real MVP |
| **Virtual me / DataBank brain** | Guardian/overseer; answers via your data | 0 (decision core proven: PersonalIntelligenceAgent) | High (research-grade once self-directed) | model runtime on home-lab (Ollama, encodings), RAG over vault, conscioious oversight gates | **Layered ~6–10 weeks** behind DataBank + Forge; self-directed mode stays ASPIRATIONAL |

## Connect sequence (sequential, one layer at a time)

1. **DataBank server on owner hardware** (self-hosted shell; `vault.*`; auth; local network first).
2. **ShrinkMedia stargate → connected action** (MCP client behind consent; ADR-012, no-INTERNET
   default preserved; release variant carries INTERNET behind explicit consent).
3. **Forge orchestrator** (lift the proven core; Planner→Reviewer loop with evals + sandbox).
4. **EasyTutor** (living on DataBank's learner profile + virtual-me `Learn` routing).
5. **Virtual me** (RAG over the filled vault + model runtime; guardian gates above agents).

**Required by the owner (not code):** home-lab hardware (RTX 3090+ 24 GB + low-power host,
≈ $2,500–4,500, ADR-013 §8), reachability/credentials, and the off-machine keystore backup. Until
item 1 exists, everything in ShrinkMedia stays on-device and the stargate remains a documented seam,
not a live door.

## Definition of "eco-system alive and accessible via the stargate"

Minimum meaningful milestone: **you can push a photo-note from ShrinkMedia into DataBank and ask
the virtual me a question whose answer comes from your own vault**, end-to-end, on your hardware,
with consent and no third-party cloud. That requires DataBank (1) + stargate action (2) + brain
routing online — i.e. ~**6–9 weeks** of sequential focused work from today, plus the hardware. This
is the honest S-curve the SOP telemetry will measure against.