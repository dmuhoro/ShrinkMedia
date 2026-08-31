import { describe, expect, it } from 'vitest';
import {
  accumulateSavings,
  buildBatchResults,
  type BatchQueueItem,
} from './batch';

describe('buildBatchResults (batch flow parity)', () => {
  const queue: BatchQueueItem[] = [
    { id: 'a', name: 'photo_1.jpg', size: 4800000, isVideo: false },
    { id: 'b', name: 'clip_1.mp4', size: 45200000, isVideo: true },
    { id: 'c', name: 'photo_2.jpg', size: 100, isVideo: false },
  ];

  it('produces one result per queued file and marks every one SUCCESS', () => {
    const results = buildBatchResults(queue, 'Medium');
    expect(results).toHaveLength(3);
    expect(results.every((r) => r.status === 'SUCCESS')).toBe(true);
  });

  it('tags the media type from the queue item', () => {
    const results = buildBatchResults(queue, 'Medium');
    expect(results.map((r) => r.mediaType)).toEqual(['image', 'video', 'image']);
  });

  it('computes compressed size and savings from the honest ratio, not a fake number', () => {
    const results = buildBatchResults(queue, 'Medium');
    // Medium ratio is 0.48 -> any result must equal a pure ratio product.
    results.forEach((r) => {
      expect(r.compressedSize).toBe(Math.round(r.originalSize * 0.48));
      expect(r.savedBytes).toBe(Math.max(0, r.originalSize - r.compressedSize));
    });
  });

  it('never reports negative per-file savings (matches native maxOf(0, saved))', () => {
    const results = buildBatchResults(queue, 'High');
    results.forEach((r) => expect(r.savedBytes).toBeGreaterThanOrEqual(0));
    // A file of size 100 with a 0.72 ratio still saves a non-negative amount.
    expect(results[2].savedBytes).toBe(28);
  });
});

describe('accumulateSavings (savings flow parity with DataStore)', () => {
  it('accumulates bytes and counts each file exactly once', () => {
    const results = buildBatchResults(
      [
        { id: 'a', name: 'a.jpg', size: 100, isVideo: false },
        { id: 'b', name: 'b.jpg', size: 100, isVideo: false },
      ],
      'Low', // ratio 0.28 -> save 72 each, total 144, 2 files
    );
    expect(accumulateSavings(results)).toEqual({ savedBytes: 144, files: 2 });
  });

  it('accumulates only non-negative savings per file', () => {
    // High ratio 0.72 leaves savings of 28 per 100-byte file; parity with native maxOf(0L, ...).
    const results = buildBatchResults(
      [{ id: 'a', name: 'a.jpg', size: 100, isVideo: false }],
      'High',
    );
    expect(accumulateSavings(results)).toEqual({ savedBytes: 28, files: 1 });
  });

  it('returns a zero summary for an empty batch', () => {
    expect(accumulateSavings([])).toEqual({ savedBytes: 0, files: 0 });
  });
});
