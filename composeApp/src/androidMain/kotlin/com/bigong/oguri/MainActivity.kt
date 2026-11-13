package com.bigong.oguri

import android.Manifest
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
import com.bigong.oguri.core.notification.TRAVEL_REMINDER_EXTRA
import com.bigong.oguri.core.notification.handleTravelReminderNotification
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import kotlinx.coroutines.CompletableDeferred

class MainActivity : ComponentActivity() {
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
        intent?.consumeTravelReminder()
        intent?.dataString?.let { urlText ->
            handleIncomingAppUrl(urlText = urlText)
        }

        setContent {
            OguriApp(
                onExitApp = ::finish,
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.consumeTravelReminder()
        intent.dataString?.let { urlText ->
            handleIncomingAppUrl(urlText = urlText)
        }
    }

    private fun android.content.Intent.consumeTravelReminder() {
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
