# 05 — Native Module: Service & Engine Contracts

## Purpose
The exact contracts of the batch service, pause/resume, and conversion helpers.

## Authority Level
Foundational — Constitution Article III.

## Consumers
Builder/auditor agents working on media or the batch queue.

## Source Documents
`app/src/main/java/com/example/mediacompressor/*.kt`.

## Update Rules
Update when the service or engine contracts change.

---

## Service contract
`BatchCompressionService.startBatch(context, uris, isVideo, qualityName,
autoSave)` starts a foreground service (`dataSync`); `ACTION_CANCEL_BATCH`
stops it. The loop in `executeBatchProcessing`:
1. awaits `BatchCompressionPauseController.isPaused.first { !paused }` per item
   (never drops a queued item);
2. runs `compressVideoFile` / `compressImageFile` per URI;
3. reads **live** DataStore `autoSaveToMediaStore` per file and autosaves when
   true;
4. notifies per-item progress and a final completion notification.

## Pause/resume
Receiver on `ACTION_BATTERY_LOW` sets `isPaused = true`; `ACTION_BATTERY_OKAY`
and `ACTION_POWER_CONNECTED` clear it. Receiver registered **only when**
`pauseOnLowBattery` is enabled; unregistered on destroy.

## Engine helpers (all `File?` / typed)
- `compressImageFile(context, uri, quality)` — bounds pass → `inSampleSize` →
  scale to `maxDimension` → JPEG `imageQuality` → cache file.
- `compressVideoFile(context, uri, quality, onProgress)` — copy to cache →
  async FFmpeg (`libx264 -crf N -b:v R` + `aac`) → **await session state** →
  cache file; `null` on failure.
- `saveToPublicMediaStore(context, file, video): Boolean` — MediaStore insert
  to `Pictures/ShrinkMedia` / `Movies/ShrinkMedia`; no storage permission.

## Guardrails
- `null` output surfaces to the user; never swallowed.
- Autosave failure returns `false`; callers report it.
- Fire-and-forget FFmpeg is forbidden.