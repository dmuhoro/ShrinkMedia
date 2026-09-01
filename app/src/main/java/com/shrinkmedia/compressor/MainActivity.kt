package com.shrinkmedia.compressor

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.SessionState
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument as ITextDoc
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Image as ITextImage
import com.itextpdf.layout.Document as ITextLayoutDoc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.UUID
import kotlin.math.log10
import kotlin.math.pow

enum class AppThemeMode { SYSTEM, LIGHT, DARK }
enum class OcrLanguage(val key: String, val label: String) {
    ENGLISH("en_latin", "English");
    companion object {
        fun fromKey(key: String): OcrLanguage = entries.firstOrNull { it.key == key } ?: ENGLISH
    }
}
enum class CompressionQuality(val label: String, val imageQuality: Int, val maxDimension: Int, val videoCrf: Int, val videoBitrate: String) {
    LOW("Low / maximum savings", 55, 1280, 32, "800k"), MEDIUM("Balanced", 75, 1920, 28, "1500k"), HIGH("High / best quality", 90, 2560, 23, "2500k")
}
enum class ToolkitTab(val label: String) { MEDIA("Media"), DOCUMENTS("Documents"), AI("Elite AI") }
data class MediaResult(val name: String, val before: Long, val output: File, val isVideo: Boolean)
data class PdfMetrics(val name: String, val pages: Int, val bytes: Long)
data class CompressionAuditDetail(
    val qualityPreset: String,
    val targetBitrate: String,
    val resolutionScaling: String,
    val durationMs: Long,
    val mediaType: String,
    val status: String = "success"
)

data class RecentCompression(
    val name: String,
    val before: Long,
    val after: Long,
    val filePath: String,
    val isVideo: Boolean,
    val audit: CompressionAuditDetail,
    val ts: Long = System.currentTimeMillis()
)
data class PendingDeletion(val recent: RecentCompression, val trashFile: File)

data class UiState(
    val tab: ToolkitTab = ToolkitTab.MEDIA, val busy: Boolean = false,
    val status: String = "Ready — all processing stays on-device.",
    val quality: CompressionQuality = CompressionQuality.MEDIUM,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val ocrLanguage: OcrLanguage = OcrLanguage.ENGLISH,
    val enableBatch: Boolean = false,
    val showSettings: Boolean = false,
    val mediaResult: MediaResult? = null, val imageUris: List<Uri> = emptyList(), val mergeUris: List<Uri> = emptyList(), val pdfUri: Uri? = null,
    val pdfMetrics: PdfMetrics? = null, val documentOutput: File? = null, val extractedText: String = "",
    val ocrUri: Uri? = null, val ocrText: String? = null, val ocrStatus: String = "",
    val autoSave: Boolean = false, val pauseOnLowBattery: Boolean = false,
    val totalSavedBytes: Long = 0L, val totalFiles: Long = 0L,
    val recent: List<RecentCompression> = emptyList(),
    val pendingDeletion: PendingDeletion? = null
)

class ToolkitViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = SettingsRepository(application.applicationContext)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Global snackbar channel: UI-side observer surfaces failures + status (I.6). */
    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toast: SharedFlow<String> = _toast.asSharedFlow()

    init {
        viewModelScope.launch {
            settings.userSettingsFlow.collect { saved ->
                _state.update {
                    it.copy(
                        quality = saved.imageQuality,
                        autoSave = saved.autoSaveToMediaStore,
                        pauseOnLowBattery = saved.pauseCompressionOnLowBattery,
                        themeMode = saved.themeMode,
                        ocrLanguage = OcrLanguage.fromKey(saved.ocrLanguage),
                        enableBatch = saved.enableBatch,
                        totalSavedBytes = saved.totalHistoricalSavedBytes,
                        totalFiles = saved.totalHistoricalFilesCount
                    )
                }
            }
        }
    }

    private val trashDir: File
        get() = File(getApplication<Application>().applicationContext.filesDir, "trash").apply { mkdirs() }

    fun tab(tab: ToolkitTab) = _state.update { it.copy(tab = tab) }
    fun quality(quality: CompressionQuality) {
        _state.update { it.copy(quality = quality) }
        viewModelScope.launch { settings.updateImageQuality(quality); settings.updateVideoQuality(quality) }
        _toast.tryEmit("Compression quality set to ${quality.label}")
    }
    fun theme(mode: AppThemeMode) {
        _state.update { it.copy(themeMode = mode) }
        viewModelScope.launch { settings.updateThemeMode(mode) }
        _toast.tryEmit("Theme set to ${mode.name.lowercase()}")
    }
    fun ocrLanguage(language: OcrLanguage) {
        _state.update { it.copy(ocrLanguage = language) }
        viewModelScope.launch { settings.updateOcrLanguage(language.key) }
        _toast.tryEmit("Recognition language set to ${language.label}")
    }
    fun setEnableBatch(enabled: Boolean) {
        _state.update { it.copy(enableBatch = enabled) }
        viewModelScope.launch { settings.updateEnableBatch(enabled) }
        _toast.tryEmit(if (enabled) "Batch mode enabled" else "Batch mode disabled")
    }
    fun autoSave(enabled: Boolean) {
        _state.update { it.copy(autoSave = enabled) }
        viewModelScope.launch { settings.updateAutoSave(enabled) }
        _toast.tryEmit(if (enabled) "Outputs will be saved to your gallery" else "Gallery auto-save off")
    }
    fun pauseOnLowBattery(enabled: Boolean) {
        _state.update { it.copy(pauseOnLowBattery = enabled) }
        viewModelScope.launch { settings.updatePauseCompressionOnLowBattery(enabled) }
    }
    fun showSettings(show: Boolean) = _state.update { it.copy(showSettings = show) }
    fun images(uris: List<Uri>) = _state.update { it.copy(imageUris = uris, documentOutput = null) }
    fun mergePdfs(uris: List<Uri>) = _state.update { it.copy(mergeUris = uris, documentOutput = null) }

    fun compress(uri: Uri, video: Boolean) = work(if (video) "Compressing video locally…" else "Compressing image locally…") {
        val context = getApplication<Application>().applicationContext
        val quality = _state.value.quality
        val inputBytes = getFileSizeFromUri(context, uri)
        val startedAt = System.currentTimeMillis()
        val output = requireNotNull(if (video) compressVideoFile(context, uri, quality) else compressImageFile(context, uri, quality)) { "Compression failed: unsupported type or unreadable file." }
        val audit = buildAuditDetail(quality, video, System.currentTimeMillis() - startedAt)
        onCompressionSucceeded(context, MediaResult(getFileNameFromUri(context, uri) ?: output.name, inputBytes, output, video), audit)
        "Finished ${output.name}"
    }

    fun batchImages(uris: List<Uri>) = startBatch(uris, video = false)
    fun batchVideos(uris: List<Uri>) = startBatch(uris, video = true)

    private fun startBatch(uris: List<Uri>, video: Boolean) {
        if (uris.isEmpty() || _state.value.busy) return
        val context = getApplication<Application>().applicationContext
        BatchCompressionService.startBatch(context, uris, video, _state.value.quality.name, _state.value.autoSave)
        _state.update { it.copy(status = "Foreground batch started: ${uris.size} ${if (video) "videos" else "images"} queued on-device.") }
    }

    private fun buildAuditDetail(quality: CompressionQuality, video: Boolean, durationMs: Long): CompressionAuditDetail =
        CompressionAuditDetail(
            qualityPreset = quality.name,
            targetBitrate = if (video) quality.videoBitrate else "JPEG q=${quality.imageQuality}",
            resolutionScaling = if (video) {
                if (quality == CompressionQuality.LOW) "scale=-2:720" else "scale=-2:1080"
            } else {
                "max ${quality.maxDimension}px"
            },
            durationMs = durationMs,
            mediaType = if (video) "video/mp4" else "image/jpeg"
        )

    /** Single source for recording a successful compression into recent history. */
    private fun onCompressionSucceeded(context: Context, result: MediaResult, audit: CompressionAuditDetail) {
        val savings = (result.before - result.output.length()).coerceAtLeast(0L)
        if (_state.value.autoSave) saveToPublicMediaStore(context, result.output, result.isVideo)
        viewModelScope.launch { settings.recordCompressionSavings(savings) }
        _state.update {
            it.copy(
                mediaResult = result,
                recent = (listOf(
                    RecentCompression(
                        result.name,
                        result.before,
                        result.output.length(),
                        result.output.absolutePath,
                        result.isVideo,
                        audit
                    )
                ) + it.recent).take(5)
            )
        }
    }

    fun pdf(uri: Uri) = work("Reading PDF metrics…") {
        val context = getApplication<Application>().applicationContext
        val metrics = readPdfMetrics(context, uri)
        _state.update { it.copy(pdfUri = uri, pdfMetrics = metrics, extractedText = "", documentOutput = null) }
        "Loaded ${metrics.pages} page${if (metrics.pages == 1) "" else "s"}"
    }

    fun buildPdf() = work("Generating PDF portfolio…") {
        val context = getApplication<Application>().applicationContext
        val output = createPdfFromImages(context, _state.value.imageUris)
        _state.update { it.copy(documentOutput = output, mediaResult = null) }
        _toast.tryEmit("Created ${output.name}")
        addDocumentToRecent(context, output)
        "Created ${output.name}"
    }

    fun mergePdfFiles() = work("Merging PDFs locally…") {
        val context = getApplication<Application>().applicationContext
        val output = mergePdfDocuments(context, _state.value.mergeUris)
        _state.update { it.copy(documentOutput = output, mediaResult = null) }
        _toast.tryEmit("Created ${output.name}")
        addDocumentToRecent(context, output)
        "Created ${output.name}"
    }

    fun splitPdf() = work("Splitting PDF pages locally…") {
        val context = getApplication<Application>().applicationContext
        val outputs = splitPdfIntoPages(context, requireNotNull(_state.value.pdfUri) { "Choose a PDF first." })
        _state.update { it.copy(documentOutput = outputs.first(), mediaResult = null) }
        _toast.tryEmit("Created ${outputs.size} PDF page files")
        addDocumentToRecent(context, outputs.first())
        "Created ${outputs.size} PDF page files"
    }

    fun extractText() = work("Extracting local PDF text…") {
        val context = getApplication<Application>().applicationContext
        val text = extractRawTextFromUri(context, requireNotNull(_state.value.pdfUri) { "Choose a PDF first." })
        _state.update { it.copy(extractedText = text) }
        _toast.tryEmit(if (text.isBlank()) "No extractable text found in this PDF." else "Extracted ${text.length} characters locally")
        "Extracted ${text.length} characters locally"
    }

    private fun addDocumentToRecent(context: Context, output: File) {
        val audit = CompressionAuditDetail(
            qualityPreset = "PDF",
            targetBitrate = "n/a",
            resolutionScaling = "vector",
            durationMs = 0L,
            mediaType = "application/pdf"
        )
        _state.update {
            it.copy(recent = (listOf(RecentCompression(output.name, output.length(), output.length(), output.absolutePath, false, audit)) + it.recent).take(5))
        }
    }

    fun ocrImage(uri: Uri) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, status = "Running on-device OCR…", ocrUri = uri, ocrText = null, ocrStatus = "") }
            try {
                val text = OcrHelper.recognizeText(getApplication<Application>().applicationContext, uri, _state.value.ocrLanguage.key)
                when {
                    text == null -> { _state.update { it.copy(ocrText = null, ocrStatus = "OCR could not read this image.", status = "OCR failed for this image.") }; _toast.tryEmit("OCR could not read this image.") }
                    text.isEmpty() -> { _state.update { it.copy(ocrText = "", ocrStatus = "No text recognized in this image.", status = "No text recognized.") }; _toast.tryEmit("No text recognized in this image.") }
                    else -> { _state.update { it.copy(ocrText = text, ocrStatus = "Recognized ${text.length} characters on-device.", status = "OCR complete.") }; _toast.tryEmit("OCR complete — ${text.length} characters") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(ocrText = null, ocrStatus = "OCR failure: ${e.message ?: "unknown error"}", status = "OCR failed.") }
                _toast.tryEmit("OCR failed: ${e.message ?: "unknown error"}")
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }

    // ----- Recent / Delete / Trash / Undo / Clear -----
    fun deleteRecent(recent: RecentCompression) {
        val context = getApplication<Application>().applicationContext
        val src = File(recent.filePath)
        if (!src.exists()) {
            _state.update { it.copy(recent = it.recent.filterNot { r -> r.filePath == recent.filePath }) }
            _toast.tryEmit("File already removed.")
            return
        }
        val trashFile = File(trashDir, "${System.currentTimeMillis()}_${src.name}")
        val moved = try { src.renameTo(trashFile) } catch (e: Exception) { false }
        val effectiveTrash = if (moved) trashFile else src
        _state.update { it.copy(pendingDeletion = PendingDeletion(recent, effectiveTrash)) }
        _toast.tryEmit("Deleted. Undo available for 5s.")
        viewModelScope.launch {
            delay(5000)
            val pd = _state.value.pendingDeletion
            if (pd?.recent?.filePath == recent.filePath) {
                effectiveTrash.delete()
                _state.update {
                    it.copy(
                        pendingDeletion = null,
                        mediaResult = it.mediaResult?.takeIf { r -> r.output.absolutePath != recent.filePath },
                        recent = it.recent.filterNot { r -> r.filePath == recent.filePath }
                    )
                }
                _toast.tryEmit("${recent.name} permanently deleted.")
            }
        }
    }

    fun undoDelete() {
        val pd = _state.value.pendingDeletion ?: return
        _state.update { it.copy(pendingDeletion = null) }
        val target = File(pd.recent.filePath)
        if (pd.trashFile.exists()) {
            runCatching { pd.trashFile.renameTo(target) }
        }
        _toast.tryEmit("Restored ${pd.recent.name}")
    }

    fun clearRecent() {
        val context = getApplication<Application>().applicationContext
        _state.value.recent.forEach { r -> runCatching { File(r.filePath).delete() } }
        _state.value.pendingDeletion?.trashFile?.let { runCatching { it.delete() } }
        _state.update { it.copy(recent = emptyList(), pendingDeletion = null, mediaResult = null) }
        _toast.tryEmit("Recent history cleared and cached files removed.")
    }

    fun shareMedia(result: MediaResult) {
        val context = getApplication<Application>().applicationContext
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", result.output)
            val share = Intent(Intent.ACTION_SEND).apply {
                type = if (result.isVideo) "video/mp4" else "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(share, "Share ${result.name}")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            _toast.tryEmit("Could not share: ${e.message ?: "unknown error"}")
        }
    }

    private fun work(start: String, task: suspend () -> String) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, status = start) }
            try {
                val message = task()
                _state.update { it.copy(status = message) }
                _toast.tryEmit(message)
            } catch (e: Exception) {
                val msg = e.message ?: "Operation failed."
                _state.update { it.copy(status = msg) }
                _toast.tryEmit(msg)
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ToolkitViewModel = viewModel()
            val theme by vm.state.collectAsState()
            ThemeWrapper(theme.themeMode) {
                ToolkitApp()
            }
        }
    }
}

