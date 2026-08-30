package com.example.mediacompressor

import android.app.Application
import android.content.ContentValues
import android.content.Context
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
enum class CompressionQuality(val label: String, val imageQuality: Int, val maxDimension: Int, val videoCrf: Int, val videoBitrate: String) {
    LOW("Low / maximum savings", 55, 1280, 32, "800k"), MEDIUM("Balanced", 75, 1920, 28, "1500k"), HIGH("High / best quality", 90, 2560, 23, "2500k")
}
enum class ToolkitTab(val label: String) { MEDIA("Media"), DOCUMENTS("Documents"), AI("Elite AI") }
data class MediaResult(val name: String, val before: Long, val output: File, val isVideo: Boolean)
data class PdfMetrics(val name: String, val pages: Int, val bytes: Long)
data class UiState(
    val tab: ToolkitTab = ToolkitTab.MEDIA, val busy: Boolean = false,
    val status: String = "Ready — all processing stays on-device.", val quality: CompressionQuality = CompressionQuality.MEDIUM,
    val mediaResult: MediaResult? = null, val imageUris: List<Uri> = emptyList(), val mergeUris: List<Uri> = emptyList(), val pdfUri: Uri? = null,
    val pdfMetrics: PdfMetrics? = null, val documentOutput: File? = null, val extractedText: String = "",
    val autoSave: Boolean = false, val pauseOnLowBattery: Boolean = false
)

class ToolkitViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = SettingsRepository(application.applicationContext)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    init { viewModelScope.launch { settings.userSettingsFlow.collect { saved -> _state.update { it.copy(quality = saved.imageQuality, autoSave = saved.autoSaveToMediaStore, pauseOnLowBattery = saved.pauseCompressionOnLowBattery) } } } }
    fun tab(tab: ToolkitTab) = _state.update { it.copy(tab = tab) }
    fun quality(quality: CompressionQuality) { _state.update { it.copy(quality = quality) }; viewModelScope.launch { settings.updateImageQuality(quality); settings.updateVideoQuality(quality) } }
    fun autoSave(enabled: Boolean) { _state.update { it.copy(autoSave = enabled) }; viewModelScope.launch { settings.updateAutoSave(enabled) } }
    fun pauseOnLowBattery(enabled: Boolean) { _state.update { it.copy(pauseOnLowBattery = enabled) }; viewModelScope.launch { settings.updatePauseCompressionOnLowBattery(enabled) } }
    fun images(uris: List<Uri>) = _state.update { it.copy(imageUris = uris, documentOutput = null) }
    fun mergePdfs(uris: List<Uri>) = _state.update { it.copy(mergeUris = uris, documentOutput = null) }
    fun compress(uri: Uri, video: Boolean) = work(if (video) "Compressing video locally…" else "Compressing image locally…") {
        val context = getApplication<Application>().applicationContext; val inputBytes = getFileSizeFromUri(context, uri)
        val output = requireNotNull(if (video) compressVideoFile(context, uri, _state.value.quality) else compressImageFile(context, uri, _state.value.quality)) { "Compression failed." }
        if (_state.value.autoSave) saveToPublicMediaStore(context, output, video)
        _state.update { it.copy(mediaResult = MediaResult(getFileNameFromUri(context, uri) ?: output.name, inputBytes, output, video)) }; "Finished ${output.name}"
    }
    fun pdf(uri: Uri) = work("Reading PDF metrics…") { val context = getApplication<Application>().applicationContext; val metrics = readPdfMetrics(context, uri); _state.update { it.copy(pdfUri = uri, pdfMetrics = metrics, extractedText = "", documentOutput = null) }; "Loaded ${metrics.pages} page${if (metrics.pages == 1) "" else "s"}" }
    fun buildPdf() = work("Generating PDF portfolio…") { val context = getApplication<Application>().applicationContext; val output = createPdfFromImages(context, _state.value.imageUris); _state.update { it.copy(documentOutput = output) }; "Created ${output.name}" }
    fun mergePdfFiles() = work("Merging PDFs locally…") { val context = getApplication<Application>().applicationContext; val output = mergePdfDocuments(context, _state.value.mergeUris); _state.update { it.copy(documentOutput = output) }; "Created ${output.name}" }
    fun splitPdf() = work("Splitting PDF pages locally…") { val context = getApplication<Application>().applicationContext; val outputs = splitPdfIntoPages(context, requireNotNull(_state.value.pdfUri) { "Choose a PDF first." }); _state.update { it.copy(documentOutput = outputs.first()) }; "Created ${outputs.size} PDF page files" }
    fun extractText() = work("Extracting local PDF text…") { val text = extractRawTextFromUri(getApplication<Application>().applicationContext, requireNotNull(_state.value.pdfUri) { "Choose a PDF first." }); _state.update { it.copy(extractedText = text) }; "Extracted ${text.length} characters locally" }
    private fun work(start: String, task: suspend () -> String) { if (_state.value.busy) return; viewModelScope.launch { _state.update { it.copy(busy = true, status = start) }; try { _state.update { it.copy(status = task()) } } catch (e: Exception) { _state.update { it.copy(status = e.message ?: "Operation failed.") } } finally { _state.update { it.copy(busy = false) } } } }
}

