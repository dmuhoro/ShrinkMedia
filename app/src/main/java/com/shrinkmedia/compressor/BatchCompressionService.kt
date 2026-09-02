package com.shrinkmedia.compressor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.BatteryManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.ArrayList

/** Shared with the ViewModel so its progress/history loop remains in step with the service. */
object BatchCompressionPauseController {
    val isPaused = kotlinx.coroutines.flow.MutableStateFlow(false)
}

/** On-device audit trail for batch failures. Separated so instrumented tests can
 *  prove an audit record is produced without weakening the production path that
 *  calls it (Constitution I.6 + AGENTS.md SS1.4: "an audit record is produced"). */
internal object BatchFailureAudit {
    fun logFile(context: Context): java.io.File {
        val dir = java.io.File(context.filesDir, "audit").apply { mkdirs() }
        return java.io.File(dir, "batch-audit.log")
    }

    fun writeLine(logFile: java.io.File, entry: String) {
        val ts = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            .format(java.util.Date())
        logFile.appendText("[$ts] $entry\n")
    }
}

class BatchCompressionService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var notificationManager: NotificationManager
    private lateinit var batteryReceiver: BroadcastReceiver
    private var batteryReceiverRegistered = false

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
        createNotificationChannel()
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_BATTERY_LOW -> BatchCompressionPauseController.isPaused.value = true
                    Intent.ACTION_BATTERY_OKAY, Intent.ACTION_POWER_CONNECTED -> {
                        BatchCompressionPauseController.isPaused.value = false
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_BATCH -> {
                val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(EXTRA_URIS, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra(EXTRA_URIS)
                } ?: emptyList()

                val isVideo = intent.getBooleanExtra(EXTRA_IS_VIDEO, false)
                val qualityName = intent.getStringExtra(EXTRA_QUALITY) ?: "MEDIUM"
                val autoSave = intent.getBooleanExtra(EXTRA_AUTO_SAVE, false)

                startForeground(NOTIFICATION_ID, buildProgressNotification(0, uris.size, "Starting batch compression..."))
                serviceScope.launch {
                    val pauseOnLowBattery = SettingsRepository(applicationContext)
                        .userSettingsFlow.first().pauseCompressionOnLowBattery
                    if (pauseOnLowBattery) registerBatteryReceiver()
                    BatchCompressionPauseController.isPaused.value = pauseOnLowBattery && isBatteryLowAndNotCharging()
                    executeBatchProcessing(uris, isVideo, qualityName, autoSave)
                }
            }
            ACTION_CANCEL_BATCH -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun executeBatchProcessing(
        uris: List<Uri>,
        isVideo: Boolean,
        qualityName: String,
        autoSave: Boolean
    ) {
        val total = uris.size
        var successCount = 0
        var failedCount = 0
        val failureReasons = ArrayList<String>()
        val auditLog = acquireAuditLog()

        uris.forEachIndexed { index, uri ->
            BatchCompressionPauseController.isPaused.first { paused -> !paused }
            val current = index + 1
            val mediaTypeStr = if (isVideo) "video" else "image"
            val statusText = "Compressing $mediaTypeStr $current of $total..."

            notificationManager.notify(
                NOTIFICATION_ID,
                buildProgressNotification(current, total, statusText)
            )

            // Execute compression step — a failure must be surfaced, never dropped
            // (Constitution I.6): record the reason + audit line, then continue the
            // batch instead of silently skipping.
            val quality = try {
                CompressionQuality.valueOf(qualityName)
            } catch (e: Exception) {
                CompressionQuality.MEDIUM
            }

            val compressedFile: File? = try {
                if (isVideo) {
                    compressVideoFile(applicationContext, uri, quality)
                } else {
                    compressImageFile(applicationContext, uri, quality)
                }
            } catch (e: Exception) {
                null.also { recordFailure(failureReasons, auditLog, uri, "compression threw: ${e.message ?: "unknown error"}") }
            }

            when {
                compressedFile == null || !compressedFile.exists() || compressedFile.length() <= 0L -> {
                    recordFailure(failureReasons, auditLog, uri, "compression produced no valid output")
                }
                else -> {
                    successCount++

                    val inputBytes = getFileSizeFromUri(applicationContext, uri)
                    val settingsRepo = SettingsRepository(applicationContext)
                    settingsRepo.recordCompressionSavings((inputBytes - compressedFile.length()).coerceAtLeast(0L))

                    // Respect live DataStore autoSaveToMediaStore preference during batch execution
                    val shouldAutoSave = try {
                        settingsRepo.userSettingsFlow.first().autoSaveToMediaStore
                    } catch (e: Exception) {
                        autoSave
                    }

                    if (shouldAutoSave) {
                        val saved = try {
                            saveToPublicMediaStore(applicationContext, compressedFile, isVideo)
                        } catch (e: Exception) {
                            false
                        }
                        if (!saved) recordFailure(failureReasons, auditLog, uri, "auto-save to gallery failed")
                    }
                }
            }
        }
        failedCount = failureReasons.size

        // Finished Notification
        val failureNote = if (failedCount > 0) {
            " $failedCount failed (${failureReasons.joinToString("; ").take(200)})."
        } else {
            ""
        }
        val finalNotification = NotificationCompat.Builder(this@BatchCompressionService, CHANNEL_ID)
            .setSmallIcon(if (failedCount > 0) android.R.drawable.stat_sys_warning else android.R.drawable.stat_sys_upload_done)
            .setContentTitle(if (failedCount > 0) "Batch compression finished with errors" else "Batch Compression Complete")
            .setContentText("Successfully compressed $successCount of $total ${if (isVideo) "videos" else "images"}.$failureNote")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createActivityPendingIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, finalNotification)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /** On-device audit log for batch failures (Constitution I.6: "an audit record
     *  is produced"). Lives inside the app sandbox — no INTERNET, no upload. */
    private fun acquireAuditLog(): java.io.File = BatchFailureAudit.logFile(applicationContext)

    private fun recordFailure(
        reasons: MutableList<String>,
        auditLog: java.io.File,
        uri: Uri,
        why: String
    ) {
        val label = try {
            getFileNameFromUri(applicationContext, uri) ?: uri.lastPathSegment ?: "item"
        } catch (e: Exception) {
            uri.lastPathSegment ?: "item"
        }
        reasons += "$label: $why"
        // Audit logging must never itself break the batch; a failure here still
        // surfaces the reason via the in-memory list + completion notification.
        runCatching { BatchFailureAudit.writeLine(auditLog, "$label: $why") }
    }

    private fun buildProgressNotification(current: Int, total: Int, message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("ShrinkMedia")
            .setContentText(message)
            .setSubText(if (total > 0) "$current / $total" else null)
            .setProgress(total, current, total == 0)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(createActivityPendingIntent())
            .build()
    }

    private fun registerBatteryReceiver() {
        if (batteryReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
            addAction(Intent.ACTION_POWER_CONNECTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(batteryReceiver, filter)
        }
        batteryReceiverRegistered = true
    }

    private fun isBatteryLowAndNotCharging(): Boolean {
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return false
        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        return !charging && level >= 0 && level * 100 / scale <= 15
    }

    private fun createActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(this, 0, intent, flags)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time progress for background media compressions"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        if (batteryReceiverRegistered) unregisterReceiver(batteryReceiver)
        batteryReceiverRegistered = false
        BatchCompressionPauseController.isPaused.value = false
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