@Composable
fun ThemeWrapper(mode: AppThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }
    val scheme = if (dark) darkColorScheme(primary = ComposeColor(0xFF6DA4FF)) else lightColorScheme(primary = ComposeColor(0xFF155EEF))
    MaterialTheme(colorScheme = scheme) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ToolkitApp(viewModel: ToolkitViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current

    LaunchedEffect(Unit) {
        viewModel.toast.collect { message ->
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            snackbarHostState.showSnackbar(message)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { viewModel.compress(it, false) } }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { viewModel.compress(it, true) } }
    val imageBatchPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { viewModel.images(it) }
    val imageCompressBatchPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { viewModel.batchImages(it) }
    val videoCompressBatchPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { viewModel.batchVideos(it) }
    val pdfBatchPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { viewModel.mergePdfs(it) }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let(viewModel::pdf) }
    val ocrImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let(viewModel::ocrImage) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    TopAppBar(
                        title = { Column { Text("ShrinkMedia", fontWeight = FontWeight.Bold); Text("Private media & document toolkit", fontSize = 12.sp) } },
                        actions = {
                            IconButton(onClick = { viewModel.showSettings(true) }) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                        }
                    )
                    PrimaryTabRow(selectedTabIndex = state.tab.ordinal) {
                        ToolkitTab.entries.forEach { tab ->
                            Tab(selected = state.tab == tab, onClick = { viewModel.tab(tab) }, text = { Text(tab.label) }, icon = { Icon(tabIcon(tab), null) })
                        }
                    }
                }
            }
        ) { inset ->
            Column(Modifier.fillMaxSize().padding(inset).padding(horizontal = 20.dp)) {
                Text(state.status, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                when (state.tab) {
                    ToolkitTab.MEDIA -> MediaTab(state, viewModel, { imagePicker.launch("image/*") }, { videoPicker.launch("video/*") }, { imageCompressBatchPicker.launch("image/*") }, { videoCompressBatchPicker.launch("video/*") })
                    ToolkitTab.DOCUMENTS -> DocumentsTab(state, viewModel, { imageBatchPicker.launch("image/*") }, { pdfPicker.launch("application/pdf") }, { pdfBatchPicker.launch("application/pdf") })
                    ToolkitTab.AI -> AiTab(state, viewModel, { pdfPicker.launch("application/pdf") }, { ocrImagePicker.launch("image/*") })
                }
                RecentSection(state, viewModel)
            }
        }
        if (state.busy) LoadingOverlay(state.status)
        if (state.showSettings) SettingsSheet(onDismiss = { viewModel.showSettings(false) }, viewModel = viewModel)
    }
}

