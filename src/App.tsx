/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState } from 'react';
import { 
  Image, Video, CheckCircle2, Copy, Check, Smartphone, FileCode2, Sparkles, RefreshCw, 
  Share2, Trash2, Sliders, History, CheckCheck, Sun, Moon, HardDrive, FileText, 
  Layers, Download, AlertCircle
} from 'lucide-react';

const GRADLE_CODE = `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.mediacompressor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mediacompressor"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX Core & Lifecycle ViewModel Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI & Material 3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Coil Image & Video Loading for Jetpack Compose
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")

    // FFmpegKit Lite (Fast, standalone video compression engine)
    implementation("io.github.root0as:ffmpeg-kit-lite:6.0-2")

    // Debug Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}`;

const MAIN_ACTIVITY_CODE = `package com.example.mediacompressor

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
                    currentAction = "Batch Processing (0/\$total)...",
                    statusMessage = "Queued \$total \${if (isVideo) "videos" else "images"} for compression."
                )
            }

            var successCount = 0
            val processedItems = mutableListOf<CompressedItem>()

            uris.forEachIndexed { index, uri ->
                val currentNum = index + 1
                _uiState.update {
                    it.copy(
                        batchCurrentIndex = currentNum,
                        currentAction = "Compressing \$currentNum of \$total \${if (isVideo) "videos" else "images"}...",
                        statusMessage = "Processing item \$currentNum/\$total..."
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
                    statusMessage = "Batch compression complete: \$successCount of \$total succeeded."
                )
            }

            _events.emit(CompressorEvent.TriggerSuccessHaptic)
            _events.emit(CompressorEvent.ShowSnackbar("Batch completed: \$successCount/\$total items compressed."))
        }
    }

    private suspend fun processSingleMedia(context: Context, uri: Uri, isVideo: Boolean) {
        val originalSize = getFileSizeFromUri(context, uri)
        val originalName = getFileNameFromUri(context, uri) ?: if (isVideo) "video_\${System.currentTimeMillis()}.mp4" else "image_\${System.currentTimeMillis()}.jpg"

        if (originalSize <= 0) {
            _events.emit(CompressorEvent.ShowSnackbar("Unable to read selected file."))
            return
        }

        val quality = if (isVideo) _uiState.value.videoQuality else _uiState.value.imageQuality

        _uiState.update {
            it.copy(
                isLoading = true,
                currentAction = "Compressing \${if (isVideo) "Video" else "Image"} (\${quality.label})...",
                statusMessage = "Original: \${formatFileSize(originalSize)} • Encoding...",
                currentResult = null
            )
        }

        val item = processSingleMediaInternal(context, uri, isVideo)

        if (item != null) {
            val reduction = if (originalSize > 0) {
                val saved = 100 - ((item.compressedSize.toDouble() / item.originalSize) * 100).roundToInt()
                "\$saved% space saved"
            } else "Optimized"

            val mediaStoreNotice = if (item.savedToMediaStore) " • Saved to \${if (isVideo) "Movies" else "Pictures"}" else ""

            _uiState.update { state ->
                val updatedRecent = (listOf(item) + state.recentItems).take(20)
                state.copy(
                    isLoading = false,
                    currentResult = item,
                    recentItems = updatedRecent,
                    statusMessage = "\${if (isVideo) "Video" else "Image"} compressed! (\$reduction)\$mediaStoreNotice"
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
        val originalName = getFileNameFromUri(context, uri) ?: if (isVideo) "video_\${System.currentTimeMillis()}.mp4" else "image_\${System.currentTimeMillis()}.jpg"
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
                sb.append("Generated on: \${dateFormat.format(Date())}\n")
                sb.append("Total Executions Logged: \${logs.size}\n")
                sb.append("=========================================================================\n\n")

                logs.forEachIndexed { i, log ->
                    sb.append("[\${i + 1}] Date: \${dateFormat.format(Date(log.timestamp))}\n")
                    sb.append("     File: \${log.fileName} (\${log.mediaType})\n")
                    sb.append("     Preset: \${log.qualityPreset} | Status: \${log.status}\n")
                    sb.append("     Original: \${formatFileSize(log.originalSize)} -> Compressed: \${formatFileSize(log.compressedSize)}\n")
                    sb.append("     Space Reduction: \${log.reductionPercent}% | Time Taken: \${log.durationMs} ms (\${(log.durationMs / 1000.0).formatDec(2)}s)\n")
                    sb.append("-------------------------------------------------------------------------\n")
                }

                val logFile = File(context.cacheDir, "compression_audit_log_\${System.currentTimeMillis()}.txt")
                FileOutputStream(logFile).use { it.write(sb.toString().toByteArray()) }

                shareMediaFile(context, logFile, "text/plain")
                _events.emit(CompressorEvent.ShowSnackbar("Audit log exported: \${logFile.name}"))
            } catch (e: Exception) {
                _events.emit(CompressorEvent.ShowSnackbar("Export failed: \${e.localizedMessage}"))
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

private fun Double.formatDec(digits: Int) = "%.` + '${digits}' + `f".format(Locale.US, this)

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
                                    text = "Reduction: \$savedPercent% smaller",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                )

                                if (res.timeTakenMs > 0) {
                                    Text(
                                        text = "Time: \${res.timeTakenMs} ms",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            if (res.savedToMediaStore) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ Saved to device's public \${if (res.isVideo) "Movies" else "Pictures"} folder",
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
                            Text(text = "Recent Compressions (\${uiState.recentItems.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

            items(uiState.recentItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = item.originalName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(
                                    text = "\${formatFileSize(item.originalSize)} -> \${formatFileSize(item.compressedSize)}" + (if (item.timeTakenMs > 0) " • \${item.timeTakenMs}ms" else ""),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        IconButton(onClick = { shareMediaFile(context, item.file, if (item.isVideo) "video/mp4" else "image/jpeg") }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Settings Bottom Sheet / Modal
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Theme Toggle Section
                    Text(text = "App Theme", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.values().forEach { mode ->
                            FilterChip(
                                selected = uiState.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(text = mode.name, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when(mode) {
                                            AppThemeMode.SYSTEM -> Icons.Default.Tune
                                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                                            AppThemeMode.DARK -> Icons.Default.DarkMode
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // MediaStore Auto-Save Setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto-save to MediaStore", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Save compressed files directly to device's public Pictures/Movies gallery",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = uiState.autoSaveToMediaStore,
                            onCheckedChange = { viewModel.toggleAutoSaveToMediaStore(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Image Quality Preset
                    Text(text = "Image Quality Preset", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompressionQuality.values().forEach { quality ->
                            FilterChip(
                                selected = uiState.imageQuality == quality,
                                onClick = { viewModel.updateImageQuality(quality) },
                                label = { Text(text = quality.name, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Text(
                        text = "Current: \${uiState.imageQuality.label} (Max \${uiState.imageQuality.maxDimension}px, \${uiState.imageQuality.imageQuality}% quality)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Video Quality Preset
                    Text(text = "Video Quality Preset", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompressionQuality.values().forEach { quality ->
                            FilterChip(
                                selected = uiState.videoQuality == quality,
                                onClick = { viewModel.updateVideoQuality(quality) },
                                label = { Text(text = quality.name, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Text(
                        text = "Current: \${uiState.videoQuality.label} (CRF \${uiState.videoQuality.videoCrf}, Target: \${uiState.videoQuality.videoBitrate})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Export Audit Logs Button inside drawer
                    OutlinedButton(
                        onClick = {
                            viewModel.exportAuditLogFile(context)
                            showSettingsSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Export Compression Audit Logs (.txt)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showSettingsSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Apply Settings", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// IMAGE COMPRESSION ENGINE (Native BitmapFactory, Fast & Configurable)
// -----------------------------------------------------------------------------------------
suspend fun compressImageFile(
    context: Context,
    inputUri: Uri,
    quality: CompressionQuality
): File? = withContext(Dispatchers.IO) {
    try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(inputUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        if (originalWidth <= 0 || originalHeight <= 0) return@withContext null

        val maxDim = quality.maxDimension
        var inSampleSize = 1
        if (originalHeight > maxDim || originalWidth > maxDim) {
            val halfHeight = originalHeight / 2
            val halfWidth = originalWidth / 2
            while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val sampledBitmap: Bitmap? = context.contentResolver.openInputStream(inputUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return@withContext null

        val finalBitmap = if (sampledBitmap.width > maxDim || sampledBitmap.height > maxDim) {
            val scaleFactor = maxDim.toFloat() / maxOf(sampledBitmap.width, sampledBitmap.height)
            val targetW = (sampledBitmap.width * scaleFactor).roundToInt()
            val targetH = (sampledBitmap.height * scaleFactor).roundToInt()
            Bitmap.createScaledBitmap(sampledBitmap, targetW, targetH, true).also {
                if (it != sampledBitmap) sampledBitmap.recycle()
            }
        } else {
            sampledBitmap
        }

        val outputFile = File(context.cacheDir, "COMPRESSED_IMG_\${UUID.randomUUID()}.jpg")
        FileOutputStream(outputFile).use { outputStream ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality.imageQuality, outputStream)
            outputStream.flush()
        }
        finalBitmap.recycle()

        return@withContext outputFile
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}

// -----------------------------------------------------------------------------------------
// VIDEO COMPRESSION ENGINE (FFmpegKit Lite with Dynamic Quality Presets)
// -----------------------------------------------------------------------------------------
suspend fun compressVideoFile(
    context: Context,
    inputUri: Uri,
    quality: CompressionQuality
): File? = withContext(Dispatchers.IO) {
    try {
        val tempInputFile = File(context.cacheDir, "TEMP_IN_\${UUID.randomUUID()}.mp4")
        context.contentResolver.openInputStream(inputUri)?.use { inputStream: InputStream ->
            FileOutputStream(tempInputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
                outputStream.flush()
            }
        }

        if (!tempInputFile.exists() || tempInputFile.length() == 0L) {
            return@withContext null
        }

        val outputFile = File(context.cacheDir, "COMPRESSED_VID_\${UUID.randomUUID()}.mp4")

        val scaleFilter = when (quality) {
            CompressionQuality.LOW -> "scale='min(640,iw)':-2"
            CompressionQuality.MEDIUM -> "scale='min(1280,iw)':-2"
            CompressionQuality.HIGH -> "scale='min(1920,iw)':-2"
        }

        val ffmpegCommand = "-y -i \"\${tempInputFile.absolutePath}\" -vf \"\$scaleFilter\" -c:v libx264 -crf \${quality.videoCrf} -b:v \${quality.videoBitrate} -preset ultrafast -c:a aac -b:a 128k \"\${outputFile.absolutePath}\""
        val session = FFmpegKit.execute(ffmpegCommand)

        if (tempInputFile.exists()) {
            tempInputFile.delete()
        }

        if (ReturnCode.isSuccess(session.returnCode) && outputFile.exists() && outputFile.length() > 0L) {
            return@withContext outputFile
        } else {
            return@withContext null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}

// -----------------------------------------------------------------------------------------
// MEDIASTORE PUBLIC EXPORT HELPER (Saves to Pictures / Movies)
// -----------------------------------------------------------------------------------------
fun saveToPublicMediaStore(context: Context, sourceFile: File, isVideo: Boolean): Boolean {
    try {
        val contentResolver = context.contentResolver
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = if (isVideo) "COMPRESSED_VID_\$timeStamp.mp4" else "COMPRESSED_IMG_\$timeStamp.jpg"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, if (isVideo) "video/mp4" else "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    if (isVideo) Environment.DIRECTORY_MOVIES + "/MediaCompressor" else Environment.DIRECTORY_PICTURES + "/MediaCompressor"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collectionUri = if (isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val destinationUri = contentResolver.insert(collectionUri, values) ?: return false

        contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
                inputStream.copyTo(outputStream)
                outputStream.flush()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(destinationUri, values, null, null)
        }

        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

// -----------------------------------------------------------------------------------------
// SHARE SHEET & FILE UTILITIES
// -----------------------------------------------------------------------------------------
fun shareMediaFile(context: Context, file: File, mimeType: String) {
    try {
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "\${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Compressed Media"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open ShareSheet: \${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun getFileSizeFromUri(context: Context, uri: Uri): Long {
    var size = 0L
    try {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (_: Exception) {}
    return size
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    try {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
    } catch (_: Exception) {}
    return name
}

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    val value = size / 1024.0.pow(digitGroups.toDouble())
    return DecimalFormat("#,##0.#").format(value) + " " + units[digitGroups]
}

@Composable
fun MediaCompressorTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFD0BCFF),
            secondary = Color(0xFFCCC2DC),
            background = Color(0xFF141218),
            surface = Color(0xFF211F26),
            onPrimary = Color(0xFF381E72),
            onBackground = Color(0xFFE6E1E5),
            onSurface = Color(0xFFE6E1E5)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            background = Color(0xFFFEF7FF),
            surface = Color(0xFFF7F2FA),
            onPrimary = Color.White,
            onBackground = Color(0xFF1D1B20),
            onSurface = Color(0xFF1D1B20)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}`;