class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme(colorScheme = lightColorScheme(primary = ComposeColor(0xFF155EEF))) { ToolkitApp() } } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ToolkitApp(viewModel: ToolkitViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { viewModel.compress(it, false) } }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { viewModel.compress(it, true) } }
    val imageBatchPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { viewModel.images(it) }
    val pdfBatchPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { viewModel.mergePdfs(it) }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let(viewModel::pdf) }
    Box(Modifier.fillMaxSize()) {
        Scaffold(topBar = { Column { TopAppBar(title = { Column { Text("ShrinkMedia", fontWeight = FontWeight.Bold); Text("Private media & document toolkit", fontSize = 12.sp) } }); PrimaryTabRow(selectedTabIndex = state.tab.ordinal) { ToolkitTab.entries.forEach { tab -> Tab(selected = state.tab == tab, onClick = { viewModel.tab(tab) }, text = { Text(tab.label) }, icon = { Icon(tabIcon(tab), null) }) } } } }) { inset ->
            Column(Modifier.fillMaxSize().padding(inset).padding(horizontal = 20.dp)) {
                Text(state.status, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                when (state.tab) {
                    ToolkitTab.MEDIA -> MediaTab(state, viewModel, { imagePicker.launch("image/*") }, { videoPicker.launch("video/*") })
                    ToolkitTab.DOCUMENTS -> DocumentsTab(state, viewModel, { imageBatchPicker.launch("image/*") }, { pdfPicker.launch("application/pdf") }, { pdfBatchPicker.launch("application/pdf") })
                    ToolkitTab.AI -> AiTab(state, viewModel, { pdfPicker.launch("application/pdf") })
                }
            }
        }
        if (state.busy) LoadingOverlay(state.status)
    }
}
private fun tabIcon(tab: ToolkitTab) = when (tab) { ToolkitTab.MEDIA -> Icons.Default.PermMedia; ToolkitTab.DOCUMENTS -> Icons.Default.PictureAsPdf; ToolkitTab.AI -> Icons.Default.AutoAwesome }

@Composable private fun MediaTab(state: UiState, vm: ToolkitViewModel, pickImage: () -> Unit, pickVideo: () -> Unit) = LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item { Hero("Media Engine", "Native Bitmap compression and FFmpeg Kit encoding. Your source files never leave the device.") }
    item { Text("Compression quality", fontWeight = FontWeight.SemiBold); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { CompressionQuality.entries.forEach { FilterChip(selected = state.quality == it, onClick = { vm.quality(it) }, label = { Text(it.name) }) } } }
    item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(pickImage, Modifier.weight(1f)) { Icon(Icons.Default.Image, null); Spacer(Modifier.width(6.dp)); Text("Image") }; Button(pickVideo, Modifier.weight(1f)) { Icon(Icons.Default.VideoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Video") } } }
    item { Card { Column(Modifier.padding(16.dp)) { Toggle("Save output to gallery", state.autoSave, vm::autoSave); HorizontalDivider(); Toggle("Pause compression on low battery", state.pauseOnLowBattery, vm::pauseOnLowBattery) } } }
    state.mediaResult?.let { item { OutputCard("Latest media", it.name, "${formatFileSize(it.before)} → ${formatFileSize(it.output.length())}") } }
}

@Composable private fun DocumentsTab(state: UiState, vm: ToolkitViewModel, pickImages: () -> Unit, pickPdf: () -> Unit, pickPdfs: () -> Unit) = LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item { Hero("Document Builder", "Image-to-PDF, PDF inspection, and local page splitting powered by android.graphics.pdf.") }
    item { Card { Column(Modifier.padding(16.dp)) { Text("Image-to-PDF portfolio", fontWeight = FontWeight.Bold); Text("${state.imageUris.size} image${if (state.imageUris.size == 1) "" else "s"} selected", fontSize = 13.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(pickImages) { Text("Choose images") }; Button({ vm.buildPdf() }, enabled = state.imageUris.isNotEmpty()) { Text("Build PDF") } } } } }
    item { Card { Column(Modifier.padding(16.dp)) { Text("PDF merge", fontWeight = FontWeight.Bold); Text("${state.mergeUris.size} PDF${if (state.mergeUris.size == 1) "" else "s"} selected", fontSize = 13.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(pickPdfs) { Text("Choose PDFs") }; Button({ vm.mergePdfFiles() }, enabled = state.mergeUris.isNotEmpty()) { Text("Merge PDFs") } } } } }
    item { Card { Column(Modifier.padding(16.dp)) { Text("PDF inspector & splitter", fontWeight = FontWeight.Bold); Text(state.pdfMetrics?.let { "${it.name} · ${it.pages} pages · ${formatFileSize(it.bytes)}" } ?: "Choose a local PDF to inspect.", fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(pickPdf) { Text("Choose PDF") }; Button({ vm.splitPdf() }, enabled = state.pdfUri != null) { Text("Split pages") } } } } }
    state.documentOutput?.let { item { OutputCard("Document output", it.name, formatFileSize(it.length())) } }
}

