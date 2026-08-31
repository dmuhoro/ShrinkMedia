package com.shrinkmedia.compressor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM unit tests — runnable on the build machine with no device. They cover
 * the deterministic quality math and the human-readable size formatter shared by
 * the UI and reports. Nothing here touches Android framework services.
 */
class CompressionQualityUnitTest {

    @Test
    fun imageQualityScalesDownFromHighToLow() {
        assertTrue("HIGH must preserve the highest JPEG quality", CompressionQuality.HIGH.imageQuality >= CompressionQuality.MEDIUM.imageQuality)
        assertTrue("MEDIUM must be at least LOW quality", CompressionQuality.MEDIUM.imageQuality >= CompressionQuality.LOW.imageQuality)
    }

    @Test
    fun maxDimensionIsAlwaysFiniteAndPositive() {
        CompressionQuality.entries.forEach { q ->
            assertTrue("maxDimension must be positive for ${q.name}", q.maxDimension > 0)
        }
    }

    @Test
    fun videoBitrateIsNonEmptyForEveryPreset() {
        CompressionQuality.entries.forEach { q ->
            assertTrue("bitrate must be specified for ${q.name}", q.videoBitrate.isNotBlank())
        }
    }

    @Test
    fun formatFileSizeRendersBytesToGb() {
        assertEquals("0 B", formatFileSize(0L))
        assertEquals("0 B", formatFileSize(-1L))
        assertEquals("1,023 B", formatFileSize(1023L))
        assertEquals("1 KB", formatFileSize(1024L))
        assertEquals("1 MB", formatFileSize(1024L * 1024L))
        assertEquals("1 GB", formatFileSize(1024L * 1024L * 1024L))
    }
}