interface CompressionItem {
  id: string;
  name: string;
  originalSize: number;
  compressedSize: number;
  type: 'image' | 'video';
  timeTakenMs: number;
  savedToMediaStore: boolean;
  timestamp: string;
}

interface AuditLogEntry {
  id: string;
  fileName: string;
  mediaType: string;
  originalSize: number;
  compressedSize: number;
  reductionPercent: number;
  durationMs: number;
  qualityPreset: string;
  status: string;
  timestamp: string;
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'preview' | 'gradle' | 'mainActivity'>('preview');
  const [copiedTab, setCopiedTab] = useState<string | null>(null);

  // App Theme State
  const [themeMode, setThemeMode] = useState<'SYSTEM' | 'LIGHT' | 'DARK'>('LIGHT');
  const isDarkMode = themeMode === 'DARK';

  // Feature Options
  const [isBatchMode, setIsBatchMode] = useState(false);
  const [autoSaveMediaStore, setAutoSaveMediaStore] = useState(false);

  // Live Simulator States
  const [isLoading, setIsLoading] = useState(false);
  const [batchProgress, setBatchProgress] = useState<{ current: number; total: number } | null>(null);
  const [currentAction, setCurrentAction] = useState('');
  const [statusMessage, setStatusMessage] = useState('Ready. Select media or toggle Batch mode.');
  const [imageQuality, setImageQuality] = useState<'Low' | 'Medium' | 'High'>('Medium');
  const [videoQuality, setVideoQuality] = useState<'Low' | 'Medium' | 'High'>('Medium');
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showAuditModal, setShowAuditModal] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [hapticTriggered, setHapticTriggered] = useState(false);

  // Active Result & History State
  const [currentResult, setCurrentResult] = useState<CompressionItem | null>(null);
  const [recentItems, setRecentItems] = useState<CompressionItem[]>([
    {
      id: '1',
      name: 'vacation_photo.jpg',
      originalSize: 4800000,
      compressedSize: 1100000,
      type: 'image',
      timeTakenMs: 420,
      savedToMediaStore: false,
      timestamp: 'Just now'
    },
    {
      id: '2',
      name: 'drone_clip.mp4',
      originalSize: 45200000,
      compressedSize: 14800000,
      type: 'video',
      timeTakenMs: 2450,
      savedToMediaStore: true,
      timestamp: '5m ago'
    }
  ]);

  const [auditLogs, setAuditLogs] = useState<AuditLogEntry[]>([
    {
      id: '1',
      fileName: 'vacation_photo.jpg',
      mediaType: 'IMAGE',
      originalSize: 4800000,
      compressedSize: 1100000,
      reductionPercent: 77,
      durationMs: 420,
      qualityPreset: 'MEDIUM',
      status: 'SUCCESS',
      timestamp: new Date().toLocaleTimeString()
    },
    {
      id: '2',
      fileName: 'drone_clip.mp4',
      mediaType: 'VIDEO',
      originalSize: 45200000,
      compressedSize: 14800000,
      reductionPercent: 67,
      durationMs: 2450,
      qualityPreset: 'MEDIUM',
      status: 'SUCCESS',
      timestamp: new Date(Date.now() - 300000).toLocaleTimeString()
    }
  ]);

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedTab(id);
    setTimeout(() => setCopiedTab(null), 2000);
  };

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  const triggerHaptic = () => {
    setHapticTriggered(true);
    setTimeout(() => setHapticTriggered(false), 300);
  };

  // Simulate Single or Batch Compression
  const handleSimulateSelect = (type: 'image' | 'video') => {
    if (isLoading) return;
    triggerHaptic();

    const selectedPreset = type === 'image' ? imageQuality : videoQuality;
    const ratio = selectedPreset === 'Low' ? 0.28 : selectedPreset === 'Medium' ? 0.48 : 0.72;

    if (isBatchMode) {
      // Batch Simulation with 3 items
      const batchTotal = 3;
      setIsLoading(true);
      setBatchProgress({ current: 1, total: batchTotal });
      setCurrentAction(`Batch Processing (1/${batchTotal})...`);
      setStatusMessage(`Queued ${batchTotal} ${type === 'image' ? 'images' : 'videos'} for batch compression.`);
      setCurrentResult(null);

      let step = 1;
      const batchItems: CompressionItem[] = [];

      const interval = setInterval(() => {
        const dummyOriginal = type === 'image' ? Math.round(3000000 + Math.random() * 2000000) : Math.round(35000000 + Math.random() * 15000000);
        const dummyCompressed = Math.round(dummyOriginal * ratio);
        const duration = type === 'image' ? Math.round(350 + Math.random() * 200) : Math.round(1800 + Math.random() * 800);

        const newItem: CompressionItem = {
          id: `${Date.now()}_${step}`,
          name: `${type.toUpperCase()}_BATCH_${step}_${Date.now().toString().slice(-4)}.${type === 'image' ? 'jpg' : 'mp4'}`,
          originalSize: dummyOriginal,
          compressedSize: dummyCompressed,
          type,
          timeTakenMs: duration,
          savedToMediaStore: autoSaveMediaStore,
          timestamp: 'Just now'
        };

        batchItems.unshift(newItem);

        // Add to audit log
        setAuditLogs(prev => [
          {
            id: newItem.id,
            fileName: newItem.name,
            mediaType: type.toUpperCase(),
            originalSize: newItem.originalSize,
            compressedSize: newItem.compressedSize,
            reductionPercent: Math.round(100 - (dummyCompressed / dummyOriginal) * 100),
            durationMs: duration,
            qualityPreset: selectedPreset.toUpperCase(),
            status: 'SUCCESS',
            timestamp: new Date().toLocaleTimeString()
          },
          ...prev
        ]);

        if (step < batchTotal) {
          step++;
          setBatchProgress({ current: step, total: batchTotal });
          setCurrentAction(`Batch Processing (${step}/${batchTotal})...`);
        } else {
          clearInterval(interval);
          setIsLoading(false);
          setBatchProgress(null);
          setCurrentResult(batchItems[0]);
          setRecentItems(prev => [...batchItems, ...prev].slice(0, 20));
          triggerHaptic();
          setStatusMessage(`Batch completed: ${batchTotal} items compressed!${autoSaveMediaStore ? ` Saved to public ${type === 'image' ? 'Pictures' : 'Movies'} gallery.` : ''}`);
          showToast(`Batch completed: ${batchTotal} items successfully compressed.`);
        }
      }, 900);

    } else {
      // Single Item
      const originalSize = type === 'image' ? 3800000 : 38500000;
      const dummyName = type === 'image' ? `IMG_${Date.now().toString().slice(-4)}.jpg` : `VID_${Date.now().toString().slice(-4)}.mp4`;

      setIsLoading(true);
      setCurrentAction(`Compressing ${type === 'image' ? 'Image' : 'Video'} (${selectedPreset})...`);
      setStatusMessage(`Original: ${(originalSize / (1024 * 1024)).toFixed(1)} MB • Applying ${selectedPreset} preset...`);
      setCurrentResult(null);

      const startTime = Date.now();

      setTimeout(() => {
        const compressedSize = Math.round(originalSize * ratio);
        const duration = Date.now() - startTime;
        const savedPercent = Math.round(100 - (compressedSize / originalSize) * 100);

        const newItem: CompressionItem = {
          id: Date.now().toString(),
          name: dummyName,
          originalSize,
          compressedSize,
          type,
          timeTakenMs: duration,
          savedToMediaStore: autoSaveMediaStore,
          timestamp: 'Just now'
        };

        setIsLoading(false);
        setCurrentResult(newItem);
        setRecentItems(prev => [newItem, ...prev].slice(0, 20));

        // Add to audit log
        setAuditLogs(prev => [
          {
            id: newItem.id,
            fileName: newItem.name,
            mediaType: type.toUpperCase(),
            originalSize: newItem.originalSize,
            compressedSize: newItem.compressedSize,
            reductionPercent: savedPercent,
            durationMs: duration,
            qualityPreset: selectedPreset.toUpperCase(),
            status: 'SUCCESS',
            timestamp: new Date().toLocaleTimeString()
          },
          ...prev
        ]);

        setStatusMessage(`${type === 'image' ? 'Image' : 'Video'} compressed successfully! (${savedPercent}% saved)${autoSaveMediaStore ? ` • Saved to MediaStore` : ''}`);
        triggerHaptic();
        showToast(`Compression finished! Saved ${savedPercent}% space.`);
      }, type === 'image' ? 1400 : 2200);
    }
  };

  const handleClearAllRecents = () => {
    triggerHaptic();
    setRecentItems([]);
    setCurrentResult(null);
    setStatusMessage('All temporary cache files and compression history cleared.');
    showToast('Cleared all temporary cache files and history list.');
  };

  const handleDelete = () => {
    triggerHaptic();
    setCurrentResult(null);
    setStatusMessage('Compressed file removed from temporary cache.');
    showToast('Temporary file deleted from cache.');
  };

  const handleShare = () => {
    triggerHaptic();
    showToast('Triggered Android ShareSheet intent (ACTION_SEND)');
  };

  const handleExportLogs = () => {
    triggerHaptic();
    const logContent = [
      '=========================================================================',
      'MEDIA COMPRESSOR - AUDIT LOG REPORT',
      `Generated on: ${new Date().toLocaleString()}`,
      `Total Executions Logged: ${auditLogs.length}`,
      '=========================================================================\n',
      ...auditLogs.map((log, index) => 
        `[${index + 1}] Date: ${log.timestamp}\n` +
        `     File: ${log.fileName} (${log.mediaType})\n` +
        `     Preset: ${log.qualityPreset} | Status: ${log.status}\n` +
        `     Original: ${(log.originalSize / (1024 * 1024)).toFixed(2)} MB -> Compressed: ${(log.compressedSize / (1024 * 1024)).toFixed(2)} MB\n` +
        `     Space Reduction: ${log.reductionPercent}% | Time Taken: ${log.durationMs} ms (${(log.durationMs / 1000).toFixed(2)}s)\n` +
        '-------------------------------------------------------------------------'
      )
    ].join('\n');

    const blob = new Blob([logContent], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `compression_audit_log_${Date.now()}.txt`;
    link.click();
    URL.revokeObjectURL(url);

    showToast('Exported audit log to local text file (.txt)');
  };

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col font-sans">
      {/* Top Navigation */}
      <header className="border-b border-neutral-800 bg-neutral-900/80 backdrop-blur-md px-6 py-3.5 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className={`w-9 h-9 rounded-xl bg-purple-600/20 border border-purple-500/30 flex items-center justify-center text-purple-400 transition-transform ${hapticTriggered ? 'scale-110 ring-2 ring-purple-400' : ''}`}>
            <Smartphone className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-semibold text-white tracking-tight">Android Media Compressor</h1>
            <p className="text-xs text-neutral-400">Jetpack Compose • Batch Queue • Theme Switcher • MediaStore Export</p>
          </div>
        </div>

        {/* Tab Switcher */}
        <div className="flex items-center gap-1 bg-neutral-800/80 p-1 rounded-xl border border-neutral-700/60">
          <button
            id="tab-preview-btn"
            onClick={() => setActiveTab('preview')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'preview' ? 'bg-purple-600 text-white shadow-sm' : 'text-neutral-300 hover:text-white'
            }`}
          >
            Interactive Compose Preview
          </button>
          <button
            id="tab-gradle-btn"
            onClick={() => setActiveTab('gradle')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'gradle' ? 'bg-purple-600 text-white shadow-sm' : 'text-neutral-300 hover:text-white'
            }`}
          >
            build.gradle.kts
          </button>
          <button
            id="tab-mainactivity-btn"
            onClick={() => setActiveTab('mainActivity')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'mainActivity' ? 'bg-purple-600 text-white shadow-sm' : 'text-neutral-300 hover:text-white'
            }`}
          >
            MainActivity.kt
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 flex overflow-hidden">
        {activeTab === 'preview' && (
          <div className="flex-1 flex flex-col lg:flex-row items-center justify-center p-6 gap-8 overflow-y-auto">
            {/* Phone Screen Canvas Simulator */}
            <div className="w-full max-w-sm rounded-[40px] border-4 border-neutral-700 bg-neutral-900 p-3 shadow-2xl relative">
              {/* Notch */}
              <div className="absolute top-5 left-1/2 -translate-x-1/2 w-28 h-4 bg-neutral-800 rounded-full z-20 flex items-center justify-center">
                <div className="w-3 h-3 rounded-full bg-neutral-900 border border-neutral-700"></div>
              </div>

              {/* Compose Screen Frame (Light / Dark Adaptive) */}
              <div className={`w-full h-[660px] rounded-[30px] transition-colors duration-200 flex flex-col pt-10 pb-3 relative overflow-hidden select-none ${
                isDarkMode ? 'bg-[#141218] text-[#E6E1E5]' : 'bg-[#FEF7FF] text-[#1D1B20]'
              }`}>
                {/* Top App Bar with Settings & Audit Triggers */}
                <div className={`px-5 py-2 flex items-center justify-between border-b ${
                  isDarkMode ? 'border-neutral-800' : 'border-purple-100'
                }`}>
                  <span className={`font-bold text-base ${isDarkMode ? 'text-white' : 'text-[#1D1B20]'}`}>Media Compressor</span>
                  <div className="flex items-center gap-1">
                    <button
                      id="sim-open-audit-btn"
                      onClick={() => {
                        triggerHaptic();
                        setShowAuditModal(true);
                      }}
                      className={`p-1.5 rounded-full transition active:scale-95 ${
                        isDarkMode ? 'hover:bg-neutral-800 text-purple-300' : 'hover:bg-purple-100 text-[#6750A4]'
                      }`}
                      title="View & Export Audit Log"
                    >
                      <FileText className="w-4 h-4" />
                    </button>
                    <button
                      id="sim-open-settings-btn"
                      onClick={() => {
                        triggerHaptic();
                        setShowSettingsModal(true);
                      }}
                      className={`p-1.5 rounded-full transition active:scale-95 ${
                        isDarkMode ? 'hover:bg-neutral-800 text-purple-300' : 'hover:bg-purple-100 text-[#6750A4]'
                      }`}
                      title="App Settings & Presets"
                    >
                      <Sliders className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                {/* Scrollable Body */}
                <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3">
                  {/* Batch Selection Mode Toggle Bar */}
                  <div className={`rounded-xl p-2.5 flex items-center justify-between border ${
                    isDarkMode ? 'bg-[#211F26] border-neutral-800' : 'bg-[#F7F2FA] border-purple-100'
                  }`}>
                    <div className="flex items-center gap-2">
                      <Layers className={`w-4 h-4 ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`} />
                      <div>
                        <div className="text-xs font-semibold">Batch Mode</div>
                        <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>
                          {isBatchMode ? 'Pick multiple files at once' : 'Single file selection'}
                        </div>
                      </div>
                    </div>
                    <button
                      onClick={() => {
                        triggerHaptic();
                        setIsBatchMode(!isBatchMode);
                        showToast(`Switched to ${!isBatchMode ? 'Batch Multi-Select' : 'Single Selection'} mode`);
                      }}
                      className={`w-10 h-5 rounded-full transition-colors relative p-0.5 ${
                        isBatchMode ? (isDarkMode ? 'bg-purple-500' : 'bg-[#6750A4]') : (isDarkMode ? 'bg-neutral-700' : 'bg-neutral-300')
                      }`}
                    >
                      <div className={`w-4 h-4 rounded-full bg-white transition-transform ${isBatchMode ? 'translate-x-5' : 'translate-x-0'}`} />
                    </button>
                  </div>

                  {/* Two Main Pickers (Single or Batch) */}
                  <div className="grid grid-cols-2 gap-2.5">
                    <button
                      id="sim-compress-img-btn"
                      disabled={isLoading}
                      onClick={() => handleSimulateSelect('image')}
                      className="h-12 bg-[#6750A4] hover:bg-[#594294] disabled:opacity-50 text-white rounded-xl flex items-center justify-center gap-1.5 font-semibold text-xs shadow transition active:scale-[0.98]"
                    >
                      <Image className="w-4 h-4" />
                      {isBatchMode ? 'Batch Images' : 'Compress Image'}
                    </button>
                    <button
                      id="sim-compress-vid-btn"
                      disabled={isLoading}
                      onClick={() => handleSimulateSelect('video')}
                      className="h-12 bg-[#625B71] hover:bg-[#534d61] disabled:opacity-50 text-white rounded-xl flex items-center justify-center gap-1.5 font-semibold text-xs shadow transition active:scale-[0.98]"
                    >
                      <Video className="w-4 h-4" />
                      {isBatchMode ? 'Batch Videos' : 'Compress Video'}
                    </button>
                  </div>

                  {/* Loading Card with Batch Progress Indicator */}
                  {isLoading && (
                    <div className={`rounded-2xl p-4 text-center border animate-in fade-in ${
                      isDarkMode ? 'bg-[#211F26] border-purple-900/40' : 'bg-[#F7F2FA] border-purple-100'
                    }`}>
                      {batchProgress ? (
                        <div className="space-y-2">
                          <div className="w-full bg-purple-200 dark:bg-neutral-800 h-2 rounded-full overflow-hidden">
                            <div
                              className="bg-[#6750A4] h-full transition-all duration-300"
                              style={{ width: `${(batchProgress.current / batchProgress.total) * 100}%` }}
                            />
                          </div>
                          <div className="text-xs font-bold text-[#6750A4] dark:text-purple-300">
                            Compressing Item {batchProgress.current} of {batchProgress.total}...
                          </div>
                        </div>
                      ) : (
                        <div className="w-7 h-7 border-2 border-[#6750A4] dark:border-purple-300 border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
                      )}
                      <div className="text-xs font-semibold text-[#6750A4] dark:text-purple-300 mt-1">{currentAction}</div>
                      <div className={`text-[10px] mt-1 ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>{statusMessage}</div>
                    </div>
                  )}

                  {/* Active Result Card */}
                  {currentResult && (
                    <div className={`rounded-2xl p-3.5 shadow-sm space-y-2.5 border animate-in fade-in ${
                      isDarkMode ? 'bg-[#211F26] border-purple-900/40' : 'bg-[#F7F2FA] border-purple-200'
                    }`}>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-1.5 text-emerald-500 text-xs font-bold">
                          <CheckCheck className="w-4 h-4" />
                          <span>{currentResult.type === 'video' ? 'Video' : 'Image'} Compressed</span>
                        </div>
                        <button
                          onClick={handleDelete}
                          className="text-red-400 hover:text-red-600 p-1 rounded-md"
                          title="Delete temporary file"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>

                      {/* Mock Thumbnail */}
                      <div className={`w-full h-24 rounded-lg flex items-center justify-center text-xs ${
                        isDarkMode ? 'bg-neutral-800' : 'bg-neutral-200'
                      }`}>
                        {currentResult.type === 'image' ? <Image className="w-8 h-8 opacity-60" /> : <Video className="w-8 h-8 opacity-60" />}
                      </div>

                      {/* Stats */}
                      <div className="flex justify-between text-xs pt-1">
                        <div>
                          <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>Original</div>
                          <div className="font-semibold">{(currentResult.originalSize / (1024 * 1024)).toFixed(2)} MB</div>
                        </div>
                        <div className="text-right">
                          <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>Compressed</div>
                          <div className="font-bold text-[#6750A4] dark:text-purple-300">{(currentResult.compressedSize / (1024 * 1024)).toFixed(2)} MB</div>
                        </div>
                      </div>

                      <div className="flex items-center justify-between text-[11px] font-semibold">
                        <span className="text-emerald-500">
                          Reduction: {Math.round(100 - (currentResult.compressedSize / currentResult.originalSize) * 100)}% smaller
                        </span>
                        <span className={isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}>
                          {currentResult.timeTakenMs} ms
                        </span>
                      </div>

                      {currentResult.savedToMediaStore && (
                        <div className="text-[10px] text-purple-600 dark:text-purple-300 font-medium flex items-center gap-1">
                          <HardDrive className="w-3 h-3" />
                          <span>Saved to public {currentResult.type === 'video' ? 'Movies' : 'Pictures'} folder</span>
                        </div>
                      )}

                      {/* Actions */}
                      <div className="flex gap-2 pt-1">
                        <button
                          onClick={handleShare}
                          className="flex-1 bg-[#6750A4] text-white py-1.5 rounded-lg text-xs font-medium flex items-center justify-center gap-1.5 shadow-sm active:scale-95 transition"
                        >
                          <Share2 className="w-3 h-3" />
                          Share
                        </button>
                        <button
                          onClick={handleDelete}
                          className="flex-1 border border-red-400/40 text-red-500 py-1.5 rounded-lg text-xs font-medium flex items-center justify-center gap-1.5 active:scale-95 transition"
                        >
                          <Trash2 className="w-3 h-3" />
                          Delete
                        </button>
                      </div>
                    </div>
                  )}

                  {/* System Status when idle */}
                  {!currentResult && !isLoading && (
                    <div className={`rounded-xl p-3 text-center border ${
                      isDarkMode ? 'bg-[#211F26] border-neutral-800' : 'bg-[#F7F2FA] border-purple-50'
                    }`}>
                      <div className={`text-[10px] font-bold uppercase tracking-wider mb-0.5 ${
                        isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'
                      }`}>Status</div>
                      <p className={`text-[11px] ${isDarkMode ? 'text-neutral-300' : 'text-neutral-600'}`}>{statusMessage}</p>
                    </div>
                  )}

                  {/* Recent Compressions Header with 'Clear All' Button */}
                  <div className="pt-2">
                    <div className="flex items-center justify-between mb-1.5">
                      <div className="flex items-center gap-1.5 text-xs font-bold">
                        <History className={`w-3.5 h-3.5 ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`} />
                        <span>Recent Compressions ({recentItems.length})</span>
                      </div>
                      {recentItems.length > 0 && (
                        <button
                          id="sim-clear-recents-btn"
                          onClick={handleClearAllRecents}
                          className="text-[10px] font-semibold text-red-500 hover:text-red-600 flex items-center gap-1 px-1.5 py-0.5 rounded transition"
                          title="Wipe entire temporary cache and list"
                        >
                          <Trash2 className="w-3 h-3" />
                          Clear All
                        </button>
                      )}
                    </div>

                    {recentItems.length === 0 ? (
                      <div className={`text-center py-4 text-xs rounded-xl border ${
                        isDarkMode ? 'bg-[#211F26] border-neutral-800 text-neutral-500' : 'bg-[#F7F2FA] border-purple-50 text-neutral-400'
                      }`}>
                        No recent compressions in cache
                      </div>
                    ) : (
                      <div className="space-y-1.5">
                        {recentItems.map((item) => (
                          <div
                            key={item.id}
                            className={`border rounded-xl p-2 flex items-center justify-between text-xs shadow-xs ${
                              isDarkMode ? 'bg-[#211F26] border-neutral-800' : 'bg-white border-purple-100'
                            }`}
                          >
                            <div className="flex items-center gap-2 truncate">
                              <div className={`w-6 h-6 rounded flex items-center justify-center ${
                                isDarkMode ? 'bg-purple-950 text-purple-300' : 'bg-purple-50 text-[#6750A4]'
                              }`}>
                                {item.type === 'video' ? <Video className="w-3.5 h-3.5" /> : <Image className="w-3.5 h-3.5" />}
                              </div>
                              <div className="truncate">
                                <div className="font-medium truncate text-[11px]">{item.name}</div>
                                <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>
                                  {(item.originalSize / (1024 * 1024)).toFixed(1)}MB → {(item.compressedSize / (1024 * 1024)).toFixed(1)}MB
                                  {item.timeTakenMs > 0 && ` • ${item.timeTakenMs}ms`}
                                </div>
                              </div>
                            </div>
                            <button onClick={handleShare} className={`p-1 ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`}>
                              <Share2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                {/* Snackbar / Toast Overlay */}
                {toastMessage && (
                  <div className="absolute bottom-6 left-4 right-4 bg-neutral-900 border border-neutral-700 text-white text-[11px] py-2 px-3 rounded-lg shadow-xl flex items-center gap-2 animate-in slide-in-from-bottom duration-200 z-30">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                    <span className="truncate">{toastMessage}</span>
                  </div>
                )}

                {/* Settings Bottom Sheet Modal Simulation */}
                {showSettingsModal && (
                  <div className="absolute inset-0 bg-black/50 z-30 flex flex-col justify-end animate-in fade-in">
                    <div className={`rounded-t-2xl p-4 shadow-2xl space-y-3.5 border-t max-h-[85%] overflow-y-auto ${
                      isDarkMode ? 'bg-[#141218] border-neutral-700 text-[#E6E1E5]' : 'bg-[#FEF7FF] border-purple-200 text-[#1D1B20]'
                    }`}>
                      <div className="flex items-center justify-between">
                        <div className="font-bold text-sm">App &amp; Quality Settings</div>
                        <button onClick={() => setShowSettingsModal(false)} className={`text-xs font-semibold ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`}>
                          Done
                        </button>
                      </div>

                      {/* Theme Toggle Section */}
                      <div>
                        <div className="text-xs font-semibold mb-1.5">App Theme</div>
                        <div className="grid grid-cols-3 gap-1.5">
                          {(['SYSTEM', 'LIGHT', 'DARK'] as const).map((mode) => (
                            <button
                              key={mode}
                              onClick={() => {
                                triggerHaptic();
                                setThemeMode(mode);
                              }}
                              className={`py-1.5 rounded-lg text-xs font-medium flex items-center justify-center gap-1 transition ${
                                themeMode === mode
                                  ? (isDarkMode ? 'bg-purple-600 text-white shadow-sm' : 'bg-[#6750A4] text-white shadow-sm')
                                  : (isDarkMode ? 'bg-neutral-800 text-neutral-300' : 'bg-[#F7F2FA] text-neutral-700')
                              }`}
                            >
                              {mode === 'LIGHT' && <Sun className="w-3 h-3" />}
                              {mode === 'DARK' && <Moon className="w-3 h-3" />}
                              {mode === 'SYSTEM' && <Smartphone className="w-3 h-3" />}
                              <span>{mode}</span>
                            </button>
                          ))}
                        </div>
                      </div>

                      <hr className={isDarkMode ? 'border-neutral-800' : 'border-purple-100'} />

                      {/* MediaStore Auto-Save Setting */}
                      <div className="flex items-center justify-between gap-2">
                        <div>
                          <div className="text-xs font-semibold">Auto-save to MediaStore</div>
                          <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>
                            Save outputs to public Pictures/Movies folders
                          </div>
                        </div>
                        <button
                          onClick={() => {
                            triggerHaptic();
                            setAutoSaveMediaStore(!autoSaveMediaStore);
                            showToast(`Auto-save to MediaStore ${!autoSaveMediaStore ? 'enabled' : 'disabled'}`);
                          }}
                          className={`w-10 h-5 rounded-full transition-colors relative p-0.5 shrink-0 ${
                            autoSaveMediaStore ? (isDarkMode ? 'bg-purple-500' : 'bg-[#6750A4]') : (isDarkMode ? 'bg-neutral-700' : 'bg-neutral-300')
                          }`}
                        >
                          <div className={`w-4 h-4 rounded-full bg-white transition-transform ${autoSaveMediaStore ? 'translate-x-5' : 'translate-x-0'}`} />
                        </button>
                      </div>

                      <hr className={isDarkMode ? 'border-neutral-800' : 'border-purple-100'} />

                      {/* Image Quality Chips */}
                      <div>
                        <div className="text-xs font-medium mb-1">Image Quality Preset</div>
                        <div className="grid grid-cols-3 gap-1.5">
                          {(['Low', 'Medium', 'High'] as const).map((q) => (
                            <button
                              key={q}
                              onClick={() => {
                                triggerHaptic();
                                setImageQuality(q);
                              }}
                              className={`py-1.5 rounded-lg text-xs font-medium transition ${
                                imageQuality === q
                                  ? 'bg-[#6750A4] text-white shadow-sm'
                                  : (isDarkMode ? 'bg-neutral-800 text-neutral-300' : 'bg-[#F7F2FA] text-neutral-700')
                              }`}
                            >
                              {q}
                            </button>
                          ))}
                        </div>
                        <p className={`text-[10px] mt-1 ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>
                          {imageQuality === 'Low' ? '55% JPEG • Max 1280px' : imageQuality === 'Medium' ? '75% JPEG • Max 1920px' : '90% JPEG • Max 2560px'}
                        </p>
                      </div>

                      {/* Video Quality Chips */}
                      <div>
                        <div className="text-xs font-medium mb-1">Video Quality Preset</div>
                        <div className="grid grid-cols-3 gap-1.5">
                          {(['Low', 'Medium', 'High'] as const).map((q) => (
                            <button
                              key={q}
                              onClick={() => {
                                triggerHaptic();
                                setVideoQuality(q);
                              }}
                              className={`py-1.5 rounded-lg text-xs font-medium transition ${
                                videoQuality === q
                                  ? 'bg-[#625B71] text-white shadow-sm'
                                  : (isDarkMode ? 'bg-neutral-800 text-neutral-300' : 'bg-[#F7F2FA] text-neutral-700')
                              }`}
                            >
                              {q}
                            </button>
                          ))}
                        </div>
                        <p className={`text-[10px] mt-1 ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>
                          {videoQuality === 'Low' ? 'CRF 32 • 800 kbps (640p)' : videoQuality === 'Medium' ? 'CRF 28 • 1500 kbps (720p)' : 'CRF 23 • 2500 kbps (1080p)'}
                        </p>
                      </div>

                      <button
                        onClick={handleExportLogs}
                        className={`w-full py-2 rounded-xl text-xs font-medium border flex items-center justify-center gap-1.5 transition ${
                          isDarkMode ? 'border-neutral-700 bg-neutral-800 text-purple-300' : 'border-purple-200 bg-white text-[#6750A4]'
                        }`}
                      >
                        <Download className="w-3.5 h-3.5" />
                        Export Audit Log Report (.txt)
                      </button>
                    </div>
                  </div>
                )}

                {/* Audit Log Modal Simulation */}
                {showAuditModal && (
                  <div className="absolute inset-0 bg-black/60 z-30 flex flex-col justify-end animate-in fade-in">
                    <div className={`rounded-t-2xl p-4 shadow-2xl space-y-3 max-h-[85%] overflow-y-auto border-t ${
                      isDarkMode ? 'bg-[#141218] border-neutral-700 text-[#E6E1E5]' : 'bg-[#FEF7FF] border-purple-200 text-[#1D1B20]'
                    }`}>
                      <div className="flex items-center justify-between">
                        <div className="font-bold text-sm flex items-center gap-1.5">
                          <FileText className="w-4 h-4 text-purple-400" />
                          <span>Performance Audit Logs</span>
                        </div>
                        <button onClick={() => setShowAuditModal(false)} className={`text-xs font-semibold ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`}>
                          Close
                        </button>
                      </div>

                      <div className="space-y-1.5 max-h-60 overflow-y-auto">
                        {auditLogs.map((log) => (
                          <div
                            key={log.id}
                            className={`p-2 rounded-lg border text-[10px] space-y-0.5 font-mono ${
                              isDarkMode ? 'bg-neutral-900 border-neutral-800 text-neutral-300' : 'bg-white border-purple-100 text-neutral-700'
                            }`}
                          >
                            <div className="flex justify-between font-bold">
                              <span>{log.fileName}</span>
                              <span className="text-emerald-500">-{log.reductionPercent}%</span>
                            </div>
                            <div className="flex justify-between text-neutral-400">
                              <span>{(log.originalSize / (1024 * 1024)).toFixed(1)}MB → {(log.compressedSize / (1024 * 1024)).toFixed(1)}MB</span>
                              <span>{log.durationMs}ms ({log.qualityPreset})</span>
                            </div>
                          </div>
                        ))}
                      </div>

                      <button
                        onClick={handleExportLogs}
                        className="w-full bg-[#6750A4] text-white py-2 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 shadow-sm active:scale-95 transition"
                      >
                        <Download className="w-3.5 h-3.5" />
                        Download &amp; Share Audit File (.txt)
                      </button>
                    </div>
                  </div>
                )}

                {/* Bottom Bar */}
                <div className={`w-28 h-1 rounded-full mx-auto mt-2 shrink-0 ${isDarkMode ? 'bg-neutral-700' : 'bg-neutral-300'}`}></div>
              </div>
            </div>

            {/* Side Information Panel */}
            <div className="w-full max-w-md space-y-4">
              <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6 shadow-xl">
                <div className="flex items-center gap-2 text-purple-400 font-semibold text-sm mb-3">
                  <Sparkles className="w-4 h-4" />
                  <span>Newly Implemented Features</span>
                </div>

                <div className="space-y-2.5 text-xs text-neutral-300 mb-5">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span><strong>Batch Selection Mode</strong>: Pick multiple images/videos using <code>PickMultipleVisualMedia()</code> with sequential ViewModel queue.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span><strong>User Theme Toggle</strong>: Switch seamlessly between System, Light, and Dark modes in the Settings bottom sheet.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span><strong>MediaStore Auto-Save</strong>: Save compressed files to public <code>Pictures/MediaCompressor</code> or <code>Movies/MediaCompressor</code>.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span><strong>Clear Recent Compressions</strong>: One-tap button wipes all temporary cache files and recent history.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span><strong>Performance Audit Log Export</strong>: Generates and exports detailed <code>.txt</code> performance reports with execution metrics.</span>
                  </div>
                </div>

                <div className="flex gap-3">
                  <button
                    onClick={() => setActiveTab('mainActivity')}
                    className="flex-1 bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold py-2.5 px-4 rounded-xl transition flex items-center justify-center gap-2 shadow"
                  >
                    <FileCode2 className="w-4 h-4" />
                    View MainActivity.kt
                  </button>
                  <button
                    onClick={() => {
                      setCurrentResult(null);
                      setStatusMessage('Ready. Select media or toggle Batch mode.');
                      showToast('Simulator reset to initial state');
                    }}
                    className="bg-neutral-800 hover:bg-neutral-700 text-neutral-300 text-xs font-medium py-2.5 px-4 rounded-xl transition flex items-center gap-2"
                  >
                    <RefreshCw className="w-3.5 h-3.5" />
                    Reset
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* build.gradle.kts View */}
        {activeTab === 'gradle' && (
          <div className="flex-1 p-6 flex flex-col h-full overflow-hidden">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono text-neutral-400">app/build.gradle.kts</span>
                <span className="text-[10px] bg-neutral-800 text-purple-400 px-2 py-0.5 rounded-full border border-neutral-700">
                  Kotlin DSL
                </span>
              </div>
              <button
                onClick={() => copyToClipboard(GRADLE_CODE, 'gradle')}
                className="flex items-center gap-1.5 bg-purple-600 hover:bg-purple-500 text-white text-xs font-medium px-3 py-1.5 rounded-lg transition"
              >
                {copiedTab === 'gradle' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedTab === 'gradle' ? 'Copied' : 'Copy build.gradle.kts'}
              </button>
            </div>
            <pre className="flex-1 bg-neutral-900 border border-neutral-800 rounded-xl p-4 text-xs font-mono text-neutral-300 overflow-auto leading-relaxed">
              <code>{GRADLE_CODE}</code>
            </pre>
          </div>
        )}

        {/* MainActivity.kt View */}
        {activeTab === 'mainActivity' && (
          <div className="flex-1 p-6 flex flex-col h-full overflow-hidden">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono text-neutral-400">app/src/main/java/com/example/mediacompressor/MainActivity.kt</span>
                <span className="text-[10px] bg-neutral-800 text-purple-400 px-2 py-0.5 rounded-full border border-neutral-700">
                  Jetpack Compose + ViewModel + Batch &amp; MediaStore
                </span>
              </div>
              <button
                onClick={() => copyToClipboard(MAIN_ACTIVITY_CODE, 'mainActivity')}
                className="flex items-center gap-1.5 bg-purple-600 hover:bg-purple-500 text-white text-xs font-medium px-3 py-1.5 rounded-lg transition"
              >
                {copiedTab === 'mainActivity' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedTab === 'mainActivity' ? 'Copied' : 'Copy MainActivity.kt'}
              </button>
            </div>
            <pre className="flex-1 bg-neutral-900 border border-neutral-800 rounded-xl p-4 text-xs font-mono text-neutral-300 overflow-auto leading-relaxed">
              <code>{MAIN_ACTIVITY_CODE}</code>
            </pre>
          </div>
        )}
      </main>
    </div>
  );
}