@Composable private fun AiTab(state: UiState, vm: ToolkitViewModel, pickPdf: () -> Unit) = LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item { Hero("Elite AI Assistant", "A private text-extraction pipeline ready to feed Android’s native AICore when a device model is available.") }
    item { Card { Column(Modifier.padding(16.dp)) { Text("Local PDF text scraper", fontWeight = FontWeight.Bold); Text("Extracts embedded text from a local PDF stream. Image-only scans require a local OCR module.", fontSize = 13.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(pickPdf) { Text("Choose PDF") }; Button({ vm.extractText() }, enabled = state.pdfUri != null) { Text("Extract text") } } } } }
    if (state.extractedText.isNotBlank()) item { Card { Text(state.extractedText.take(12000), Modifier.padding(16.dp), fontSize = 13.sp) } }
    item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Text("AICore handoff: extracted text is held locally and can be sent to a device model after adding the platform AICore dependency and availability check.", Modifier.padding(16.dp), fontSize = 13.sp) } }
}

@Composable private fun Hero(title: String, body: String) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(18.dp)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp); Spacer(Modifier.height(5.dp)); Text(body) } } }
@Composable private fun OutputCard(label: String, file: String, detail: String) { Card { Column(Modifier.padding(16.dp)) { Text(label, fontWeight = FontWeight.Bold); Text(file, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(detail, fontSize = 13.sp) } } }
@Composable private fun Toggle(label: String, checked: Boolean, set: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f)); Switch(checked, set) } }
@Composable private fun LoadingOverlay(text: String) { Box(Modifier.fillMaxSize().background(ComposeColor.Black.copy(alpha = .42f)), contentAlignment = Alignment.Center) { Card { Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(28.dp)); Spacer(Modifier.width(16.dp)); Text(text, fontWeight = FontWeight.SemiBold) } } } }

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

suspend fun createPdfFromImages(context: Context, uris: List<Uri>): File = withContext(Dispatchers.IO) {
    require(uris.isNotEmpty()) { "Select one or more images first." }; val document = PdfDocument(); val width = 1240; val height = 1754; val padding = 64
    try { var pageNumber = 0; uris.forEach { uri -> val bitmap = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return@forEach; pageNumber++; val page = document.startPage(PdfDocument.PageInfo.Builder(width, height, pageNumber).create()); page.canvas.drawColor(Color.WHITE); val ratio = minOf((width - padding * 2f) / bitmap.width, (height - padding * 2f) / bitmap.height); val w = bitmap.width * ratio; val h = bitmap.height * ratio; page.canvas.drawBitmap(bitmap, null, RectF((width - w) / 2, (height - h) / 2, (width + w) / 2, (height + h) / 2), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)); document.finishPage(page); bitmap.recycle() }; require(pageNumber > 0) { "None of the selected images could be decoded." }; File(context.cacheDir, "ShrinkMedia_Portfolio_${System.currentTimeMillis()}.pdf").also { FileOutputStream(it).use(document::writeTo) } } finally { document.close() }
}

