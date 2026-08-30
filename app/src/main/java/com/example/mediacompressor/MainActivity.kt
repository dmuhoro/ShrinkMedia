package com.example.mediacompressor

import android.Manifest
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.SessionState
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

// -----------------------------------------------------------------------------------------
// DATA MODELS & ENUMS
// -----------------------------------------------------------------------------------------
enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class CompressionQuality(val label: String, val imageQuality: Int, val maxDimension: Int, val videoCrf: Int, val videoBitrate: String) {
    LOW(label = "Low (Max Savings)", imageQuality = 55, maxDimension = 1280, videoCrf = 32, videoBitrate = "800k"),
    MEDIUM(label = "Medium (Balanced)", imageQuality = 75, maxDimension = 1920, videoCrf = 28, videoBitrate = "1500k"),
    HIGH(label = "High (Best Quality)", imageQuality = 90, maxDimension = 2560, videoCrf = 23, videoBitrate = "2500k")
}

data class CompressedItem(
    val id: String = UUID.randomUUID().toString(),
    val originalName: String,
    val originalSize: Long,
    val compressedSize: Long,
    val file: File,
    val isVideo: Boolean,
    val timeTakenMs: Long = 0,
    val savedToMediaStore: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class PendingDeletion(
    val item: CompressedItem,
    val originalIndex: Int,
    val originalFileLocation: File,
    val trashFileLocation: File
)

data class CompressionAuditLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val fileName: String,
    val mediaType: String,
    val originalSize: Long,
    val compressedSize: Long,
    val reductionPercent: Int,
    val durationMs: Long,
    val qualityPreset: String,
    val status: String,
    val targetBitrate: String = "N/A",
    val resolutionScaling: String = "N/A",
    val crfOrQuality: String = "N/A",
    val codec: String = "N/A",
    val savedToMediaStore: Boolean = false
)

data class BatchQueueItem(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val name: String,
    val size: Long,
    val formattedSize: String = "0 B",
    val dimensions: String = "N/A",
    val mimeType: String = "media/*",
    val durationText: String? = null,
    val isVideo: Boolean = false,
    val preCheckPassed: Boolean = true
)

data class CompressorUiState(
    val isLoading: Boolean = false,
    val currentAction: String = "",
    val statusMessage: String = "Ready. Select an image or video to compress.",
    val fileProgressPercent: Float = 0f,
    val currentResult: CompressedItem? = null,
    val recentItems: List<CompressedItem> = emptyList(),
    // Pending Deletion & Trash State
    val pendingDeletion: PendingDeletion? = null,
    // Quality & Batch State
    val imageQuality: CompressionQuality = CompressionQuality.MEDIUM,
    val videoQuality: CompressionQuality = CompressionQuality.MEDIUM,
    val isBatchMode: Boolean = false,
    val batchTotalCount: Int = 0,
    val batchCurrentIndex: Int = 0,
    // Batch Preview Modal State
    val pendingBatchQueue: List<BatchQueueItem> = emptyList(),
    val showBatchPreviewModal: Boolean = false,
    val batchIsVideo: Boolean = false,
    // DataStore Persistent Settings
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val autoSaveToMediaStore: Boolean = false,
    val historicalSavedBytes: Long = 0L,
    val historicalFilesCount: Long = 0L,
    // Audit Logs
    val auditLogs: List<CompressionAuditLog> = emptyList()
)

sealed class CompressorEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null, val onAction: (() -> Unit)? = null) : CompressorEvent()
    object TriggerSuccessHaptic : CompressorEvent()
}

