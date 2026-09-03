# Evidence — On-device install + launch of the current build (L1)

**Date:** 2026-09-03
**Status:** PASS — app installed and running (top-focused activity) on the real device.

## Steps + observed results (real commands)

| Step | Command | Observed result |
|------|---------|-----------------|
| Free space | `adb shell df -h /data` | `/dev/block/dm-64 106G 102G 2.9G 98% /data/user/0` (2.9G free — user freed storage, unblocked prior `INSTALL_FAILED_INSUFFICIENT_STORAGE`) |
| Install | `adb install -r app/build/outputs/apk/release/app-release.apk` | `Success` |
| Present | `adb shell pm list packages` | `package:com.shrinkmedia.compressor` |
| Version | `adb shell dumpsys package ...` | `versionCode=6 versionName=0.6.0 targetSdk=35 minSdk=24` |
| Launch | `adb shell am start -n com.shrinkmedia.compressor/.MainActivity` | `Starting: Intent { cmp=com.shrinkmedia.compressor/.MainActivity }` |
| Focus | `adb shell dumpsys activity activities` | `topResumedActivity=ActivityRecord{... com.shrinkmedia.compressor/.MainActivity}` |
| Screenshot | `adb shell screencap -p` + `adb pull` | `docs/evidence/shrinkmedia_v060_home.png` (128KB, non-empty) |

## Important learnings (why this got unblocked)

- Storage was the earlier blocker; once the user freed space the install proceeded past the old `INSTALL_FAILED_INSUFFICIENT_STORAGE`.
- **`adb install -g` (grant-all-runtime-permissions) FAILS** on this device with
  `SecurityException: You need the android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS ...`
  — the `-g` flag must be **omitted** for release installs (the app grants its own runtime
  permissions on first use). Prior attempts incorrectly mixed `-g`, which masked the actual fix.
- Prior `INSTALL_FAILED_USER_RESTRICTED` was resolved once storage freed + `-g` removed; no OEM
  "Install via USB" toggle change was needed after all.

## Honest caveat

- This proves the **current v0.6.0** build boots and is focused. The **SDK-36** build (Layer 2)
  still needs its own on-device boot check once built; Layer 2's host gates are green but the
  device boot remains PENDING until the SDK-36 APK is installed.