suspend fun readPdfMetrics(context: Context, uri: Uri): PdfMetrics = withContext(Dispatchers.IO) { val descriptor = requireNotNull(context.contentResolver.openFileDescriptor(uri, "r")) { "Unable to open PDF." }; descriptor.use { PdfRenderer(it).use { renderer -> PdfMetrics(getFileNameFromUri(context, uri) ?: "Document.pdf", renderer.pageCount, getFileSizeFromUri(context, uri)) } } }
suspend fun mergePdfDocuments(context: Context, uris: List<Uri>): File = withContext(Dispatchers.IO) {
    require(uris.isNotEmpty()) { "Select one or more PDFs first." }; val document = PdfDocument(); var number = 0
    try { uris.forEach { uri -> val descriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return@forEach; descriptor.use { PdfRenderer(it).use { renderer -> (0 until renderer.pageCount).forEach { index -> renderer.openPage(index).use { source -> val scale = minOf(1f, 2048f / maxOf(source.width, source.height)); val width = (source.width * scale).toInt().coerceAtLeast(1); val height = (source.height * scale).toInt().coerceAtLeast(1); val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); source.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY); number++; val page = document.startPage(PdfDocument.PageInfo.Builder(width, height, number).create()); page.canvas.drawColor(Color.WHITE); page.canvas.drawBitmap(bitmap, 0f, 0f, null); document.finishPage(page); bitmap.recycle() } } } } }; require(number > 0) { "No readable PDF pages were found." }; File(context.cacheDir, "ShrinkMedia_Merged_${System.currentTimeMillis()}.pdf").also { FileOutputStream(it).use(document::writeTo) } } finally { document.close() }
}
suspend fun splitPdfIntoPages(context: Context, uri: Uri): List<File> = withContext(Dispatchers.IO) { val descriptor = requireNotNull(context.contentResolver.openFileDescriptor(uri, "r")) { "Unable to open PDF." }; descriptor.use { PdfRenderer(it).use { renderer -> (0 until renderer.pageCount).map { index -> renderer.openPage(index).use { source -> val scale = minOf(1f, 2048f / maxOf(source.width, source.height)); val width = (source.width * scale).toInt().coerceAtLeast(1); val height = (source.height * scale).toInt().coerceAtLeast(1); val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); source.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY); val doc = PdfDocument(); try { val page = doc.startPage(PdfDocument.PageInfo.Builder(width, height, 1).create()); page.canvas.drawColor(Color.WHITE); page.canvas.drawBitmap(bitmap, 0f, 0f, null); doc.finishPage(page); File(context.cacheDir, "split_page_${index + 1}_${System.currentTimeMillis()}.pdf").also { FileOutputStream(it).use(doc::writeTo) } } finally { doc.close(); bitmap.recycle() } } } } } }
suspend fun extractRawTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) { val bytes = context.contentResolver.openInputStream(uri)?.use { input -> BufferedInputStream(input).use { buffered -> ByteArrayOutputStream().use { output -> val chunk = ByteArray(8192); var total = 0; while (total < 8 * 1024 * 1024) { val count = buffered.read(chunk, 0, minOf(chunk.size, 8 * 1024 * 1024 - total)); if (count <= 0) break; output.write(chunk, 0, count); total += count }; output.toByteArray() } } } ?: return@withContext ""; Regex("\\(([^()]*)\\)").findAll(bytes.toString(Charsets.ISO_8859_1)).map { it.groupValues[1].replace("\\\\n", "\n").replace("\\\\(", "(").replace("\\\\)", ")") }.filter { it.any(Char::isLetterOrDigit) }.joinToString(" ").ifBlank { "No embedded text was found. This document may be a scan and needs a local OCR engine." } }
fun getFileSizeFromUri(context: Context, uri: Uri): Long = try { context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { if (it.moveToFirst()) it.getLong(0) else 0L } ?: 0L } catch (_: Exception) { 0L }
fun getFileNameFromUri(context: Context, uri: Uri): String? = try { context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else null } } catch (_: Exception) { null }
fun formatFileSize(bytes: Long): String { if (bytes <= 0) return "0 B"; val group = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(3); return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(group)) + " " + arrayOf("B", "KB", "MB", "GB")[group] }
fun saveToPublicMediaStore(context: Context, file: File, video: Boolean): Boolean {
    return try { val values = ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, file.name); put(MediaStore.MediaColumns.MIME_TYPE, if (video) "video/mp4" else "image/jpeg"); put(MediaStore.MediaColumns.RELATIVE_PATH, if (video) "Movies/ShrinkMedia" else "Pictures/ShrinkMedia") }; val uri = context.contentResolver.insert(if (video) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false; context.contentResolver.openOutputStream(uri)?.use { output -> FileInputStream(file).use { it.copyTo(output) } } != null } catch (_: Exception) { false }
}
