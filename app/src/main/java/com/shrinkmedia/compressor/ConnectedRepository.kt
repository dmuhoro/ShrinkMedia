package com.shrinkmedia.compressor

/**
 * Fail-closed enforcement seam for every "connected" action (ADR-012 / ADR-013).
 *
 * Connected mode is the opt-in bridge to the personal ecosystem (DataBank portal, cloud AI,
 * Google Bridge, future MCP `vault.*` tools). The default build ships with Connected mode OFF and
 * NO INTERNET permission; this gate is the single chokepoint every future connected action must
 * pass through so that nothing ever connects unless the user both enabled Connected mode AND
 * explicitly invoked that specific action at run time.
 *
 * HONESTY GUARD (no false confidence): today there are ZERO connected actions in the offline
 * build, so this gate does not claim to protect live traffic — there is no live traffic. What it
 * provides and what the JVM tests PROVE is the fail-closed decision logic: an action is refused
 * (a typed result, never a silent drop) unless Connected mode is ON, the disclosure has been
 * acknowledged, and the action was explicitly invoked. The actual downstream work is injected via
 * [block] and only runs after the gate passes, which is the path the L2 MCP actions will take.
 */
object ConnectedRepository {

    /** Best-effort capability state for the UI (pure decision, no I/O). */
    enum class ModeState {
        /** Connected mode is OFF (the default) — nothing connects, nothing leaves the device. */
        OFF,
        /** The user wants to enable Connected mode but has not yet acknowledged the disclosure. */
        CONSENT_REQUIRED,
        /** Connected mode is ON and the disclosure has been acknowledged. */
        ON
    }

    /** Human-readable state for the UI, derived from the two fail-closed settings. */
    fun modeState(connectedModeEnabled: Boolean, consentShown: Boolean): ModeState = when {
        !connectedModeEnabled -> ModeState.OFF
        !consentShown -> ModeState.CONSENT_REQUIRED
        else -> ModeState.ON
    }

    /** Typed result — a rejected action is never swallowed (no silent drop). */
    sealed class ConnectResult {
        /** The action ran and completed (outcome string describes what happened). */
        data class Allowed(val outcome: String) : ConnectResult()

        /** Connected mode is OFF — refused without invoking [block]. */
        object Off : ConnectResult()

        /** The disclosure has not been acknowledged — refused (fail-closed, ADR-012). */
        data class Refused(val reason: String) : ConnectResult()

        /** The action itself threw while running. */
        data class Error(val reason: String) : ConnectResult()
    }

    /**
     * The enforcement gate. [block] is invoked ONLY when every condition holds; otherwise it is
     * never called and a typed refusal is returned.
     *
     * @param connectedModeEnabled the additive setting (default false).
     * @param consentShown         whether the first-run disclosure was acknowledged.
     * @param explicitlyInvoked    must be true — the user invoked THIS action at run time; there is
     *                             no silent/automatic fallback to any cloud or self-host path.
     */
    suspend fun run(
        connectedModeEnabled: Boolean,
        consentShown: Boolean,
        explicitlyInvoked: Boolean,
        block: suspend () -> String
    ): ConnectResult {
        if (!connectedModeEnabled) return ConnectResult.Off
        if (!consentShown) {
            return ConnectResult.Refused("Connected mode disclosure has not been acknowledged. Read and accept it first.")
        }
        if (!explicitlyInvoked) {
            return ConnectResult.Refused("Action must be invoked explicitly by you; no silent cloud/self-host fallback is permitted.")
        }
        return try {
            ConnectResult.Allowed(block())
        } catch (e: Exception) {
            ConnectResult.Error(e.message ?: "Connected action failed.")
        }
    }
}
