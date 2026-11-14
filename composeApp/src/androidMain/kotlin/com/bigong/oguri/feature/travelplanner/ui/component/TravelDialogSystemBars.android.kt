package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsControllerCompat
import com.bigong.oguri.core.designsystem.OguriTheme

@Composable
internal actual fun TravelDialogSystemBars() {
    val view = LocalView.current
    val dark = OguriTheme.isDarkTheme
    DisposableEffect(view, dark) {
        val window = (view.parent as? DialogWindowProvider)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = !dark
        controller?.isAppearanceLightNavigationBars = !dark
        onDispose {
            if (previousStatus != null) controller.isAppearanceLightStatusBars = previousStatus
            if (previousNavigation != null) controller.isAppearanceLightNavigationBars = previousNavigation
        }
    }
}
