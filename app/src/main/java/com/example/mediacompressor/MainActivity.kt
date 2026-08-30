package com.example.mediacompressor

import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val status: String
)

data class CompressorUiState(
    val isLoading: Boolean = false,
    val currentAction: String = "",
    val statusMessage: String = "Ready. Select an image or video to compress.",
    val currentResult: CompressedItem? = null,
    val recentItems: List<CompressedItem> = emptyList(),
    val imageQuality: CompressionQuality = CompressionQuality.MEDIUM,
    val videoQuality: CompressionQuality = CompressionQuality.MEDIUM,
    // Batch Mode State
    val isBatchMode: Boolean = false,
    val batchTotalCount: Int = 0,
    val batchCurrentIndex: Int = 0,
    // Theme & MediaStore Settings
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val autoSaveToMediaStore: Boolean = false,
    // Audit Logs
    val auditLogs: List<CompressionAuditLog> = emptyList()
)

sealed class CompressorEvent {
    data class ShowSnackbar(val message: String) : CompressorEvent()
    object TriggerSuccessHaptic : CompressorEvent()
}

// -----------------------------------------------------------------------------------------
// VIEWMODEL ARCHITECTURE
// -----------------------------------------------------------------------------------------
class CompressorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CompressorUiState())
    val uiState: StateFlow<CompressorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CompressorEvent>()
    val events: SharedFlow<CompressorEvent> = _events.asSharedFlow()

    fun updateImageQuality(quality: CompressionQuality) {
        _uiState.update { it.copy(imageQuality = quality) }
    }

    fun updateVideoQuality(quality: CompressionQuality) {
        _uiState.update { it.copy(videoQuality = quality) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleAutoSaveToMediaStore(enabled: Boolean) {
        _uiState.update { it.copy(autoSaveToMediaStore = enabled) }
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

    // Process Batch of Multiple Images / Videos
    fun processBatchQueue(context: Context, uris: List<Uri>, isVideo: Boolean) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val total = uris.size
            _uiState.update {
                it.copy(
                    isLoading = true,
                    batchTotalCount = total,
                    batchCurrentIndex = 0,
                    currentAction = "Batch Processing (0/$total)...",
                    statusMessage = "Queued $total ${if (isVideo) "videos" else "images"} for compression."
                )
            }

            var successCount = 0
            val processedItems = mutableListOf<CompressedItem>()

            uris.forEachIndexed { index, uri ->
                val currentNum = index + 1
                _uiState.update {
                    it.copy(
                        batchCurrentIndex = currentNum,
                        currentAction = "Compressing $currentNum of $total ${if (isVideo) "videos" else "images"}...",
                        statusMessage = "Processing item $currentNum/$total..."
                    )
                }

                val result = processSingleMediaInternal(context, uri, isVideo)
                if (result != null) {
                    successCount++
                    processedItems.add(result)
                }
            }

            _uiState.update { state ->
                val updatedRecent = (processedItems + state.recentItems).take(20)
                state.copy(
                    isLoading = false,
                    batchTotalCount = 0,
                    batchCurrentIndex = 0,
                    currentResult = processedItems.firstOrNull(),
                    recentItems = updatedRecent,
                    statusMessage = "Batch compression complete: $successCount of $total succeeded."
                )
            }

            _events.emit(CompressorEvent.TriggerSuccessHaptic)
            _events.emit(CompressorEvent.ShowSnackbar("Batch completed: $successCount/$total items compressed."))
        }
    }

    private suspend fun processSingleMedia(context: Context, uri: Uri, isVideo: Boolean) {
        val originalSize = getFileSizeFromUri(context, uri)
        val originalName = getFileNameFromUri(context, uri) ?: if (isVideo) "video_${System.currentTimeMillis()}.mp4" else "image_${System.currentTimeMillis()}.jpg"

        if (originalSize <= 0) {
            _events.emit(CompressorEvent.ShowSnackbar("Unable to read selected file."))
            return
        }

        val quality = if (isVideo) _uiState.value.videoQuality else _uiState.value.imageQuality

        _uiState.update {
            it.copy(
                isLoading = true,
                currentAction = "Compressing ${if (isVideo) "Video" else "Image"} (${quality.label})...",
                statusMessage = "Original: ${formatFileSize(originalSize)} • Encoding...",
                currentResult = null
            )
        }

        val item = processSingleMediaInternal(context, uri, isVideo)

        if (item != null) {
            val reduction = if (originalSize > 0) {
                val saved = 100 - ((item.compressedSize.toDouble() / item.originalSize) * 100).roundToInt()
                "$saved% space saved"
            } else "Optimized"

            val mediaStoreNotice = if (item.savedToMediaStore) " • Saved to ${if (isVideo) "Movies" else "Pictures"}" else ""

            _uiState.update { state ->
                val updatedRecent = (listOf(item) + state.recentItems).take(20)
                state.copy(
                    isLoading = false,
                    currentResult = item,
                    recentItems = updatedRecent,
                    statusMessage = "${if (isVideo) "Video" else "Image"} compressed! ($reduction)$mediaStoreNotice"
                )
            }
            _events.emit(CompressorEvent.TriggerSuccessHaptic)
        } else {
            _uiState.update { it.copy(isLoading = false, statusMessage = "Compression failed.") }
            _events.emit(CompressorEvent.ShowSnackbar("Compression failed. Please check storage or file format."))
        }
    }

    private suspend fun processSingleMediaInternal(context: Context, uri: Uri, isVideo: Boolean): CompressedItem? {
        val startTime = System.currentTimeMillis()
        val originalSize = getFileSizeFromUri(context, uri)
        val originalName = getFileNameFromUri(context, uri) ?: if (isVideo) "video_${System.currentTimeMillis()}.mp4" else "image_${System.currentTimeMillis()}.jpg"
        val quality = if (isVideo) _uiState.value.videoQuality else _uiState.value.imageQuality

        val compressedFile = if (isVideo) {
            compressVideoFile(context, uri, _uiState.value.videoQuality)
        } else {
            compressImageFile(context, uri, _uiState.value.imageQuality)
        }

        val duration = System.currentTimeMillis() - startTime

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

            val auditLog = CompressionAuditLog(
                fileName = originalName,
                mediaType = if (isVideo) "VIDEO" else "IMAGE",
                originalSize = originalSize,
                compressedSize = item.compressedSize,
                reductionPercent = reductionPercent,
                durationMs = duration,
                qualityPreset = quality.name,
                status = "SUCCESS"
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
                status = "FAILED"
            )
            _uiState.update { it.copy(auditLogs = listOf(auditLog) + it.auditLogs) }
            return null
        }
    }

    fun clearAllRecentCompressions(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = _uiState.value.recentItems
            items.forEach { item ->
                if (item.file.exists()) {
                    item.file.delete()
                }
            }
            _uiState.update { state ->
                state.copy(
                    recentItems = emptyList(),
                    currentResult = null,
                    statusMessage = "All cache files & history cleared."
                )
            }
            _events.emit(CompressorEvent.ShowSnackbar("Cleared all temporary files and history."))
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

    fun deleteCurrentFile() {
        _uiState.value.currentResult?.let { item ->
            if (item.file.exists()) {
                item.file.delete()
            }
            _uiState.update { state ->
                state.copy(
                    currentResult = null,
                    statusMessage = "Compressed file removed from cache."
                )
            }
            viewModelScope.launch {
                _events.emit(CompressorEvent.ShowSnackbar("Temporary file deleted from cache."))
            }
        }
    }
}

