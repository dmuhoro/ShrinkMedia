package com.shrinkmedia.compressor

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.SessionState
import kotlinx.coroutines.delay
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

    /**
     * Generate a real, decodable high-bitrate H.264 MP4 on-device with FFmpegKit.
     * `testsrc2` is chosen over `testsrc` because its detailed, high-entropy frames
     * do not collapse under high target bitrates — so the generated source file is
     * genuinely large (several MB), and re-encoding it via the production
     * `compressVideoFile` path at the MEDIUM target (~1500k) must strictly shrink
     * it. That makes the "output smaller than input" assertion meaningful against
     * the real codec rather than a trivially-compressible synthetic.
     */
    private fun writeHighBitrateMp4(file: File, durationSec: Int = 6) {
        val session = FFmpegKit.execute(
            "-y -f lavfi -i testsrc2=size=1920x1080:rate=30 -t $durationSec " +
                "-c:v h264 -b:v 8000k -pix_fmt yuv420p \"${file.absolutePath}\""
        )
        var waited = 0
        while ((session.state == SessionState.CREATED || session.state == SessionState.RUNNING) && waited < 40_000) {
            Thread.sleep(100); waited += 100
        }
        val rc = session.returnCode
        assertTrue("input MP4 must encode before test proceeds (rc=$rc)", ReturnCode.isSuccess(rc))
        assertTrue("input MP4 must exist on disk", file.exists() && file.length() > 0L)
    }

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
    fun compressVideoFile_runsTheRealFFmpegKitPipeline_andProducesASmallerValidH264Mp4() {
        runBlocking {
            val inputFile = File(context.cacheDir, "video_in_${System.currentTimeMillis()}.mp4")
            writeHighBitrateMp4(inputFile)
            val inputSize = inputFile.length()

            var lastProgress = 0f
            val output = compressVideoFile(
                context,
                uriFor(inputFile),
                CompressionQuality.MEDIUM,
                onProgress = { lastProgress = it }
            )

            assertNotNull("real FFmpegKit compression must return an output file", output)
            val out = output!!
            assertTrue("output must exist", out.exists())
            assertTrue("output must be non-empty", out.length() > 0L)
            assertTrue("output must be smaller than input ($inputSize -> ${out.length()})", out.length() < inputSize)

            val probe = FFmpegKit.execute("-v error -i \"${out.absolutePath}\" -f null -")
            var probed = 0
            while ((probe.state == SessionState.CREATED || probe.state == SessionState.RUNNING) && probed < 40_000) { Thread.sleep(100); probed += 100 }
            assertTrue("output must be a decodable H.264 MP4", ReturnCode.isSuccess(probe.returnCode))
            assertTrue("onProgress must have reported completion (100)", lastProgress >= 100f)

            inputFile.delete()
            out.delete()
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
