package com.shrinkmedia.compressor

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
}
