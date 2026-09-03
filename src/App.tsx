/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useEffect } from 'react';
import { 
  Image, Video, CheckCircle2, Copy, Check, Smartphone, FileCode2, Sparkles, RefreshCw, 
  Share2, Trash2, Sliders, History, CheckCheck, Sun, Moon, HardDrive, FileText, 
  Layers, Download, Undo, BellRing, Database, ArrowUpRight, AlertTriangle, X, Play,
  ChevronDown, ChevronUp, Activity
} from 'lucide-react';
import { compressionRatio, reductionPercent } from './lib/compression';
import { formatBytes } from './lib/format';
import { buildBatchResults, accumulateSavings } from './lib/batch';

const GRADLE_CODE = `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.shrinkmedia.compressor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shrinkmedia.compressor"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "0.6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.findByName("release")
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

    // Jetpack DataStore Preferences (Persistence)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Coil Image & Video Loading for Jetpack Compose
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")

    // FFmpegKit Lite (Fast, standalone video compression engine)
    implementation("io.github.root0as:ffmpeg-kit-lite:6.0-2")

    // ML Kit on-device text recognition (OCR) - runs locally, no INTERNET
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // Debug Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}`;

const FOREGROUND_SERVICE_CODE = `package com.shrinkmedia.compressor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayList

class BatchCompressionService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var notificationManager: NotificationManager
    private lateinit var settingsRepository: SettingsRepository

    companion object {
        const val CHANNEL_ID = "batch_compression_channel"
        const val CHANNEL_NAME = "Media Compression Background Tasks"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_BATCH = "com.shrinkmedia.compressor.START_BATCH"
        const val ACTION_CANCEL_BATCH = "com.shrinkmedia.compressor.CANCEL_BATCH"

        const val EXTRA_URIS = "extra_uris"
        const val EXTRA_IS_VIDEO = "extra_is_video"
        const val EXTRA_QUALITY = "extra_quality"
        const val EXTRA_AUTO_SAVE = "extra_auto_save"

        fun startBatch(
            context: Context,
            uris: List<Uri>,
            isVideo: Boolean,
            qualityName: String,
            autoSave: Boolean
        ) {
            val intent = Intent(context, BatchCompressionService::class.java).apply {
                action = ACTION_START_BATCH
                putParcelableArrayListExtra(EXTRA_URIS, ArrayList(uris))
                putExtra(EXTRA_IS_VIDEO, isVideo)
                putExtra(EXTRA_QUALITY, qualityName)
                putExtra(EXTRA_AUTO_SAVE, autoSave)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        settingsRepository = SettingsRepository(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val uris = intent.getParcelableArrayListExtra<Uri>(EXTRA_URIS) ?: emptyList()
        val isVideo = intent.getBooleanExtra(EXTRA_IS_VIDEO, false)
        val qualityName = intent.getStringExtra(EXTRA_QUALITY) ?: "MEDIUM"

        val initialNotification = buildNotification(
            title = "Batch Compression Running",
            content = "Starting batch queue of \${uris.size} \${if (isVideo) "videos" else "images"}...",
            progress = 0,
            maxProgress = uris.size,
            isIndeterminate = uris.isEmpty()
        )

        startForeground(NOTIFICATION_ID, initialNotification)

        serviceScope.launch {
            processBatchInService(uris, isVideo, qualityName)
        }

        return START_STICKY
    }

    private suspend fun processBatchInService(
        uris: List<Uri>,
        isVideo: Boolean,
        qualityName: String
    ) {
        // Query live DataStore preference for autoSaveToMediaStore
        val userSettings = settingsRepository.userSettingsFlow.first()
        val shouldAutoSave = userSettings.autoSaveToMediaStore

        val total = uris.size
        val quality = try {
            CompressionQuality.valueOf(qualityName)
        } catch (_: Exception) {
            CompressionQuality.MEDIUM
        }

        uris.forEachIndexed { index, uri ->
            val current = index + 1
            val fileName = getFileNameFromUri(this, uri) ?: "file_\$current"

            updateNotification(
                title = "Compressing Queue (\${current}/\${total})",
                content = "Optimizing: \$fileName",
                progress = current,
                maxProgress = total,
                isIndeterminate = false
            )

            val compressedFile = if (isVideo) {
                compressVideoFile(this, uri, quality)
            } else {
                compressImageFile(this, uri, quality)
            }

            if (compressedFile != null && shouldAutoSave) {
                saveToPublicMediaStore(this, compressedFile, isVideo)
            }
        }

        updateNotification(
            title = "Batch Compression Finished",
            content = "Successfully processed \$total items in background.",
            progress = total,
            maxProgress = total,
            isIndeterminate = false,
            isOngoing = false
        )

        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time progress for background media compression queues"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        title: String,
        content: String,
        progress: Int,
        maxProgress: Int,
        isIndeterminate: Boolean,
        isOngoing: Boolean = true
    ): Notification {
        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(openAppIntent)
            .setOngoing(isOngoing)
            .setProgress(maxProgress, progress, isIndeterminate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(
        title: String,
        content: String,
        progress: Int,
        maxProgress: Int,
        isIndeterminate: Boolean,
        isOngoing: Boolean = true
    ) {
        val notification = buildNotification(title, content, progress, maxProgress, isIndeterminate, isOngoing)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}`;

