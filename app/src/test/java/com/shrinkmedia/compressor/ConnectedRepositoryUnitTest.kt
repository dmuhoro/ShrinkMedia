package com.shrinkmedia.compressor

import com.shrinkmedia.compressor.ConnectedRepository.ConnectResult
import com.shrinkmedia.compressor.ConnectedRepository.ModeState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM unit tests for the Connected-mode fail-closed gate (ADR-012/013).
 *
 * HONESTY GUARD: these test the DECISION LOGIC of the gate — what the repository does for each
 * combination of settings and invocation — exactly as [OnDeviceInferenceRepositoryUnitTest] tests
 * its availability branches. They do NOT fabricate a network call: there is no connected action in
 * the offline build, and no real connect work exists to fake. What is proven on the real gate path
 * is: OFF => never invokes the block; not-consented => refuses; not-explicit => refuses; and only a
 * passing combination runs the injected block (the seam the L2 MCP actions will use).
 */
class ConnectedRepositoryUnitTest {

    @Test
    fun offByDefault_Refuses_AndDoesNotRunBlock() = runBlocking {
        var ran = false
        val result = ConnectedRepository.run(
            connectedModeEnabled = false,
            consentShown = false,
            explicitlyInvoked = true
        ) {
            ran = true
            "should never run"
        }
        assertTrue("Connected mode OFF (the default) must refuse", result is ConnectResult.Off)
        assertTrue("the action block must never execute when OFF", !ran)
    }

    @Test
    fun enabledButNoConsent_Refused_BlockNotRun() = runBlocking {
        var ran = false
        val result = ConnectedRepository.run(
            connectedModeEnabled = true,
            consentShown = false,
            explicitlyInvoked = true
        ) { ran = true; "nope" }
        assertTrue("must refuse when disclosure not acknowledged", result is ConnectResult.Refused)
        assertTrue("block must not run without consent", !ran)
    }

    @Test
    fun enabledAndConsented_ButNotExplicitlyInvoked_Refused() = runBlocking {
        var ran = false
        val result = ConnectedRepository.run(
            connectedModeEnabled = true,
            consentShown = true,
            explicitlyInvoked = false
        ) { ran = true; "nope" }
        assertTrue("must refuse a non-explicit/silent action", result is ConnectResult.Refused)
        assertTrue("block must not run for a silent action", !ran)
    }

    @Test
    fun enabledConsentedAndExplicitlyInvoked_Allows_AndRunsBlock() = runBlocking {
        val result = ConnectedRepository.run(
            connectedModeEnabled = true,
            consentShown = true,
            explicitlyInvoked = true
        ) { "vault.put: transfer complete" }
        assertTrue("must allow when every condition holds", result is ConnectResult.Allowed)
        assertEquals("vault.put: transfer complete", (result as ConnectResult.Allowed).outcome)
    }

    @Test
    fun blockThrows_SurfacesTypedError_NotSilent() = runBlocking {
        val result = ConnectedRepository.run(
            connectedModeEnabled = true,
            consentShown = true,
            explicitlyInvoked = true
        ) { throw RuntimeException("backend down") }
        assertTrue("an exception in the action must surface as a typed error", result is ConnectResult.Error)
    }

    @Test
    fun modeState_OffWhenDisabled_EvenIfConsentShown() {
        assertEquals(ModeState.OFF, ConnectedRepository.modeState(false, true))
        assertEquals(ModeState.OFF, ConnectedRepository.modeState(false, false))
    }

    @Test
    fun modeState_ConsentRequired_WhenEnabledButNotAcknowledged() {
        assertEquals(ModeState.CONSENT_REQUIRED, ConnectedRepository.modeState(true, false))
    }

    @Test
    fun modeState_On_OnlyWhenEnabledAndConsentShown() {
        assertEquals(ModeState.ON, ConnectedRepository.modeState(true, true))
    }
}