private fun tabIcon(tab: ToolkitTab) = when (tab) { ToolkitTab.MEDIA -> Icons.Default.PermMedia; ToolkitTab.DOCUMENTS -> Icons.Default.PictureAsPdf; ToolkitTab.AI -> Icons.Default.AutoAwesome }

@Composable private fun MediaTab(state: UiState, vm: ToolkitViewModel, pickImage: () -> Unit, pickVideo: () -> Unit, pickImages: () -> Unit, pickVideos: () -> Unit) = LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item { Hero("Media Engine", "Native Bitmap compression and FFmpeg Kit encoding. Your source files never leave the device.") }
    item { Text("Compression quality", fontWeight = FontWeight.SemiBold); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { CompressionQuality.entries.forEach { FilterChip(selected = state.quality == it, onClick = { vm.quality(it) }, label = { Text(it.label) }) } } }
    item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(pickImage, Modifier.weight(1f)) { Icon(Icons.Default.Image, null); Spacer(Modifier.width(6.dp)); Text("Image") }; Button(pickVideo, Modifier.weight(1f)) { Icon(Icons.Default.VideoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Video") } } }
    // Batch is OFF the front face by default — only shown when the user opts in from Settings
    // (prevents surprise gallery writes; AGENTS/privacy guardrail).
    if (state.enableBatch) {
        item { Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Batch mode", fontWeight = FontWeight.Bold); Text("Enabled from Settings. Batch compressions run as a foreground service.", fontSize = 13.sp); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedButton(pickImages, Modifier.weight(1f)) { Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Batch Images") }; OutlinedButton(pickVideos, Modifier.weight(1f)) { Icon(Icons.Default.VideoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Batch Videos") } } } } }
    }
    item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Column(Modifier.padding(16.dp)) { Text("Lifetime savings", fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text("${formatFileSize(state.totalSavedBytes)} recovered across ${state.totalFiles} file${if (state.totalFiles == 1L) "" else "s"}", fontSize = 15.sp) } } }
    state.mediaResult?.let { result ->
        item { ResultCard("Latest media", result) { vm.shareMedia(result) } }
    }
}

@Composable private fun DocumentsTab(state: UiState, vm: ToolkitViewModel, pickImages: () -> Unit, pickPdf: () -> Unit, pickPdfs: () -> Unit) = LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item { Hero("Document Builder", "Image-to-PDF, PDF inspection, and page splitting. Embedded text extracted on-device with iText.") }
    item { Card { Column(Modifier.padding(16.dp)) { Text("Image-to-PDF portfolio", fontWeight = FontWeight.Bold); Text("${state.imageUris.size} image${if (state.imageUris.size == 1) "" else "s"} selected", fontSize = 13.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(pickImages) { Text("Choose images") }; Button({ vm.buildPdf() }, enabled = state.imageUris.isNotEmpty()) { Text("Build PDF") } } } } }
    item { Card { Column(Modifier.padding(16.dp)) { Text("PDF merge", fontWeight = FontWeight.Bold); Text("${state.mergeUris.size} PDF${if (state.mergeUris.size == 1) "" else "s"} selected", fontSize = 13.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(pickPdfs) { Text("Choose PDFs") }; Button({ vm.mergePdfFiles() }, enabled = state.mergeUris.isNotEmpty()) { Text("Merge PDFs") } } } } }
    item { Card { Column(Modifier.padding(16.dp)) { Text("PDF inspector & splitter", fontWeight = FontWeight.Bold); Text(state.pdfMetrics?.let { "${it.name} · ${it.pages} pages · ${formatFileSize(it.bytes)}" } ?: "Choose a local PDF to inspect.", fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(pickPdf) { Text("Choose PDF") }; Button({ vm.splitPdf() }, enabled = state.pdfUri != null) { Text("Split pages") } } } } }
    state.documentOutput?.let { file -> item { smallOutput(file.name, formatFileSize(file.length())) } }
}

