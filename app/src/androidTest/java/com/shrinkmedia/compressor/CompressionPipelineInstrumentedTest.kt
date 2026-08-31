package com.shrinkmedia.compressor

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.Random

/**
 * Instrumented tests that exercise the REAL production compression pipeline.
 *
 * These run on a device/emulator as the final release gate. They drive the
 * same top-level `compressImageFile`/`saveToPublicMediaStore` functions and the
 * `SettingsRepository.recordCompressionSavings` DataStore write that the live
 * UI and `BatchCompressionService` call — not a mocked copy.
 */
@RunWith(AndroidJUnit4::class)
class CompressionPipelineInstrumentedTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Write a solid-noise bitmap as a PNG. PNG is lossless and decodable by
     * BitmapFactory on every Android device (unlike the synthetic BMP, which
     * returned bounds w=-1/h=-1 on the API-36 test device), and a pure-noise
     * scene stays larger than any real JPEG encoding of the same pixels — so the
     * "output must be smaller than input" assertion is meaningful.
     */
    private fun writeNoiseBitmapAsPng(file: File, width: Int = 1024, height: Int = 1024) {
        val random = Random(42)
        val pixels = IntArray(width * height) { 0xFF000000.toInt() or random.nextInt(0xFFFFFF) }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        FileOutputStream(file).use { out ->
            assertTrue("PNG encoding of the test input must succeed", bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
        }
        bitmap.recycle()
    }

    private fun uriFor(file: File): android.net.Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    @Test
    fun compressImageFile_runsTheRealPipeline_andProducesASmallerValidJpeg() {
        runBlocking {
            val inputFile = File(context.cacheDir, "in_${System.currentTimeMillis()}.png")
            writeNoiseBitmapAsPng(inputFile)

            val inputSize = inputFile.length()
            val output = compressImageFile(context, uriFor(inputFile), CompressionQuality.MEDIUM)

            assertNotNull("real compression must return an output file", output)
            val out = output!!
            assertTrue("output must exist", out.exists())
            assertTrue("output must be non-empty", out.length() > 0L)
            assertTrue("output must be smaller than input ($inputSize -> ${out.length()})", out.length() < inputSize)

            val decoded = android.graphics.BitmapFactory.decodeFile(out.absolutePath)
            assertNotNull("output must be a decodable image", decoded)
            assertTrue("output must have valid dimensions", decoded.width > 0 && decoded.height > 0)
            decoded.recycle()

            inputFile.delete()
            out.delete()
        }
    }

    @Test
    fun compressImageFile_returnsNull_forUnreadableOrNonImageContent() {
        runBlocking {
            val bogus = File(context.cacheDir, "bogus_${System.currentTimeMillis()}.bin")
            FileOutputStream(bogus).use { it.write(ByteArray(4096) { 0x41 }) }

            val output = compressImageFile(context, uriFor(bogus), CompressionQuality.MEDIUM)
            assertNull("non-decodable content must surface as null, never a bogus file", output)
            bogus.delete()
        }
    }

    @Test
    fun saveToPublicMediaStore_insertsIntoPublicGallery() {
        val outFile = File(context.cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
        writeNoiseBitmapAsPng(outFile, 320, 240)
        val saved = saveToPublicMediaStore(context, outFile, video = false)
        assertTrue("real media-store insert should succeed", saved)
        outFile.delete()
    }

    private fun currentSavings(repo: SettingsRepository): PersistedUserSettings =
        runBlocking { repo.userSettingsFlow.first() }

    @Test
    fun recordCompressionSavings_accumulatesMonotonically_inRealDataStore() = runBlocking {
        val repo = SettingsRepository(context)
        val before = currentSavings(repo)

        repo.recordCompressionSavings(1_000_000L)

        val after = currentSavings(repo)

        assertTrue("bytes saved must never decrease", after.totalHistoricalSavedBytes >= before.totalHistoricalSavedBytes)
        assertEquals(
            "each recorded file increments the count exactly once",
            before.totalHistoricalFilesCount + 1L,
            after.totalHistoricalFilesCount
        )
    }

    @Test
    fun recordCompressionSavings_neverAccumulatesNegatively() = runBlocking {
        val repo = SettingsRepository(context)
        val before = currentSavings(repo)

        repo.recordCompressionSavings(-500L)

        val after = currentSavings(repo)
        assertEquals(
            "negative savings must be clamped to 0 (no byte count regression)",
            before.totalHistoricalSavedBytes,
            after.totalHistoricalSavedBytes
        )
        assertEquals(
            "a recorded file is still counted even when its savings clamp to 0",
            before.totalHistoricalFilesCount + 1L,
            after.totalHistoricalFilesCount
        )
    }
}
