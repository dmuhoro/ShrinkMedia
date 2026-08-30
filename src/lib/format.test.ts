import { describe, expect, it } from 'vitest';
import { formatBytes } from './format';

describe('formatBytes', () => {
  it('renders zero and negative sizes as 0 B', () => {
    expect(formatBytes(0)).toBe('0 B');
    expect(formatBytes(-5)).toBe('0 B');
  });

  it('renders bytes without decimals', () => {
    expect(formatBytes(1)).toBe('1 B');
    expect(formatBytes(1023)).toBe('1023 B');
  });

  it('renders kilobytes', () => {
    expect(formatBytes(1024)).toBe('1 KB');
    expect(formatBytes(1024 * 12)).toBe('12 KB');
  });

  it('renders megabytes with one decimal', () => {
    expect(formatBytes(1024 * 1024)).toBe('1.0 MB');
    expect(formatBytes(4800000)).toBe('4.6 MB');
  });

  it('renders gigabytes with one decimal', () => {
    expect(formatBytes(45200000)).toBe('43.1 MB');
    const oneGb = 1024 ** 3;
    expect(formatBytes(oneGb)).toBe('1.0 GB');
  });

  it('clamps to terabytes above the unit range', () => {
    expect(formatBytes(1024 ** 5)).toBe('1024.0 TB');
  });
});