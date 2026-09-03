package com.shrinkmedia.compressor

import com.shrinkmedia.compressor.OnDeviceInferenceRepository.AiResult
import com.shrinkmedia.compressor.OnDeviceInferenceRepository.Status
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM unit tests for the on-device AI availability gate and its result contract (ADR-011).
 *
 * IMPORTANT (honesty guard): these tests exercise the DECISION LOGIC — what the repo does when the
 * platform probe reports each availability state — via the [statusOverride] injected probe. They do
 * NOT fabricate a model output: the real `Generation`/AICore inference path is only exercised on a
 * Nano-capable device (Layer 5, the hardware wall). Injecting the probe tests the branches on any
 * machine without faking the actual inference.
 */
class OnDeviceInferenceRepositoryUnitTest {

    @Test
    fun unavailableGateRefuses_AndReturnsExplicitUnavailable() = runBlocking {
        val result = OnDeviceInferenceRepository.summarize("summarize this", statusOverride = Status.UNAVAILABLE)
        assertTrue("must be an explicit Unavailable, never a silent swallow", result is AiResult.Unavailable)
    }

    @Test
    fun downloadableGateRefuses_NoOutputProduced() = runBlocking {
        val result = OnDeviceInferenceRepository.summarize("hi", statusOverride = Status.DOWNLOADABLE)
        assertTrue("model not downloaded => no output, no cloud fallback", result is AiResult.Unavailable)
    }

    @Test
    fun apiTooOldProducesClearError_NotCloud() = runBlocking {
        val result = OnDeviceInferenceRepository.summarize("hi", statusOverride = Status.API_TOO_OLD)
        assertTrue("API_TOO_OLD must produce a typed Error explaining the block", result is AiResult.Error)
        val error = result as AiResult.Error
        assertTrue("error must reference the API-26 requirement", error.reason.contains("26"))
    }

    @Test
    fun streamingUnavailableEmitsUnavailable() = runBlocking {
        val out = OnDeviceInferenceRepository.summarizeStream("hi", statusOverride = Status.UNAVAILABLE).toList()
        assertTrue(out.all { it is AiResult.Unavailable })
    }

    @Test
    fun minApiLevelIs26() {
        // Matches the ML Kit GenAI Prompt API's documented requirement (guards the overrideLibrary
        // rationale in the manifest — never load GenAI below this).
        assertEquals(26, OnDeviceInferenceRepository.MIN_API_LEVEL)
    }
}