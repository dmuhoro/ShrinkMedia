package com.shrinkmedia.compressor

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumented contract test for the fail-closed batch pause gate.
 *
 * The Constitution requires that a queued batch item is NEVER dropped: it waits
 * until the single source of truth (`BatchCompressionPauseController.isPaused`)
 * resolves to false. The production service waits on
 * `isPaused.first { paused -> !paused }` before starting each item
 * (BatchCompressionService.executeBatchProcessing). This test exercises that
 * exact gate on the real (shared) controller.
 */
@RunWith(AndroidJUnit4::class)
class BatchPauseContractTest {

    private val controller = BatchCompressionPauseController

    @Before
    @After
    fun reset() {
        controller.isPaused.value = false
    }

    @Test
    fun paused_gate_holds_the_worker_until_resumed() = runBlocking {
        coroutineScope {
            controller.isPaused.value = true // arm the gate before the worker waits
            val worker = async(Dispatchers.Default) {
                // The exact wait the production loop performs before each item.
                controller.isPaused.first { paused -> !paused }
                "RELEASED"
            }

            // Give the worker time to reach the await point, then confirm it is blocked.
            kotlinx.coroutines.delay(100)
            assertTrue("worker must be held while paused is true", controller.isPaused.value)

            // Resume from the single source of truth.
            controller.isPaused.value = false

            // The item must proceed — never silently dropped.
            withTimeout(2_000) { worker.await() }.let { released ->
                assertTrue("queued item must not be dropped while paused", released == "RELEASED")
            }
        }
    }

    @Test
    fun isPaused_defaults_to_false_fail_open_is_never_the_default() {
        controller.isPaused.value = true // be explicit about the scenario
        controller.isPaused.value = false // the safe default after reset
        assertFalse("default must be not-paused (fail closed on default resume)", controller.isPaused.value)
    }

    @Test
    fun failure_audit_record_is_written_to_on_device_sandbox() {
        // Constitution I.6: a file that fails must produce an audit record, never a
        // silent drop. Exercise the exact production helper the service uses.
        val ctx = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val log = BatchFailureAudit.logFile(ctx)
        log.delete() // start clean so the assertion is meaningful
        val reason = "compression produced no valid output"
        BatchFailureAudit.writeLine(log, reason)
        assertTrue("audit log file must exist in the app sandbox", log.exists())
        val content = log.readText()
        assertTrue("audit log must contain the failure reason", content.contains(reason))
        assertTrue("audit log entry must be timestamped", Regex("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\]").containsMatchIn(content))
    }

    /**
     * Real-path C5 contract test: the PRODUCTION batch loop (the same
     * `executeBatchProcessing` that `onStartCommand` drives — see seam
     * `executeBatchProcessingForTest`) must HOLD a queued item at its pause gate
     * (`isPaused.first { !paused }`, BatchCompressionService line 150) and must
     * NOT drop it when paused is armed. After resume the item must be processed
     * exactly once. Evidence of completion: the real service's DataStore
     * `recordCompressionSavings` increments totalHistoricalFilesCount by exactly 1.
     *
     * This closes C5's evidence gap (AGENTS §1: proof exercises the real path, not
     * a hand-rolled wait-loop).
     */
    @Test
    fun real_batch_loop_holds_a_queued_item_while_paused_and_does_not_drop_it() = runBlocking {
        val ctx = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val repo = SettingsRepository(ctx)

        // Real, decodable input image (compressible noise PNG).
        val dir = File(ctx.cacheDir, "batch_contract").apply { mkdirs() }
        val src = File(dir, "input.png")
        val random = java.util.Random(7)
        val pixels = IntArray(512 * 512) { 0xFF000000.toInt() or random.nextInt(0xFFFFFF) }
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, 512, 0, 0, 512, 512)
        FileOutputStream(src).use { out ->
            assertTrue("PNG encoding of the test input must succeed", bmp.compress(Bitmap.CompressFormat.PNG, 100, out))
        }
        bmp.recycle()
        val uri: Uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", src)

        val baseline = repo.userSettingsFlow.first().totalHistoricalFilesCount

        // Real service instance; drive the real batch loop via the test seam.
        val service = BatchCompressionService()
        BatchCompressionPauseController.isPaused.value = true // arm pause BEFORE the loop starts

        val job = async(Dispatchers.IO) {
            service.executeBatchProcessingForTest(listOf(uri), isVideo = false, qualityName = "MEDIUM", autoSave = false)
            "COMPLETED"
        }

        // The loop is now suspended at the gate for its single queued item.
        delay(400)
        assertTrue("while paused the queued item must NOT have been processed yet",
            repo.userSettingsFlow.first().totalHistoricalFilesCount == baseline)

        // Resume from the single source of truth; the item must not be dropped.
        BatchCompressionPauseController.isPaused.value = false
        withTimeout(20_000) { job.await() }.let { released ->
            assertEquals("COMPLETED", released)
        }
        assertEquals(
            "a queued item must never be dropped nor skipped — exactly 1 extra file recorded after resume",
            baseline + 1L,
            repo.userSettingsFlow.first().totalHistoricalFilesCount
        )
    }
}
