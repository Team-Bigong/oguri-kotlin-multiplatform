package com.bigong.oguri

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.app.OguriApp
import com.bigong.oguri.core.ad.setCurrentAdMobActivity
import com.bigong.oguri.core.deeplink.handleIncomingAppUrl
import com.bigong.oguri.core.deeplink.parseTravelInvite
import com.bigong.oguri.core.notification.TRAVEL_REMINDER_EXTRA
import com.bigong.oguri.core.notification.handleTravelReminderNotification
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import kotlinx.coroutines.CompletableDeferred

private const val TRAVEL_INVITE_DELIVERED = "travel-invite-delivered"

class MainActivity : ComponentActivity() {
    private var travelInviteDelivered = false
    private var notificationPermission: CompletableDeferred<Boolean>? = null
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationPermission?.complete(granted)
        notificationPermission = null
    }

    internal suspend fun requestTravelNotificationPermission(): Boolean {
        notificationPermission?.let { return it.await() }
        val result = CompletableDeferred<Boolean>()
        notificationPermission = result
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        return result.await()
    }

    override fun onDestroy() {
        notificationPermission?.cancel()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        travelInviteDelivered = savedInstanceState?.getBoolean(TRAVEL_INVITE_DELIVERED) == true
        intent?.consumeTravelReminder()
        intent?.deliverAppUrl()

        setContent {
            OguriApp(
                onExitApp = ::finish,
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(TRAVEL_INVITE_DELIVERED, travelInviteDelivered)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        travelInviteDelivered = false
        intent.consumeTravelReminder()
        intent.deliverAppUrl()
    }

    private fun Intent.deliverAppUrl() {
        val urlText = dataString ?: return
        if (parseTravelInvite(urlText) != null) {
            if (travelInviteDelivered) return
            travelInviteDelivered = true
        }
        handleIncomingAppUrl(urlText = urlText)
    }

    private fun Intent.consumeTravelReminder() {
        getStringExtra(TRAVEL_REMINDER_EXTRA)?.let(::handleTravelReminderNotification)
        removeExtra(TRAVEL_REMINDER_EXTRA)
    }

    override fun onStart() {
        super.onStart()
        OguriPlatformContextHolder.currentActivity = this
        setCurrentAdMobActivity(this)
    }

    override fun onStop() {
        setCurrentAdMobActivity(null)
        if (OguriPlatformContextHolder.currentActivity === this) {
            OguriPlatformContextHolder.currentActivity = null
        }
        super.onStop()
    }
}

@Preview
@Composable
fun OguriAppAndroidPreview() {
    OguriApp()
}