@Composable private fun AiTab(state: UiState, vm: ToolkitViewModel, pickPdf: () -> Unit, pickOcrImage: () -> Unit) = LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item { Hero("Elite AI Assistant", "A private, on-device text pipeline. Read text from PDFs and photos without any file leaving this device.") }
    item { Card { Column(Modifier.padding(16.dp)) { Text("Local PDF text scraper", fontWeight = FontWeight.Bold); Text("Extracts embedded text from a local PDF stream (iText).", fontSize = 13.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(pickPdf) { Text("Choose PDF") }; Button({ vm.extractText() }, enabled = state.pdfUri != null) { Text("Extract text") } } } } }
    if (state.extractedText.isNotBlank()) item { Card { Column(Modifier.padding(16.dp)) { Text("Extracted PDF text", fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(state.extractedText.take(12000), fontSize = 13.sp) } } }
    item { Card { Column(Modifier.padding(16.dp)) { Text("Scan reader (OCR)", fontWeight = FontWeight.Bold); Text("Reads printed ${state.ocrLanguage.label} text from a photo or scan — fully on-device via ML Kit, no model download and no upload. (Handwriting recognition is not yet supported.)", fontSize = 13.sp); Spacer(Modifier.height(10.dp)); Button({ pickOcrImage() }, enabled = !state.busy) { Text(if (state.busy) "Working…" else "Choose scan image") } } } }
    if (state.ocrUri != null) item { Card { Column(Modifier.padding(16.dp)) { Text(state.ocrStatus, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp); if (!state.ocrText.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(state.ocrText.take(12000), fontSize = 13.sp) } } } }
    item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Text("AICore handoff: extracted text is held locally and can be sent to a device model after adding the platform AICore dependency and availability check (staged for v2).", Modifier.padding(16.dp), fontSize = 13.sp) } }
}