// -----------------------------------------------------------------------------------------
// VIEWMODEL WITH DATASTORE PERSISTENCE & TRASH / PENDING DELETION SYSTEM
// -----------------------------------------------------------------------------------------
class CompressorViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application.applicationContext)
    private val trashFolder = File(application.applicationContext.cacheDir, "trash").apply {
        if (!exists()) mkdirs()
    }

    private var pendingDeletionJob: Job? = null

    private val _uiState = MutableStateFlow(CompressorUiState())
    val uiState: StateFlow<CompressorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CompressorEvent>()
    val events: SharedFlow<CompressorEvent> = _events.asSharedFlow()

    // Task 3: StateFlow that aggregates (originalSize - compressedSize) from all audit logs
    val totalSpaceSaved: StateFlow<Long> = _uiState.map { state ->
        val fromAuditLogs = state.auditLogs
            .filter { it.status == "SUCCESS" }
            .sumOf { maxOf(0L, it.originalSize - it.compressedSize) }
        
        val fromRecent = state.recentItems.sumOf { maxOf(0L, it.originalSize - it.compressedSize) }
        
        // Take maximum of logged audit savings, current session items, or persistent historical saved bytes
        maxOf(fromAuditLogs, fromRecent, state.historicalSavedBytes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    init {
        // Collect persistent settings from Jetpack DataStore (Task 4)
        viewModelScope.launch {
            settingsRepo.userSettingsFlow.collect { persisted ->
                _uiState.update { state ->
                    state.copy(
                        themeMode = persisted.themeMode,
                        autoSaveToMediaStore = persisted.autoSaveToMediaStore,
                        imageQuality = persisted.imageQuality,
                        videoQuality = persisted.videoQuality,
                        historicalSavedBytes = persisted.totalHistoricalSavedBytes,
                        historicalFilesCount = persisted.totalHistoricalFilesCount
                    )
                }
            }
        }
    }

    fun updateImageQuality(quality: CompressionQuality) {
        _uiState.update { it.copy(imageQuality = quality) }
        viewModelScope.launch { settingsRepo.updateImageQuality(quality) }
    }

    fun updateVideoQuality(quality: CompressionQuality) {
        _uiState.update { it.copy(videoQuality = quality) }
        viewModelScope.launch { settingsRepo.updateVideoQuality(quality) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        viewModelScope.launch { settingsRepo.updateThemeMode(mode) }
    }

    fun toggleAutoSaveToMediaStore(enabled: Boolean) {
        _uiState.update { it.copy(autoSaveToMediaStore = enabled) }
        viewModelScope.launch { settingsRepo.updateAutoSave(enabled) }
    }

    fun toggleBatchMode(enabled: Boolean) {
        _uiState.update { it.copy(isBatchMode = enabled) }
    }

    fun processImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            processSingleMedia(context, uri, isVideo = false)
        }
    }

    fun processVideo(context: Context, uri: Uri) {
        viewModelScope.launch {
            processSingleMedia(context, uri, isVideo = true)
        }
    }

    // Pre-compression check & Batch Preview Queue Management
    fun prepareBatchQueue(context: Context, uris: List<Uri>, isVideo: Boolean) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val queueItems = uris.map { uri ->
                val size = getFileSizeFromUri(context, uri)
                val name = getFileNameFromUri(context, uri) ?: if (isVideo) "video_${System.currentTimeMillis()}.mp4" else "image_${System.currentTimeMillis()}.jpg"
                val meta = getMediaMetadataInfo(context, uri, isVideo)
                BatchQueueItem(
                    uri = uri,
                    name = name,
                    size = size,
                    formattedSize = formatFileSize(size),
                    dimensions = meta.first,
                    mimeType = meta.third,
                    durationText = meta.second,
                    isVideo = isVideo,
                    preCheckPassed = size > 0L
                )
            }
            _uiState.update {
                it.copy(
                    pendingBatchQueue = queueItems,
                    showBatchPreviewModal = true,
                    batchIsVideo = isVideo
                )
            }
        }
    }

    fun removeFromBatchQueue(itemId: String) {
        _uiState.update { state ->
            val updated = state.pendingBatchQueue.filterNot { it.id == itemId }
            state.copy(
                pendingBatchQueue = updated,
                showBatchPreviewModal = updated.isNotEmpty()
            )
        }
    }

    fun dismissBatchPreview() {
        _uiState.update { it.copy(showBatchPreviewModal = false, pendingBatchQueue = emptyList()) }
    }

    fun confirmAndStartBatch(context: Context) {
        val items = _uiState.value.pendingBatchQueue
        val isVideo = _uiState.value.batchIsVideo
        _uiState.update { it.copy(showBatchPreviewModal = false, pendingBatchQueue = emptyList()) }
        if (items.isNotEmpty()) {
            processBatchQueue(context, items.map { it.uri }, isVideo)
        }
    }

    // Process Batch Queue with Foreground Service Integration & Granular Progress Tracking
    fun processBatchQueue(context: Context, uris: List<Uri>, isVideo: Boolean) {
        if (uris.isEmpty()) return

        // Launch Foreground Service for background execution & notification updates
        val qualityName = if (isVideo) _uiState.value.videoQuality.name else _uiState.value.imageQuality.name
        val autoSave = _uiState.value.autoSaveToMediaStore

        BatchCompressionService.startBatch(
            context = context,
            uris = uris,
            isVideo = isVideo,
            qualityName = qualityName,
            autoSave = autoSave
        )

        viewModelScope.launch {
            val total = uris.size
            _uiState.update {
                it.copy(
                    isLoading = true,
                    batchTotalCount = total,
                    batchCurrentIndex = 0,
                    fileProgressPercent = 0f,
                    currentAction = "Batch Processing (0/$total)...",
                    statusMessage = "Foreground Service processing $total ${if (isVideo) "videos" else "images"}."
                )
            }

            var successCount = 0
            val processedItems = mutableListOf<CompressedItem>()

            uris.forEachIndexed { index, uri ->
                val currentNum = index + 1
                _uiState.update {
                    it.copy(
                        batchCurrentIndex = currentNum,
                        fileProgressPercent = 0f,
                        currentAction = "Compressing $currentNum of $total ${if (isVideo) "videos" else "images"}...",
                        statusMessage = "Encoding item $currentNum/$total in background..."
                    )
                }

                try {
                    val result = processSingleMediaInternal(context, uri, isVideo)
                    if (result != null) {
                        successCount++
                        processedItems.add(result)
                    }
                } catch (e: Exception) {
                    val fileName = getFileNameFromUri(context, uri) ?: "file_$currentNum"
                    _events.emit(
                        CompressorEvent.ShowSnackbar(
                            message = "Batch item \"$fileName\" failed: ${e.localizedMessage ?: "Engine error"}",
                            actionLabel = "Retry",
                            onAction = { viewModelScope.launch { processSingleMedia(context, uri, isVideo) } }
                        )
                    )
                }
            }

            _uiState.update { state ->
                val updatedRecent = (processedItems + state.recentItems).take(30)
                state.copy(
                    isLoading = false,
                    batchTotalCount = 0,
                    batchCurrentIndex = 0,
                    fileProgressPercent = 100f,
                    currentResult = processedItems.firstOrNull(),
                    recentItems = updatedRecent,
                    statusMessage = "Batch compression complete: $successCount of $total succeeded."
                )
            }

            _events.emit(CompressorEvent.TriggerSuccessHaptic)
            _events.emit(
                CompressorEvent.ShowSnackbar("Batch finished: $successCount of $total files compressed.")
            )
        }
    }

    private suspend fun processSingleMedia(context: Context, uri: Uri, isVideo: Boolean) {
        val originalSize = getFileSizeFromUri(context, uri)
        val originalName = getFileNameFromUri(context, uri) ?: if (isVideo) "video_${System.currentTimeMillis()}.mp4" else "image_${System.currentTimeMillis()}.jpg"

        if (originalSize <= 0) {
            _events.emit(
                CompressorEvent.ShowSnackbar(
                    message = "Unable to read file \"$originalName\".",
                    actionLabel = "Retry",
                    onAction = { viewModelScope.launch { processSingleMedia(context, uri, isVideo) } }
                )
            )
            return
        }

        val quality = if (isVideo) _uiState.value.videoQuality else _uiState.value.imageQuality

        _uiState.update {
            it.copy(
                isLoading = true,
                fileProgressPercent = 0f,
                currentAction = "Compressing ${if (isVideo) "Video" else "Image"} (${quality.label})...",
                statusMessage = "Original: ${formatFileSize(originalSize)} • Encoding...",
                currentResult = null
            )
        }

        try {
            val item = processSingleMediaInternal(context, uri, isVideo)

            if (item != null) {
                val reduction = if (originalSize > 0) {
                    val saved = 100 - ((item.compressedSize.toDouble() / item.originalSize) * 100).roundToInt()
                    "$saved% space saved"
                } else "Optimized"

                val mediaStoreNotice = if (item.savedToMediaStore) " • Saved to ${if (isVideo) "Movies" else "Pictures"}" else ""

                _uiState.update { state ->
                    val updatedRecent = (listOf(item) + state.recentItems).take(30)
                    state.copy(
                        isLoading = false,
                        fileProgressPercent = 100f,
                        currentResult = item,
                        recentItems = updatedRecent,
                        statusMessage = "${if (isVideo) "Video" else "Image"} compressed! ($reduction)$mediaStoreNotice"
                    )
                }
                _events.emit(CompressorEvent.TriggerSuccessHaptic)
            } else {
                _uiState.update { it.copy(isLoading = false, fileProgressPercent = 0f, statusMessage = "Compression failed.") }
                _events.emit(
                    CompressorEvent.ShowSnackbar(
                        message = "Compression failed for \"$originalName\".",
                        actionLabel = "Retry",
                        onAction = { viewModelScope.launch { processSingleMedia(context, uri, isVideo) } }
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(isLoading = false, fileProgressPercent = 0f, statusMessage = "Engine error: ${e.localizedMessage}") }
            _events.emit(
                CompressorEvent.ShowSnackbar(
                    message = "Engine exception on \"$originalName\": ${e.localizedMessage ?: "Engine error"}",
                    actionLabel = "Retry",
                    onAction = { viewModelScope.launch { processSingleMedia(context, uri, isVideo) } }
                )
            )
        }
    }

    private suspend fun processSingleMediaInternal(context: Context, uri: Uri, isVideo: Boolean): CompressedItem? {
        val startTime = System.currentTimeMillis()
        val originalSize = getFileSizeFromUri(context, uri)
        val originalName = getFileNameFromUri(context, uri) ?: if (isVideo) "video_${System.currentTimeMillis()}.mp4" else "image_${System.currentTimeMillis()}.jpg"
        val quality = if (isVideo) _uiState.value.videoQuality else _uiState.value.imageQuality

        val compressedFile = if (isVideo) {
            compressVideoFile(context, uri, _uiState.value.videoQuality) { progress ->
                _uiState.update { it.copy(fileProgressPercent = progress) }
            }
        } else {
            // Simulated granular image processing steps
            _uiState.update { it.copy(fileProgressPercent = 30f) }
            val result = compressImageFile(context, uri, _uiState.value.imageQuality)
            _uiState.update { it.copy(fileProgressPercent = 100f) }
            result
        }

        val duration = System.currentTimeMillis() - startTime

        val targetBitrate = if (isVideo) quality.videoBitrate else "Auto"
        val resolutionScaling = if (isVideo) {
            when (quality) {
                CompressionQuality.LOW -> "720p (scale=-2:720)"
                CompressionQuality.MEDIUM -> "1080p (scale=-2:1080)"
                CompressionQuality.HIGH -> "1080p (scale=-2:1080)"
            }
        } else "Max ${quality.maxDimension}px"
        val crfOrQuality = if (isVideo) "CRF ${quality.videoCrf}" else "Quality ${quality.imageQuality}%"
        val codec = if (isVideo) "H.264 (libx264) + AAC" else "JPEG Bitmap"

        if (compressedFile != null && compressedFile.exists() && compressedFile.length() > 0L) {
            var savedToMediaStore = false
            if (_uiState.value.autoSaveToMediaStore) {
                savedToMediaStore = saveToPublicMediaStore(context, compressedFile, isVideo)
            }

            val item = CompressedItem(
                originalName = originalName,
                originalSize = originalSize,
                compressedSize = compressedFile.length(),
                file = compressedFile,
                isVideo = isVideo,
                timeTakenMs = duration,
                savedToMediaStore = savedToMediaStore
            )

            val reductionPercent = if (originalSize > 0) {
                100 - ((item.compressedSize.toDouble() / originalSize) * 100).roundToInt()
            } else 0

            val spaceSavedBytes = maxOf(0L, originalSize - item.compressedSize)

            // Persist saved bytes to DataStore for lifetime statistics
            settingsRepo.recordCompressionSavings(spaceSavedBytes)

            val auditLog = CompressionAuditLog(
                fileName = originalName,
                mediaType = if (isVideo) "VIDEO" else "IMAGE",
                originalSize = originalSize,
                compressedSize = item.compressedSize,
                reductionPercent = reductionPercent,
                durationMs = duration,
                qualityPreset = quality.name,
                status = "SUCCESS",
                targetBitrate = targetBitrate,
                resolutionScaling = resolutionScaling,
                crfOrQuality = crfOrQuality,
                codec = codec,
                savedToMediaStore = savedToMediaStore
            )

            _uiState.update { it.copy(auditLogs = listOf(auditLog) + it.auditLogs) }

            return item
        } else {
            val auditLog = CompressionAuditLog(
                fileName = originalName,
                mediaType = if (isVideo) "VIDEO" else "IMAGE",
                originalSize = originalSize,
                compressedSize = 0L,
                reductionPercent = 0,
                durationMs = duration,
                qualityPreset = quality.name,
                status = "FAILED",
                targetBitrate = targetBitrate,
                resolutionScaling = resolutionScaling,
                crfOrQuality = crfOrQuality,
                codec = codec,
                savedToMediaStore = false
            )
            _uiState.update { it.copy(auditLogs = listOf(auditLog) + it.auditLogs) }
            return null
        }
    }

    // -----------------------------------------------------------------------------------------
    // TRASH FOLDER & PENDING DELETION 5-SECOND UNDO (TASK 2)
    // -----------------------------------------------------------------------------------------
    fun deleteRecentItem(item: CompressedItem) {
        // If there was an existing pending deletion, permanently delete it before starting a new one
        commitPendingDeletionImmediately()

        val currentIndex = _uiState.value.recentItems.indexOfFirst { it.id == item.id }
        val updatedList = _uiState.value.recentItems.filterNot { it.id == item.id }

        // Move file to temporary trash folder
        val trashFile = File(trashFolder, "trash_${System.currentTimeMillis()}_${item.file.name}")
        try {
            if (item.file.exists()) {
                item.file.renameTo(trashFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val pending = PendingDeletion(
            item = item,
            originalIndex = if (currentIndex >= 0) currentIndex else 0,
            originalFileLocation = item.file,
            trashFileLocation = trashFile
        )

        _uiState.update { state ->
            state.copy(
                recentItems = updatedList,
                pendingDeletion = pending,
                currentResult = if (state.currentResult?.id == item.id) null else state.currentResult
            )
        }

        // Launch 5-second countdown job for permanent deletion
        pendingDeletionJob?.cancel()
        pendingDeletionJob = viewModelScope.launch {
            // Emit Snackbar with 5s duration and Undo action
            _events.emit(
                CompressorEvent.ShowSnackbar(
                    message = "Moved \"${item.originalName}\" to trash",
                    actionLabel = "UNDO",
                    onAction = { restorePendingDeletion() }
                )
            )

            delay(5000L) // 5 seconds window
            permanentlyDeletePendingItem()
        }
    }

    fun restorePendingDeletion() {
        pendingDeletionJob?.cancel()
        val pending = _uiState.value.pendingDeletion ?: return

        // Restore file from trash folder back to original location
        try {
            if (pending.trashFileLocation.exists()) {
                pending.trashFileLocation.renameTo(pending.originalFileLocation)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val restoredItem = pending.item.copy(file = pending.originalFileLocation)
        val targetIndex = pending.originalIndex.coerceIn(0, _uiState.value.recentItems.size)

        val restoredList = _uiState.value.recentItems.toMutableList().apply {
            add(targetIndex, restoredItem)
        }

        _uiState.update { state ->
            state.copy(
                recentItems = restoredList,
                pendingDeletion = null,
                currentResult = restoredItem,
                statusMessage = "Restored \"${restoredItem.originalName}\""
            )
        }

        viewModelScope.launch {
            _events.emit(CompressorEvent.ShowSnackbar("Restored \"${restoredItem.originalName}\" from trash."))
        }
    }

    private fun permanentlyDeletePendingItem() {
        val pending = _uiState.value.pendingDeletion ?: return
        try {
            if (pending.trashFileLocation.exists()) {
                pending.trashFileLocation.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _uiState.update { state ->
            if (state.pendingDeletion?.item?.id == pending.item.id) {
                state.copy(pendingDeletion = null)
            } else {
                state
            }
        }
    }

    private fun commitPendingDeletionImmediately() {
        pendingDeletionJob?.cancel()
        permanentlyDeletePendingItem()
    }

    fun clearAllRecentCompressions(context: Context) {
        val currentItems = _uiState.value.recentItems
        if (currentItems.isEmpty()) return

        // Delete all files in recent
        viewModelScope.launch(Dispatchers.IO) {
            currentItems.forEach { item ->
                try {
                    if (item.file.exists()) item.file.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        _uiState.update { state ->
            state.copy(
                recentItems = emptyList(),
                currentResult = null,
                statusMessage = "All recent items cleared."
            )
        }

        viewModelScope.launch {
            _events.emit(CompressorEvent.ShowSnackbar("Cleared ${currentItems.size} items from history."))
        }
    }

    fun deleteCurrentFile() {
        _uiState.value.currentResult?.let { item ->
            deleteRecentItem(item)
        }
    }

    fun exportAuditLogFile(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val logs = _uiState.value.auditLogs
            if (logs.isEmpty()) {
                _events.emit(CompressorEvent.ShowSnackbar("No compression logs to export yet."))
                return@launch
            }

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val sb = StringBuilder()
                sb.append("=========================================================================\n")
                sb.append("MEDIA COMPRESSOR - AUDIT LOG REPORT\n")
                sb.append("Generated on: ${dateFormat.format(Date())}\n")
                sb.append("Total Executions Logged: ${logs.size}\n")
                sb.append("=========================================================================\n\n")

                logs.forEachIndexed { i, log ->
                    sb.append("[${i + 1}] Date: ${dateFormat.format(Date(log.timestamp))}\n")
                    sb.append("     File: ${log.fileName} (${log.mediaType})\n")
                    sb.append("     Preset: ${log.qualityPreset} | Status: ${log.status}\n")
                    sb.append("     Original: ${formatFileSize(log.originalSize)} -> Compressed: ${formatFileSize(log.compressedSize)}\n")
                    sb.append("     Space Reduction: ${log.reductionPercent}% | Time Taken: ${log.durationMs} ms (${(log.durationMs / 1000.0).formatDec(2)}s)\n")
                    sb.append("-------------------------------------------------------------------------\n")
                }

                val logFile = File(context.cacheDir, "compression_audit_log_${System.currentTimeMillis()}.txt")
                FileOutputStream(logFile).use { it.write(sb.toString().toByteArray()) }

                shareMediaFile(context, logFile, "text/plain")
                _events.emit(CompressorEvent.ShowSnackbar("Audit log exported: ${logFile.name}"))
            } catch (e: Exception) {
                _events.emit(CompressorEvent.ShowSnackbar("Export failed: ${e.localizedMessage}"))
            }
        }
    }
}

private fun Double.formatDec(digits: Int) = "%.${digits}f".format(Locale.US, this)

// -----------------------------------------------------------------------------------------
// MAIN ACTIVITY & COMPOSE UI
// -----------------------------------------------------------------------------------------
@Composable
fun ThemeWrapper(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MediaCompressorTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CompressorViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            ThemeWrapper(themeMode = uiState.themeMode) {
                MainCompressorApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCompressorApp(viewModel: CompressorViewModel = viewModel()) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    val totalSpaceSaved by viewModel.totalSpaceSaved.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAuditLogSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val auditSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Notification Permission Launcher for Android 13+ (Foreground Service Progress)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission required for background progress", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Global Snackbar Observer for Undo, Engine Exceptions & Retry Actions
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CompressorEvent.ShowSnackbar -> {
                    if (event.actionLabel != null && event.onAction != null) {
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            event.onAction.invoke()
                        }
                    } else {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is CompressorEvent.TriggerSuccessHaptic -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    // Single Visual Pickers
    val singleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.processImage(context, uri)
        }
    }

    val singleVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.processVideo(context, uri)
        }
    }

    // Multiple / Batch Visual Pickers (Opens Batch Preview Modal for Queue Verification)
    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.prepareBatchQueue(context, uris, isVideo = false)
        }
    }

    val multipleVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.prepareBatchQueue(context, uris, isVideo = true)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Media Compressor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAuditLogSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "View Audit Logs & Parameters",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings & Presets",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Description
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "High-performance compression with customizable presets & background batch queues",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Batch Selection Mode Switch Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Batch Selection Mode",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (uiState.isBatchMode) "Pick multiple files with Foreground Service" else "Pick single file",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = uiState.isBatchMode,
                            onCheckedChange = { viewModel.toggleBatchMode(it) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (uiState.isBatchMode) {
                                multipleImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } else {
                                singleImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isBatchMode) "Batch Images" else "Compress Image",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (uiState.isBatchMode) {
                                multipleVideoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            } else {
                                singleVideoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isBatchMode) "Batch Videos" else "Compress Video",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Foreground Service / Batch & Individual Progress Section
            item {
                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (uiState.batchTotalCount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Batch Progress (${uiState.batchCurrentIndex}/${uiState.batchTotalCount})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${((uiState.batchCurrentIndex.toFloat() / uiState.batchTotalCount.toFloat()) * 100f).roundToInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { uiState.batchCurrentIndex.toFloat() / uiState.batchTotalCount.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Individual File Granular Progress Tracking (Task 1)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Current File Encoding",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${uiState.fileProgressPercent.roundToInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (uiState.fileProgressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = uiState.currentAction,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.statusMessage,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Undo Alert Banner when an item is in Pending Deletion (Task 2)
            item {
                AnimatedVisibility(
                    visible = uiState.pendingDeletion != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    val pending = uiState.pendingDeletion
                    if (pending != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "In trash: \"${pending.item.originalName}\" (5s undo)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                Button(
                                    onClick = { viewModel.restorePendingDeletion() },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Undo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Results Display Card with Share & Delete
            item {
                uiState.currentResult?.let { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (res.isVideo) "Video Compressed" else "Image Compressed",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteCurrentFile() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete File",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = res.file,
                                    contentDescription = "Compressed Media Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Original Size", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(text = formatFileSize(res.originalSize), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Compressed Size", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(text = formatFileSize(res.compressedSize), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val savedPercent = if (res.originalSize > 0) {
                                100 - ((res.compressedSize.toDouble() / res.originalSize) * 100).roundToInt()
                            } else 0

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reduction: $savedPercent% smaller",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                )

                                if (res.timeTakenMs > 0) {
                                    Text(
                                        text = "Time: ${res.timeTakenMs} ms",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            if (res.savedToMediaStore) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ Saved to device's public ${if (res.isVideo) "Movies" else "Pictures"} folder",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { shareMediaFile(context, res.file, if (res.isVideo) "video/mp4" else "image/jpeg") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Share File", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.deleteCurrentFile() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Delete", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // -----------------------------------------------------------------------------------------
            // SUMMARY CARD AT TOP OF RECENT COMPRESSIONS (TASK 3)
            // -----------------------------------------------------------------------------------------
            item {
                val totalSessionOriginal = uiState.recentItems.sumOf { it.originalSize }
                val totalSessionCompressed = uiState.recentItems.sumOf { it.compressedSize }
                val sessionSavedBytes = maxOf(0L, totalSessionOriginal - totalSessionCompressed)
                val displayTotalSaved = maxOf(totalSpaceSaved, sessionSavedBytes)
                val totalCount = maxOf(uiState.recentItems.size.toLong(), uiState.historicalFilesCount)

                val avgSavingsPercent = if (totalSessionOriginal > 0) {
                    ((sessionSavedBytes.toDouble() / totalSessionOriginal.toDouble()) * 100).roundToInt()
                } else if (displayTotalSaved > 0) 65 else 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Total Space Saved: ${formatFileSize(displayTotalSaved)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "$avgSavingsPercent% Avg Saved",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Space Saved",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = formatFileSize(displayTotalSaved),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Files Processed",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "$totalCount files",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Active Cache",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = formatFileSize(totalSessionCompressed),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recent Compressions List Header with 'Clear All' Button
            item {
                if (uiState.recentItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Recent Compressions (${uiState.recentItems.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { viewModel.clearAllRecentCompressions(context) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Clear All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Recent Compressions Items with Delete & Undo capability
            items(uiState.recentItems, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.originalName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${formatFileSize(item.originalSize)} → ${formatFileSize(item.compressedSize)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { shareMediaFile(context, item.file, if (item.isVideo) "video/mp4" else "image/jpeg") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { viewModel.deleteRecentItem(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete item",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Batch Preview Modal BottomSheet (Task 4)
    if (uiState.showBatchPreviewModal && uiState.pendingBatchQueue.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissBatchPreview() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Batch Preview Queue",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.pendingBatchQueue.size} ${if (uiState.batchIsVideo) "videos" else "images"} ready to compress",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    TextButton(onClick = { viewModel.dismissBatchPreview() }) {
                        Text("Cancel")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Summary size card
                val totalQueueBytes = uiState.pendingBatchQueue.sumOf { it.size }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Input Size:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatFileSize(totalQueueBytes),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Items in Queue (${uiState.pendingBatchQueue.size}) - Tap ✕ to remove from batch:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable items in queue with individual removal and pre-compression checks (Task 3)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.pendingBatchQueue, key = { it.id }) { queueItem ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (queueItem.isVideo) Icons.Default.Videocam else Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = queueItem.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Size: ${queueItem.formattedSize}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (queueItem.dimensions.isNotEmpty()) {
                                            Text(
                                                text = " • ${queueItem.dimensions}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        if (queueItem.durationText.isNotEmpty()) {
                                            Text(
                                                text = " • ${queueItem.durationText}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                    if (queueItem.mimeType.isNotEmpty()) {
                                        Text(
                                            text = "Format: ${queueItem.mimeType}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.removeFromBatchQueue(queueItem.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove file from queue",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.confirmAndStartBatch(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Batch Compression (${uiState.pendingBatchQueue.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Audit Logs & Details BottomSheet (Task 2)
    if (showAuditLogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAuditLogSheet = false },
            sheetState = auditSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Compression Audit Logs", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.auditLogs.size} historical runs logged with parameters",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { showAuditLogSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (uiState.auditLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No audit logs recorded yet. Compress files to view performance benchmarks and parameters.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "History & Engine Parameters:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { viewModel.exportAuditLogFile(context) }) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.auditLogs, key = { it.id }) { log ->
                            var isExpanded by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (log.mediaType == "VIDEO") Icons.Default.Videocam else Icons.Default.Image,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = log.fileName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${formatFileSize(log.originalSize)} ➔ ${formatFileSize(log.compressedSize)} (${log.reductionPercent}% saved)",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                        }

                                        TextButton(
                                            onClick = { isExpanded = !isExpanded }
                                        ) {
                                            Text(if (isExpanded) "Hide Details" else "View Details", fontSize = 11.sp)
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Expandable Details Section (Task 2)
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Preset / Mode:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text(log.qualityPreset, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Target Bitrate:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text(log.targetBitrate, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Resolution Scaling:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text(log.resolutionScaling, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("CRF / Quality Factor:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text(log.crfOrQuality, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Encoding Codec:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text(log.codec, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Execution Time:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text("${(log.durationMs / 1000.0).formatDec(2)}s", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Public Gallery Saved:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text(if (log.savedToMediaStore) "Yes (MediaStore)" else "No (Cache only)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Settings Modal BottomSheet with DataStore Persistence (Task 4)
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "App & Compression Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showSettingsSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // App Theme Selector (Persisted via DataStore)
                Text(text = "Appearance Theme (Persisted in DataStore)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { theme ->
                        FilterChip(
                            selected = uiState.themeMode == theme,
                            onClick = { viewModel.setThemeMode(theme) },
                            label = { Text(text = theme.label, fontSize = 11.sp) },
                            leadingIcon = {
                                when (theme) {
                                    AppThemeMode.LIGHT -> Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(14.dp))
                                    AppThemeMode.DARK -> Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp))
                                    AppThemeMode.SYSTEM -> {}
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image Quality Presets
                Text(text = "Image Quality Preset", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompressionQuality.entries.forEach { q ->
                        FilterChip(
                            selected = uiState.imageQuality == q,
                            onClick = { viewModel.updateImageQuality(q) },
                            label = { Text(text = q.name, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video Quality Presets
                Text(text = "Video Quality Preset", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompressionQuality.entries.forEach { q ->
                        FilterChip(
                            selected = uiState.videoQuality == q,
                            onClick = { viewModel.updateVideoQuality(q) },
                            label = { Text(text = q.name, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto Save to MediaStore Gallery Toggle (Persisted via DataStore)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Save directly to Public Gallery", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Auto-save output files to device Pictures/Movies storage (Saved in DataStore)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = uiState.autoSaveToMediaStore,
                        onCheckedChange = { viewModel.toggleAutoSaveToMediaStore(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// COMPRESSION LOGIC & HELPER FUNCTIONS
// -----------------------------------------------------------------------------------------
suspend fun compressImageFile(context: Context, uri: Uri, quality: CompressionQuality): File? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val maxDim = quality.maxDimension

            val scaledBitmap = if (width > maxDim || height > maxDim) {
                val ratio = width.toFloat() / height.toFloat()
                val targetWidth: Int
                val targetHeight: Int
                if (width > height) {
                    targetWidth = maxDim
                    targetHeight = (maxDim / ratio).toInt()
                } else {
                    targetHeight = maxDim
                    targetWidth = (maxDim * ratio).toInt()
                }
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(outputFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality.imageQuality, outputStream)
            outputStream.flush()
            outputStream.close()

            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

suspend fun compressVideoFile(
    context: Context,
    uri: Uri,
    quality: CompressionQuality,
    onProgress: (Float) -> Unit = {}
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val tempInputFile = File(context.cacheDir, "temp_video_in_${System.currentTimeMillis()}.mp4")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempInputFile).use { output ->
                    input.copyTo(output)
                }
            }

            val retriever = MediaMetadataRetriever()
            var durationMs = 1000L
            try {
                retriever.setDataSource(tempInputFile.absolutePath)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (durationStr != null) {
                    durationMs = maxOf(1000L, durationStr.toLongOrNull() ?: 1000L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try { retriever.release() } catch (_: Exception) {}
            }

            val outputFile = File(context.cacheDir, "compressed_video_${System.currentTimeMillis()}.mp4")

            val scaleFilter = when (quality) {
                CompressionQuality.LOW -> "scale=-2:720"
                CompressionQuality.MEDIUM -> "scale=-2:1080"
                CompressionQuality.HIGH -> "scale=-2:1080"
            }

            val cmd = "-y -i \"${tempInputFile.absolutePath}\" -vf $scaleFilter -vcodec libx264 -crf ${quality.videoCrf} -b:v ${quality.videoBitrate} -preset veryfast -c:a aac -b:a 128k \"${outputFile.absolutePath}\""

            val session = FFmpegKit.executeAsync(
                cmd,
                { /* completion callback */ },
                { /* log callback */ },
                { stats ->
                    if (stats != null && durationMs > 0) {
                        val currentMs = stats.time.coerceAtLeast(0)
                        val progress = ((currentMs.toFloat() / durationMs.toFloat()) * 100f).coerceIn(0f, 99f)
                        onProgress(progress)
                    }
                }
            )

            // Wait for session to finish
            while (session.state == SessionState.CREATED || session.state == SessionState.RUNNING) {
                delay(100)
            }

            tempInputFile.delete()

            if (ReturnCode.isSuccess(session.returnCode)) {
                onProgress(100f)
                outputFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun getMediaMetadataInfo(context: Context, uri: Uri, isVideo: Boolean): Triple<String, String, String> {
    var dimensions = ""
    var durationText = ""
    var mimeType = context.contentResolver.getType(uri) ?: (if (isVideo) "video/mp4" else "image/jpeg")

    try {
        if (isVideo) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            val extractedMime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            if (!extractedMime.isNullOrEmpty()) mimeType = extractedMime

            if (!width.isNullOrEmpty() && !height.isNullOrEmpty()) {
                dimensions = "${width}x${height} px"
            }
            if (durationMs != null && durationMs > 0) {
                val totalSec = durationMs / 1000
                val min = totalSec / 60
                val sec = totalSec % 60
                durationText = String.format(Locale.US, "%02d:%02d", min, sec)
            }
            retriever.release()
        } else {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            if (options.outWidth > 0 && options.outHeight > 0) {
                dimensions = "${options.outWidth}x${options.outHeight} px"
            }
            if (!options.outMimeType.isNullOrEmpty()) {
                mimeType = options.outMimeType
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return Triple(dimensions, durationText, mimeType)
}

fun saveToPublicMediaStore(context: Context, file: File, isVideo: Boolean): Boolean {
    return try {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            val mime = if (isVideo) "video/mp4" else "image/jpeg"
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relativePath = if (isVideo) Environment.DIRECTORY_MOVIES + "/MediaCompressor" else Environment.DIRECTORY_PICTURES + "/MediaCompressor"
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collectionUri = if (isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collectionUri, values) ?: return false

        resolver.openOutputStream(uri)?.use { out ->
            FileInputStream(file).use { input ->
                input.copyTo(out)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun shareMediaFile(context: Context, file: File, mimeType: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Compressed Media"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

fun getFileSizeFromUri(context: Context, uri: Uri): Long {
    var size = 0L
    try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = it.getLong(sizeIndex)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return size
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return name
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    val value = bytes / 1024.0.pow(digitGroups.toDouble())
    return DecimalFormat("#,##0.#").format(value) + " " + units[digitGroups]
}

// -----------------------------------------------------------------------------------------
// COMPOSE THEME DEFINITION
// -----------------------------------------------------------------------------------------
@Composable
fun MediaCompressorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF64B5F6),
            secondary = Color(0xFF81C784),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color(0xFF0D47A1),
            onSecondary = Color(0xFF1B5E20),
            onBackground = Color(0xFFEEEEEE),
            onSurface = Color(0xFFEEEEEE)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF1976D2),
            secondary = Color(0xFF388E3C),
            background = Color(0xFFF8F9FA),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF212121),
            onSurface = Color(0xFF212121)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
