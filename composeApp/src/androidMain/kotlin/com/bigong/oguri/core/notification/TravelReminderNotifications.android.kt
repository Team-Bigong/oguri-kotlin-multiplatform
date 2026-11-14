package com.bigong.oguri.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bigong.oguri.MainActivity
import com.bigong.oguri.R
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal const val TRAVEL_REMINDER_EXTRA: String = "travelReminderId"
private const val CHANNEL_ID = "travel_reminders"
private const val NOTIFICATION_ID = 1

internal actual suspend fun requestTravelReminderPermission(): Boolean {
    val activity = OguriPlatformContextHolder.currentActivity as? MainActivity ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        if (!activity.requestTravelNotificationPermission()) return false
    }
    if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) return false
    ensureReminderChannel(activity)
    return true
}

internal actual suspend fun travelReminderPushToken(): String? = FirebaseMessaging.getInstance().token.awaitResult()

internal actual suspend fun clearTravelReminderPushToken() {
    FirebaseMessaging.getInstance().deleteToken().awaitResult()
    OguriPlatformContextHolder.applicationContext?.let {
        ContextCompat.getSystemService(it, NotificationManager::class.java)?.activeNotifications
            ?.filter { notification -> notification.notification.channelId == CHANNEL_ID }
            ?.forEach { notification -> NotificationManagerCompat.from(it).cancel(notification.tag, notification.id) }
    }
}

internal actual fun readTravelReminderDeviceState(): String? =
    preferences()?.getString("state", null)

internal actual fun writeTravelReminderDeviceState(value: String) {
    check(requireNotNull(preferences()).edit().putString("state", value).commit())
}

private fun preferences() =
    OguriPlatformContextHolder.applicationContext?.getSharedPreferences("travel_reminder_device", Context.MODE_PRIVATE)

private suspend fun <Result> Task<Result>.awaitResult(): Result? =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { if (continuation.isActive) continuation.resume(if (it.isSuccessful) it.result else null) }
    }

private fun ensureReminderChannel(context: Context) {
    val manager = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return
    manager.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, context.getString(R.string.travel_reminder_push_title), NotificationManager.IMPORTANCE_DEFAULT),
    )
}

class TravelReminderMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) = onTravelReminderTokenChanged()

    @OptIn(ExperimentalUuidApi::class)
    override fun onMessageReceived(message: RemoteMessage) {
        val id = message.data[TRAVEL_REMINDER_EXTRA] ?: return
        if (runCatching { Uuid.parse(id) }.isFailure) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        ensureReminderChannel(this)
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(TRAVEL_REMINDER_EXTRA, id)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val action = PendingIntent.getActivity(this, id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.travel_reminder_notification)
            .setContentTitle(getString(R.string.travel_reminder_push_title))
            .setContentText(getString(R.string.travel_reminder_push_body))
            .setContentIntent(action)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(id, NOTIFICATION_ID, notification)
    }
}