private fun Double.formatDec(digits: Int) = "%.${digits}f".format(Locale.US, this)

// -----------------------------------------------------------------------------------------
// MAIN ACTIVITY & COMPOSE UI
// -----------------------------------------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CompressorViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            val isDarkTheme = when (uiState.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MediaCompressorTheme(darkTheme = isDarkTheme) {
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

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAuditLogDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Listen for ViewModel Events
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CompressorEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
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

    // Multiple / Batch Visual Pickers
    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.processBatchQueue(context, uris, isVideo = false)
        }
    }

    val multipleVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.processBatchQueue(context, uris, isVideo = true)
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
                    IconButton(onClick = { viewModel.exportAuditLogFile(context) }) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Export Audit Logs",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Quality Settings",
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
            // Header Description & Batch Mode Switch Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "High-performance compression with customizable presets & batch queues",
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
                                    text = if (uiState.isBatchMode) "Pick multiple files at once" else "Pick single file",
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

            // Action Buttons (Single or Batch depending on mode)
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
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

            // Loading / Spinner & Batch Progress Section
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
                                LinearProgressIndicator(
                                    progress = { uiState.batchCurrentIndex.toFloat() / uiState.batchTotalCount.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

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

            // Results Display Card with Share & Delete
            item {
                uiState.currentResult?.let { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
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

                                // Delete Button
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

                            // Thumbnail Preview
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

                            // Size Comparison Grid
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

                            // Share and Action Buttons
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

            // Status Card
            item {
                if (uiState.currentResult == null && !uiState.isLoading) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "System Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = uiState.statusMessage, fontSize = 13.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Recent Compressions List Header with 'Clear All' Button
            item {
                if (uiState.recentItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Recent Compressions (${uiState.recentItems.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        // Clear Recent Compressions Button
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

            // Recent Compressions Items
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
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Settings Modal BottomSheet
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

                // App Theme Selector
                Text(text = "Appearance Theme", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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

                // Auto Save to MediaStore Gallery Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Save directly to Public Gallery", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Auto-save output files to device Pictures/Movies storage",
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

suspend fun compressVideoFile(context: Context, uri: Uri, quality: CompressionQuality): File? {
    return withContext(Dispatchers.IO) {
        try {
            // 1. Copy source video content stream to a temporary working file
            val tempInputFile = File(context.cacheDir, "temp_video_in_${System.currentTimeMillis()}.mp4")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempInputFile).use { output ->
                    input.copyTo(output)
                }
            }

            val outputFile = File(context.cacheDir, "compressed_video_${System.currentTimeMillis()}.mp4")

            // 2. Build fast & optimized FFmpegKit command
            val scaleFilter = when (quality) {
                CompressionQuality.LOW -> "scale=-2:720"
                CompressionQuality.MEDIUM -> "scale=-2:1080"
                CompressionQuality.HIGH -> "scale=-2:1080"
            }

            val cmd = "-y -i \"${tempInputFile.absolutePath}\" -vf $scaleFilter -vcodec libx264 -crf ${quality.videoCrf} -b:v ${quality.videoBitrate} -preset veryfast -c:a aac -b:a 128k \"${outputFile.absolutePath}\""

            val session = FFmpegKit.execute(cmd)
            tempInputFile.delete()

            if (ReturnCode.isSuccess(session.returnCode)) {
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
