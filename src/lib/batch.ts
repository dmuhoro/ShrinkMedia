/**
 * Honest, testable model of the native batch-compression pipeline.
 *
 * The native app compresses a queued set of files on-device, records the bytes
 * saved per file (never negative), and accumulates a lifetime total in
 * DataStore. These pure functions mirror that contract so the web simulator's
 * batch/audit/savings math matches the real behaviour instead of inventing
 * arbitrary numbers.
 *
 * @license Apache-2.0
 */

import { compressedSize, compressionRatio, reductionPercent, type QualityPreset } from './compression';

export type MediaType = 'image' | 'video';

export interface BatchQueueItem {
  id: string;
  name: string;
  size: number;
  isVideo: boolean;
}

export interface BatchResultItem {
  id: string;
  name: string;
  mediaType: MediaType;
  originalSize: number;
  compressedSize: number;
  savedBytes: number;
  reductionPercent: number;
  status: 'SUCCESS';
}

export interface SavingsSummary {
  savedBytes: number;
  files: number;
}

/**
 * Build honest per-file results for a batch queue under a quality preset.
 * All figures are derived from the real input size and the preset ratio; no
 * random or fabricated sizes are introduced.
 */
export function buildBatchResults(
  queue: readonly BatchQueueItem[],
  preset: QualityPreset,
): BatchResultItem[] {
  return queue.map((item) => {
    const out = compressedSize(item.size, preset);
    return {
      id: item.id,
      name: item.name,
      mediaType: item.isVideo ? 'video' : 'image',
      originalSize: item.size,
      compressedSize: out,
      savedBytes: Math.max(0, item.size - out),
      reductionPercent: reductionPercent(item.size, out),
      status: 'SUCCESS',
    };
  });
}

/**
 * Fold results into a lifetime savings summary.
 * Mirrors native `recordCompressionSavings`: only non-negative per-file savings
 * are accumulated, and every successfully processed file is counted once.
 */
export function accumulateSavings(results: readonly BatchResultItem[]): SavingsSummary {
  return results.reduce<SavingsSummary>(
    (acc, item) => {
      acc.savedBytes += Math.max(0, item.savedBytes);
      acc.files += 1;
      return acc;
    },
    { savedBytes: 0, files: 0 },
  );
}

export { compressionRatio };
