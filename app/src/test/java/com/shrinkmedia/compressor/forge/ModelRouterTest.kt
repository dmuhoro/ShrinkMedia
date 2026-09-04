package com.shrinkmedia.compressor.forge

import com.shrinkmedia.compressor.forge.ModelRouter.route
import com.shrinkmedia.compressor.forge.RouterDecision.AllowedLocal
import com.shrinkmedia.compressor.forge.RouterDecision.AllowedRemote
import com.shrinkmedia.compressor.forge.RouterDecision.Off
import com.shrinkmedia.compressor.forge.RouterDecision.Refused
import com.shrinkmedia.compressor.forge.RouterDecision.Unavailable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelRouterTest {

    // ---- precondition gates (fail-closed, mirror ConnectedRepository) ----

    @Test
    fun offByDefault_Refuses() {
        val d = route(false, true, true, online = true, availableRemote = listOf("qwen"), availableLocal = listOf("nano"))
        assertTrue(d is Off)
        assertFalse(d.allowed)
    }

    @Test
    fun enabled_NoConsent_Refused() {
        val d = route(true, false, true, online = true, availableRemote = listOf("qwen"), availableLocal = listOf("nano"))
        assertTrue(d is Refused)
        assertFalse(d.allowed)
    }

    @Test
    fun enabledConsented_NotExplicit_Refused() {
        val d = route(true, true, false, online = true, availableRemote = listOf("qwen"), availableLocal = listOf("nano"))
        assertTrue(d is Refused)
    }

    // ---- routing edge: online → open-weight remote; offline → local fallback ----

    @Test
    fun online_RoutesToOpenWeightRemote_NotLocal() {
        val d = route(true, true, true, online = true, availableRemote = listOf("qwen-2.5-70b"), availableLocal = listOf("gemini-nano"))
        assertTrue(d is AllowedRemote)
        assertEquals("qwen-2.5-70b", (d as AllowedRemote).model)
    }

    @Test
    fun online_PrefersRequestedRemoteWhenAvailable() {
        val d = route(
            true, true, true, online = true,
            availableRemote = listOf("llama-3.1-70b", "qwen-2.5-70b"),
            availableLocal = listOf("nano"),
            preferredRemote = "qwen-2.5-70b",
        )
        assertEquals("qwen-2.5-70b", (d as AllowedRemote).model)
    }

    @Test
    fun offline_FallsBackToLocal() {
        val d = route(true, true, true, online = false, availableRemote = listOf("qwen"), availableLocal = listOf("gemini-nano"))
        assertTrue(d is AllowedLocal)
        assertEquals("gemini-nano", (d as AllowedLocal).model)
    }

    @Test
    fun offline_PrefersRequestedLocalWhenAvailable() {
        val d = route(
            true, true, true, online = false,
            availableRemote = listOf("qwen"), availableLocal = listOf("nano", "ollama:llama3.1"),
            preferredLocal = "ollama:llama3.1",
        )
        assertEquals("ollama:llama3.1", (d as AllowedLocal).model)
    }

    @Test
    fun online_ButNoRemoteModel_Unavailable_WithReason_NotSilent() {
        val d = route(true, true, true, online = true, availableRemote = emptyList(), availableLocal = listOf("nano"))
        assertTrue(d is Unavailable)
        assertTrue((d as Unavailable).reason.isNotBlank())
        assertFalse(d.allowed)
    }

    @Test
    fun offline_ButNoLocalModel_Unavailable_WithReason() {
        val d = route(true, true, true, online = false, availableRemote = listOf("qwen"), availableLocal = emptyList())
        assertTrue(d is Unavailable)
        assertTrue((d as Unavailable).reason.isNotBlank())
    }

    @Test
    fun everyDecisionIsExhaustiveTypedResult_NeverNull() {
        val offlineDefault = route(false, false, false, online = false)
        assertTrue(offlineDefault is Off)
    }

    // ---- invariant: default build has no notion of connectivity until the gate passes ----

    @Test
    fun gateChecksRunBeforeConnectivity_EvenWhenOnlineFlagIsTrue() {
        // Preconditions dominate: OFF beats "online" every time.
        val d = route(false, false, false, online = true, availableRemote = listOf("qwen"), availableLocal = listOf("nano"))
        assertTrue(d is Off)
    }
}