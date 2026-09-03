# Evidence — Keystore off-machine backup (L6)

**Date:** 2026-09-03
**Status:** PARTIAL — verification performed (file + checksum anchor), human copy step
outstanding.

## The risk being closed
The release signing keystore is the single identity that makes every future Play-store
AAB updatable. It lives in ONE place only:
`/home/daniel-muhoro/.android/keystores/shrinkmedia-release.jks` (gitignored, referenced
by `app/build.gradle.kts`). Loss of this host = permanent loss of the release identity.

## What was verified (real commands + observed result)
| Check | Command | Observed result |
|-------|---------|-----------------|
| Keystore exists, single copy | `ls -la .../shrinkmedia-release.jks` | 2770 bytes, `2026-08-31` |
| Integrity anchor (checksum) | `sha256sum .../shrinkmedia-release.jks` | `2ecf8c807beeabef5a7f3df07902b997ebf846476eb85a56b4288ede4dca2d5b` |
| Keystore kept out of git | `.gitignore` + `git check-ignore` | `keystore.properties`, `*.jks` ignored; keystore is outside repo |

## Deliverable
`docs/runbooks/keystore-backup.md` — a verified checklist:
1. Confirm live file + SHA256 (above);
2. copy bit-for-bit to an encrypted USB drive / hardware token / private KMS-backed
   object store (NOT the repo);
3. record the backup location + holder in a private, non-repo place;
4. re-verify the copy's SHA256 equals the anchor `2ecf8c80...dca2d5b`;
5. equality is REQUIRED — any mismatch means redo (fail closed);
6. `L6_ANCHOR_SHA256=2ecf8c807beeabef5a7f3df07902b997ebf846476eb85a56b4288ede4dca2d5b`.

## Honest caveat
The **off-machine copy itself is a human step and is NOT done** as of this writing. The
release identity still has a single point of failure on this host until a verified
off-machine copy exists. This evidence proves the checklist + checksum anchor are
ready and break the task down to one executable human action; it does not claim the
copy already exists.