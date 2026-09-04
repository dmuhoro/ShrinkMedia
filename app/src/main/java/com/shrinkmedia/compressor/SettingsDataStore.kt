package com.shrinkmedia.compressor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class PersistedUserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val autoSaveToMediaStore: Boolean = false,
    val pauseCompressionOnLowBattery: Boolean = false,
    val imageQuality: CompressionQuality = CompressionQuality.MEDIUM,
    val videoQuality: CompressionQuality = CompressionQuality.MEDIUM,
    val ocrLanguage: String = OcrLanguage.ENGLISH.key,
    val enableBatch: Boolean = false,
    val onboardingDismissed: Boolean = false,
    val connectedMode: Boolean = false,
    val connectedConsentShown: Boolean = false,
    val totalHistoricalSavedBytes: Long = 0L,
    val totalHistoricalFilesCount: Long = 0L
)

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_SAVE = booleanPreferencesKey("auto_save_mediastore")
        val PAUSE_ON_LOW_BATTERY = booleanPreferencesKey("pause_compression_on_low_battery")
        val IMAGE_QUALITY = stringPreferencesKey("image_quality")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val OCR_LANGUAGE = stringPreferencesKey("ocr_language")
        val ENABLE_BATCH = booleanPreferencesKey("enable_batch")
        val ONBOARDING_DISMISSED = booleanPreferencesKey("onboarding_dismissed")
        val CONNECTED_MODE = booleanPreferencesKey("connected_mode")
        val CONNECTED_CONSENT_SHOWN = booleanPreferencesKey("connected_consent_shown")
        val TOTAL_SAVED_BYTES = longPreferencesKey("total_saved_bytes")
        val TOTAL_FILES_COUNT = longPreferencesKey("total_files_count")
    }

    val userSettingsFlow: Flow<PersistedUserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
            val themeMode = try { AppThemeMode.valueOf(themeStr) } catch (e: Exception) { AppThemeMode.SYSTEM }

            val autoSave = preferences[PreferencesKeys.AUTO_SAVE] ?: false
            val pauseOnLowBattery = preferences[PreferencesKeys.PAUSE_ON_LOW_BATTERY] ?: false

            val imgQStr = preferences[PreferencesKeys.IMAGE_QUALITY] ?: CompressionQuality.MEDIUM.name
            val imgQuality = try { CompressionQuality.valueOf(imgQStr) } catch (e: Exception) { CompressionQuality.MEDIUM }

            val vidQStr = preferences[PreferencesKeys.VIDEO_QUALITY] ?: CompressionQuality.MEDIUM.name
            val vidQuality = try { CompressionQuality.valueOf(vidQStr) } catch (e: Exception) { CompressionQuality.MEDIUM }

            val ocrLang = preferences[PreferencesKeys.OCR_LANGUAGE] ?: OcrLanguage.ENGLISH.key
            val ocrLanguage = try { OcrLanguage.fromKey(ocrLang).key } catch (e: Exception) { OcrLanguage.ENGLISH.key }

            val enableBatch = preferences[PreferencesKeys.ENABLE_BATCH] ?: false
            val onboardingDismissed = preferences[PreferencesKeys.ONBOARDING_DISMISSED] ?: false

            // Connected mode (ADR-012/013): additive, OFF-by-default (fail closed). Nothing
            // may assume this is enabled; the value defaults to false and every connected
            // action is gated on both this flag AND an explicit per-action invocation.
            val connectedMode = preferences[PreferencesKeys.CONNECTED_MODE] ?: false
            val connectedConsentShown = preferences[PreferencesKeys.CONNECTED_CONSENT_SHOWN] ?: false

            val savedBytes = preferences[PreferencesKeys.TOTAL_SAVED_BYTES] ?: 0L
            val filesCount = preferences[PreferencesKeys.TOTAL_FILES_COUNT] ?: 0L

            PersistedUserSettings(
                themeMode = themeMode,
                autoSaveToMediaStore = autoSave,
                pauseCompressionOnLowBattery = pauseOnLowBattery,
                imageQuality = imgQuality,
                videoQuality = vidQuality,
                ocrLanguage = ocrLanguage,
                enableBatch = enableBatch,
                onboardingDismissed = onboardingDismissed,
                connectedMode = connectedMode,
                connectedConsentShown = connectedConsentShown,
                totalHistoricalSavedBytes = savedBytes,
                totalHistoricalFilesCount = filesCount
            )
        }

    suspend fun updateThemeMode(themeMode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updateAutoSave(autoSave: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE] = autoSave
        }
    }

    suspend fun updatePauseCompressionOnLowBattery(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PAUSE_ON_LOW_BATTERY] = enabled
        }
    }

    suspend fun updateOcrLanguage(ocrLanguage: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OCR_LANGUAGE] = ocrLanguage
        }
    }

    suspend fun updateEnableBatch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_BATCH] = enabled
        }
    }

    suspend fun updateOnboardingDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_DISMISSED] = dismissed
        }
    }

    /**
     * Sets whether Connected mode is enabled. Additive + fail-closed (ADR-012/013). The UI must
     * gate any call to `true` behind the explicit privacy disclosure ([updateConnectedConsentShown])
     * on first enable — the repository never enables it on its own.
     */
    suspend fun updateConnectedMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONNECTED_MODE] = enabled
        }
    }

    /** Records that the Connected-mode privacy disclosure has been acknowledged (first-run consent). */
    suspend fun updateConnectedConsentShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONNECTED_CONSENT_SHOWN] = shown
        }
    }

    suspend fun updateImageQuality(quality: CompressionQuality) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IMAGE_QUALITY] = quality.name
        }
    }

    suspend fun updateVideoQuality(quality: CompressionQuality) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIDEO_QUALITY] = quality.name
        }
    }

    suspend fun recordCompressionSavings(savedBytes: Long) {
        context.dataStore.edit { preferences ->
            val currentBytes = preferences[PreferencesKeys.TOTAL_SAVED_BYTES] ?: 0L
            val currentCount = preferences[PreferencesKeys.TOTAL_FILES_COUNT] ?: 0L
            preferences[PreferencesKeys.TOTAL_SAVED_BYTES] = currentBytes + maxOf(0L, savedBytes)
            preferences[PreferencesKeys.TOTAL_FILES_COUNT] = currentCount + 1L
        }
    }
}
