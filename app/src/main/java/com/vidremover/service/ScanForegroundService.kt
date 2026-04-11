package com.vidremover.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vidremover.presentation.ui.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service for scanning videos in the background.
 * Uses FOREGROUND_SERVICE_TYPE_DATA_SYNC for data synchronization operations.
 *
 * This service displays a persistent notification showing scan progress
 * and allows users to cancel the operation via notification action.
 */
class ScanForegroundService : Service() {

private lateinit var notificationManager: NotificationManager

private val _progress = MutableStateFlow(ScanServiceProgress(0, 0, "", false, false))
val progress: StateFlow<ScanServiceProgress> = _progress.asStateFlow()

private var isCancelled = false

override fun onCreate() {
super.onCreate()
notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
createNotificationChannel()
}

override fun onBind(intent: Intent?): IBinder? = null

override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
when (intent?.action) {
ACTION_START -> {
isCancelled = false
val notification = createNotification(0, 0, "Scanning...")
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
startForeground(
NOTIFICATION_ID,
notification,
ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
)
} else {
startForeground(NOTIFICATION_ID, notification)
}
}
ACTION_CANCEL -> {
isCancelled = true
_progress.value = _progress.value.copy(isCancelled = true)
stopSelf()
}
ACTION_UPDATE_PROGRESS -> {
val current = intent.getIntExtra(EXTRA_CURRENT, 0)
val total = intent.getIntExtra(EXTRA_TOTAL, 0)
val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: ""
val isComplete = intent.getBooleanExtra(EXTRA_IS_COMPLETE, false)
updateProgress(current, total, fileName, isComplete)
}
}
return START_NOT_STICKY
}

/**
 * Updates the scan progress and notification.
 */
fun updateProgress(current: Int, total: Int, fileName: String, isComplete: Boolean = false) {
_progress.value = ScanServiceProgress(current, total, fileName, isComplete, isCancelled)
val notification = createNotification(current, total, fileName, isComplete)
notificationManager.notify(NOTIFICATION_ID, notification)
}

private fun createNotificationChannel() {
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
val channel = NotificationChannel(
CHANNEL_ID,
"Video Scanning",
NotificationManager.IMPORTANCE_LOW
).apply {
description = "Shows progress of video scanning operation"
setShowBadge(false)
}
notificationManager.createNotificationChannel(channel)
}
}

private fun createNotification(
current: Int,
total: Int,
fileName: String,
isComplete: Boolean = false
): Notification {
val intent = Intent(this, MainActivity::class.java).apply {
flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
}
val pendingIntent = PendingIntent.getActivity(
this,
0,
intent,
PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
)

val cancelIntent = Intent(this, ScanForegroundService::class.java).apply {
action = ACTION_CANCEL
}
val cancelPendingIntent = PendingIntent.getService(
this,
1,
cancelIntent,
PendingIntent.FLAG_IMMUTABLE
)

val progressPercent = if (total > 0) (current * 100) / total else 0
val contentText = when {
isComplete -> "Scan complete!"
fileName.isNotEmpty() -> "Scanning: $fileName"
else -> "Scanning videos..."
}

return NotificationCompat.Builder(this, CHANNEL_ID)
.setContentTitle(if (isComplete) "Scan Complete" else "Scanning Videos")
.setContentText(contentText)
.setSmallIcon(android.R.drawable.ic_menu_search)
.setProgress(100, progressPercent, false)
.setContentIntent(pendingIntent)
.setOngoing(!isComplete)
.apply {
if (!isComplete) {
addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)
}
}
.setOnlyAlertOnce(true)
.build()
}

override fun onDestroy() {
super.onDestroy()
isCancelled = true
}

/**
 * Data class representing scan progress within the service.
 */
data class ScanServiceProgress(
val current: Int,
val total: Int,
val currentFile: String,
val isComplete: Boolean,
val isCancelled: Boolean
)

companion object {
const val CHANNEL_ID = "scan_channel"
const val NOTIFICATION_ID = 1
const val ACTION_START = "com.vidremover.ACTION_START"
const val ACTION_CANCEL = "com.vidremover.ACTION_CANCEL"
const val ACTION_UPDATE_PROGRESS = "com.vidremover.ACTION_UPDATE_PROGRESS"

const val EXTRA_CURRENT = "current"
const val EXTRA_TOTAL = "total"
const val EXTRA_FILE_NAME = "file_name"
const val EXTRA_IS_COMPLETE = "is_complete"

/**
 * Creates an intent to start the scan service.
 */
fun createStartIntent(context: Context): Intent {
return Intent(context, ScanForegroundService::class.java).apply {
action = ACTION_START
}
}

/**
 * Creates an intent to update progress.
 */
fun createProgressIntent(
context: Context,
current: Int,
total: Int,
fileName: String,
isComplete: Boolean = false
): Intent {
return Intent(context, ScanForegroundService::class.java).apply {
action = ACTION_UPDATE_PROGRESS
putExtra(EXTRA_CURRENT, current)
putExtra(EXTRA_TOTAL, total)
putExtra(EXTRA_FILE_NAME, fileName)
putExtra(EXTRA_IS_COMPLETE, isComplete)
}
}
}
}