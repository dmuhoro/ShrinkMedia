# Checkpoint Map — Path to "DataBank and ShrinkMedia function end-to-end without crashing"

> The 2026-09-05 directive asks a direct question: **how many checkpoints until DataBank and
> ShrinkMedia function end-to-end without crashing?** This is the numbered, exit-tested answer.
>
> Definition of "end-to-end without crashing": ShrinkMedia and Daftari are the writers, DataBank is
> the durable archive, and the daily loop — produce a record → vault accepts it (or refuses with a
> reason, never a crash) → archive stays verifiable — survives restarts, offline gaps, and a full
> disk. Locally that loop already runs (ShrinkMedia C24 vault on-device; DataBank proven on this
> machine). What remains is the *connected* loop and the always-on host, below.

## Done (2026-09-05)

| # | Checkpoint | Exit test | Status |
|---|-----------|-----------|--------|
| 0 | DataBank is not server-specific | full suite + live capture/query/backup/restore on Ubuntu 26.04 (Python 3.14) + CI ×3 OS | ✅ PASS (evidence 2026-09-05, PORTABILITY.md) |
| — | Each ecosystem repo CI-green on every push | separate workflows each green (DataBank python · Forge python · ShrinkMedia 4 jobs · Daftari incl. build + 5 prod e2e) | ✅ PASS (this sprint) |

## Open — numbered to the connected verdict

| # | Checkpoint | What "done" means (exit test) | Est. effort |
|---|-----------|-------------------------------|-------------|
| 1 | **Always-on host** — DataBank live on the Elitebook 2540p (Debian 13) or equivalent always-on machine, run as a service (systemd), WAL on, autostart | `curl`/health + `dvault check` green after a reboot; service restarts itself if killed | ~1–2 focused days |
| 2 | **Restore drill** — true bare-metal restore proven | take `dvault backup`, wipe the store, restore from the `VACUUM INTO` snapshot, `check` + journal audit green, record count matches | ½–1 day |
| 3 | **Private transport** — reach the vault only over a tailnet/keystore-authenticated channel (stargate), no public URL | a capture over the private channel on one machine, read from another; **no** `INTERNET`-granting change to ShrinkMedia/Daftari manifests | ~2–4 days (transport code + auth) |
| 4 | **Writers contract** — ShrinkMedia `SaveToVault` path and Daftari offline sync both land real records in DataBank through the channel in #3 | one real capture from each writer lands in the vault on the always-on host; each writer surfaces its own result (success or refusal-with-reason) | ~2–4 days |
| 5 | **Forge exit criterion** where the brief says so | hand-run 5 *real* Daftari task-lifts (owner's tasks, not synthetic) through `forge-task`; merged+journal report | parallel-track, needs the owner's tasks |
| 6 | **Device truth** — Daftari v6.4.0 installed + ShrinkMedia on-device against the connected vault | manual install checklist (Chrome add-to-home-screen → app-style install); offline sale syncs after reconnection on a real handset | ½–1 day on the device |
| 7 | **Fault drills baked in** — the crash-free claim is a *tested invariant*, not a hope | kill the host vault mid-write → restart → journal audit clean, queue intact; full-disk vault write → refusal not corruption; Daftari offline-a-month agent uploads on reconnect | continuous in CI where the failure is injectable |

## The straight answer

- **DataBank + ShrinkMedia, each functioning without crashing, on their own: already demonstrated**
  (Checkpoint 0 + ShrinkMedia's on-device suite, plus Daftari's offline proof).
- **The connected, always-on end-to-end verdict: checkpoints 1 → 7 above.** The honest estimate for
  #1–7 (transport + writers contract + device pass + drills) is **roughly 1–2 focused weeks of
  execution**, gated ~2/3 of the way by two things no command can conjure: the **always-on host**
  (owner hardware) and the **real handset** for the install checklist. #5 runs in parallel and only
  needs the owner's tasks.
- Nothing here changes RSI (still OFF, dial 0) or the no-INTERNET invariant (transport is a private
  tailnet/keystore path, not an INTERNET-permission change).

Updated 2026-09-05 (Sprint 22).