@Composable private fun RecentSection(state: UiState, vm: ToolkitViewModel) {
    if (state.recent.isEmpty() && state.pendingDeletion == null) return
    var expandedPath by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent compressions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (state.recent.isNotEmpty()) {
                TextButton(onClick = { vm.clearRecent() }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            }
        }
        state.pendingDeletion?.let { pd ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Deleted: ${pd.recent.name}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Permanently removed in 5s unless you undo.", fontSize = 12.sp)
                    Row { TextButton(onClick = { vm.undoDelete() }) { Text("Undo") }; Spacer(Modifier.weight(1f)); Text("Tap Undo to restore", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        state.recent.forEach { recent ->
            val expanded = expandedPath == recent.filePath
            Card {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(recent.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${formatFileSize(recent.before)} → ${formatFileSize(recent.after)} saved ${formatFileSize((recent.before - recent.after).coerceAtLeast(0L))}", fontSize = 13.sp)
                        }
                        IconButton(onClick = { vm.shareMedia(MediaResult(recent.name, recent.before, File(recent.filePath), recent.isVideo)) }) { Icon(Icons.Default.Share, "Share") }
                        IconButton(onClick = { vm.deleteRecent(recent) }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { expandedPath = if (expanded) null else recent.filePath }) {
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (expanded) "Hide details" else "View details", fontSize = 12.sp)
                        }
                    }
                    if (expanded) {
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        AuditDetailPanel(recent.audit)
                    }
                }
            }
        }
    }
}

@Composable private fun AuditDetailPanel(audit: CompressionAuditDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AuditDetailRow("Quality preset", audit.qualityPreset)
        AuditDetailRow("Target bitrate / quality", audit.targetBitrate)
        AuditDetailRow("Resolution scaling", audit.resolutionScaling)
        AuditDetailRow("Processing duration", "${audit.durationMs} ms")
        AuditDetailRow("Media type / status", "${audit.mediaType} (${audit.status})")
    }
}

@Composable private fun AuditDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SettingsSheet(onDismiss: () -> Unit, viewModel: ToolkitViewModel) {
    val state by viewModel.state.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item { HorizontalDivider() }
            item { Text("Appearance", fontWeight = FontWeight.SemiBold) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AppThemeMode.entries.forEach { FilterChip(selected = state.themeMode == it, onClick = { viewModel.theme(it) }, label = { Text(it.name) }) } } }
            item { HorizontalDivider(); Spacer(Modifier.height(2.dp)) }
            item { Text("Recognition language", fontWeight = FontWeight.SemiBold); Text("OCR reads printed text best in the selected language.", fontSize = 12.sp) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OcrLanguage.entries.forEach { FilterChip(selected = state.ocrLanguage == it, onClick = { viewModel.ocrLanguage(it) }, label = { Text(it.label) }) } } }
            item { HorizontalDivider(); Spacer(Modifier.height(2.dp)) }
            item { Text("Media output", fontWeight = FontWeight.SemiBold) }
            item { Toggle("Save output to gallery", state.autoSave, viewModel::autoSave) }
            item { Toggle("Pause compression on low battery", state.pauseOnLowBattery, viewModel::pauseOnLowBattery) }
            item { Toggle("Enable batch mode (advanced)", state.enableBatch, viewModel::setEnableBatch) }
            item { HorizontalDivider(); Spacer(Modifier.height(2.dp)) }
            item { Text("Privacy & data", fontWeight = FontWeight.SemiBold) }
            item { Text("All processing stays on-device. No files are uploaded. The app declares no INTERNET permission.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable private fun Hero(title: String, body: String) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(18.dp)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp); Spacer(Modifier.height(5.dp)); Text(body) } } }
@Composable private fun ResultCard(label: String, result: MediaResult, share: () -> Unit) { Card { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(label, fontWeight = FontWeight.Bold); Text(result.name, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${formatFileSize(result.before)} → ${formatFileSize(result.output.length())}", fontSize = 13.sp) }; IconButton(onClick = share) { Icon(Icons.Default.Share, "Share") } } } }
@Composable private fun smallOutput(file: String, detail: String) { Card { Column(Modifier.padding(16.dp)) { Text("Document output", fontWeight = FontWeight.Bold); Text(file, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(detail, fontSize = 13.sp) } } }
@Composable private fun Toggle(label: String, checked: Boolean, set: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f)); Switch(checked, set) } }
@Composable private fun LoadingOverlay(text: String) { Box(Modifier.fillMaxSize().background(ComposeColor.Black.copy(alpha = .42f)), contentAlignment = Alignment.Center) { Card { Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(28.dp)); Spacer(Modifier.width(16.dp)); Text(text, fontWeight = FontWeight.SemiBold) } } } }

// ============================ ENGINE HELPERS ============================

suspend fun compressImageFile(context: Context, uri: Uri, quality: CompressionQuality): File? = withContext(Dispatchers.IO) { try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }; context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }; if (bounds.outWidth <= 0) return@withContext null
    var sample = 1; while (bounds.outWidth / sample > quality.maxDimension * 2 || bounds.outHeight / sample > quality.maxDimension * 2) sample *= 2
    val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) } ?: return@withContext null
    val ratio = minOf(1f, quality.maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)); val scaled = if (ratio < 1f) Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true) else bitmap
    val output = File(context.cacheDir, "compressed_${UUID.randomUUID()}.jpg"); FileOutputStream(output).use { scaled.compress(Bitmap.CompressFormat.JPEG, quality.imageQuality, it) }; if (scaled !== bitmap) scaled.recycle(); bitmap.recycle(); output
} catch (_: Exception) { null } }

