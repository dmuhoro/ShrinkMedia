package com.shrinkmedia.compressor

import android.os.Build
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * On-device AI surface backed by ML Kit GenAI (Gemini Nano via Android AICore) per ADR-011.
 *
 * Hard guarantees (fail-closed, no false confidence):
 *  - Runs inference **on-device only**. Never implicitly falls back to cloud (that is ADR-012's
 *    separate, opt-in "Connected mode"); if AICore/Nano is unavailable this returns an explicit
 *    [AiResult.Unavailable] / null and surfaces that state — it does NOT silently upload.
 *  - No INTERNET permission is added by this code path; the merged-manifest
 *    tools:node="remove" guard + CI step strip any library-provided INTERNET.
 *  - GenAI requires API 26+; on API 24-25 this short-circuits to [Status.API_TOO_OLD] without
 *    ever touching the framework (the real call is guarded by [Build.VERSION.SDK_INT]).
 *  - AICore is device-gated: not every handset has Gemini Nano. Availability MUST be probed at
 *    runtime ([checkStatus]) before any inference; the outcome is surfaced honestly to the UI.
 */
object OnDeviceInferenceRepository {

    /** Typed availability gate so callers can render honest states without guessing. */
    enum class Status {
        /** Gemini Nano downloaded and ready. Run [summarize] / [summarizeStream]. */
        AVAILABLE,
        /** Device supports Nano but the model is not downloaded yet. Show "download to enable". */
        DOWNLOADABLE,
        /** Model is currently being downloaded by AICore. */
        DOWNLOADING,
        /** Device/OS/build does not support Gemini Nano here. Refuse (fail-closed). */
        UNAVAILABLE,
        /** GenAI Prompt API requires API 26+; this device is older. Refuse with a clear reason. */
        API_TOO_OLD
    }

    /** The minimum Android API level required by the ML Kit GenAI Prompt API. */
    const val MIN_API_LEVEL: Int = 26

    sealed class AiResult {
        /** A genuinely produced on-device answer. */
        data class Text(val content: String) : AiResult()

        /** AICore/Nano is not usable right now; no output was produced (nothing uploaded). */
        object Unavailable : AiResult()

        /** The request failed (e.g. quota/busy/model error). No output, explicitly surfaced. */
        data class Error(val reason: String) : AiResult()
    }

    /**
     * Availability probe. The [statusOverride] parameter is for deterministic JVM unit tests only
     * (a pure launcher that never touches the Android framework); production callers omit it and
     * get the real GenAI `checkStatus()` result.
     */
    suspend fun checkStatus(statusOverride: Status? = null): Status {
        statusOverride?.let { return it }
        // Fail closed on OS versions that cannot run GenAI at all.
        if (Build.VERSION.SDK_INT < MIN_API_LEVEL) return Status.API_TOO_OLD
        return try {
            // Probing AICore binds the service; keep it off the main thread so a capable
            // device's availability check never degrades the UI.
            withContext(Dispatchers.IO) {
                when (Generation.getClient().checkStatus()) {
                    FeatureStatus.AVAILABLE -> Status.AVAILABLE
                    FeatureStatus.DOWNLOADABLE -> Status.DOWNLOADABLE
                    FeatureStatus.DOWNLOADING -> Status.DOWNLOADING
                    else -> Status.UNAVAILABLE
                }
            }
        } catch (_: Exception) {
            // Connecting to AICore can fail transiently (binding, setup). Treat as unavailable —
            // never surface a crash, never fall to cloud.
            Status.UNAVAILABLE
        }
    }

    /**
     * Summarise text fully on-device. Returns an explicit [AiResult] so no-silent-drop holds:
     * unavailable/errors surface as typed failures rather than swallowed nulls.
     */
    suspend fun summarize(prompt: String, statusOverride: Status? = null): AiResult {
        val status = checkStatus(statusOverride)
        if (status != Status.AVAILABLE) {
            return if (status == Status.API_TOO_OLD) {
                AiResult.Error("On-device AI needs Android 8.0+ (API 26).")
            } else {
                AiResult.Unavailable
            }
        }
        return runOnDevice(prompt)
    }

    /** Runs ONE inference via AICore, suspending on the real call. Coarse error mapping to [AiResult.Error].
     *  Runs on a background dispatcher so a model query never blocks/degrades the UI thread. */
    private suspend fun runOnDevice(prompt: String): AiResult = try {
        val text = withContext(Dispatchers.IO) {
            val response = Generation.getClient().generateContent(prompt)
            response.candidates?.firstOrNull()?.text
        }
        if (text.isNullOrBlank()) AiResult.Unavailable
        else AiResult.Text(text.trim())
    } catch (e: Exception) {
        // Quota (BUSY) / battery-quota / model binding errors are common on real devices and
        // must be surfaced, not swallowed (AGENTS/Constitution no-silent-drop).
        AiResult.Error(e.message ?: "On-device model request failed.")
    }

    /**
     * Streaming inference. Each emission is a delta chunk appended by the caller. Empty / failure
     * yields an upstream [AiResult.Error] so failures are never silent.
     */
    fun summarizeStream(prompt: String, statusOverride: Status? = null): Flow<AiResult> = flow {
        val status = checkStatus(statusOverride)
        if (status != Status.AVAILABLE) {
            emit(if (status == Status.API_TOO_OLD) AiResult.Error("On-device AI needs Android 8.0+ (API 26).") else AiResult.Unavailable)
            return@flow
        }
        try {
            val sb = StringBuilder()
            Generation.getClient().generateContentStream(prompt).collect { chunk ->
                val delta = chunk.candidates?.firstOrNull()?.text.orEmpty()
                sb.append(delta)
                emit(AiResult.Text(sb.toString()))
            }
            if (sb.isEmpty()) emit(AiResult.Unavailable)
        } catch (e: Exception) {
            emit(AiResult.Error(e.message ?: "On-device model stream failed."))
        }
    }.flowOn(Dispatchers.IO)
}