const DATASTORE_CODE = `package com.shrinkmedia.compressor

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

data class UserSettings(
    val themeMode: AppThemeMode,
    val autoSaveToMediaStore: Boolean,
    val imageQuality: CompressionQuality,
    val videoQuality: CompressionQuality,
    val totalHistoricalSavedBytes: Long,
    val totalHistoricalFilesCount: Long
)

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_SAVE_MEDIASTORE = booleanPreferencesKey("auto_save_mediastore")
        val IMAGE_QUALITY = stringPreferencesKey("image_quality")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val HISTORICAL_SAVED_BYTES = longPreferencesKey("historical_saved_bytes")
        val HISTORICAL_FILES_COUNT = longPreferencesKey("historical_files_count")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
            val themeMode = try { AppThemeMode.valueOf(themeStr) } catch (_: Exception) { AppThemeMode.SYSTEM }

            val autoSave = preferences[PreferencesKeys.AUTO_SAVE_MEDIASTORE] ?: false

            val imgQualityStr = preferences[PreferencesKeys.IMAGE_QUALITY] ?: CompressionQuality.MEDIUM.name
            val imgQuality = try { CompressionQuality.valueOf(imgQualityStr) } catch (_: Exception) { CompressionQuality.MEDIUM }

            val vidQualityStr = preferences[PreferencesKeys.VIDEO_QUALITY] ?: CompressionQuality.MEDIUM.name
            val vidQuality = try { CompressionQuality.valueOf(vidQualityStr) } catch (_: Exception) { CompressionQuality.MEDIUM }

            val savedBytes = preferences[PreferencesKeys.HISTORICAL_SAVED_BYTES] ?: 0L
            val filesCount = preferences[PreferencesKeys.HISTORICAL_FILES_COUNT] ?: 0L

            UserSettings(
                themeMode = themeMode,
                autoSaveToMediaStore = autoSave,
                imageQuality = imgQuality,
                videoQuality = vidQuality,
                totalHistoricalSavedBytes = savedBytes,
                totalHistoricalFilesCount = filesCount
            )
        }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateAutoSave(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE_MEDIASTORE] = enabled
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
            val currentBytes = preferences[PreferencesKeys.HISTORICAL_SAVED_BYTES] ?: 0L
            val currentCount = preferences[PreferencesKeys.HISTORICAL_FILES_COUNT] ?: 0L
            preferences[PreferencesKeys.HISTORICAL_SAVED_BYTES] = currentBytes + maxOf(0L, savedBytes)
            preferences[PreferencesKeys.HISTORICAL_FILES_COUNT] = currentCount + 1L
        }
    }
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
  targetBitrate?: string;
  resolutionScaling?: string;
  status: string;
  timestamp: string;
}

interface BatchQueueItem {
  id: string;
  name: string;
  size: number;
  isVideo: boolean;
}

interface PendingTrashItem {
  item: CompressionItem;
  originalIndex: number;
  expiresAt: number;
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'preview' | 'gradle' | 'service' | 'datastore'>('preview');
  const [copiedTab, setCopiedTab] = useState<string | null>(null);

  // App Theme State (persisted with DataStore)
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
  const [hapticTriggered, setHapticTriggered] = useState(false);

  // Batch Preview Modal State
  const [pendingBatchQueue, setPendingBatchQueue] = useState<BatchQueueItem[]>([]);
  const [showBatchPreviewModal, setShowBatchPreviewModal] = useState(false);
  const [batchIsVideo, setBatchIsVideo] = useState(false);

  // Global Actionable Snackbar (with Retry & Undo actions)
  const [globalSnackbar, setGlobalSnackbar] = useState<{
    message: string;
    actionLabel?: string;
    onAction?: () => void;
    isError?: boolean;
  } | null>(null);

  // Pending Deletion (5-Second Undo system)
  const [pendingTrash, setPendingTrash] = useState<PendingTrashItem | null>(null);
  const [trashCountdown, setTrashCountdown] = useState<number>(5);

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

  const [fileProgressPercent, setFileProgressPercent] = useState<number | null>(null);
  const [expandedLogId, setExpandedLogId] = useState<string | null>(null);

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
      targetBitrate: 'JPEG Quality 80%',
      resolutionScaling: 'Original (1920x1080)',
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
      targetBitrate: '2500 kbps (x264 crf 26)',
      resolutionScaling: 'Scale 720p (-vf scale=-2:720)',
      status: 'SUCCESS',
      timestamp: new Date(Date.now() - 300000).toLocaleTimeString()
    }
  ]);

  // Aggregate Total Space Saved from all Audit Logs (Task 3)
  const totalSpaceSaved = auditLogs
    .filter(log => log.status === 'SUCCESS')
    .reduce((acc, log) => acc + Math.max(0, log.originalSize - log.compressedSize), 0);

  // Handle 5-second countdown timer for pendingDeletion
  useEffect(() => {
    if (!pendingTrash) return;

    const timer = setInterval(() => {
      const remaining = Math.max(0, Math.ceil((pendingTrash.expiresAt - Date.now()) / 1000));
      setTrashCountdown(remaining);

      if (remaining <= 0) {
        // Permanently delete
        setPendingTrash(null);
        clearInterval(timer);
      }
    }, 250);

    return () => clearInterval(timer);
  }, [pendingTrash]);

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedTab(id);
    setTimeout(() => setCopiedTab(null), 2000);
  };

  const showSnackbar = (message: string, actionLabel?: string, onAction?: () => void, isError: boolean = false) => {
    setGlobalSnackbar({ message, actionLabel, onAction, isError });
    setTimeout(() => {
      setGlobalSnackbar(prev => prev?.message === message ? null : prev);
    }, 5000);
  };

  const showToast = (msg: string) => showSnackbar(msg);

  const triggerHaptic = () => {
    setHapticTriggered(true);
    setTimeout(() => setHapticTriggered(false), 300);
  };

  // Delete item with 5-second trash & Undo
  const handleDeleteItem = (item: CompressionItem) => {
    triggerHaptic();
    const index = recentItems.findIndex(i => i.id === item.id);
    const updated = recentItems.filter(i => i.id !== item.id);
    
    setRecentItems(updated);
    if (currentResult?.id === item.id) {
      setCurrentResult(null);
    }

    setPendingTrash({
      item,
      originalIndex: index >= 0 ? index : 0,
      expiresAt: Date.now() + 5000
    });
    setTrashCountdown(5);
    showSnackbar(`Moved "${item.name}" to trash`, 'UNDO', () => handleRestoreTrash());
  };

  const handleRestoreTrash = () => {
    if (!pendingTrash) return;
    triggerHaptic();
    
    const restored = [...recentItems];
    restored.splice(pendingTrash.originalIndex, 0, pendingTrash.item);
    
    setRecentItems(restored);
    setCurrentResult(pendingTrash.item);
    setPendingTrash(null);
    showSnackbar(`Restored "${pendingTrash.item.name}" from trash.`);
  };

  // Prepare Batch Queue & Open Batch Preview Modal
  const handleOpenBatchPreview = (isVideo: boolean) => {
    triggerHaptic();
    const count = 3;
    const items: BatchQueueItem[] = Array.from({ length: count }, (_, i) => ({
      id: `queue_${Date.now()}_${i + 1}`,
      name: isVideo ? `video_clip_${i + 1}_${Date.now().toString().slice(-4)}.mp4` : `photo_${i + 1}_${Date.now().toString().slice(-4)}.jpg`,
      size: isVideo ? Math.round(25000000 + Math.random() * 20000000) : Math.round(2500000 + Math.random() * 2500000),
      isVideo
    }));

    setPendingBatchQueue(items);
    setBatchIsVideo(isVideo);
    setShowBatchPreviewModal(true);
  };

  const handleRemoveFromBatchQueue = (id: string) => {
    triggerHaptic();
    const updated = pendingBatchQueue.filter(item => item.id !== id);
    setPendingBatchQueue(updated);
    if (updated.length === 0) {
      setShowBatchPreviewModal(false);
      showSnackbar('Batch queue emptied.');
    }
  };

  const handleDismissBatchPreview = () => {
    setShowBatchPreviewModal(false);
    setPendingBatchQueue([]);
  };

  // Confirm and start Foreground Batch Compression
  const handleConfirmAndStartBatch = () => {
    if (pendingBatchQueue.length === 0) return;
    setShowBatchPreviewModal(false);
    triggerHaptic();

    const isVideo = batchIsVideo;
    const queueToProcess = [...pendingBatchQueue];
    const batchTotal = queueToProcess.length;
    const selectedPreset = isVideo ? videoQuality : imageQuality;

    // Honest result set derived from the real queued file sizes (parity with native).
    const results = buildBatchResults(queueToProcess, selectedPreset);
    const totalSaved = accumulateSavings(results).savedBytes;

    setIsLoading(true);
    setBatchProgress({ current: 1, total: batchTotal });
    setCurrentAction(`Foreground Service Queue (1/${batchTotal})...`);
    setStatusMessage(`Background worker processing ${batchTotal} ${isVideo ? 'videos' : 'images'} via Notification.`);
    setCurrentResult(null);

    let step = 1;
    const batchItems: CompressionItem[] = [];
    setFileProgressPercent(0);

    const interval = setInterval(() => {
      const currentResultItem = results[step - 1];
      const duration = isVideo ? Math.round(1800 + Math.random() * 800) : Math.round(350 + Math.random() * 200);

      const targetBitrate = isVideo
        ? (selectedPreset === 'Low' ? '1200 kbps (x264 crf 30)' : selectedPreset === 'Medium' ? '2500 kbps (x264 crf 26)' : '4500 kbps (x264 crf 22)')
        : `JPEG Quality ${selectedPreset === 'Low' ? '60%' : selectedPreset === 'Medium' ? '80%' : '92%'}`;

      const resolutionScaling = isVideo
        ? (selectedPreset === 'Low' ? 'Scale 480p (-vf scale=-2:480)' : selectedPreset === 'Medium' ? 'Scale 720p (-vf scale=-2:720)' : 'Original / 1080p')
        : (selectedPreset === 'Low' ? 'Scale 1080px max' : selectedPreset === 'Medium' ? 'Scale 1920px max' : 'Original Resolution');

      const newItem: CompressionItem = {
        id: `${Date.now()}_${step}`,
        name: currentResultItem.name,
        originalSize: currentResultItem.originalSize,
        compressedSize: currentResultItem.compressedSize,
        type: isVideo ? 'video' : 'image',
        timeTakenMs: duration,
        savedToMediaStore: autoSaveMediaStore,
        timestamp: 'Just now'
      };

      batchItems.unshift(newItem);

      // Add to audit log with the honest computed parameters
      setAuditLogs(prev => [
        {
          id: newItem.id,
          fileName: newItem.name,
          mediaType: isVideo ? 'VIDEO' : 'IMAGE',
          originalSize: newItem.originalSize,
          compressedSize: newItem.compressedSize,
          reductionPercent: currentResultItem.reductionPercent,
          durationMs: duration,
          qualityPreset: selectedPreset.toUpperCase(),
          targetBitrate,
          resolutionScaling,
          status: 'SUCCESS',
          timestamp: new Date().toLocaleTimeString()
        },
        ...prev
      ]);

      if (step < batchTotal) {
        step++;
        setBatchProgress({ current: step, total: batchTotal });
        setFileProgressPercent(Math.round(((step - 1) / batchTotal) * 100));
        setCurrentAction(`Foreground Service Queue (${step}/${batchTotal})...`);
      } else {
        clearInterval(interval);
        setIsLoading(false);
        setBatchProgress(null);
        setFileProgressPercent(null);
        setCurrentResult(batchItems[0]);
        setRecentItems(prev => [...batchItems, ...prev].slice(0, 20));
        triggerHaptic();
        setStatusMessage(`Foreground batch completed: ${batchTotal} items compressed — ${formatBytes(totalSaved)} recovered!${autoSaveMediaStore ? ` Saved to public ${isVideo ? 'Movies' : 'Pictures'} gallery.` : ''}`);
        showSnackbar(`Foreground Service completed: ${batchTotal} files processed — ${formatBytes(totalSaved)} recovered.`);
      }
    }, 900);
  };

  // Simulate Single Compression or Error Trigger
  const handleSimulateSingle = (type: 'image' | 'video', simulateError: boolean = false) => {
    if (isLoading) return;
    triggerHaptic();

    const selectedPreset = type === 'image' ? imageQuality : videoQuality;
    const ratio = compressionRatio(selectedPreset);
    const originalSize = type === 'image' ? 3800000 : 38500000;
    const dummyName = type === 'image' ? `IMG_${Date.now().toString().slice(-4)}.jpg` : `VID_${Date.now().toString().slice(-4)}.mp4`;

    const targetBitrate = type === 'video'
      ? (selectedPreset === 'Low' ? '1200 kbps (x264 crf 30)' : selectedPreset === 'Medium' ? '2500 kbps (x264 crf 26)' : '4500 kbps (x264 crf 22)')
      : `JPEG Quality ${selectedPreset === 'Low' ? '60%' : selectedPreset === 'Medium' ? '80%' : '92%'}`;

    const resolutionScaling = type === 'video'
      ? (selectedPreset === 'Low' ? 'Scale 480p (-vf scale=-2:480)' : selectedPreset === 'Medium' ? 'Scale 720p (-vf scale=-2:720)' : 'Original / 1080p')
      : (selectedPreset === 'Low' ? 'Scale 1080px max' : selectedPreset === 'Medium' ? 'Scale 1920px max' : 'Original Resolution');

    setIsLoading(true);
    setFileProgressPercent(15);
    setCurrentAction(`Compressing ${type === 'image' ? 'Image' : 'Video'} (${selectedPreset})...`);
    setStatusMessage(`Original: ${(originalSize / (1024 * 1024)).toFixed(1)} MB • Applying ${selectedPreset} preset...`);
    setCurrentResult(null);

    const startTime = Date.now();

    // Simulate real-time progress callbacks from FFmpeg/Bitmap
    const progressTimer = setInterval(() => {
      setFileProgressPercent(prev => {
        if (prev === null || prev >= 90) return prev;
        return prev + 25;
      });
    }, type === 'image' ? 250 : 400);

    setTimeout(() => {
      clearInterval(progressTimer);
      setFileProgressPercent(null);
      if (simulateError) {
        setIsLoading(false);
        setStatusMessage(`Engine exception during ${type} compression.`);
        showSnackbar(`Compression failed for ${dummyName}`, 'RETRY', () => handleSimulateSingle(type, false), true);
        return;
      }

      const compressedSize = Math.round(originalSize * ratio);
      const duration = Date.now() - startTime;
      const savedPercent = reductionPercent(originalSize, compressedSize);

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
          targetBitrate,
          resolutionScaling,
          status: 'SUCCESS',
          timestamp: new Date().toLocaleTimeString()
        },
        ...prev
      ]);

      setStatusMessage(`${type === 'image' ? 'Image' : 'Video'} compressed successfully! (${savedPercent}% saved)${autoSaveMediaStore ? ` • Saved to MediaStore` : ''}`);
      triggerHaptic();
      showSnackbar(`Compression finished! Saved ${savedPercent}% space.`);
    }, type === 'image' ? 1200 : 2000);
  };

  const handleClearAllRecents = () => {
    triggerHaptic();
    setRecentItems([]);
    setCurrentResult(null);
    setStatusMessage('All temporary cache files and compression history cleared.');
    showToast('Cleared all temporary cache files and history list.');
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
      <header className="border-b border-neutral-800 bg-neutral-900/80 backdrop-blur-md px-6 py-3 flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center space-x-3">
          <div className={`w-9 h-9 rounded-xl bg-purple-600/20 border border-purple-500/30 flex items-center justify-center text-purple-400 transition-transform ${hapticTriggered ? 'scale-110 ring-2 ring-purple-400' : ''}`}>
            <Smartphone className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-semibold text-white tracking-tight">ShrinkMedia</h1>
            <p className="text-xs text-neutral-400">Foreground Service • DataStore Persistence • 5s Trash Undo • Audit Summary</p>
          </div>
        </div>

        {/* Tab Switcher */}
        <div className="flex items-center gap-1 bg-neutral-800/80 p-1 rounded-xl border border-neutral-700/60 flex-wrap">
          <button
            id="tab-preview-btn"
            onClick={() => setActiveTab('preview')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'preview' ? 'bg-purple-600 text-white shadow-sm' : 'text-neutral-300 hover:text-white'
            }`}
          >
            Live Compose Preview
          </button>
          <button
            id="tab-service-btn"
            onClick={() => setActiveTab('service')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'service' ? 'bg-purple-600 text-white shadow-sm' : 'text-neutral-300 hover:text-white'
            }`}
          >
            Foreground Service
          </button>
          <button
            id="tab-datastore-btn"
            onClick={() => setActiveTab('datastore')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'datastore' ? 'bg-purple-600 text-white shadow-sm' : 'text-neutral-300 hover:text-white'
            }`}
          >
            DataStore Repo
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
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 flex overflow-hidden">
        {activeTab === 'preview' && (
          <div className="flex-1 flex flex-col lg:flex-row items-center justify-center p-6 gap-8 overflow-y-auto">
            {/* Phone Screen Canvas Simulator */}
            <div className="w-full max-w-sm rounded-[40px] border-4 border-neutral-700 bg-neutral-900 p-3 shadow-2xl relative">
              {/* Foreground Service Live Notification Bar Banner */}
              {isLoading && (
                <div className="absolute -top-12 left-2 right-2 bg-neutral-800/95 border border-purple-500/50 rounded-2xl px-3 py-2 text-xs flex items-center gap-2.5 shadow-2xl z-30 animate-in slide-in-from-top duration-300 backdrop-blur-md">
                  <div className="w-6 h-6 rounded-full bg-purple-500/20 flex items-center justify-center text-purple-400 animate-pulse">
                    <BellRing className="w-3.5 h-3.5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="font-semibold text-white truncate text-[11px]">Batch Compression Service</div>
                    <div className="text-[10px] text-purple-300 truncate">
                      {batchProgress ? `Processing ${batchProgress.current}/${batchProgress.total} items...` : 'Encoding background task...'}
                    </div>
                  </div>
                  <span className="text-[9px] bg-purple-900/60 text-purple-200 px-1.5 py-0.5 rounded font-mono">ONGOING</span>
                </div>
              )}

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
                  <span className={`font-bold text-base ${isDarkMode ? 'text-white' : 'text-[#1D1B20]'}`}>ShrinkMedia</span>
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
                      title="App Settings & Presets (DataStore)"
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
                          {isBatchMode ? 'Foreground Service batch queue' : 'Single file compression'}
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
                      onClick={() => isBatchMode ? handleOpenBatchPreview(false) : handleSimulateSingle('image')}
                      className="h-12 bg-[#6750A4] hover:bg-[#594294] disabled:opacity-50 text-white rounded-xl flex items-center justify-center gap-1.5 font-semibold text-xs shadow transition active:scale-[0.98]"
                    >
                      <Image className="w-4 h-4" />
                      {isBatchMode ? 'Batch Images...' : 'Compress Image'}
                    </button>
                    <button
                      id="sim-compress-vid-btn"
                      disabled={isLoading}
                      onClick={() => isBatchMode ? handleOpenBatchPreview(true) : handleSimulateSingle('video')}
                      className="h-12 bg-[#625B71] hover:bg-[#534d61] disabled:opacity-50 text-white rounded-xl flex items-center justify-center gap-1.5 font-semibold text-xs shadow transition active:scale-[0.98]"
                    >
                      <Video className="w-4 h-4" />
                      {isBatchMode ? 'Batch Videos...' : 'Compress Video'}
                    </button>
                  </div>

                  {/* Engine Failure & Retry Test Trigger */}
                  <div className="flex justify-end">
                    <button
                      id="sim-test-error-retry-btn"
                      disabled={isLoading}
                      onClick={() => handleSimulateSingle('image', true)}
                      className={`text-[10px] font-medium flex items-center gap-1 transition ${
                        isDarkMode ? 'text-amber-400 hover:text-amber-300' : 'text-amber-700 hover:text-amber-800'
                      }`}
                      title="Simulates a compression engine exception to test global Retry Snackbar observer"
                    >
                      <AlertTriangle className="w-3 h-3" />
                      <span>Test Engine Error &amp; Retry Flow</span>
                    </button>
                  </div>

                  {/* Loading Card with Batch & Granular Progress Indicator */}
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
                          <div className="flex items-center justify-between text-xs">
                            <span className="font-bold text-[#6750A4] dark:text-purple-300">
                              Batch File {batchProgress.current} of {batchProgress.total}
                            </span>
                            <span className="text-[11px] font-mono text-purple-500 font-semibold">
                              {Math.round((batchProgress.current / batchProgress.total) * 100)}%
                            </span>
                          </div>
                        </div>
                      ) : fileProgressPercent !== null ? (
                        <div className="space-y-2 mb-2">
                          <div className="w-full bg-purple-200 dark:bg-neutral-800 h-2.5 rounded-full overflow-hidden">
                            <div
                              className="bg-emerald-500 h-full transition-all duration-200"
                              style={{ width: `${fileProgressPercent}%` }}
                            />
                          </div>
                          <div className="flex items-center justify-between text-xs">
                            <span className="text-[11px] text-neutral-400 flex items-center gap-1">
                              <Activity className="w-3 h-3 text-emerald-400 animate-pulse" />
                              FFmpeg Session StateFlow
                            </span>
                            <span className="font-mono font-bold text-emerald-500">{fileProgressPercent}%</span>
                          </div>
                        </div>
                      ) : (
                        <div className="w-7 h-7 border-2 border-[#6750A4] dark:border-purple-300 border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
                      )}
                      <div className="text-xs font-semibold text-[#6750A4] dark:text-purple-300 mt-1">{currentAction}</div>
                      <div className={`text-[10px] mt-1 ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>{statusMessage}</div>
                    </div>
                  )}

                  {/* Pending Deletion 5-Second Undo Alert Banner (Task 2) */}
                  {pendingTrash && (
                    <div className="bg-amber-500/15 border border-amber-500/40 rounded-xl p-2.5 flex items-center justify-between animate-in slide-in-from-top duration-200">
                      <div className="flex items-center gap-2 min-w-0 pr-2">
                        <Trash2 className="w-4 h-4 text-amber-500 shrink-0" />
                        <div className="min-w-0">
                          <div className="text-[11px] font-semibold text-amber-600 dark:text-amber-400 truncate">
                            In trash: "{pendingTrash.item.name}"
                          </div>
                          <div className="text-[10px] text-amber-600/80 dark:text-amber-400/80">
                            Auto-delete in {trashCountdown}s
                          </div>
                        </div>
                      </div>
                      <button
                        onClick={handleRestoreTrash}
                        className="bg-amber-600 hover:bg-amber-700 text-white text-[11px] font-bold px-2.5 py-1 rounded-lg flex items-center gap-1 shadow transition active:scale-95"
                      >
                        <Undo className="w-3 h-3" />
                        Undo
                      </button>
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
                          onClick={() => handleDeleteItem(currentResult)}
                          className="text-red-400 hover:text-red-600 p-1 rounded-md"
                          title="Move to trash (5s undo)"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>

                      {/* Mock Thumbnail */}
                      <div className={`w-full h-20 rounded-lg flex items-center justify-center text-xs ${
                        isDarkMode ? 'bg-neutral-800' : 'bg-neutral-200'
                      }`}>
                        {currentResult.type === 'image' ? <Image className="w-8 h-8 opacity-60" /> : <Video className="w-8 h-8 opacity-60" />}
                      </div>

                      {/* Stats */}
                      <div className="flex justify-between text-xs pt-1">
                        <div>
                          <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>Original</div>
                          <div className="font-semibold">{formatBytes(currentResult.originalSize)}</div>
                        </div>
                        <div className="text-right">
                          <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>Compressed</div>
                          <div className="font-bold text-[#6750A4] dark:text-purple-300">{formatBytes(currentResult.compressedSize)}</div>
                        </div>
                      </div>

                      <div className="flex items-center justify-between text-[11px] font-semibold">
                        <span className="text-emerald-500">
                          Reduction: {reductionPercent(currentResult.originalSize, currentResult.compressedSize)}% smaller
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
                          onClick={() => handleDeleteItem(currentResult)}
                          className="flex-1 border border-red-400/40 text-red-500 py-1.5 rounded-lg text-xs font-medium flex items-center justify-center gap-1.5 active:scale-95 transition"
                        >
                          <Trash2 className="w-3 h-3" />
                          Delete
                        </button>
                      </div>
                    </div>
                  )}

                  {/* ----------------------------------------------------------------------------------- */}
                  {/* SUMMARY STATS CARD AT TOP OF RECENT COMPRESSIONS (TASK 3) */}
                  {/* ----------------------------------------------------------------------------------- */}
                  <div className={`rounded-2xl p-3.5 border shadow-sm ${
                    isDarkMode ? 'bg-[#211F26] border-purple-900/30' : 'bg-white border-purple-100'
                  }`}>
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-[11px] font-semibold text-neutral-400 uppercase tracking-wider">Audit Overview</span>
                      <span className="text-[10px] bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 font-bold px-2 py-0.5 rounded-full">
                        Live Metrics
                      </span>
                    </div>

                    <div className="space-y-1">
                      <div className="text-[11px] text-neutral-400">Total Space Saved</div>
                      <div className="text-xl font-extrabold text-[#6750A4] dark:text-purple-300">
                        Total Space Saved: {formatBytes(totalSpaceSaved)}
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-2 mt-3 pt-2.5 border-t border-neutral-800/20 dark:border-neutral-800 text-[11px]">
                      <div>
                        <div className="text-[10px] text-neutral-400">Files Logged</div>
                        <div className="font-semibold">{auditLogs.length} processed</div>
                      </div>
                      <div>
                        <div className="text-[10px] text-neutral-400">Avg Reduction</div>
                        <div className="font-semibold text-emerald-500">~68% saved</div>
                      </div>
                    </div>
                  </div>

                  {/* Recent Compressions Header with 'Clear All' Button */}
                  <div className="pt-1">
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
                                  {formatBytes(item.originalSize)} → {formatBytes(item.compressedSize)}
                                  {item.timeTakenMs > 0 && ` • ${item.timeTakenMs}ms`}
                                </div>
                              </div>
                            </div>
                            <div className="flex items-center gap-1">
                              <button 
                                onClick={handleShare} 
                                className={`p-1 rounded hover:bg-neutral-700/30 ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`}
                                title="Share via ShareSheet"
                              >
                                <Share2 className="w-3.5 h-3.5" />
                              </button>
                              <button 
                                onClick={() => handleDeleteItem(item)} 
                                className="p-1 rounded hover:bg-red-500/10 text-red-400 hover:text-red-600"
                                title="Delete to trash (5s undo)"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                {/* Actionable Global Snackbar (Retry & Undo actions) */}
                {globalSnackbar && (
                  <div className={`absolute bottom-5 left-3 right-3 py-2 px-3 rounded-xl shadow-2xl flex items-center justify-between gap-2 animate-in slide-in-from-bottom duration-200 z-40 border ${
                    globalSnackbar.isError
                      ? 'bg-neutral-900 border-red-500/60 text-white'
                      : 'bg-neutral-900 border-neutral-700 text-white'
                  }`}>
                    <div className="flex items-center gap-2 truncate min-w-0">
                      {globalSnackbar.isError ? (
                        <AlertTriangle className="w-3.5 h-3.5 text-red-400 shrink-0" />
                      ) : (
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                      )}
                      <span className="text-[11px] truncate font-medium">{globalSnackbar.message}</span>
                    </div>
                    <div className="flex items-center gap-1 shrink-0">
                      {globalSnackbar.actionLabel && globalSnackbar.onAction && (
                        <button
                          onClick={() => {
                            globalSnackbar.onAction?.();
                            setGlobalSnackbar(null);
                          }}
                          className={`font-bold text-[10px] px-2 py-1 rounded transition uppercase ${
                            globalSnackbar.isError
                              ? 'bg-red-600 hover:bg-red-500 text-white'
                              : 'bg-purple-600 hover:bg-purple-500 text-white'
                          }`}
                        >
                          {globalSnackbar.actionLabel}
                        </button>
                      )}
                      <button
                        onClick={() => setGlobalSnackbar(null)}
                        className="text-neutral-400 hover:text-white p-0.5"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </div>
                  </div>
                )}

                {/* Batch Preview Bottom Sheet Modal (Review & Remove items before Starting) */}
                {showBatchPreviewModal && (
                  <div className="absolute inset-0 bg-black/60 z-30 flex flex-col justify-end animate-in fade-in">
                    <div className={`rounded-t-2xl p-4 shadow-2xl space-y-3 border-t max-h-[85%] overflow-y-auto ${
                      isDarkMode ? 'bg-[#141218] border-neutral-700 text-[#E6E1E5]' : 'bg-[#FEF7FF] border-purple-200 text-[#1D1B20]'
                    }`}>
                      <div className="flex items-center justify-between">
                        <div className="font-bold text-sm flex items-center gap-1.5">
                          <Layers className={`w-4 h-4 ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`} />
                          <span>Batch Preview Queue ({pendingBatchQueue.length})</span>
                        </div>
                        <button
                          onClick={handleDismissBatchPreview}
                          className="p-1 rounded-full text-neutral-400 hover:text-neutral-200"
                        >
                          <X className="w-4 h-4" />
                        </button>
                      </div>

                      {/* Info & Total Input Size Card */}
                      <div className={`p-2.5 rounded-xl text-xs flex items-center justify-between border ${
                        isDarkMode ? 'bg-[#211F26] border-neutral-800' : 'bg-[#F7F2FA] border-purple-100'
                      }`}>
                        <div>
                          <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>Total Input Size</div>
                          <div className="font-bold text-[#6750A4] dark:text-purple-300">
                            {formatBytes(pendingBatchQueue.reduce((acc, item) => acc + item.size, 0))}
                          </div>
                        </div>
                        <div className="text-right">
                          <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>Target Service</div>
                          <div className="font-semibold text-emerald-500">Foreground Service</div>
                        </div>
                      </div>

                      <div className="text-[11px] text-neutral-400">
                        Review files before running. Tap ✕ to remove any file from the batch queue:
                      </div>

                      {/* Queue List with Delete Action */}
                      <div className="space-y-1.5 max-h-44 overflow-y-auto">
                        {pendingBatchQueue.map((item, index) => (
                          <div
                            key={item.id}
                            className={`p-2 rounded-xl border flex items-center justify-between text-xs ${
                              isDarkMode ? 'bg-neutral-900 border-neutral-800' : 'bg-white border-purple-100'
                            }`}
                          >
                            <div className="flex items-center gap-2 truncate pr-2">
                              <span className="text-[10px] font-mono text-neutral-400 w-4">{index + 1}.</span>
                              <div className="truncate">
                                <div className="font-medium text-[11px] truncate">{item.name}</div>
                                <div className="text-[10px] text-neutral-400">{formatBytes(item.size)}</div>
                              </div>
                            </div>
                            <button
                              onClick={() => handleRemoveFromBatchQueue(item.id)}
                              className="p-1 rounded text-red-400 hover:text-red-600 hover:bg-red-500/10 shrink-0"
                              title="Remove from batch queue"
                            >
                              <X className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        ))}
                      </div>

                      {/* Actions */}
                      <div className="flex gap-2 pt-1">
                        <button
                          onClick={handleDismissBatchPreview}
                          className="flex-1 py-2 rounded-xl text-xs font-semibold border border-neutral-700 hover:bg-neutral-800 transition"
                        >
                          Cancel
                        </button>
                        <button
                          disabled={pendingBatchQueue.length === 0}
                          onClick={handleConfirmAndStartBatch}
                          className="flex-1 bg-[#6750A4] hover:bg-[#594294] disabled:opacity-50 text-white py-2 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 shadow-sm transition active:scale-95"
                        >
                          <Play className="w-3.5 h-3.5 fill-current" />
                          <span>Start Batch ({pendingBatchQueue.length})</span>
                        </button>
                      </div>
                    </div>
                  </div>
                )}

                {/* Settings Bottom Sheet Modal Simulation (DataStore) */}
                {showSettingsModal && (
                  <div className="absolute inset-0 bg-black/50 z-30 flex flex-col justify-end animate-in fade-in">
                    <div className={`rounded-t-2xl p-4 shadow-2xl space-y-3.5 border-t max-h-[85%] overflow-y-auto ${
                      isDarkMode ? 'bg-[#141218] border-neutral-700 text-[#E6E1E5]' : 'bg-[#FEF7FF] border-purple-200 text-[#1D1B20]'
                    }`}>
                      <div className="flex items-center justify-between">
                        <div className="font-bold text-sm flex items-center gap-1.5">
                          <Database className="w-4 h-4 text-purple-400" />
                          <span>DataStore Settings &amp; Presets</span>
                        </div>
                        <button onClick={() => setShowSettingsModal(false)} className={`text-xs font-semibold ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`}>
                          Done
                        </button>
                      </div>

                      {/* Theme Toggle Section (Task 4: themeMode) */}
                      <div>
                        <div className="text-xs font-semibold mb-1.5">themeMode (Jetpack DataStore)</div>
                        <div className="grid grid-cols-3 gap-1.5">
                          {(['SYSTEM', 'LIGHT', 'DARK'] as const).map((mode) => (
                            <button
                              key={mode}
                              onClick={() => {
                                triggerHaptic();
                                setThemeMode(mode);
                                showToast(`themeMode set to ${mode} (saved to DataStore)`);
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

                      {/* MediaStore Auto-Save Setting (Task 4: autoSaveToMediaStore) */}
                      <div className="flex items-center justify-between gap-2">
                        <div>
                          <div className="text-xs font-semibold">autoSaveToMediaStore</div>
                          <div className={`text-[10px] ${isDarkMode ? 'text-neutral-400' : 'text-neutral-500'}`}>
                            Persisted in DataStore; saves to public Pictures/Movies
                          </div>
                        </div>
                        <button
                          onClick={() => {
                            triggerHaptic();
                            const newVal = !autoSaveMediaStore;
                            setAutoSaveMediaStore(newVal);
                            showToast(`autoSaveToMediaStore: ${newVal} (saved to DataStore)`);
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
                          <span>Audit Logs &amp; Total Space Saved</span>
                        </div>
                        <button onClick={() => setShowAuditModal(false)} className={`text-xs font-semibold ${isDarkMode ? 'text-purple-300' : 'text-[#6750A4]'}`}>
                          Close
                        </button>
                      </div>

                      <div className="p-3 rounded-xl bg-purple-950/40 border border-purple-500/30 text-xs">
                        <div className="text-purple-300 font-semibold">Aggregated Space Savings</div>
                        <div className="text-lg font-bold text-white mt-0.5">{formatBytes(totalSpaceSaved)}</div>
                      </div>

                      <div className="space-y-1.5 max-h-52 overflow-y-auto">
                        {auditLogs.map((log) => {
                          const isExpanded = expandedLogId === log.id;
                          return (
                            <div
                              key={log.id}
                              className={`p-2.5 rounded-xl border text-[10px] space-y-1 font-mono transition ${
                                isDarkMode ? 'bg-neutral-900 border-neutral-800 text-neutral-300' : 'bg-white border-purple-100 text-neutral-700'
                              }`}
                            >
                              <div className="flex justify-between font-bold items-center">
                                <span className="truncate max-w-[180px]">{log.fileName}</span>
                                <span className="text-emerald-500">-{log.reductionPercent}%</span>
                              </div>
                              <div className="flex justify-between text-neutral-400 items-center">
                                <span>{formatBytes(log.originalSize)} → {formatBytes(log.compressedSize)}</span>
                                <button
                                  onClick={() => setExpandedLogId(isExpanded ? null : log.id)}
                                  className="flex items-center gap-1 text-[10px] text-purple-400 hover:text-purple-300 font-sans font-medium"
                                >
                                  <span>{isExpanded ? 'Hide' : 'Details'}</span>
                                  {isExpanded ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
                                </button>
                              </div>

                              {/* View Details Expander */}
                              {isExpanded && (
                                <div className="mt-2 pt-2 border-t border-neutral-700/50 space-y-1 text-[9.5px] font-sans bg-neutral-950/40 p-2 rounded-lg text-neutral-300">
                                  <div className="flex justify-between">
                                    <span className="text-neutral-400">Target Bitrate / Quality:</span>
                                    <span className="font-semibold text-purple-300">{log.targetBitrate || `${log.qualityPreset} preset`}</span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-neutral-400">Resolution Scaling:</span>
                                    <span className="font-semibold text-purple-300">{log.resolutionScaling || 'Auto Scale'}</span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-neutral-400">Processing Duration:</span>
                                    <span className="font-semibold text-emerald-400">{log.durationMs} ms</span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-neutral-400">Media Type / Status:</span>
                                    <span className="font-semibold text-blue-400">{log.mediaType} ({log.status})</span>
                                  </div>
                                </div>
                              )}
                            </div>
                          );
                        })}
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
                  <span>Implemented Architecture Tasks</span>
                </div>

                <div className="space-y-3 text-xs text-neutral-300 mb-5">
                  <div className="flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                    <span><strong>FFmpeg Session StateFlow Progress Tracking</strong>: Binds FFmpegKit <code>enableStatisticsCallback</code> to a <code>fileProgressPercent</code> StateFlow in ViewModel for real-time progress.</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                    <span><strong>Audit Log "View Details" Expander</strong>: Interactive accordion expander displaying specific compression parameters (target bitrate, resolution scaling, duration ms).</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                    <span><strong>Pre-Compression Metadata Check via ContentResolver</strong>: Reads actual file sizes using <code>OpenableColumns.SIZE</code> and displays them in the Batch Preview Modal before starting.</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                    <span><strong>Foreground Service &amp; DataStore Settings</strong>: Background batch processing with notification progress, respects <code>autoSaveToMediaStore</code> and reactive theme toggle.</span>
                  </div>
                </div>

                <div className="flex gap-2">
                  <button
                    onClick={() => setActiveTab('service')}
                    className="flex-1 bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold py-2.5 px-3 rounded-xl transition flex items-center justify-center gap-1.5 shadow"
                  >
                    <BellRing className="w-3.5 h-3.5" />
                    Service Code
                  </button>
                  <button
                    onClick={() => setActiveTab('datastore')}
                    className="flex-1 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 text-xs font-semibold py-2.5 px-3 rounded-xl transition flex items-center justify-center gap-1.5"
                  >
                    <Database className="w-3.5 h-3.5" />
                    DataStore Code
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Foreground Service View */}
        {activeTab === 'service' && (
          <div className="flex-1 p-6 flex flex-col h-full overflow-hidden">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono text-neutral-400">app/src/main/java/com/shrinkmedia/compressor/BatchCompressionService.kt</span>
                <span className="text-[10px] bg-purple-900/60 text-purple-300 px-2 py-0.5 rounded-full border border-purple-700/50">
                  Foreground Service + NotificationCompat
                </span>
              </div>
              <button
                onClick={() => copyToClipboard(FOREGROUND_SERVICE_CODE, 'service')}
                className="flex items-center gap-1.5 bg-purple-600 hover:bg-purple-500 text-white text-xs font-medium px-3 py-1.5 rounded-lg transition"
              >
                {copiedTab === 'service' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedTab === 'service' ? 'Copied' : 'Copy Service Code'}
              </button>
            </div>
            <pre className="flex-1 bg-neutral-900 border border-neutral-800 rounded-xl p-4 text-xs font-mono text-neutral-300 overflow-auto leading-relaxed">
              <code>{FOREGROUND_SERVICE_CODE}</code>
            </pre>
          </div>
        )}

        {/* DataStore View */}
        {activeTab === 'datastore' && (
          <div className="flex-1 p-6 flex flex-col h-full overflow-hidden">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono text-neutral-400">app/src/main/java/com/shrinkmedia/compressor/SettingsDataStore.kt</span>
                <span className="text-[10px] bg-purple-900/60 text-purple-300 px-2 py-0.5 rounded-full border border-purple-700/50">
                  Jetpack DataStore Preferences
                </span>
              </div>
              <button
                onClick={() => copyToClipboard(DATASTORE_CODE, 'datastore')}
                className="flex items-center gap-1.5 bg-purple-600 hover:bg-purple-500 text-white text-xs font-medium px-3 py-1.5 rounded-lg transition"
              >
                {copiedTab === 'datastore' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedTab === 'datastore' ? 'Copied' : 'Copy DataStore Code'}
              </button>
            </div>
            <pre className="flex-1 bg-neutral-900 border border-neutral-800 rounded-xl p-4 text-xs font-mono text-neutral-300 overflow-auto leading-relaxed">
              <code>{DATASTORE_CODE}</code>
            </pre>
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
      </main>
    </div>
  );
}
