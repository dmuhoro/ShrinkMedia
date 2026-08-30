export type QualityPreset = 'Low' | 'Medium' | 'High';

export const COMPRESSION_RATIO: Record<QualityPreset, number> = {
  Low: 0.28,
  Medium: 0.48,
  High: 0.72,
};

export function compressionRatio(preset: QualityPreset): number {
  return COMPRESSION_RATIO[preset];
}

export function compressedSize(originalSize: number, preset: QualityPreset): number {
  return Math.round(originalSize * compressionRatio(preset));
}

export function reductionPercent(originalSize: number, compressedSizeValue: number): number {
  if (originalSize <= 0) return 0;
  return Math.round(100 - (compressedSizeValue / originalSize) * 100);
}