suspend fun compressVideoFile(context: Context, uri: Uri, quality: CompressionQuality, onProgress: (Float) -> Unit = {}): File? = withContext(Dispatchers.IO) { try {
    val input = File(context.cacheDir, "video_in_${UUID.randomUUID()}.mp4"); context.contentResolver.openInputStream(uri)?.use { source -> FileOutputStream(input).use { source.copyTo(it) } } ?: return@withContext null
    val output = File(context.cacheDir, "compressed_video_${UUID.randomUUID()}.mp4"); val scale = if (quality == CompressionQuality.LOW) "scale=-2:720" else "scale=-2:1080"
    val session = FFmpegKit.executeAsync("-y -i \"${input.absolutePath}\" -vf $scale -c:v libx264 -crf ${quality.videoCrf} -b:v ${quality.videoBitrate} -preset veryfast -c:a aac -b:a 128k \"${output.absolutePath}\"") { }
    while (session.state == SessionState.CREATED || session.state == SessionState.RUNNING) delay(100)
    input.delete(); onProgress(100f); if (ReturnCode.isSuccess(session.returnCode) && output.exists()) output else null
} catch (_: Exception) { null } }

// ---- PDF build (iText: true vector pages from bitmaps) ----
suspend fun createPdfFromImages(context: Context, uris: List<Uri>): File = withContext(Dispatchers.IO) {
    require(uris.isNotEmpty()) { "Select one or more images first." }
    val output = File(context.cacheDir, "ShrinkMedia_Portfolio_${System.currentTimeMillis()}.pdf")
    val writer = PdfWriter(output)
    val doc = ITextDoc(writer)
    val layout = ITextLayoutDoc(doc)
    var pageNumber = 0
    try {
        uris.forEach { uri ->
            val bitmap = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return@forEach
            try {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                val imageData = baos.toByteArray()
                layout.add(AreaBreak(PageSize.A4))
                val width = PageSize.A4.width.toFloat()
                val height = PageSize.A4.height.toFloat()
                val padding = 48f
                val imageXObject = PdfImageXObject(com.itextpdf.io.image.ImageDataFactory.create(imageData))
                val image = ITextImage(imageXObject)
                val ratio = minOf((width - padding * 2f) / image.imageWidth, (height - padding * 2f) / image.imageHeight)
                image.scaleToFit(image.imageWidth * ratio, image.imageHeight * ratio)
                val x = (width - image.imageScaledWidth) / 2f
                val y = (height - image.imageScaledHeight) / 2f
                image.setFixedPosition(x, y)
                layout.add(image)
                pageNumber++
            } finally {
                bitmap.recycle()
            }
        }
        require(pageNumber > 0) { "None of the selected images could be decoded." }
    } finally {
        layout.close()
        doc.close()
    }
    output
}

