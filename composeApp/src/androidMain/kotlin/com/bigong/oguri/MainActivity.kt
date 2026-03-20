package com.bigong.oguri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.app.OguriApp
import com.bigong.oguri.core.ad.setCurrentAdMobActivity
import com.bigong.oguri.core.deeplink.handleIncomingAppUrl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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
        intent.dataString?.let { urlText ->
            handleIncomingAppUrl(urlText = urlText)
        }
    }

    override fun onStart() {
        super.onStart()
        setCurrentAdMobActivity(this)
    }

    override fun onStop() {
        setCurrentAdMobActivity(null)
        super.onStop()
    }
}

@Preview
@Composable
fun OguriAppAndroidPreview() {
    OguriApp()
}
