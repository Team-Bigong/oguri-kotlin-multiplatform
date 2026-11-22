package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
internal actual fun TravelDialogSystemBars() = Unit

internal actual fun travelDialogProperties(): DialogProperties = DialogProperties(usePlatformDefaultWidth = false)