// ---- PDF merge (iText: page-accurate import) ----
suspend fun mergePdfDocuments(context: Context, uris: List<Uri>): File = withContext(Dispatchers.IO) {
    require(uris.isNotEmpty()) { "Select one or more PDFs first." }
    val output = File(context.cacheDir, "ShrinkMedia_Merged_${System.currentTimeMillis()}.pdf")
    val resultDoc = ITextDoc(PdfWriter(output))
    var number = 0
    try {
        uris.forEach { uri ->
            val tmp = File(context.cacheDir, "merge_in_${UUID.randomUUID()}.pdf")
            try {
                context.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(tmp).use { input.copyTo(it) } } ?: return@forEach
                val src = ITextDoc(PdfReader(tmp))
                try {
                    src.copyPagesTo(1, src.numberOfPages, resultDoc)
                    number += src.numberOfPages
                } finally {
                    src.close()
                }
            } finally {
                tmp.delete()
            }
        }
        require(number > 0) { "No readable PDF pages were found." }
    } finally {
        resultDoc.close()
    }
    output
}

// ---- PDF metrics (PdfRenderer: page count + size; no rasterization) ----
suspend fun readPdfMetrics(context: Context, uri: Uri): PdfMetrics = withContext(Dispatchers.IO) {
    val descriptor = requireNotNull(context.contentResolver.openFileDescriptor(uri, "r")) { "Unable to open PDF." }
    descriptor.use {
        PdfRenderer(it).use { renderer ->
            PdfMetrics(getFileNameFromUri(context, uri) ?: "Document.pdf", renderer.pageCount, getFileSizeFromUri(context, uri))
        }
    }
}

// ---- PDF split (kept on android.graphics.pdf: renders each page to a bitmap PDF) ----
suspend fun splitPdfIntoPages(context: Context, uri: Uri): List<File> = withContext(Dispatchers.IO) {
    val descriptor = requireNotNull(context.contentResolver.openFileDescriptor(uri, "r")) { "Unable to open PDF." }
    descriptor.use { PdfRenderer(it).use { renderer -> (0 until renderer.pageCount).map { index -> renderer.openPage(index).use { source -> val scale = minOf(1f, 2048f / maxOf(source.width, source.height)); val width = (source.width * scale).toInt().coerceAtLeast(1); val height = (source.height * scale).toInt().coerceAtLeast(1); val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); source.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY); val doc = PdfDocument(); try { val page = doc.startPage(PdfDocument.PageInfo.Builder(width, height, 1).create()); page.canvas.drawColor(Color.WHITE); page.canvas.drawBitmap(bitmap, 0f, 0f, null); doc.finishPage(page); File(context.cacheDir, "split_page_${index + 1}_${System.currentTimeMillis()}.pdf").also { FileOutputStream(it).use(doc::writeTo) } } finally { doc.close(); bitmap.recycle() } } } } }
}

// ---- PDF embedded-text extraction (iText: reads real, compressed PDF text streams) ----
suspend fun extractRawTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    try {
        val tmp = File(context.cacheDir, "extract_${UUID.randomUUID()}.pdf")
        try {
            context.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(tmp).use { input.copyTo(it) } } ?: return@withContext ""
            val pdf = ITextDoc(PdfReader(tmp))
            try {
                val sb = StringBuilder()
                for (pageNum in 1..pdf.numberOfPages) {
                    val pageText = PdfTextExtractor.getTextFromPage(
                        pdf.getPage(pageNum),
                        SimpleTextExtractionStrategy()
                    )
                    if (pageText.isNotBlank()) {
                        if (sb.isNotEmpty()) sb.append("\n\n")
                        sb.append(pageText.trim())
                    }
                }
                sb.toString().trim().ifBlank {
                    "No embedded text was found. This document may be a scan — use the Scan reader (OCR) instead."
                }
            } finally {
                pdf.close()
            }
        } finally {
            tmp.delete()
        }
    } catch (_: Exception) {
        ""
    }
}

fun getFileSizeFromUri(context: Context, uri: Uri): Long = try { context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { if (it.moveToFirst()) it.getLong(0) else 0L } ?: 0L } catch (_: Exception) { 0L }
fun getFileNameFromUri(context: Context, uri: Uri): String? = try { context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else null } } catch (_: Exception) { null }
fun formatFileSize(bytes: Long): String { if (bytes <= 0) return "0 B"; val group = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(3); return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(group)) + " " + arrayOf("B", "KB", "MB", "GB")[group] }
fun saveToPublicMediaStore(context: Context, file: File, video: Boolean): Boolean {
    return try { val values = ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, file.name); put(MediaStore.MediaColumns.MIME_TYPE, if (video) "video/mp4" else "image/jpeg"); put(MediaStore.MediaColumns.RELATIVE_PATH, if (video) "Movies/ShrinkMedia" else "Pictures/ShrinkMedia") }; val uri = context.contentResolver.insert(if (video) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false; context.contentResolver.openOutputStream(uri)?.use { output -> FileInputStream(file).use { it.copyTo(output) } } != null } catch (_: Exception) { false }
}
