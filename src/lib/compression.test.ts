import { describe, expect, it } from 'vitest';
import { COMPRESSION_RATIO, compressionRatio, compressedSize, reductionPercent } from './compression';

describe('compressionRatio', () => {
  it('matches the preset table used by the simulator', () => {
    expect(COMPRESSION_RATIO).toEqual({ Low: 0.28, Medium: 0.48, High: 0.72 });
    expect(compressionRatio('Low')).toBe(0.28);
    expect(compressionRatio('Medium')).toBe(0.48);
    expect(compressionRatio('High')).toBe(0.72);
  });
});

describe('compressedSize', () => {
  it('rounds the ratio application to whole bytes', () => {
    expect(compressedSize(100, 'Low')).toBe(28);
    expect(compressedSize(1000, 'Medium')).toBe(480);
    expect(compressedSize(1000, 'High')).toBe(720);
  });
});

describe('reductionPercent', () => {
  it('reports the percent saved versus original', () => {
    expect(reductionPercent(4800000, 1100000)).toBe(77);
    expect(reductionPercent(45200000, 14800000)).toBe(67);
  });

  it('returns 0 for a non-positive original size', () => {
    expect(reductionPercent(0, 10)).toBe(0);
    expect(reductionPercent(-4, 10)).toBe(0);
  });

  it('can report zero or negative savings honestly', () => {
    expect(reductionPercent(100, 100)).toBe(0);
    expect(reductionPercent(100, 130)).toBe(-30);
  });
});